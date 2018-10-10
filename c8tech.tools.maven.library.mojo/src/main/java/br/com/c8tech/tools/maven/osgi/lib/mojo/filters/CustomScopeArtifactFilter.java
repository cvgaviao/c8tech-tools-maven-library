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

import java.util.ArrayList;
import java.util.Collection;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.components.DefaultDependenciesHelper;

public class CustomScopeArtifactFilter implements ArtifactFilter {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultDependenciesHelper.class);

    private final List<String> filters = new ArrayList<>();

    public CustomScopeArtifactFilter(Collection<String> scopes) {
        this.filters.addAll(scopes);
    }

    @Override
    public boolean include(Artifact pArtifact) {

        if (filters.isEmpty()){
            return true;
        }
        if (!filters.contains(pArtifact.getScope())){
            LOG.debug("Scope filter failed for {}", pArtifact.getArtifactId());            
            return false;
        }
        return true;
    }
}
