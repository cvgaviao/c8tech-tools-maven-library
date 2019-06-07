/**
 * ======================================================================
 * Copyright © 2015-2019, OSGi Alliance, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package org.osgi.service.indexer.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.util.List;
import java.util.Map;

import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Namespaces;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.log.LogService;

/**
 * Detects JARs that are OSGi Frameworks, using the presence of
 * META-INF/services/org.osgi.framework.launch.FrameworkFactory
 */
public class OSGiFrameworkAnalyzer implements ResourceAnalyzer {

    private static final String SERVICE_FRAMEWORK_FACTORY = "META-INF/services/org.osgi.framework.launch.FrameworkFactory";
    private static final String FRAMEWORK_PACKAGE = "org.osgi.framework";

    private final LogService log;

    private final boolean verbose;

    public OSGiFrameworkAnalyzer(LogService log, boolean pVerbose) {
        this.log = log;
        this.verbose = pVerbose;
    }

    protected static final Map<Integer, String> VERSIONS;

    static {
        HashMap<Integer, String> mymap = new HashMap<>();

        mymap.put(0, "1.0.0");
        mymap.put(1, "2.0.0");
        mymap.put(2, "3.0.0");
        mymap.put(3, "4.0.0");
        mymap.put(4, "4.1.0");
        mymap.put(5, "4.2.0");
        mymap.put(6, "4.3.0");
        mymap.put(7, "5.0.0");
        mymap.put(8, "6.0.0");
        mymap.put(9, "7.0.0");

        VERSIONS = Collections.unmodifiableMap(mymap);
    }

    @Override
    public void analyzeResource(Resource resource, List<Capability> caps,
            List<Requirement> reqs) throws AnalyzerException {
        Resource fwkFactorySvc;
        try {
            fwkFactorySvc = resource.getChild(SERVICE_FRAMEWORK_FACTORY);
            if (fwkFactorySvc == null) {
                return;
            }
            if (verbose)
                this.log.log(LogService.LOG_DEBUG,
                        "Processing resource with OSGI Framework analyzer");
            Builder builder = new Builder().setNamespace(Namespaces.NS_CONTRACT)
                    .addAttribute(Namespaces.NS_CONTRACT,
                            Namespaces.CONTRACT_OSGI_FRAMEWORK);

            Version specVersion = null;
            StringBuilder uses = new StringBuilder();
            boolean firstPkg = true;

            for (Capability cap : caps) {

                if (!Namespaces.NS_WIRING_PACKAGE.equals(cap.getNamespace())) {
                    continue;
                }
                // Add to the uses directive
                if (!firstPkg)
                    uses.append(',');
                String pkgName = (String) cap.getAttributes()
                        .get(Namespaces.NS_WIRING_PACKAGE);
                uses.append(pkgName);
                firstPkg = false;

                // If it's org.osgi.framework, get the package version and
                // map to OSGi spec version
                if (FRAMEWORK_PACKAGE.equals(pkgName)) {
                    Version frameworkPkgVersion = (Version) cap.getAttributes()
                            .get(Namespaces.ATTR_VERSION);
                    specVersion = mapFrameworkPackageVersion(
                            frameworkPkgVersion);
                }
            }

            if (specVersion != null)
                builder.addAttribute(Namespaces.ATTR_VERSION, specVersion);

            builder.addDirective(Namespaces.DIRECTIVE_USES, uses.toString());
            caps.add(builder.buildCapability());
        } catch (IOException e) {
            throw new AnalyzerException("", e);
        }
    }

    /**
     * Map the version of package {@code org.osgi.framework} to an OSGi
     * specification release version
     * 
     * @param pv
     *               VERSION of the {@code org.osgi.framework} package
     * @return The OSGi specification release version, or {@code null} if not
     *         known.
     */
    private static Version mapFrameworkPackageVersion(final Version pv) {
        if (pv.getMajor() != 1)
            return null;

        return new Version(VERSIONS.get(pv.getMinor()));
    }

}
