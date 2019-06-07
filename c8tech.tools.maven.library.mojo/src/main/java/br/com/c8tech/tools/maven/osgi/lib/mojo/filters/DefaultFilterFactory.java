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
package br.com.c8tech.tools.maven.osgi.lib.mojo.filters;

import java.util.Collection;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;

@Named
@Singleton
public class DefaultFilterFactory implements FilterFactory {


    @Inject
    public DefaultFilterFactory() {
        // required by injection
    }

    @Override
    public ArtifactFilter newFilterAnyOfTheseScopes(Collection<String> pScopes) {
        return new CustomScopeArtifactFilter(pScopes);
    }

    @Override
    public ArtifactFilter newFilterExcludeTheseArtifacts(
            List<String> pExcludedArtifacts) {
        return new ExcludesArtifactFilter(pExcludedArtifacts);
    }

    @Override
    public ArtifactFilter newFilterAllowOptionalArtifacts() {
        return new OptionalArtifactFilter(true);
    }

    @Override
    public ArtifactFilter newFilterDenyOptionalArtifacts() {
        return new OptionalArtifactFilter(false);
    }

}
