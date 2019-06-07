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

import java.util.HashSet;
import java.util.Set;

public class MavenArtifactSets {

    private Set<MavenArtifactSet> mavenArtifactSetCollection = new HashSet<>();

    public void addMavenArtifactSet(MavenArtifactSet pMavenArtifactSet) {
        if (pMavenArtifactSet != null && !pMavenArtifactSet.isEmpty()) {
            mavenArtifactSetCollection.add(pMavenArtifactSet);
        }
    }

    public boolean isEmpty() {
        return mavenArtifactSetCollection.isEmpty();
    }

    public Set<MavenArtifactSet> getMavenArtifactSets() {
        return mavenArtifactSetCollection;
    }

    @Override
    public String toString() {
        return "MavenArtifactSets [" + (mavenArtifactSetCollection != null
                ? "mavenArtifactSetCollection=" + mavenArtifactSetCollection
                : "") + "]";
    }

}
