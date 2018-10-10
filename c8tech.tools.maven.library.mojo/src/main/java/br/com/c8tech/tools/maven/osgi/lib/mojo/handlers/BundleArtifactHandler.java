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

@Named(BundleArtifactHandler.PACKAGING)
@Singleton
@Typed(value = { ArtifactHandler.class, ExtendedArtifactHandler.class })
public class BundleArtifactHandler extends AbstractTypeHandler {

    private static final String[] DEFAULT_VALID_TYPES = { "jar",
            BundleArtifactHandler.PACKAGING, "eclipse-plugin", "takari-jar" };

    public static final String PACKAGING = "bundle";

    public BundleArtifactHandler() {
        super(PACKAGING);
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_BUNDLE_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(true);
        setPackOnTheFlyAllowed(true);
    }

    @Inject
    public BundleArtifactHandler(ArtifactHandler pArtifactHandler) {
        super(pArtifactHandler.getPackaging());
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_BUNDLE_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(true);
        setPackOnTheFlyAllowed(true);
    }

    public static String[] getDefaultValidTypes() {
        return DEFAULT_VALID_TYPES;
    }

    @Override
    public Path defaultManifestLocation() {
        return Paths.get(CommonMojoConstants.JAR_MANIFEST_LOCATION);
    }

    @Override
    public String getDirectory() {
        return CommonMojoConstants.OSGI_BUNDLES_DIRECTORY;
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList(DEFAULT_VALID_TYPES);
    }

    @Override
    public File getWorkspaceDirectory(final File pArtifactFile) {
        File inputFile = null;
        if (pArtifactFile.isFile()
                && pArtifactFile.toPath().endsWith(POM_FILE)) {
            inputFile = pArtifactFile.toPath().getParent()
                    .resolve("target/classes/").toFile();
        } else
            if (pArtifactFile.isDirectory()
                    && pArtifactFile.toPath().endsWith("target/classes/")) {
                inputFile = pArtifactFile;
            }
        return inputFile;
    }

    @Override
    public boolean isAssemblyUrlSchemaAllowed() {
        return true;
    }

    @Override
    public boolean isManifestFileRequired() {
        return true;
    }

    /**
     * Check the validity of the informed bundle jar manifest headers map
     * values.
     *
     * @param pJarManifestHeaders
     *                                A map containing the bundle manifest
     *                                header values.
     * @return True when the inputFile where a valid bundle jar, false
     *         otherwise.
     */
    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pJarManifestHeaders) {
        if (pJarManifestHeaders == null || pJarManifestHeaders.isEmpty())
            return false;

        return (pJarManifestHeaders
                .get(CommonMojoConstants.OSGI_BUNDLE_HEADER_SN) != null
                && pJarManifestHeaders.get(
                        CommonMojoConstants.OSGI_BUNDLE_HEADER_VERSION) != null);
    }

    @Override
    public boolean isExtensionValid(Path pFilePath) {
        return CommonMojoConstants.OSGI_BUNDLE_EXTENSION
                .equals(pFilePath.toString()
                        .substring(pFilePath.toString().lastIndexOf('.') + 1));
    }

    @Override
    public String defaultSymbolicNameHeader() {
        return CommonMojoConstants.OSGI_BUNDLE_HEADER_SN;
    }

    @Override
    public String defaultVersionHeader() {
        return CommonMojoConstants.OSGI_BUNDLE_HEADER_VERSION;
    }

}
