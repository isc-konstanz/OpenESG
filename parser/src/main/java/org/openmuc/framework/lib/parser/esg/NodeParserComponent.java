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

import java.util.Dictionary;
import java.util.Hashtable;

import org.openmuc.framework.parser.spi.ParserService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class NodeParserComponent {

    private ServiceRegistration<?> registration;

    @Activate
    public void activate(BundleContext context) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("parserID", "esg-node");

        String serviceName = ParserService.class.getName();

        registration = context.registerService(serviceName, new NodeParser(), properties);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }
}
