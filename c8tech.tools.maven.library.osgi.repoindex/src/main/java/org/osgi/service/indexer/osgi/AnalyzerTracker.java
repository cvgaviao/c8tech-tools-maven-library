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

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.impl.RepoIndex;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

class AnalyzerTracker extends
        ServiceTracker<ResourceAnalyzer, AnalyzerTracker.TrackingStruct> {

    private final RepoIndex indexer;
    private final LogService log;

    static class TrackingStruct {
        ResourceAnalyzer analyzer;
        Filter filter;
        boolean valid;
    }

    public AnalyzerTracker(BundleContext context, RepoIndex indexer,
            LogService log) {
        super(context, ResourceAnalyzer.class.getName(), null);
        this.indexer = indexer;
        this.log = log;
    }

    @Override
    public TrackingStruct addingService(
            ServiceReference<ResourceAnalyzer> reference) {
        TrackingStruct struct = new TrackingStruct();
        try {
            String filterStr = (String) reference
                    .getProperty(ResourceAnalyzer.FILTER);
            Filter lfilter = (filterStr != null)
                    ? FrameworkUtil.createFilter(filterStr) : null;

            ResourceAnalyzer analyzer = context.getService(reference);
            if (analyzer == null)
                return null;

            struct = new TrackingStruct();
            struct.analyzer = analyzer;
            struct.filter = lfilter;
            struct.valid = true;

            indexer.addAnalyzer(analyzer, lfilter);
        } catch (InvalidSyntaxException e) {
            struct.valid = false;
            log.log(reference, LogService.LOG_ERROR,
                    "Ignoring ResourceAnalyzer due to invalid filter expression",
                    e);
        }
        return struct;
    }

    @Override
    public void modifiedService(ServiceReference<ResourceAnalyzer> reference,
            TrackingStruct service) {
        if (service.valid) {
            indexer.removeAnalyzer(service.analyzer, service.filter);
        }

        TrackingStruct struct = null;
        try {
            String filterStr = (String) reference
                    .getProperty(ResourceAnalyzer.FILTER);
            Filter lfilter = (filterStr != null)
                    ? FrameworkUtil.createFilter(filterStr) : null;

            struct = new TrackingStruct();
            struct.filter = lfilter;
            struct.valid = true;

            indexer.addAnalyzer(struct.analyzer, lfilter);
        } catch (InvalidSyntaxException e) {
            log.log(reference, LogService.LOG_ERROR,
                    "Ignoring ResourceAnalyzer due to invalid filter expression",
                    e);
        }
    }

    @Override
    public void removedService(ServiceReference<ResourceAnalyzer> reference,
            TrackingStruct service) {
        if (service.valid)
            indexer.removeAnalyzer(service.analyzer, service.filter);
    }
}
