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
package br.com.c8tech.tools.maven.osgi.lib.mojo.beans;

import java.net.MalformedURLException;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

public final class P2ArtifactSet extends ArtifactSet {

    private static final Logger LOG = LoggerFactory
            .getLogger(P2ArtifactSet.class);

    /**
     * The groupId to be used when installing or deploying artifacts coming from
     * a p2 repository.
     */
    private String defaultGroupId;

    /**
     * The URL of the p2 repository where the artifacts should be searched from
     * and if found downloaded.
     */
    private URL repositoryURL = null;

    public P2ArtifactSet() {
        super();
    }

    @Override
    public void addArtifact(BundleRef pArtifactItem)
            throws MojoExecutionException {
        if (pArtifactItem.getVersion() == null) {
            throw new MojoExecutionException(
                    "The declared p2 artifact '" + pArtifactItem.getArtifactId()
                            + "' has no version defined.");

        }
        try {
            if (pArtifactItem.getCachePath() == null && getCacheDirectory() != null) {
                pArtifactItem.setCachePath(getCacheDirectory());
            }
            URL url = calculateBundleUrl(pArtifactItem);
            if (url != null) {
                pArtifactItem.setLocationURL(url);
            } else
                throw new MojoExecutionException("Missing Repository URL");
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error adding the bundle "
                    + pArtifactItem.getArtifactId() + " into bundle set.", e);
        }
        super.addArtifact(pArtifactItem);
    }

    private URL calculateBundleUrl(BundleRef pArtifactItem)
            throws MalformedURLException {
        if (repositoryURL != null) {
            String p2ArtifactFileName = pArtifactItem.getArtifactId() + "_"
                    + pArtifactItem.getVersion() + ".jar";
            String eclipseDownloadFormatPath = repositoryURL + "/"  //NOSONAR
                    + CommonMojoConstants.OSGI_BUNDLES_DIRECTORY + "/" //NOSONAR
                    + p2ArtifactFileName;
            LOG.debug("Using the following URL: {}", eclipseDownloadFormatPath);
            return new URL(eclipseDownloadFormatPath);
        } else
            if (pArtifactItem.getLocationURL() != null) {
                return pArtifactItem.getLocationURL();
            } else {
                return null;
            }
    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public URL getRepositoryURL() {
        return repositoryURL;
    }

    public void setDefaultGroupId(String pDefaultGroupId) {
        defaultGroupId = pDefaultGroupId;
    }

    public void setRepositoryURL(String pRepositoryURL)
            throws MalformedURLException {
        if (pRepositoryURL != null)
            this.repositoryURL = new URL(pRepositoryURL);
    }

    @Override
    public String toString() {
        return String.format("P2ArtifactSet {repositoryURL=%s, artifacts=%s}",
                repositoryURL, getImmutableArtifactSet());
    }

}
