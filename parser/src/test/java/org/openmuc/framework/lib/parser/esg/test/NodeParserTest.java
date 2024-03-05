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
package org.openmuc.framework.lib.parser.esg.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.lib.parser.esg.NodeParser;
import org.openmuc.framework.parser.spi.SerializationContainer;
import org.openmuc.framework.parser.spi.SerializationException;


public class NodeParserTest {

	private final NodeParser parser = new NodeParser();

	private static byte[] POWER_JSON;
	private static byte[] STIMULUS_JSON;

	@BeforeAll
	public static void loadJson() throws IOException {
		POWER_JSON = NodeParserTest.class.getResourceAsStream("power.json").readAllBytes();
		STIMULUS_JSON = NodeParserTest.class.getResourceAsStream("stimulus.json").readAllBytes();
		
		byte[] FOO = new String(NodeParserTest.class.getResourceAsStream("power.json").readAllBytes(), StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
        System.out.println(IntStream.range(0, FOO.length).mapToObj(i -> String.valueOf(FOO[i])).collect(Collectors.joining(",")));
	}

	@Test
	public void testJsonTopicFailure() {
		assertEquals(deserializePower("esg/node/unknown").getFlag(), Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
		assertEquals(deserializeStimulus("esg/node/unknown").getFlag(), Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
	}

	@Test
	public void testPower() throws SerializationException {
		assertEquals(deserializePower("esg/node/power").getValue().asDouble(), 100000.);
		assertNotEquals(deserializePower("esg/node/energy").getValue().asDouble(), 100000.);

        ZoneId timezone = ZoneId.of("UTC");
        ZonedDateTime timestamp = ZonedDateTime.of(LocalDate.of(2024, 01, 01), LocalTime.of(00, 00), timezone);
		assertArrayEquals(serialize("esg/node/power", timestamp, new DoubleValue(100000.)), POWER_JSON);
    }

	private Record deserializePower(String topic) {
		return deserialize(POWER_JSON, topic);
	}

	@Test
	public void testStimulus() throws SerializationException {
		assertEquals(deserializeStimulus("esg/node/stimulus").getValue().asDouble(), .5);

        ZoneId timezone = ZoneId.of("UTC");
        ZonedDateTime timestamp = ZonedDateTime.of(LocalDate.of(2024, 01, 01), LocalTime.of(00, 00), timezone);
		assertArrayEquals(serialize("esg/node/stimulus", timestamp, new DoubleValue(.5)), STIMULUS_JSON);
    }

	@Test
	public void testStimulusForecast() throws SerializationException {
		List<LoggingRecord> containers = new ArrayList<LoggingRecord>();
        IntStream.range(0, 24).forEachOrdered(h -> {
        	containers.add(new NodeParserContainer("esg/node/stimulus/forecast", String.format("hour=%s", String.valueOf(h)), h/100.));
        });
        String forecast = new String(parser.serialize(containers));
        
		assertEquals(deserialize(forecast.getBytes(StandardCharsets.UTF_8), 
        		new NodeParserContainer("esg/node/stimulus/forecast", "hour=12")).getValue().asDouble(), 12.);
    }

	private Record deserializeStimulus(String topic) {
		return deserialize(STIMULUS_JSON, topic);
	}

	private Record deserialize(byte[] jsonBytes, String topic) {
		return deserialize(jsonBytes, new NodeParserContainer(topic));
	}

	private Record deserialize(byte[] jsonBytes, SerializationContainer container) {
		return parser.deserialize(jsonBytes, container);
	}

	private byte[] serialize(String topic, ZonedDateTime timestamp, Value value) throws SerializationException {
		return serialize(new Record(value, timestamp.toInstant().toEpochMilli()), new NodeParserContainer(topic));
	}

	private byte[] serialize(Record record, SerializationContainer container) throws SerializationException {
		return parser.serialize(record, container);
	}

}
