/**
 * ======================================================================
 * Copyright © 2015-2019, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.takari.incrementalbuild.ResourceStatus;
import io.takari.incrementalbuild.spi.ResourceHolder;
import io.takari.incrementalbuild.util.URLResourceHolder;

public class URLResourceBySizeHolder implements ResourceHolder<URL> {

    private static final Logger LOG = LoggerFactory
            .getLogger(URLResourceBySizeHolder.class);

    private static final long serialVersionUID = -5524089194700208024L;

    private final byte[] hash;

    private final URL url;

    public URLResourceBySizeHolder(URL pUrl) throws IOException {
        this.url = pUrl;
        this.hash = hash(this.url);
    }

    public static byte[] hash(URL resourceURL) throws IOException {
        String lastModifiedDate;
        String resourceSize;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        URLConnection connection = resourceURL.openConnection();
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpCon = (HttpURLConnection) resourceURL
                    .openConnection();
            lastModifiedDate = Long.toString(httpCon.getLastModified());
            digest.update(lastModifiedDate.getBytes());
            resourceSize = Long.toString(httpCon.getContentLengthLong());
            digest.update(resourceSize.getBytes());
        } else {
            lastModifiedDate = Long.toString(connection.getLastModified());
            digest.update(lastModifiedDate.getBytes());
            resourceSize = Long.toString(connection.getContentLengthLong());
            digest.update(resourceSize.getBytes());
        }
        return digest.digest();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URLResourceHolder)) {
            return false;
        }
        URLResourceBySizeHolder other = (URLResourceBySizeHolder) obj; // NOSONAR
        return url.equals(other.url) // NOSONAR
                && Arrays.equals(hash, other.hash);
    }

    @Override
    public URL getResource() {
        return url;
    }

    @Override
    public ResourceStatus getStatus() {
        byte[] newHash;
        try {
            newHash = hash(url);
        } catch (IOException x) {
            LOG.warn("Error generating a Hash for an URL.", x);
            return ResourceStatus.REMOVED;
        } // NOSONAR
        return Arrays.equals(hash, newHash) ? ResourceStatus.UNMODIFIED
                : ResourceStatus.MODIFIED;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url.hashCode(), Arrays.hashCode(this.hash)); // NOSONAR
    }
}
