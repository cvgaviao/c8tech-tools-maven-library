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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.BundleRef;
import br.com.c8tech.tools.maven.osgi.lib.mojo.components.DirectoryHelperDefault;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.BundleArtifactHandler;

public class P2ArtifactTracker extends
        AbstractArtifactTracker<P2ArtifactTracker, P2ArtifactTracker.Builder> {
    public static class Builder extends
            AbstractArtifactTrackerBuilder<P2ArtifactTracker.Builder, P2ArtifactTracker> {

        private final String defaultGroupId;

        protected Builder(String pDefaultGroupId, Path pCacheDir,
                boolean pGroupingByTypeDirectory,
                boolean pPreviousCachingRequired) {
            super(pCacheDir, pGroupingByTypeDirectory,
                    pPreviousCachingRequired);
            defaultGroupId = pDefaultGroupId;
        }

        @Override
        public P2ArtifactTracker build() throws MojoExecutionException {
            String name;
            try {
                name = DirectoryHelperDefault
                        .getFileNameFromUrl(getDownloadUrl());
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Error occurred while creating a P2Tracker object.", e);
            }

            Path filePath = groupingByTypeDirectory
                    ? cacheDir
                            .resolve(
                                    getExtendedArtifactHandler().getDirectory())
                            .resolve(name)
                    : cacheDir.resolve(name);

            if (isPreviousCachingRequired()) {
                if (!filePath.toFile().exists()) {
                    throw new MojoExecutionException(
                            "Previous P2 artifact cached file was not found at "
                                    + filePath.toString());
                } else {
                    Map<String, String> jarManifestHeaders = getExtendedArtifactHandler()
                            .extractManifestHeadersFromArchive(
                                    filePath.toFile());
                    withManifestMap(jarManifestHeaders);
                }
            }
            withOriginalFile(filePath.toFile());
            withCachedFilePath(filePath);
            withToBeEmbedded(true);

            return super.build();
        }

        public String getDefaultGroupId() {
            return defaultGroupId;
        }

        public Builder withBundleRef(BundleRef pBundleFromP2) {

            withArtifactId(pBundleFromP2.getArtifactId());
            withExtendedArtifactHandler(new BundleArtifactHandler());
            withGroupId(pBundleFromP2.getGroupId() != null
                    ? pBundleFromP2.getGroupId()
                    : defaultGroupId);
            withScope(Artifact.SCOPE_COMPILE);
            withDownloadUrl(pBundleFromP2.getLocationURL() != null
                    ? pBundleFromP2.getLocationURL().toString()
                    : "");
            withStartLevel(pBundleFromP2.getStartLevel());
            withVersion(pBundleFromP2.getVersion());
            withType(CommonMojoConstants.JAR_EXTENSION);
            withOptional(false);
            withWorkspaceProject(false);
            withToBeCached(true);
            return this;
        }

    }

    private final String defaultGroupId;

    protected P2ArtifactTracker(Builder pBuilder) {
        super(pBuilder);
        this.defaultGroupId = pBuilder.defaultGroupId;
    }

    public static Builder builder(String pDefaultGroupId, Path pCacheDir,
            boolean pGroupingByTypeDirectory,
            boolean pPreviousCachingRequired) {
        return new Builder(
                pDefaultGroupId == null ? "no-group" : pDefaultGroupId,
                pCacheDir, pGroupingByTypeDirectory, pPreviousCachingRequired);

    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    @Override
    protected Builder toBuilderInstance() {
        return new Builder(getDefaultGroupId(), getCacheDir(),
                isGroupingByTypeDirectory(), isPreviousCachingRequired());

    }

}
