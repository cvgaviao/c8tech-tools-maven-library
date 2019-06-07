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
package br.com.c8tech.tools.maven.osgi.lib.mojo.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;

public interface ExtendedArtifactHandler extends ArtifactHandler {

    Path defaultManifestLocation();

    String defaultResourceProcessorClassName();

    String defaultSymbolicNameHeader();

    String defaultVersionHeader();

    Map<String, String> extractHeadersFromManifestFile(File pManifestFile)
            throws IOException;

    Map<String, String> extractManifestHeadersFromArchive(
            File pArtifactArchiveFile) throws MojoExecutionException;

    File getOutputDirectory(File pArtifactFile);

    File getWorkspaceDirectory(File pArtifactFile);

    boolean isArtifactManifestValid(
            Map<String, String> pExtractedManifestHeaders) throws IOException;

    boolean isAssemblyUrlSchemaAllowed();

    boolean isExtensionValid(Path pFilePath);

    boolean isManifestFileRequired();

    boolean isPackOnTheFlyAllowed();

    boolean isTypeValid(String pType);

    boolean isWorkspaceProject(File pArtifactFile);

    Path lookupManifestFileInProjectDirectory(Artifact pArtifact)
            throws IOException;

}
