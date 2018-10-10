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
package br.com.c8tech.tools.maven.osgi.lib.mojo.filters;

import javax.inject.Named;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named()
public class OptionalArtifactFilter implements ArtifactFilter {

    private static final Logger LOG = LoggerFactory
            .getLogger(OptionalArtifactFilter.class);

    final boolean acceptOptional;

    protected OptionalArtifactFilter(boolean pDenyIIfTrue) {
        this.acceptOptional = pDenyIIfTrue;
    }

    @Override
    public boolean include(Artifact artifact) {
        if (!this.acceptOptional && artifact.isOptional()) {
            LOG.debug("Optional filter didn't pass.");
            return false;
        }
        return true;
    }

}
