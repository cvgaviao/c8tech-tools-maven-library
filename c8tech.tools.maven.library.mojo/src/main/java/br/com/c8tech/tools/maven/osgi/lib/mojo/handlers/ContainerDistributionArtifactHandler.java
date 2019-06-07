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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.ArtifactHandler;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

@Named(CommonMojoConstants.OSGI_CONTAINER_PACKAGING)
@Singleton
@Typed(value = { ArtifactHandler.class, ExtendedArtifactHandler.class })
public class ContainerDistributionArtifactHandler extends AbstractTypeHandler {

    @Inject
    public ContainerDistributionArtifactHandler() {
        super(CommonMojoConstants.OSGI_CONTAINER_PACKAGING);
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_CONTAINER_ARCHIVE_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(false);
        setPackOnTheFlyAllowed(false);
    }

    @Override
    public Path defaultManifestLocation() {
        return null;
    }

    @Override
    public String getDirectory() {
        return "";
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList(CommonMojoConstants.OSGI_CONTAINER_PACKAGING);
    }

    @Override
    public boolean isManifestFileRequired() {
        return false;
    }

    @Override
    public String defaultVersionHeader() {
        return "";
    }

    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pExtractedManifestHeaders) throws IOException {
        return false;
    }

    @Override
    public boolean isExtensionValid(Path pFilePath) {
        return pFilePath.toString()
                .endsWith(CommonMojoConstants.OSGI_CONTAINER_ARCHIVE_EXTENSION);
    }

    @Override
    public File getWorkspaceDirectory(File pArtifactFile) {
        return null;
    }
}
