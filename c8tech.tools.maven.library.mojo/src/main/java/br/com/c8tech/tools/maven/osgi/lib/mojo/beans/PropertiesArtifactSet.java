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

import org.apache.maven.plugin.MojoExecutionException;

public class PropertiesArtifactSet extends ArtifactSet {

    public PropertiesArtifactSet() {
        super();
    }

    @Override
    public void addArtifact(BundleRef pArtifactItem)
            throws MojoExecutionException {
        if (pArtifactItem.getType() == null) {
            pArtifactItem.setType("properties");
        }
        artifacts.add(pArtifactItem);
    }

    @Override
    public String toString() {
        return String.format("Properties ArtifactSet {%s}", artifacts);
    }

    public BundleRef findArtifact(String pGroupId, String pArtifactId,
            String pClassifier) {
        for (BundleRef bundleRef : getImmutableArtifactSet()) {
            if (bundleRef.getGroupId().equals(pGroupId)
                    && bundleRef.getArtifactId().equals(pArtifactId)
                    && bundleRef.getClassifier().equals(pClassifier)) {
                return bundleRef;
            }
        }
        return null;
    }

}
