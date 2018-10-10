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
package org.osgi.service.indexer.impl.types;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import org.osgi.framework.Constants;
import org.osgi.service.indexer.Namespaces;

public enum VersionKey {

    PACKAGEVERSION(Constants.VERSION_ATTRIBUTE), BUNDLEVERSION(
            Constants.BUNDLE_VERSION_ATTRIBUTE), NATIVEOSVERSION(
                    Namespaces.ATTR_NATIVE_OSVERSION);

    private String key;

    VersionKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
