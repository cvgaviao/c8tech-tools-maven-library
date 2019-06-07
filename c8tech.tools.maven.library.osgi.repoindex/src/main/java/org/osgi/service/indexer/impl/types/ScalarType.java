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
package org.osgi.service.indexer.impl.types;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
public enum ScalarType {
    STRING("String"), VERSION("Version"), LONG("Long"), DOUBLE("Double");

    private String key;

    private ScalarType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static ScalarType fromString(String text) {
        if (text != null) {
            for (ScalarType b : ScalarType.values()) {
                if (text.equalsIgnoreCase(b.key)) {
                    return b;
                }
            }
        }
        return null;
    }
}
