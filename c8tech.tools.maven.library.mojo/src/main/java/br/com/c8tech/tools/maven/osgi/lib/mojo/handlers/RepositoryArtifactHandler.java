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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.ArtifactHandler;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

@Named(CommonMojoConstants.OSGI_REPOSITORY_PACKAGING)
@Singleton
@Typed(value = { ArtifactHandler.class, ExtendedArtifactHandler.class })
public class RepositoryArtifactHandler extends AbstractTypeHandler {

    @Inject
    public RepositoryArtifactHandler() {
        super(CommonMojoConstants.OSGI_REPOSITORY_PACKAGING);
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_REPOSITORY_ARCHIVE_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(true);
        setPackOnTheFlyAllowed(false);
    }

    @Override
    public Path defaultManifestLocation() {
        return Paths.get(CommonMojoConstants.JAR_MANIFEST_LOCATION);
    }

    @Override
    public String getDirectory() {
        return CommonMojoConstants.OSGI_REPOSITORIES_DIRECTORY;
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList(CommonMojoConstants.OSGI_REPOSITORY_PACKAGING);
    }

    @Override
    public File getWorkspaceDirectory(File pArtifactFile) {
        File inputFile = null;
        if (pArtifactFile.isFile() && pArtifactFile.toPath().endsWith(POM_FILE)) {
            inputFile = pArtifactFile.toPath().getParent()
                    .resolve("target/repository/").toFile();
        }
        return inputFile;
    }

    @Override
    public boolean isManifestFileRequired() {
        return false;
    }
    
    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pExtractedManifestHeaders) throws IOException {
        return false;
    }

    @Override
    public boolean isExtensionValid(Path pFilePath) {
        return CommonMojoConstants.OSGI_REPOSITORY_ARCHIVE_EXTENSION
                .equals(pFilePath.toString()
                        .substring(pFilePath.toString().lastIndexOf('.') + 1));
    }

}
