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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public class ValidTypeArtifactFilter implements ArtifactFilter {

    private final Set<String> validArtifactTypes = new TreeSet<>();

    public ValidTypeArtifactFilter() {
        super();
    }

    public void addItem(String pFilterItem) {
        validArtifactTypes.add(pFilterItem);
    }

    public void addItems(Collection<String> pFilterItems) {
        validArtifactTypes.addAll(pFilterItems);
    }

    @Override
    public boolean include(Artifact pArtifact) {
        if (pArtifact == null || pArtifact.getType() == null) {
            return false;
        }
        String type = pArtifact.getType();
        Optional<String> result = validArtifactTypes.stream()
                .filter(t -> t.trim().equals(type.trim())).findFirst();
        return result.isPresent();
    }

}
