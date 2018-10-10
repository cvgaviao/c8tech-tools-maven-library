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

import java.util.Map;

import org.osgi.resource.Resource;

/**
 * A requirement
 */
public final class RequirementImpl extends AbstractCapability
        implements org.osgi.resource.Requirement {
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
    RequirementImpl(String namespace, Map<String, Object> attributes,
            Map<String, String> directives) {
        super(namespace, attributes, directives);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("REQUIREMENT [namespace=").append(getNamespace())
                .append(", attributes=").append(getAttributes())
                .append(", directives=").append(getDirectives()).append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RequirementImpl))
            return false;
        RequirementImpl c = (RequirementImpl) o;
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
    
    @Override
    public Resource getResource() {
        return resource;
    }
}
