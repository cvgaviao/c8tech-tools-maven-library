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
package br.com.c8tech.tools.maven.osgi.lib.mojo.handlers;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.ArtifactHandler;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

@Named(CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_COMPOSITE)
@Singleton
@Typed(value = { ArtifactHandler.class, ExtendedArtifactHandler.class })
public class SubsystemCompositeArtifactHandler
        extends AbstractSubsystemArtifactHandler {

    @Inject
    public SubsystemCompositeArtifactHandler() {
        super(CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_COMPOSITE);
        setIncludesDependencies(false);
        setExtension(CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION);
        setLanguage(CommonMojoConstants.LANGUAGE_JAVA);
        setAddedToClasspath(true);
    }

}
