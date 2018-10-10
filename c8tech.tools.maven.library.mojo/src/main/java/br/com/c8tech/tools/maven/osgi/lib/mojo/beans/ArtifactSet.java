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
package br.com.c8tech.tools.maven.osgi.lib.mojo.beans;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.common.collect.ImmutableSet;

public abstract class ArtifactSet {

    /**
     * A list of artifacts to search and download from the specified
     * repositoryURL.
     */
    protected List<BundleRef> artifacts = new ArrayList<>();

    protected Path cacheDirectory;

    private Set<Artifact> resolvedArtifacts;

    public void addArtifact(BundleRef pArtifactItem)
            throws MojoExecutionException // NOSONAR
    {
        if (pArtifactItem.getType() == null) {
            pArtifactItem.setType("jar");
        }
        artifacts.add(pArtifactItem);
    }

    public void addArtifact(Artifact pResolvedArtifact) {
        getResolvedArtifacts().add(pResolvedArtifact);
    }

    public BundleRef findArtifact(String pGroupId, String pArtifactId) {
        for (BundleRef bundleRef : getImmutableArtifactSet()) {
            if (bundleRef.getGroupId().equals(pGroupId)
                    && bundleRef.getArtifactId().equals(pArtifactId)) {
                bundleRef.setCachePath(this.getCacheDirectory());
                return bundleRef;
            }
        }
        return null;
    }

    public Path getCacheDirectory() {
        return cacheDirectory;
    }

    public Set<BundleRef> getImmutableArtifactSet() {
        return ImmutableSet.copyOf(artifacts);
    }

    public Set<Artifact> getResolvedArtifacts() {
        if (resolvedArtifacts == null) {
            resolvedArtifacts = new HashSet<>();
        }
        return resolvedArtifacts;
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

    public void setArtifacts(List<BundleRef> bundleList)
            throws MojoExecutionException {
        for (BundleRef bundle : bundleList) {
            addArtifact(bundle);
        }
    }

    public void setCacheDirectory(Path cacheDir) {
        this.cacheDirectory = cacheDir;
    }

    public void setResolvedArtifacts(Set<Artifact> resolvedArtifacts) {
        this.resolvedArtifacts = resolvedArtifacts;
    }

    public int size() {
        return artifacts.size();
    }

    @Override
    public String toString() {
        return String.format("ArtifactSet {%s}", artifacts);
    }

}
