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

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.impl.types.SymbolicName;
import org.osgi.service.indexer.impl.types.VersionRange;
import org.osgi.service.indexer.impl.util.OSGiHeader;
import org.osgi.service.log.LogService;

public class KnownBundleAnalyzer implements ResourceAnalyzer {

    private enum IndicatorType {
        CAPABILITY("cap="), REQUIREMENT("req=");

        String prefix;

        IndicatorType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private final Properties defaultProperties;

    private Properties extraProperties = null;

    private final LogService log;

    private final boolean verbose;

    public KnownBundleAnalyzer(LogService log, boolean pVerbose) {
        this.log = log;
        this.verbose = pVerbose;

        defaultProperties = new Properties();
        InputStream stream = KnownBundleAnalyzer.class
                .getResourceAsStream("/known-bundles.properties");
        if (stream != null) {
            try {
                defaultProperties.load(stream);
            } catch (IOException e) {
                this.log.log(LogService.LOG_DEBUG,
                        "error while loading know bundles properties file", e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    this.log.log(LogService.LOG_DEBUG,
                            "error while closing know bundles properties file",
                            e);
                }
            }
        }
    }

    public KnownBundleAnalyzer(Properties properties, LogService log,
            boolean pVerbose) {
        this.defaultProperties = properties;
        this.log = log;
        this.verbose = pVerbose;

    }

    private static void processClause(String bundleRef, String clauseStr,
            List<Capability> caps, List<Requirement> reqs) {
        Map<String, Map<String, String>> header = OSGiHeader
                .parseHeader(clauseStr);

        for (Entry<String, Map<String, String>> entry : header.entrySet()) {
            String indicator = OSGiHeader.removeDuplicateMarker(entry.getKey());
            IndicatorType type;

            String namespace;
            if (indicator.startsWith(IndicatorType.CAPABILITY.getPrefix())) {
                type = IndicatorType.CAPABILITY;
                namespace = indicator.substring(
                        IndicatorType.CAPABILITY.getPrefix().length());
            } else
                if (indicator
                        .startsWith(IndicatorType.REQUIREMENT.getPrefix())) {
                    type = IndicatorType.REQUIREMENT;
                    namespace = indicator.substring(
                            IndicatorType.REQUIREMENT.getPrefix().length());
                } else {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Invalid indicator format in known-bundle parsing for bundle  \"{0}\", expected cap=namespace or req=namespace, found \"{1}\".",
                            bundleRef, indicator));
                }

            Builder builder = new Builder().setNamespace(namespace);

            Map<String, String> attribs = entry.getValue();
            Util.copyAttribsToBuilder(builder, attribs);

            if (type == IndicatorType.CAPABILITY)
                caps.add(builder.buildCapability());
            else
                if (type == IndicatorType.REQUIREMENT)
                    reqs.add(builder.buildRequirement());
        }
    }

    private static void processPropertyName(Resource resource,
            List<Capability> caps, List<Requirement> reqs,
            SymbolicName resourceName, String name,
            Properties... propertiesList) throws IOException {
        String[] bundleRef = name.split(";");
        String bsn = bundleRef[0];

        if (resourceName.getName().equals(bsn)) {
            VersionRange versionRange = null;
            if (bundleRef.length > 1)
                versionRange = new VersionRange(bundleRef[1]);

            Version version = Util.getVersion(resource);
            if (versionRange == null || versionRange.match(version)) {
                processClause(name,
                        Util.readProcessedProperty(name, propertiesList), caps,
                        reqs);
            }
        }
    }

    @Override
    public void analyzeResource(Resource resource, List<Capability> caps,
            List<Requirement> reqs) throws AnalyzerException {
        SymbolicName resourceName;
        try {
            resourceName = Util.getSymbolicName(resource);
            if (verbose) {
                this.log.log(LogService.LOG_INFO, "Analyzing " + resourceName);
            }
        } catch (IllegalArgumentException e) {
            this.log.log(LogService.LOG_DEBUG,
                    "Ignoring resource since it doesn't have a symbolic name.",
                    e);
            // not a bundle, so return without analyzing
            return;
        } catch (IOException e) {
            throw new AnalyzerException("Failure while analyzing a resource.",
                    e);
        }

        try {
            for (Enumeration<?> names = defaultProperties.propertyNames(); names
                    .hasMoreElements();) {
                String propName = (String) names.nextElement();
                processPropertyName(resource, caps, reqs, resourceName,
                        propName, defaultProperties);
            }

            if (extraProperties != null)
                for (Enumeration<?> names = extraProperties
                        .propertyNames(); names.hasMoreElements();) {
                    String propName = (String) names.nextElement();
                    processPropertyName(resource, caps, reqs, resourceName,
                            propName, extraProperties, defaultProperties);
                }
        } catch (IOException e) {
            throw new AnalyzerException("Failure while processing artifact.",
                    e);
        }
    }

    public void setKnownBundlesExtra(Properties extras) {
        this.extraProperties = extras;
    }

}
