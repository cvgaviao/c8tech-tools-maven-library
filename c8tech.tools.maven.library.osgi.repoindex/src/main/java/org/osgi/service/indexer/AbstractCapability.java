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
package org.osgi.service.indexer;

import java.util.Collections;
import java.util.Map;

public class AbstractCapability {

    /** the namespace */
    private final String namespace;

    /** the attributes */
    private final Map<String, Object> attributes;

    /** the directives */
    private final Map<String, String> directives;

    /**
     * Constructor
     * 
     * @param namespace
     *            the namespace
     * @param attributes
     *            the attributes
     * @param directives
     *            the directives
     */
    AbstractCapability(String namespace, Map<String, Object> attributes,
            Map<String, String> directives) {
        this.namespace = namespace;
        this.attributes = attributes;
        this.directives = directives;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * @return the directives
     */
    public Map<String, String> getDirectives() {
        return Collections.unmodifiableMap(directives);
    }

}
