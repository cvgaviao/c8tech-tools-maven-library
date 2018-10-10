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

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
public enum MimeType {
    BUNDLE("application/vnd.osgi.bundle"), FRAGMENT(
            "application/vnd.osgi.bundle"), SUBSYSTEM(
                    "application/vnd.osgi.subsystem"), JAR(
                            "application/java-archive");

    private String mimeTypeAtribute;

    MimeType(String mimeType) {
        this.mimeTypeAtribute = mimeType;
    }

    @Override
    public String toString() {
        return mimeTypeAtribute;
    }
}
