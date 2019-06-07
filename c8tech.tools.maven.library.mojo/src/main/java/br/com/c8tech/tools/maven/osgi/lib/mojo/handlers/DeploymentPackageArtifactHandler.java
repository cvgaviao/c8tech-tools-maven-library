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

@Named(CommonMojoConstants.OSGI_DP_PACKAGING)
@Singleton
@Typed(value = { ArtifactHandler.class, ExtendedArtifactHandler.class })
public class DeploymentPackageArtifactHandler extends AbstractTypeHandler {

    @Inject
    public DeploymentPackageArtifactHandler() {
        super(CommonMojoConstants.OSGI_DP_PACKAGING);
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_DP_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(true);
        setPackOnTheFlyAllowed(true);
    }

    @Override
    public Path defaultManifestLocation() {
        return Paths.get(CommonMojoConstants.JAR_MANIFEST_LOCATION);
    }

    @Override
    public String getDirectory() {
        return CommonMojoConstants.OSGI_DP_DIRECTORY;
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList(CommonMojoConstants.OSGI_DP_PACKAGING);
    }

    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pJarManifestHeaders) {
        return (pJarManifestHeaders != null && pJarManifestHeaders
                .get(CommonMojoConstants.OSGI_DP_MANIFEST_SYMBOLIC_NAME) != null
                && pJarManifestHeaders.get(
                        CommonMojoConstants.OSGI_DP_MANIFEST_VERSION) != null);

    }

    @Override
    public String defaultSymbolicNameHeader() {
        return CommonMojoConstants.OSGI_DP_MANIFEST_SYMBOLIC_NAME;
    }

    @Override
    public boolean isExtensionValid(Path pFilePath) {
        return CommonMojoConstants.OSGI_DP_EXTENSION.equals(pFilePath.toString()
                .substring(pFilePath.toString().lastIndexOf('.') + 1));
    }

    @Override
    public File getWorkspaceDirectory(File pArtifactFile) {

        return calculateWorkspaceDirectory(pArtifactFile, POM_FILE,
                "target/dp/", "target/classes", "dp/");
    }
}
