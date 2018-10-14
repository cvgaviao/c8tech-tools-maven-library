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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;
import org.osgi.service.indexer.Resource;

public class JarResourceUnitTest {

    @Test
	public void testJarName() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		String location = resource.getLocation();
		assertEquals("testdata/01-bsn+version.jar", location.substring(location.length()-27));
	}

    @Test
	public void testJarSize() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		assertEquals(1104L, resource.getSize());
	}

    @Test
	public void testJarListing() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		List<String> children = resource.listChildren("org/example/a/");
		assertEquals(2, children.size());
		assertEquals("A.class", children.get(0));
		assertEquals("packageinfo", children.get(1));
	}

    @Test
	public void testJarListingInvalidPaths() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		assertNull(resource.listChildren("org/wibble/"));
		assertNull(resource.listChildren("org/example/a"));
	}

    @Test
	public void testJarListingRoot() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/org.eclipse.osgi_3.7.2.v20120110-1415.jar").getPath()));
		List<String> children = resource.listChildren("");
		assertEquals(21, children.size());
		assertEquals("META-INF/", children.get(0));
	}

    @Test
	public void testJarFileContent() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		Resource pkgInfoResource = resource.getChild("org/example/a/packageinfo");

		assertEquals("version 1.0", Utils.readStream(pkgInfoResource.getStream()));
	}

    @Test
	public void testJarManifest() throws Exception {
		JarResource resource = new JarResource(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
		Manifest manifest = resource.getManifest();
		assertEquals("org.example.a", manifest.getMainAttributes().getValue("BUNDLE-SymbolicName"));
	}

}
