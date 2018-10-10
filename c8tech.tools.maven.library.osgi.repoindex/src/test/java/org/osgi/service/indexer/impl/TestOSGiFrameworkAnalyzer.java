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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.osgi.service.indexer.impl.Utils.findCaps;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

public class TestOSGiFrameworkAnalyzer {

    @Test
    public void testOsgiFramework() throws Exception {
        LinkedList<Capability> caps = new LinkedList<Capability>();
        LinkedList<Requirement> reqs = new LinkedList<Requirement>();

        JarResource input = new JarResource(new File(getClass()
                .getResource("/testdata/org.apache.felix.framework-4.0.2.jar")
                .getPath()));
        new BundleAnalyzer(new NullLogSvc(), true).analyzeResource(input, caps,
                reqs);
        new OSGiFrameworkAnalyzer(new NullLogSvc(), true).analyzeResource(input,
                caps, reqs);

        List<Capability> fwkCaps = findCaps("osgi.contract", caps);
        assertNotNull(fwkCaps);
        assertEquals(1, fwkCaps.size());
        Capability fwkCap = fwkCaps.get(0);

        assertEquals("OSGiFramework",
                fwkCap.getAttributes().get("osgi.contract"));
        assertEquals(new Version("4.3.0"),
                fwkCap.getAttributes().get("version"));
        assertEquals(
                "org.osgi.framework.startlevel,org.osgi.framework.wiring,org.osgi.framework.hooks.bundle,org.osgi.framework.hooks.service,org.osgi.framework.hooks.resolver,org.osgi.framework.launch,org.osgi.framework,org.osgi.framework.hooks.weaving,org.osgi.service.packageadmin,org.osgi.service.url,org.osgi.service.startlevel,org.osgi.util.tracker",
                fwkCap.getDirectives().get("uses"));
    }

    @Test
    public void testOsgiFrameworkSpecificationVersions() throws Exception {
        LinkedList<Capability> caps;
        LinkedList<Requirement> reqs;

        caps = new LinkedList<Capability>();
        reqs = new LinkedList<Requirement>();
        JarResource inputResource = new JarResource(new File(getClass()
                .getResource("/testdata/org.apache.felix.framework-4.0.2.jar")
                .getPath()));
        new BundleAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        new OSGiFrameworkAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        assertEquals(new Version("4.3.0"), findCaps("osgi.contract", caps)
                .get(0).getAttributes().get("version"));

        caps = new LinkedList<Capability>();
        reqs = new LinkedList<Requirement>();
        inputResource = new JarResource(new File(getClass()
                .getResource(
                        "/testdata/org.eclipse.osgi_3.7.2.v20120110-1415.jar")
                .getPath()));
        new BundleAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        new OSGiFrameworkAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        assertEquals(new Version("4.3.0"), findCaps("osgi.contract", caps)
                .get(0).getAttributes().get("version"));

        caps = new LinkedList<Capability>();
        reqs = new LinkedList<Requirement>();
        inputResource = new JarResource(new File(getClass()
                .getResource("/testdata/org.apache.felix.framework-3.2.2.jar")
                .getPath()));
        new BundleAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        new OSGiFrameworkAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        assertEquals(new Version("4.2.0"), findCaps("osgi.contract", caps)
                .get(0).getAttributes().get("version"));

        caps = new LinkedList<Capability>();
        reqs = new LinkedList<Requirement>();
        inputResource = new JarResource(new File(getClass()
                .getResource(
                        "/testdata/org.eclipse.osgi_3.6.2.R36x_v20110210.jar")
                .getPath()));
        new BundleAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        new OSGiFrameworkAnalyzer(new NullLogSvc(), true)
                .analyzeResource(inputResource, caps, reqs);
        assertEquals(new Version("4.2.0"), findCaps("osgi.contract", caps)
                .get(0).getAttributes().get("version"));

    }

    @Test
    public void testNonOsgiFramework() throws Exception {
        OSGiFrameworkAnalyzer a = new OSGiFrameworkAnalyzer(new NullLogSvc(),
                true);
        LinkedList<Capability> caps = new LinkedList<Capability>();
        LinkedList<Requirement> reqs = new LinkedList<Requirement>();

        a.analyzeResource(
                new JarResource(new File(getClass()
                        .getResource("/testdata/03-export.jar").getPath())),
                caps, reqs);

        List<Capability> fwkCaps = findCaps("osgi.contract", caps);
        assertNotNull(fwkCaps);
        assertEquals(0, fwkCaps.size());
    }
}
