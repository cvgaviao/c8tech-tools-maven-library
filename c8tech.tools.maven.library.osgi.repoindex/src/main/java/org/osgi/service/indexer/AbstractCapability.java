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

import org.osgi.resource.Resource;

public class AbstractCapability {

    /** the namespace */
    private final String namespace;

    /** the attributes */
    private final Map<String, Object> attributes;

    /** the directives */
    private final Map<String, String> directives;

    private Resource resource;

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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof AbstractCapability))
            return false;
        AbstractCapability c = (AbstractCapability) o;
        return (c.getNamespace().equals(getNamespace())
                && c.getAttributes().equals(getAttributes())
                && c.getDirectives().equals(getDirectives())
                && (c.getResource() != null
                        ? c.getResource().equals(getResource())
                        : getResource() == null));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + getNamespace().hashCode();
        result = 31 * result + getAttributes().hashCode();
        result = 31 * result + getDirectives().hashCode();
        result = 31 * result
                + (getResource() == null ? 0 : getResource().hashCode());
        return result;
    }

    public Resource getResource() {
        return resource;
    }

}
