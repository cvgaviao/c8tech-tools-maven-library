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
package br.com.c8tech.tools.maven.osgi.lib.mojo.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.FileSet;

public interface DirectoryHelper {

    /**
     * 
     * @param directoryPath
     *                          The path of a directory to be cleaned.
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    public void cleanDirectory(final Path directoryPath) throws IOException;

    /**
     * 
     * @param sourcePath
     *                       The source path to be copied.
     * @param targetPath
     *                       The target path.
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    public void copyDirectory(final Path sourcePath, final Path targetPath)
            throws IOException;

    /**
     * 
     * @param fileSets
     *                     An array of FileSet.
     * @return A set of found files.
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    Set<File> findFiles(FileSet... fileSets) throws IOException;

    /**
     * 
     * @param fileSetCollection
     *                              A list of FileSet.
     * @return A set of found files.
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    Set<File> findFiles(List<FileSet> fileSetCollection) throws IOException;

    /**
     * 
     * @param directory
     *                            The root directory where to start looking for.
     * @param includePatterns
     *                            The patterns of directories and files to be
     *                            considered in the search.
     * @param excludePatterns
     *                            The patterns to of directories and files to be
     *                            unconsidered in the search.
     * @return A set of found files.
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    Set<File> findFiles(Path directory, List<String> includePatterns,
            List<String> excludePatterns) throws IOException;

    File copyResourceToDirectory(URL pResource, Path pCacheDirectory)
            throws IOException;

    File copyResourceFromWebServerToDirectory(URL resourceUrl,
            Path targetDirectory) throws IOException;
}
