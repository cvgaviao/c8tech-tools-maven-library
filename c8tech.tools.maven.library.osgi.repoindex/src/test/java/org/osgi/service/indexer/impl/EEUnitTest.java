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

import org.junit.jupiter.api.Test;

public class EEUnitTest {

    @Test
    public void testSingleVersioned() {
		EE ee = EE.parseBREE("JavaSE-1.6");
		assertEquals("JavaSE", ee.getName());
		assertEquals("1.6.0", ee.getVersion().toString());
		assertEquals("(&(osgi.ee=JavaSE)(version=1.6.0))", ee.toFilter());
	}

    @Test
    public void testBadVersion() {
		EE ee = EE.parseBREE("MyEE-badVersion");
		assertEquals("MyEE-badVersion", ee.getName());
		assertNull(ee.getVersion());
		assertEquals("(osgi.ee=MyEE-badVersion)", ee.toFilter());
	}

    @Test
    public void testAlias1() {
		EE ee = EE.parseBREE("OSGi/Minimum-1.2");
		assertEquals("OSGi/Minimum", ee.getName());
		assertEquals("1.2.0", ee.getVersion());
		assertEquals("(&(osgi.ee=OSGi/Minimum)(version=1.2.0))", ee.toFilter());
	}

    @Test
    public void testAlias2() {
		EE ee = EE.parseBREE("AA/BB-1.7");
		assertEquals("AA/BB", ee.getName());
		assertEquals("1.7.0", ee.getVersion());
		assertEquals("(&(osgi.ee=AA/BB)(version=1.7.0))", ee.toFilter());
	}

    @Test
    public void testVersionedAlias() {
		EE ee = EE.parseBREE("CDC-1.0/Foundation-1.0");
		assertEquals("CDC/Foundation", ee.getName());
		assertEquals("1.0.0", ee.getVersion());
		assertEquals("(&(osgi.ee=CDC/Foundation)(version=1.0.0))", ee.toFilter());
	}

    @Test
    public void testUnmatchedAliasVersions() {
		EE ee = EE.parseBREE("V1-1.5/V2-1.6");
		assertEquals("V1-1.5.0/V2-1.6.0", ee.getName());
		assertNull(ee.getVersion());
		assertEquals("(osgi.ee=V1-1.5.0/V2-1.6.0)", ee.toFilter());
	}

    @Test
    public void testReplaceJ2SEWithJavaSE() {
		EE ee = EE.parseBREE("J2SE-1.4");
		assertEquals("JavaSE", ee.getName());
		assertEquals("1.4.0", ee.getVersion().toString());
		assertEquals("(&(osgi.ee=JavaSE)(version=1.4.0))", ee.toFilter());
	}
}
