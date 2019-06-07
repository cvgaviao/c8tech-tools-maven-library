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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

public abstract class AbstractTypeHandler implements ExtendedArtifactHandler {

    private static final String BUNDLE_SYMBOLICNAME = "Bundle-SymbolicName";
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractTypeHandler.class);

    protected static final String POM_FILE = "pom.xml";

    private boolean addedToClasspath;

    private String classifier;

    private String directory;

    private String extension;

    private boolean includesDependencies;

    private String language;

    private String packaging;

    private boolean packOnTheFlyAllowed;

    private String type;

    public AbstractTypeHandler() {
    }

    public AbstractTypeHandler(String pType) {
        this.type = pType;
    }

    protected static File calculateWorkspaceDirectory(File pArtifactFile,
            String pFileExtension, String pFileTarget, String pDirExtension,
            String pDirTarget) {
        File inputFile = null;
        if (pArtifactFile == null) {
            return null;
        }
        if (pArtifactFile.isFile()
                && pArtifactFile.toPath().endsWith(pFileExtension)) {
            inputFile = pArtifactFile.toPath().getParent().resolve(pFileTarget)
                    .toFile();
        } else
            if (pArtifactFile.toPath().endsWith(pDirExtension)) {
                inputFile = pArtifactFile.toPath().getParent()
                        .resolve(pDirTarget).toFile();
            }
        return inputFile;
    }

    protected static final Map<String, String> readJarManifest(JarFile pJarFile, // NOSONAR
            Manifest manifest) { // NOSONAR
        if (manifest == null || manifest.getMainAttributes() == null) {
            return new HashMap<>(0);
        }
        PropertyResourceBundle resourceBundle = null;
        Map<String, String> result = new HashMap<>(
                manifest.getMainAttributes().size());
        String localization = manifest.getMainAttributes()
                .getValue("Bundle-Localization") + ".properties";
        if (!localization.isEmpty()) {
            ZipEntry entry = pJarFile.getEntry(localization);
            try {
                if (entry != null) {
                    InputStream io = pJarFile.getInputStream(entry);
                    resourceBundle = new PropertyResourceBundle(io);
                }
            } catch (IOException e) {
                LOG.warn("Localization file was not found for " + localization,
                        e);
            }
        }
        Set<Object> keys = manifest.getMainAttributes().keySet();

        for (Object key : keys) {
            String value = manifest.getMainAttributes().get(key).toString();
            if (value.startsWith("%") && resourceBundle != null) {
                try {
                    String localized = resourceBundle
                            .getString(value.substring(1));
                    result.put(key.toString(), localized);
                } catch (MissingResourceException e) {
                    LOG.warn("Localized resource was not found. " + value, e);
                }
            } else {
                result.put(key.toString(), value);
            }
        }
        return result;
    }

    protected static final Map<String, String> readJarManifest(
            Manifest manifest) {
        Map<String, String> result = new HashMap<>(
                manifest.getMainAttributes().size());
        Set<Entry<Object, Object>> entries = manifest.getMainAttributes()
                .entrySet();
        for (Entry<Object, Object> entry : entries) {
            String keystr = entry.getKey().toString();
            if (keystr.startsWith(BUNDLE_SYMBOLICNAME)) {
                keystr = keystr.split(";")[0];
            }
            result.put(keystr, entry.getValue().toString());
        }
        return result;
    }

    @Override
    public String defaultResourceProcessorClassName() {
        return null;
    }

    @Override
    public String defaultSymbolicNameHeader() {
        return "";
    }

    @Override
    public String defaultVersionHeader() {
        return "";
    }

    @Override
    public Map<String, String> extractHeadersFromManifestFile(
            File pManifestFile) throws IOException {
        Manifest manifest;
        try {
            manifest = new Manifest(new FileInputStream(pManifestFile));
            return readJarManifest(manifest);
        } catch (IOException e) {
            throw new IOException(
                    "Failure while opening an artifact archive from "
                            + pManifestFile,
                    e);
        }
    }

    @Override
    public Map<String, String> extractManifestHeadersFromArchive(
            File pArtifactArchiveFile) throws MojoExecutionException {
        Map<String, String> result;
        if (!pArtifactArchiveFile.isFile()) {
            throw new MojoExecutionException("The specified archive "
                    + pArtifactArchiveFile.getPath() + " is not valid.");
        }

        try (JarFile fis = new JarFile(pArtifactArchiveFile)) {
            result = readJarManifest(fis, fis.getManifest());
            return Collections.unmodifiableMap(result);
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Failure while opening an artifact archive from "
                            + pArtifactArchiveFile.getPath(),
                    e);
        }
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getDirectory() {
        if (directory == null) {
            directory = getPackaging() + "s";
        }
        return directory;
    }

    @Override
    public String getExtension() {
        if (extension == null) {
            extension = type;
        }
        return extension;
    }

    @Override
    public String getLanguage() {
        if (language == null) {
            language = "none";
        }

        return language;
    }

    protected File getManifestFileLocation(File pArtifactFile) {

        return getWorkspaceDirectory(pArtifactFile.toPath()
                .resolve(defaultManifestLocation()).toFile());
    }

    @Override
    public File getOutputDirectory(File pArtifactFile) {
        File inputFile = null;
        if (pArtifactFile == null) {
            return null;
        }
        if (pArtifactFile.isFile()
                && pArtifactFile.toPath().endsWith(POM_FILE)) {
            inputFile = pArtifactFile.toPath().getParent().resolve("target")
                    .toFile();
        } else
            if (pArtifactFile.toPath().endsWith("target/classes")) {
                inputFile = pArtifactFile.toPath().getParent().toFile();
            }
        return inputFile;
    }

    @Override
    public String getPackaging() {
        if (packaging == null) {
            packaging = type;
        }
        return packaging;
    }

    public String getType() {
        return type;
    }

    protected abstract List<String> getValidTypes();

    @Override
    public File getWorkspaceDirectory(File pArtifactFile) {
        File inputFile = null;
        if (pArtifactFile.isFile()
                && pArtifactFile.toPath().endsWith(POM_FILE)) {
            inputFile = pArtifactFile.toPath().getParent().resolve("target/")
                    .toFile();
        }
        return inputFile;
    }

    @Override
    public boolean isAddedToClasspath() {
        return addedToClasspath;
    }

    @Override
    public boolean isAssemblyUrlSchemaAllowed() {
        return false;
    }

    @Override
    public boolean isIncludesDependencies() {
        return includesDependencies;
    }

    @Override
    public boolean isManifestFileRequired() {
        return false;
    }

    @Override
    public boolean isPackOnTheFlyAllowed() {
        return packOnTheFlyAllowed;
    }

    @Override
    public final boolean isTypeValid(String pType) {
        return getValidTypes().contains(pType);
    }

    @Override
    public boolean isWorkspaceProject(File pArtifactFile) {
        LOG.debug("   Checking whether is a workspace project:'{}'",
                pArtifactFile);

        return pArtifactFile.toPath()
                .endsWith(CommonMojoConstants.MAVEN_TARGET_CLASSES_FOLDER)
                || pArtifactFile.toPath().endsWith(POM_FILE);
    }

    @Override
    public Path lookupManifestFileInProjectDirectory(Artifact pArtifact)
            throws IOException {
        File parent = getWorkspaceDirectory(pArtifact.getFile());
        if (parent == null || !parent.exists())
            return null;
        Path p = parent.toPath()
                .resolve(CommonMojoConstants.JAR_MANIFEST_LOCATION);
        if (Files.isReadable(p)) {
            return p;
        }
        throw new IOException(String.format("File %s was not found.", p));
    }

    protected void setAddedToClasspath(boolean addedToClasspath) {
        this.addedToClasspath = addedToClasspath;
    }

    protected void setClassifier(String pClassifier) {
        classifier = pClassifier;
    }

    protected void setDirectory(String pDirectory) {
        directory = pDirectory;
    }

    protected void setExtension(String extension) {
        this.extension = extension;
    }

    protected void setIncludesDependencies(boolean includesDependencies) {
        this.includesDependencies = includesDependencies;
    }

    protected void setLanguage(String language) {
        this.language = language;
    }

    protected void setPackOnTheFlyAllowed(boolean pPackOnTheFlyAllowed) {
        packOnTheFlyAllowed = pPackOnTheFlyAllowed;
    }

}
