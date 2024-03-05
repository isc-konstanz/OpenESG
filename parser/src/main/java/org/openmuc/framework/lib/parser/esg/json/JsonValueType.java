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

public enum JsonValueType {

    POWER("kW", 0.001),
    ENERGY("kWh", 1),
    STIMULUS("%", 100);

    private final String unit;
    private final double scaling;

    private JsonValueType(String unit, double scaling) {
        this.unit = unit;
        this.scaling = scaling;
    }

    public String getUnit() {
        return unit;
    }

    public double getScaling() {
    	return scaling;
    }

    public static JsonValueType ofUnit(String unit) throws IllegalArgumentException {
    	switch (unit.toLowerCase()) {
    	case "kw":
    		return POWER;
    	case "kwh":
    		return ENERGY;
    	case "%":
    		return STIMULUS;
    	default:
    		throw new IllegalArgumentException(String.format("Unknown unit \"%s\"", unit));
    	}
    }
}
