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

public class P2ArtifactSets {

    private Set<P2ArtifactSet> p2ArtifactSetCollection = new HashSet<>();

    public void addP2ArtifactSet(
            P2ArtifactSet pP2ArtifactConfigSet) {
        p2ArtifactSetCollection.add(pP2ArtifactConfigSet);
    }

    public Set<P2ArtifactSet> getP2ArtifactSets() {
        return p2ArtifactSetCollection;
    }

    @Override
    public String toString() {
        return "P2ArtifactSets ["
                + (p2ArtifactSetCollection != null
                        ? "p2ArtifactSetCollection=" + p2ArtifactSetCollection : "")
                + "]";
    }

}
