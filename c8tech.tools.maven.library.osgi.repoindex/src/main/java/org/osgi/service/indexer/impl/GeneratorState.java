/**
 * ==========================================================================
 * Copyright © 2015-2018 OSGi Alliance, Cristiano Gavião.
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
package org.osgi.service.indexer.impl;

import java.nio.file.Path;

public class GeneratorState {

    private final boolean forceAbsolutePath;
    private final Path futureBundleCopyPath;
    private final Path futureSubsystemCopyPath;
    private final Path rootPath;
    private final String urlTemplate;

    public GeneratorState(Path pRootPath, Path pFutureBundleCopyPath,
            Path pFutureSubsystemCopyPath, String urlTemplate,
            boolean skipRelativise) {
        this.rootPath = pRootPath;
        this.futureBundleCopyPath = pFutureBundleCopyPath;
        this.futureSubsystemCopyPath = pFutureSubsystemCopyPath;
        this.urlTemplate = urlTemplate;
        this.forceAbsolutePath = skipRelativise;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GeneratorState other = (GeneratorState) obj;
        if (rootPath == null) {
            if (other.rootPath != null)
                return false;
        } else
            if (!rootPath.equals(other.rootPath))
                return false;
        if (urlTemplate == null) {
            if (other.urlTemplate != null)
                return false;
        } else
            if (!urlTemplate.equals(other.urlTemplate))
                return false;
        return true;
    }

    public Path getBundleCopyDirPath() {
        return futureBundleCopyPath;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public Path getSubsystemCopyDirPath() {
        return futureSubsystemCopyPath;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((rootPath == null) ? 0 : rootPath.hashCode());
        result = prime * result
                + ((urlTemplate == null) ? 0 : urlTemplate.hashCode());
        return result;
    }

    public boolean isForceAbsolutePath() {
        return forceAbsolutePath;
    }

    @Override
    public String toString() {
        return "GeneratorState [rootPath=" + rootPath + ", urlTemplate="
                + urlTemplate + "]";
    }

}
