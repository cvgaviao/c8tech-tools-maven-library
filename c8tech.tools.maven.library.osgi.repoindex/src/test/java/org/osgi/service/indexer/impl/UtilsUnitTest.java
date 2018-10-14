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

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UtilsUnitTest {

    @Test
	public void testFindPlainPath() throws Exception {
		JarResource jar = new JarResource(new File(getClass()
                .getResource("/testdata/org.eclipse.osgi_3.7.2.v20120110-1415.jar").getPath()));
		List<String> list = Util.findMatchingPaths(jar, "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		assertEquals(1, list.size());
		assertEquals("META-INF/services/org.osgi.framework.launch.FrameworkFactory", list.get(0));
	}

    @Test
	public void testFindGlobPattern() throws Exception {
		JarResource jar = new JarResource(new File(getClass()
                .getResource("/testdata/org.eclipse.osgi_3.7.2.v20120110-1415.jar").getPath()));
		List<String> list = Util.findMatchingPaths(jar, "*.profile");

		assertEquals(12, list.size());
		assertEquals("CDC-1.0_Foundation-1.0.profile", list.get(0));
	}
}
