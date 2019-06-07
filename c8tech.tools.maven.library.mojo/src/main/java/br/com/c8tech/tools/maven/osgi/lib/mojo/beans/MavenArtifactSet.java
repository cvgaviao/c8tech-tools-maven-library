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

public class MavenArtifactSet extends ArtifactSet {

    public MavenArtifactSet() {
        super();
    }

    @Override
    public String toString() {
        return String.format("Maven ArtifactSet {%s}", artifacts);
    }

}
