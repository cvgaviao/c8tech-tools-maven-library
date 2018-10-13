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
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.ArtifactTracker;

@SuppressWarnings("rawtypes")
public abstract class AbstractArtifactTracker<I extends AbstractArtifactTracker<I, B>, B extends AbstractArtifactTrackerBuilder<B, I>>
        implements ArtifactTracker, Comparable<AbstractArtifactTracker> {

    private final String artifactId;

    private boolean cached;

    private Path cachedFilePath;

    private final Path cacheDir;

    private final String classifier;

    /**
     * When this object is mapping an artifact coming from a maven repository
     * then this attribute will initially be <i>null</i>, what is normal in most
     * of cases. <br>
     * After the normalization process (that is madden against a target location
     * with a remote or local URL) it may pass to point to the file in the cache
     * directory, when the use case requires it to be copied to there.
     * <p>
     * When it is mapping an artifact coming from a p2 repository then it points
     * to the file in remote repository.
     * <p>
     */
    private String downloadUrl;

    private ExtendedArtifactHandler extendedArtifactHandler;

    private final String groupId;

    private final boolean groupingByTypeDirectory;

    private final Map<String, String> manifestMap;

    private final boolean optional;

    private final File originalFile;

    private final boolean previousCachingRequired;

    private final String scope;

    private final int startLevel;

    private final String symbolicName;

    private final boolean toBeCached;

    private final boolean toBeEmbedded;

    private final String type;

    private final String typeHandlerDirectory;

    private final String version;

    private final boolean workspaceProject;

    protected AbstractArtifactTracker(B pBuilder) {
        this.artifactId = pBuilder.getArtifactId();
        this.groupId = pBuilder.getGroupId();
        this.downloadUrl = pBuilder.getDownloadUrl();
        this.classifier = pBuilder.getClassifier();
        this.extendedArtifactHandler = pBuilder.getExtendedArtifactHandler();
        this.cachedFilePath = pBuilder.getCachedFilePath();
        this.scope = pBuilder.getScope();
        this.type = pBuilder.getType();
        this.originalFile = pBuilder.getOriginalFile();
        this.toBeCached = pBuilder.isToBeCached();
        this.toBeEmbedded = pBuilder.isToBeEmbedded();
        this.optional = pBuilder.isOptional();
        this.version = pBuilder.getVersion();
        this.startLevel = pBuilder.getStartLevel();
        this.workspaceProject = pBuilder.isWorkspaceProject();
        this.cacheDir = pBuilder.getCachedDir();
        this.typeHandlerDirectory = pBuilder.getTypeHandlerDirectory();
        this.groupingByTypeDirectory = pBuilder.groupingByTypeDirectory;
        this.manifestMap = pBuilder.getManifestMap();
        this.symbolicName = pBuilder.getSymbolicName();
        this.previousCachingRequired = pBuilder.isPreviousCachingRequired();
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public Path getCachedFilePath() {
        return cachedFilePath;
    }

    @Override
    public Path getCacheDir() {
        return cacheDir;
    }

    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public String getExtension() {
        return getTypeHandler().getExtension();
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public Map<String, String> getManifestHeaders() throws IOException {
        return manifestMap;
    }

    @Override
    public File getOriginalFile() {
        return originalFile;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public int getStartLevel() {
        return startLevel;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ExtendedArtifactHandler getTypeHandler() {
        return extendedArtifactHandler;
    }

    public String getTypeHandlerDirectory() {
        return typeHandlerDirectory;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isCached() {
        return cached;
    }

    public boolean isGroupingByTypeDirectory() {
        return groupingByTypeDirectory;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public boolean isPreviousCachingRequired() {
        return previousCachingRequired;
    }

    @Override
    public boolean isToBeCached() {
        return toBeCached;
    }

    @Override
    public boolean isToBeEmbedded() {
        return toBeEmbedded;
    }

    @Override
    public boolean isWorkspaceProject() {
        return workspaceProject;
    }

    public void setCached() {

        if (cachedFilePath != null && cachedFilePath.toFile().exists()) {
            cached = true;
        } else {
            cached = false;
        }
    }

    @Override
    public int compareTo(AbstractArtifactTracker pOther) {
        return this.getArtifactId().compareTo(pOther.getArtifactId());
    }

    @Override
    public boolean equals(Object pOther) {
        if (pOther == this)
            return true;
        if (!(pOther instanceof AbstractArtifactTracker)) {
            return false;
        }
        AbstractArtifactTracker artifactTracker = (AbstractArtifactTracker) pOther;

        return Objects.equals(getArtifactId(), artifactTracker.getArtifactId())
                && Objects.equals(getVersion(), artifactTracker.getVersion());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtifactId(), getVersion());
    }

    @Override
    public Artifact toArtifact() {
        DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId,
                version, scope, type, "", extendedArtifactHandler);
        artifact.setFile(originalFile);
        return artifact;
    }

    public B toBuilder() {

        return toBuilderInstance().withArtifactId(getArtifactId())
                .withCachedFilePath(getCachedFilePath())
                .withClassifier(getClassifier()).withVersion(getVersion())
                .withOriginalFile(getOriginalFile()).withScope(getScope())
                .withExtendedArtifactHandler(getTypeHandler())
                .withGroupId(getGroupId()).withOptional(isOptional())
                .withStartLevel(getStartLevel())
                .withWorkspaceProject(isWorkspaceProject())
                .withToBeCached(isToBeCached()).withManifestMap(manifestMap)
                .withToBeEmbedded(isToBeEmbedded()).withType(getType())
                .withTypeHandlerDirectory(getTypeHandlerDirectory())
                .withDownloadUrl(getDownloadUrl());
    }

    protected abstract B toBuilderInstance();

    @Override
    public String toString() {
        return String.format(
                "ArtifactTracker [ws=%s, groupId=%s, artifactId=%s, optional=%s, scope=%s, "
                        + "type=%s, version=%s, toEmbed=%s, toCache=%s, cached=%s, startLevel=%s]",
                workspaceProject, groupId, artifactId, optional, scope, type,
                version, toBeEmbedded, toBeCached, cached, startLevel);
    }
}
