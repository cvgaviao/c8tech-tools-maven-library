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
package org.osgi.service.indexer;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A container for attributes and directives under a certain namespace. Can
 * generate a capability and/or a requirement from the contained information.
 */
public final class Builder {
    /** the namespace */
    private String namespace = null;

    /** the attributes */
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /** the directives */
    private final Map<String, String> directives = new LinkedHashMap<>();

    /**
     * @param namespace
     *            the namespace to set
     * @return this
     */
    public Builder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Add an attribute
     * 
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public Builder addAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Add a directive
     * 
     * @param name
     *            directive name
     * @param value
     *            directive value
     * @return this
     */
    public Builder addDirective(String name, String value) {
        directives.put(name, value);
        return this;
    }

    /**
     * @return a new capability, constructed from the namespace, attributes and
     *         directives
     * @throws IllegalStateException
     *             when the namespace isn't set
     */
    public Capability buildCapability() {
        if (namespace == null)
            throw new IllegalStateException("Namespace not set");

        return new CapabilityImpl(namespace,
                new LinkedHashMap<String, Object>(attributes),
                new LinkedHashMap<String, String>(directives));
    }

    /**
     * @return a new requirement, constructed from the namespace, attributes and
     *         directives
     * @throws IllegalStateException
     *             when the namespace isn't set
     */
    public Requirement buildRequirement() {
        if (namespace == null)
            throw new IllegalStateException("Namespace not set");

        return new RequirementImpl(namespace,
                new LinkedHashMap<String, Object>(attributes),
                new LinkedHashMap<String, String>(directives));
    }
}
