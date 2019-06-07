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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;

public interface ArtifactTracker {

    String getArtifactId();

    Path getCachedFilePath();

    Path getCacheDir();

    String getClassifier();

    /**
     * This point to the original file in the format of URL.
     * <p>
     * It must be a String in order to avoid {@link MalformedURLException} when using
     * the assembly:// protocol.
     *
     * @return the artifact's download URL.
     */
    String getDownloadUrl();

    String getGroupId();

    String getExtension();
    
    /**
     * Finds the artifact's manifest file (if its type allows one) and put its
     * headers into a map.
     *
     * @return a Map of the manifest headers, if exists.
     * @throws IOException when no manifest was found
     */
    Map<String, String> getManifestHeaders() throws IOException;

    /**
     * The file resolved by maven core.
     *
     * @return The artifact file from the local maven repository. It can be null
     *         when maven can't perform its resolution process.
     */
    File getOriginalFile();

    String getScope();

    int getStartLevel();

    String getSymbolicName() throws IOException;

    String getType();

    ExtendedArtifactHandler getTypeHandler();

    String getVersion();

    boolean isCached();

    boolean isOptional();
    
    boolean isToBeCached();

    boolean isToBeEmbedded();
    
    boolean isWorkspaceProject();

    void setCached();

    Artifact toArtifact();

}
