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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

public abstract class AbstractSubsystemArtifactHandler
        extends AbstractTypeHandler {

    private static final String[] DEFAULT_VALID_TYPES = {
            CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_APPLICATION,
            CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_COMPOSITE,
            CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_FEATURE, };

    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractSubsystemArtifactHandler.class);

    public AbstractSubsystemArtifactHandler(String pType) {
        super(pType);
        setPackOnTheFlyAllowed(true);
    }

    public static String[] getDefaultValidTypes() {
        return DEFAULT_VALID_TYPES;
    }

    @Override
    public Path defaultManifestLocation() {
        return Paths.get(CommonMojoConstants.OSGI_SUBSYSTEM_MANIFEST_LOCATION);
    }

    @Override
    public String defaultResourceProcessorClassName() {
        return "com.c8tech.hawkeyes.provisioning.processors.SubsystemResourceProcessor";
    }

    @Override
    public String defaultSymbolicNameHeader() {
        return CommonMojoConstants.OSGI_SUBSYSTEM_SN;
    }

    @Override
    public String defaultVersionHeader() {
        return CommonMojoConstants.OSGI_SUBSYSTEM_VERSION;
    }

    @Override
    public Map<String, String> extractManifestHeadersFromArchive(
            File pArtifactArchiveFile) throws MojoExecutionException {
        Map<String, String> result;

        try (ZipFile fis = new ZipFile(pArtifactArchiveFile)) {
            Manifest manifest = new Manifest(fis.getInputStream(fis.getEntry(
                    CommonMojoConstants.OSGI_SUBSYSTEM_MANIFEST_LOCATION)));
            result = new HashMap<>(manifest.getMainAttributes().size());
            Set<Object> keys = manifest.getMainAttributes().keySet();
            for (Object key : keys) {
                result.put(key.toString(),
                        manifest.getMainAttributes().get(key).toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Failure extracting the manifest headers of a subsystem archive from"
                            + pArtifactArchiveFile + ".",
                    e);
        }

        return Collections.unmodifiableMap(result);

    }

    @Override
    public File getWorkspaceDirectory(File pArtifactFile) {

        return calculateWorkspaceDirectory(pArtifactFile, POM_FILE,
                "target/esa/", "target/classes", "esa/");
    }

    @Override
    public String getDirectory() {
        return CommonMojoConstants.OSGI_SUBSYSTEM_DIRECTORY;
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList(DEFAULT_VALID_TYPES);
    }

    @Override
    public boolean isManifestFileRequired() {
        return true;
    }

    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pExtractedManifestHeaders) throws IOException {
        return (pExtractedManifestHeaders != null
                && pExtractedManifestHeaders
                        .get(CommonMojoConstants.OSGI_SUBSYSTEM_SN) != null
                && pExtractedManifestHeaders
                        .get(CommonMojoConstants.OSGI_SUBSYSTEM_VERSION) != null
                && pExtractedManifestHeaders
                        .get(CommonMojoConstants.OSGI_SUBSYSTEM_TYPE) != null);
    }

    @Override
    public final boolean isExtensionValid(Path pFilePath) {
        return CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION
                .equals(pFilePath.toString()
                        .substring(pFilePath.toString().lastIndexOf('.') + 1));
    }

    @Override
    public Path lookupManifestFileInProjectDirectory(Artifact pArtifact)
            throws IOException {
        File parent = getWorkspaceDirectory(pArtifact.getFile());

        if (!parent.exists()) {
            return null;
        }
        Path p = parent.toPath()
                .resolve(CommonMojoConstants.OSGI_SUBSYSTEM_MANIFEST_LOCATION);
        if (Files.isReadable(p)) {
            LOG.debug("Found Subsystem Manifest at {}.", p);
            return p;
        }
        throw new IOException(String.format("File %s was not found.", p));
    }
}
