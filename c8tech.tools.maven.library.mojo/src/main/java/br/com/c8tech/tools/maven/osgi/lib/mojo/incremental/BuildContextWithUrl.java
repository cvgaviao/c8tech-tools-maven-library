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
import java.net.URL;
import java.nio.file.Path;

import io.takari.incrementalbuild.BuildContext;
import io.takari.incrementalbuild.ResourceMetadata;

/**
 * Build context that supports 1..* input-output associations.
 */
public interface BuildContextWithUrl extends BuildContext {

    /**
     * Registers specified input {@code URL} with this build context.
     * 
     * @param inputURL
     *                     The URL of the resource to input in the build
     *                     context.
     * 
     * @return {@link ResourceMetadata} representing the input file, never
     *         {@code null}.
     * @throws IOException
     *                         if inputFile is not a file or cannot be read
     */
    public ResourceMetadata<URL> registerInput(URL inputURL) throws IOException;

    /**
     * Registers specified input {@code URL} or the URL of the local copy in
     * cache with this build context.
     * <p>
     * In this case when there exists the same file pointed by the inputURL in
     * the cache directory the cache one will be used instead. This is necessary
     * in order to avoid to download the same file multiple times.
     * 
     * @param inputURL
     *                     The URL of the resource to input in the build
     *                     context.
     * @param cacheDir
     *                     The cache directory.
     * @return {@link ResourceMetadata} representing the input file, never
     *         {@code null}.
     * @throws IOException
     *                         if inputFile is not a file or cannot be read
     */
    public ResourceMetadata<URL> registerInput(URL inputURL, Path cacheDir)
            throws IOException;

}
