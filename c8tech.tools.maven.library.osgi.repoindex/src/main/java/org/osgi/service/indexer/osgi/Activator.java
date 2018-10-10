/**
 * ==========================================================================
 * Copyright © 2015-2018 OSGi Alliance, Cristiano Gavião.
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
package org.osgi.service.indexer.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.indexer.impl.RepoIndex;

public class Activator implements BundleActivator {

    private LogTracker logTracker;
    private AnalyzerTracker analyzerTracker;

    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        logTracker = new LogTracker(context);
        logTracker.open();
        
        boolean verbose = Boolean.parseBoolean(context.getProperty("verbose"));

        RepoIndex indexer = new RepoIndex(logTracker, verbose);

        analyzerTracker = new AnalyzerTracker(context, indexer, logTracker);
        analyzerTracker.open();

        registration = context.registerService(ResourceIndexer.class.getName(),
                indexer, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
        analyzerTracker.close();
        logTracker.close();
    }

}
