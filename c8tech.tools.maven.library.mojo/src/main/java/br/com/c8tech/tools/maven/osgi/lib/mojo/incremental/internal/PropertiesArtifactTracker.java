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

import java.nio.file.Path;

public class PropertiesArtifactTracker extends
        AbstractArtifactTracker<PropertiesArtifactTracker, PropertiesArtifactTracker.Builder> {
    protected static class Builder extends
            AbstractArtifactTrackerBuilder<PropertiesArtifactTracker.Builder, PropertiesArtifactTracker> {

        protected Builder(Path pCacheDir, boolean pGroupingByTypeDirectory,
                boolean pPreviousCachingRequired) {
            super(pCacheDir, pGroupingByTypeDirectory,
                    pPreviousCachingRequired);
        }

    }

    protected PropertiesArtifactTracker(Builder pBuilder) {
        super(pBuilder);
    }

    public static Builder builder(Path pCacheDir,
            boolean pGroupingByTypeDirectory,
            boolean pPreviousCachingRequired) {
        return new Builder(pCacheDir, pGroupingByTypeDirectory,
                pPreviousCachingRequired);
    }

    @Override
    protected Builder toBuilderInstance() {
        return new Builder(getCacheDir(), isGroupingByTypeDirectory(),
                isPreviousCachingRequired());
    }
}
