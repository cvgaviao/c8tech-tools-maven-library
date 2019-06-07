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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.junit.jupiter.api.Test;

public class MacroUnitTest {

    @Test
	public void testSimpleProperty() {
		Properties props = new Properties();
		props.setProperty("foo", "bar");

		assertEquals("bar", Util.readProcessedProperty("foo", props));
	}

    @Test
	public void testMacroProperty() {
		Properties props = new Properties();
		props.setProperty("gnu", "GNU is not UNIX");
		props.setProperty("message", "The meaning of GNU is \"${gnu}\".");

		assertEquals("The meaning of GNU is \"GNU is not UNIX\".", Util.readProcessedProperty("message", props));
	}

    @Test
	public void testMultiLevelPropertiesMacro() {
		Properties baseProps = new Properties();
		baseProps.setProperty("gnu", "GNU is not UNIX");

		Properties extensionProps = new Properties();
		extensionProps.put("message", "The meaning of GNU is \"${gnu}\".");

		assertEquals("The meaning of GNU is \"GNU is not UNIX\".", Util.readProcessedProperty("message", extensionProps, baseProps));
	}
}
