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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.Manifest;

import org.osgi.service.indexer.Resource;

class FlatStreamResource implements Resource {

    private final String iLocation;
    private final InputStream stream;

    private final Dictionary<String, Object> properties = new Hashtable<>();//NOSONAR

    FlatStreamResource(String name, String pLocation, InputStream stream) {
        this.iLocation = pLocation;
        this.stream = stream;

        properties.put(NAME, name);
        properties.put(LOCATION, pLocation);
    }

    @Override
    public String getLocation() {
        return iLocation;
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        return properties;
    }

    @Override
    public long getSize() {
        return 0L;
    }

    @Override
    public InputStream getStream() throws IOException {
        return stream;
    }

    @Override
    public Manifest getManifest() throws IOException {
        return null;
    }

    @Override
    public List<String> listChildren(String prefix) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Resource getChild(String path) throws IOException {
        return null;
    }

    @Override
    public void close() {
        // do nothing
    }

}
