/**
 * ==========================================================================
 * Copyright © 2015-2018 Cristiano Gavião, C8 Technology ME.
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
package br.com.c8tech.tools.maven.osgi.lib.mojo.handlers;

import java.io.UnsupportedEncodingException;

import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipShort;

public class ContentTypeExtraField implements ZipExtraField {
    private static final ZipShort ID = new ZipShort(0x67D5);
    private static final String[] PREFIX = { "application/", "audio/",
            "example/", "image/", "message/", "model/", "multipart/", "text/",
            "video/" };

    private final byte[] data;

    public ContentTypeExtraField(String type) {
        if (!isContentType(type))
            throw new IllegalArgumentException(
                    "Not a valid MIME Media Type: " + type);
        try {
            this.data = type.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean isContentType(String str) {
        for (int i = 0; i < PREFIX.length; i++) {
            if (str.startsWith(PREFIX[i]))
                return true;
        }
        return false;
    }

    static String parseExtraField(ZipArchiveEntry entry) {
        try {
            ZipExtraField field = entry.getExtraField(ID);
            if (field == null)
                return null;
            byte[] data = field.getLocalFileDataData();
            String str = new String(data, "UTF-8");
            if (isContentType(str))
                return str;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        return null;
    }

    @Override
    public byte[] getCentralDirectoryData() {
        return getLocalFileDataData();
    }

    @Override
    public ZipShort getCentralDirectoryLength() {
        return getLocalFileDataLength();
    }

    @Override
    public ZipShort getHeaderId() {
        return ID;
    }

    @Override
    public byte[] getLocalFileDataData() {
        return data;
    }

    @Override
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(data.length);
    }

    @Override
    public void parseFromCentralDirectoryData(byte[] buffer, int offset,
            int length) throws ZipException {
        parseFromLocalFileData(buffer, offset, length);
    }

    @Override
    public void parseFromLocalFileData(byte[] buffer, int offset, int length)
            throws ZipException {
        throw new UnsupportedOperationException();
    }

}
