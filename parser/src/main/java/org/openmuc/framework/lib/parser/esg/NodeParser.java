/*
 * Copyright 2024-2025 ISC Konstanz
 *
 * This file is part of OpenESG.
 * For more information visit http://www.openmuc.org
 *
 * OpenESG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenESG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenESG.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.framework.lib.parser.esg;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.lib.parser.esg.json.JsonValue;
import org.openmuc.framework.lib.parser.esg.json.JsonValueType;
import org.openmuc.framework.lib.parser.esg.json.JsonValue.JsonValueAdapter;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationContainer;
import org.openmuc.framework.parser.spi.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Easy Smart Grid Stimulus parser library for the OpenMUC framework.
 */
public class NodeParser implements ParserService {
    private final Logger logger = LoggerFactory.getLogger(NodeParser.class);

    private final Gson gson;

    public NodeParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(JsonValue.class, new JsonValueAdapter());
        gsonBuilder.disableHtmlEscaping();
        //gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    @Override
    public synchronized byte[] serialize(List<LoggingRecord> containers) throws SerializationException {
        if (containers.size() == 1) {
            return serialize(containers.get(0));
        }
        if (containers.stream().map(c -> parseTopic(c)).distinct().count() > 1) {
            logger.warn("Received multiple topics to parse at once: {}", containers.stream()
            		.map(c -> parseTopic(c))
            		.distinct()
            		.collect(Collectors.joining(", ")));
            throw new UnsupportedOperationException("Unable to parse several topics at once");
        }
        // Since all topics are the same, getting the topic of the first container is sufficient
        String topic = parseTopic(containers.get(0));
        LinkedList<String> topicPath = new LinkedList<String>(List.of(topic.split("/")));
        if (topicPath.getLast().equals("forecast")) {
            topicPath.removeLast();
            topic = String.join("/", topicPath);
        }
        ZonedDateTime currentHourTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS);
        ZonedDateTime currentDayTimestamp = currentHourTimestamp.truncatedTo(ChronoUnit.DAYS);
        
        JsonValueType type = parseType(topic);
        
        List<JsonValue> values = new LinkedList<JsonValue>();
        for (LoggingRecord container : containers) {
            Record record = container.getRecord();
            if (record.getFlag() != Flag.VALID) {
            	if (record.getFlag() != Flag.NO_VALUE_RECEIVED_YET) {
                    logger.warn("Unable to serialize record of flag \"{}\"", record.getFlag().toString());
            	}
                continue;
            }
            try {
                int hour = parseHour(container.getChannelSettings());
                
                ZonedDateTime timestamp = currentDayTimestamp.plusHours(hour);
                if (timestamp.isBefore(currentHourTimestamp)) {
                    timestamp = timestamp.plusDays(1);
                }
                values.add(new JsonValue(timestamp, scaleValue(record.getValue(), type), null));
                
            } catch(IllegalArgumentException e) {
                logger.warn("Error parsing ValueType: {}" + e.getMessage());
            }
        }
        values.sort(Comparator.comparing(v -> v.getTimestamp()));
        
        return gson.toJson(values).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public synchronized byte[] serialize(LoggingRecord container) throws SerializationException {
        String topic = parseTopic(container);
        return serialize(container.getRecord(), topic);
    }

    @Override
    public synchronized byte[] serialize(Record record, SerializationContainer container) throws SerializationException {
        return serialize(record, container.getChannelAddress());
    }

    private byte[] serialize(Record record, String topic) throws SerializationException {
        if (record.getFlag() != Flag.VALID) {
            throw new SerializationException(String.format("Unable to serialize record of flag \"%s\"", record.getFlag().toString()));
        }
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(record.getTimestamp()), ZoneId.of("UTC"));
        try {
            JsonValueType type = parseType(topic);
            JsonValue value = new JsonValue(timestamp, scaleValue(record.getValue(), type), type.getUnit());
            
            return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
            
        } catch(IllegalArgumentException e) {
            throw new SerializationException("Error parsing ValueType: " + e.getMessage());
        }
    }

    @Override
    public synchronized Record deserialize(byte[] byteArray, SerializationContainer container) {
        try {
            JsonValue jsonValue;
            
            String topic = parseTopic(container.getChannelAddress());
            LinkedList<String> topicPath = new LinkedList<String>(List.of(topic.split("/")));
            if (!topicPath.getLast().equals("forecast")) {
                jsonValue = gson.fromJson(new String(byteArray), JsonValue.class);
            }
            else {
                topicPath.removeLast();
                topic = String.join("/", topicPath);
                
                int hour = parseHour(container.getChannelSettings());

                ZonedDateTime currentTimestamp = ZonedDateTime.now();
                ZonedDateTime targetTimestamp = currentTimestamp.truncatedTo(ChronoUnit.DAYS).plusHours(hour);
                
                Type listType = new TypeToken<ArrayList<JsonValue>>(){}.getType();
                List<JsonValue> values = gson.fromJson(new String(byteArray), listType);
                try {
                    jsonValue = values.stream()
                            .filter(v -> v.getTimestamp().isEqual(targetTimestamp))
                            .findFirst().orElseThrow();
                    jsonValue.setTimestamp(currentTimestamp);
                    
                } catch(NoSuchElementException e) {
                    return new Record(Flag.DRIVER_ERROR_CHANNEL_TEMPORARILY_NOT_ACCESSIBLE);
                }
            }
            return deserialize(jsonValue, topic);
            
        } catch(JsonParseException | SerializationException e) {
            logger.warn("Error decoding JSON string \"{}\": {}", new String(byteArray, StandardCharsets.UTF_8), e.getMessage());
            return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
        }
    }

    private synchronized Record deserialize(JsonValue jsonValue, String topic) {
        Long timestamp = jsonValue.getTimestamp().toInstant().toEpochMilli();
        Value value = jsonValue.getValue();
        try {
            value = new DoubleValue(value.asDouble() / parseType(topic).getScaling());
                
        } catch(IllegalArgumentException e) {
            logger.warn("Error parsing value type: {}", e.getMessage());
            return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
        }
        return new Record(value, timestamp, Flag.VALID);
    }

    private static Value scaleValue(Value value, JsonValueType type) throws SerializationException {
        switch (value.getValueType()) {
        case SHORT:
        case INTEGER:
        case LONG:
        case FLOAT:
        case DOUBLE:
            return new DoubleValue(value.asDouble() * type.getScaling());
        default:
            throw new SerializationException("Unsupported ValueType: " + value.getValueType());
        }
    }

    private static JsonValueType parseType(String topic) throws IllegalArgumentException {
        String type = new LinkedList<String>(List.of(parseTopic(topic).split("/"))).getLast();
        return JsonValueType.valueOf(type.toUpperCase());
    }

    private static String parseTopic(LoggingRecord container) {
        return Arrays.stream(container.getLoggingSettings().split("[;:]"))
                .filter(part -> part.contains("topic"))
                .map(queue -> queue.split("=")[1])
                .findFirst()
                .orElse("");
    }

    private static String parseTopic(String topic) {
        return Stream.of(topic.split(";")).findFirst().orElse("");
    }

    private static int parseHour(String settings) throws SerializationException {
         return Stream.of(settings.split(";"))
                .filter(part -> part.contains("hour"))
                .mapToInt(keyVal -> Integer.parseInt(keyVal.split("=")[1]))
                .findFirst().orElseThrow(() -> new SerializationException("Unable to find forecast hour in settings: " + settings));
    }

}
