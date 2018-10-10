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
package org.osgi.service.indexer.impl;

import java.io.IOException;
/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.util.List;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Namespaces;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.log.LogService;

public class BlueprintAnalyzer implements ResourceAnalyzer {

    private static final String BUNDLE_BLUEPRINT_HEADER = "BUNDLE-Blueprint";

    private final LogService log;

    private final boolean verbose;

    public BlueprintAnalyzer(LogService log, boolean pVerbose) {
        this.log = log;
        this.verbose = pVerbose;
    }

    @Override
    public void analyzeResource(Resource resource,
            List<Capability> capabilities, List<Requirement> requirements)
            throws AnalyzerException {
        boolean blueprintEnabled = false;

        try {
            String header = resource.getManifest().getMainAttributes()
                    .getValue(BUNDLE_BLUEPRINT_HEADER);
            if (header != null) {
                blueprintEnabled = true;
            } else {
                blueprintEnabled = hasBlueprintXml(resource);
            }

            if (blueprintEnabled) {
                requirements.add(createRequirement());
            } else
                if (verbose) {
                    this.log.log(LogService.LOG_DEBUG,
                            "ignoring Blueprint analizer");
                }
        } catch (IOException e) {
            throw new AnalyzerException("", e);
        }
    }

    private static boolean hasBlueprintXml(Resource resource)
            throws IOException {
        List<String> children = resource.listChildren("OSGI-INF/blueprint/");
        if (children == null) {
            return false;
        }
        for (String child : children) {
            if (child.toLowerCase().endsWith(".xml")) {
                return true;
            }
        }
        return false;
    }

    private static Requirement createRequirement() {
        Builder builder = new Builder().setNamespace(Namespaces.NS_EXTENDER);
        String filter = String.format(
                "(&(%s=%s)(version>=1.0.0)(!(version>=2.0.0)))",
                Namespaces.NS_EXTENDER, Namespaces.EXTENDER_BLUEPRINT);
        builder.addDirective(Namespaces.DIRECTIVE_FILTER, filter).addDirective(
                Namespaces.DIRECTIVE_EFFECTIVE, Namespaces.EFFECTIVE_ACTIVE);
        return builder.buildRequirement();
    }

}
