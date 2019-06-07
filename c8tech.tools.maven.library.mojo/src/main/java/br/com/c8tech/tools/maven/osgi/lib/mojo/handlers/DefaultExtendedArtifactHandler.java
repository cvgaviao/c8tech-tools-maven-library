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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.handler.ArtifactHandler;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

public class DefaultExtendedArtifactHandler extends AbstractTypeHandler {

    public DefaultExtendedArtifactHandler() {
        super();
        setPackOnTheFlyAllowed(false);
    }

    public DefaultExtendedArtifactHandler(ArtifactHandler pArtifactHandler) {
        super(pArtifactHandler.getPackaging());
        setPackOnTheFlyAllowed(false);
    }

    @Override
    public Path defaultManifestLocation() {
        return Paths.get(CommonMojoConstants.MAVEN_TARGET_CLASSES_FOLDER,
                CommonMojoConstants.JAR_MANIFEST_LOCATION);
    }

    @Override
    protected List<String> getValidTypes() {
        return Arrays.asList();
    }

    @Override
    public boolean isArtifactManifestValid(
            Map<String, String> pJarManifestHeaders) {
        return false;
    }

    @Override
    public boolean isExtensionValid(Path pFilePath) {
        return false;
    }

}
