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

/**
 * A capability
 */
public final class CapabilityImpl extends AbstractCapability
        implements org.osgi.resource.Capability {

    /**
     * Constructor
     * 
     * @param namespace
     *                       the namespace
     * @param attributes
     *                       the attributes
     * @param directives
     *                       the directives
     */
    CapabilityImpl(String namespace, Map<String, Object> attributes,
            Map<String, String> directives) {
        super(namespace, attributes, directives);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CAPABILITY [namespace=").append(getNamespace())
                .append(", attributes=").append(getAttributes())
                .append(", directives=").append(getDirectives()).append("]");
        return builder.toString();
    }

}
