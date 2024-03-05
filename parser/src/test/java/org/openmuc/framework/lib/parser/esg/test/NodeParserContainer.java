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

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.parser.spi.SerializationContainer;


public class NodeParserContainer extends LoggingRecord implements SerializationContainer {

	public NodeParserContainer(String address, String settings) {
	    super(null, address, settings, parseLoggingSettings(address),
	    		ValueType.DOUBLE, null, null);
	}

	public NodeParserContainer(String address, String settings, double value) {
	    super(null, address, settings, parseLoggingSettings(address),
	    		ValueType.DOUBLE, null, new Record(new DoubleValue(value), System.currentTimeMillis(), Flag.VALID));
	}

	public NodeParserContainer(String address) {
	    super(null, address, "", parseLoggingSettings(address),
	    		ValueType.DOUBLE, null, null);
	}

	private static String parseLoggingSettings(String address) {
		return String.format("logger:topic=%s", address);
	}

}
