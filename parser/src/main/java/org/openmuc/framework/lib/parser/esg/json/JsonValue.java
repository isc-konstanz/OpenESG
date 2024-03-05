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
package org.openmuc.framework.lib.parser.esg.json;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Value;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonValue {

	ZonedDateTime timestamp;

	Value value;

	String unit;

	public JsonValue(ZonedDateTime timestamp, Value value, String unit) {
		this.timestamp = timestamp;
		this.value = value;
		this.unit = unit;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public boolean hasUnit() {
		return unit != null && !unit.isEmpty();
	}

	public static JsonElement serialize(JsonValue jsonValue) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("timestamp", jsonValue.getTimestamp().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        jsonObj.addProperty("value", jsonValue.getValue().asDouble());
        if (jsonValue.hasUnit()) {
        	jsonObj.addProperty("unit", jsonValue.getUnit());
        }
        return jsonObj;
	}

	public static JsonValue deserialize(JsonElement json) throws JsonParseException {
    	if (!json.isJsonObject()) {
    		throw new JsonParseException("Received malformed JSON string: " + json.getAsString());
    	}
    	JsonObject jsonObj = json.getAsJsonObject();
    	
		ZonedDateTime timestamp = ZonedDateTime.parse(jsonObj.get("timestamp").getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		DoubleValue value = new DoubleValue(jsonObj.get("value").getAsDouble());
		String unit = null;
		if (jsonObj.has("unit")) {
			unit = jsonObj.get("unit").getAsString();
		}
		return new JsonValue(timestamp, value, unit);
	}

    public static class JsonValueAdapter implements JsonSerializer<JsonValue>, JsonDeserializer<JsonValue> {

        @Override
        public JsonElement serialize(JsonValue jsonValue, Type typeOfSrc, JsonSerializationContext context) {
            return JsonValue.serialize(jsonValue);
        }

		@Override
		public JsonValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		        throws JsonParseException {
			return JsonValue.deserialize(json);
		}
    }
}
