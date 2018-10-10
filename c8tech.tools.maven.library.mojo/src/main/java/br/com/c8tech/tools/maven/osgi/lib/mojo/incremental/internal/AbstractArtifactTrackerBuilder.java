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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;

public abstract class AbstractArtifactTrackerBuilder<B extends AbstractArtifactTrackerBuilder<B, I>, I extends AbstractArtifactTracker<I, B>> {

    private String artifactId;

    private Path cachedFilePath;

    protected final Path cacheDir;

    private String classifier;

    private String downloadUrl;

    private ExtendedArtifactHandler extendedArtifactHandler;

    private String groupId;

    protected final boolean groupingByTypeDirectory;

    private Map<String, String> manifestMap;

    private boolean optional;

    private File originalFile;

    private String scope;

    private int startLevel;

    private String symbolicName;

    private boolean toBeCached;

    private boolean toBeEmbedded;

    private String type;

    private String typeHandlerDirectory;

    private String version;

    private boolean workspaceProject;

    private final boolean previousCachingRequired;
    
    protected AbstractArtifactTrackerBuilder(Path pCacheDir,
            boolean pGroupingByTypeDirectory, boolean pPreviousCachingRequired) {
        this.cacheDir = pCacheDir;
        this.groupingByTypeDirectory = pGroupingByTypeDirectory;
        this.previousCachingRequired = pPreviousCachingRequired;
    }

    public I build() throws MojoExecutionException {
        Type builderType;

        Type trackerType;

        try {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new IllegalArgumentException("Missing type parameter.");
            }
            builderType = ((ParameterizedType) superclass)
                    .getActualTypeArguments()[0];
            trackerType = ((ParameterizedType) superclass)
                    .getActualTypeArguments()[1];
            return newInstance(builderType, trackerType);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            throw new MojoExecutionException(
                    "Error building ArtifactTracker instance", e);
        }
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Path getCachedDir() {
        return cacheDir;
    }

    public Path getCachedFilePath() {
        return cachedFilePath;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public ExtendedArtifactHandler getExtendedArtifactHandler() {
        return extendedArtifactHandler;
    }

    public String getGroupId() {
        return groupId;
    }

    public Map<String, String> getManifestMap() {
        if (manifestMap == null) {
            manifestMap = new HashMap<>();
        }

        return manifestMap;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public String getScope() {
        return scope;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getType() {
        return type;
    }

    public String getTypeHandlerDirectory() {
        return typeHandlerDirectory;
    }

    public String getVersion() {
        return version;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isPreviousCachingRequired() {
        return previousCachingRequired;
    }

    public boolean isToBeCached() {
        return toBeCached;
    }

    public boolean isToBeEmbedded() {
        return toBeEmbedded;
    }

    public boolean isWorkspaceProject() {
        return workspaceProject;
    }

    @SuppressWarnings("unchecked")
    private I newInstance(Type pBuilderType, Type pTrackerType)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Class<?> builderRawType = pBuilderType instanceof Class<?>
                ? (Class<?>) pBuilderType
                : (Class<?>) ((ParameterizedType) pBuilderType).getRawType();
        Class<?> trackerRawType = pTrackerType instanceof Class<?>
                ? (Class<?>) pTrackerType
                : (Class<?>) ((ParameterizedType) pTrackerType).getRawType();
        Constructor<I> trackerConstructor = (Constructor<I>) trackerRawType
                .getDeclaredConstructor(builderRawType);

        return trackerConstructor.newInstance(this);
    }

    @SuppressWarnings("unchecked")
    public B withArtifactId(String pArtifactId) {
        artifactId = pArtifactId;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withCachedFilePath(Path pCachedFilePath) {
        cachedFilePath = pCachedFilePath;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withClassifier(String pClassifier) {
        classifier = pClassifier;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withDownloadUrl(String pDownloadUrl) {
        downloadUrl = pDownloadUrl;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withExtendedArtifactHandler(
            ExtendedArtifactHandler pExtendedArtifactHandler) {
        extendedArtifactHandler = pExtendedArtifactHandler;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withGroupId(String pGroupId) {
        groupId = pGroupId;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withManifestMap(Map<String, String> pManifestMap) {
        manifestMap = pManifestMap == null ? Collections.emptyMap()
                : pManifestMap;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOptional(boolean pOptional) {
        optional = pOptional;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOriginalFile(File pOriginalFile) {
        originalFile = pOriginalFile;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOriginalFile(Path pOriginalFile) {
        originalFile = pOriginalFile != null ? pOriginalFile.toFile() : null;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withScope(String pScope) {
        scope = pScope;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withStartLevel(int pStartLevel) {
        startLevel = pStartLevel;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withSymbolicName(String pSymbolicName) {
        symbolicName = pSymbolicName;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withToBeCached(boolean pToBeCached) {
        toBeCached = pToBeCached;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withToBeEmbedded(boolean pToBeEmbedded) {
        toBeEmbedded = pToBeEmbedded;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withType(String pType) {
        type = pType;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withTypeHandlerDirectory(String pTypeHandlerDirectory) {
        typeHandlerDirectory = pTypeHandlerDirectory;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withVersion(String pVersion) {
        version = pVersion;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withWorkspaceProject(boolean pWorkspaceProject) {
        workspaceProject = pWorkspaceProject;
        return (B) this;
    }

}
