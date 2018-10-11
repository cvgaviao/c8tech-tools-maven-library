/**
 * ==========================================================================
 * Copyright © 2015-2018 Cristiano Gavião, C8 Technology ME.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cristiano Gavião (cvgaviao@c8tech.com.br)- initial API and implementation
 * ==========================================================================
 */
package br.com.c8tech.tools.maven.osgi.lib.subsystem;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.Constants;

public class Activator implements BundleActivator {

    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        SubsystemResourceAnalyzer analyzer;
        analyzer = new SubsystemResourceAnalyzer();

        Dictionary<String, String> properties = new Hashtable<>(); // NOSONAR
        properties.put(Constants.FILTER, "(name=*.esa)");
        registration = context.registerService(ResourceAnalyzer.class, analyzer,
                properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

}
