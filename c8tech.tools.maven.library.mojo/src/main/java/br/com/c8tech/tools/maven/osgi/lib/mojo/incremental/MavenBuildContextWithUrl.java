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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.maven.execution.scope.MojoExecutionScoped;

import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal.DefaultBuildContextWithUrl;
import io.takari.incrementalbuild.Resource;
import io.takari.incrementalbuild.ResourceMetadata;
import io.takari.incrementalbuild.spi.BuildContextEnvironment;

@Named
public class MavenBuildContextWithUrl implements BuildContextWithUrl {

    @Named
    @Typed(MojoExecutionScopedBuildContextForURL.class)
    @MojoExecutionScoped
    public static class MojoExecutionScopedBuildContextForURL
            extends DefaultBuildContextWithUrl {
        @Inject
        public MojoExecutionScopedBuildContextForURL(
                BuildContextEnvironment configuration) {
            super(configuration);
        }
    }

    private final Provider<MojoExecutionScopedBuildContextForURL> provider;

    @Inject
    public MavenBuildContextWithUrl(
            Provider<MojoExecutionScopedBuildContextForURL> delegate) {
        this.provider = delegate;
    }

    @Override
    public void markSkipExecution() {
        provider.get().markSkipExecution();
    }

    @Override
    public Iterable<? extends Resource<File>> registerAndProcessInputs(
            File basedir, Collection<String> includes,
            Collection<String> excludes) throws IOException {
        return provider.get().registerAndProcessInputs(basedir, includes,
                excludes);
    }

    @Override
    public ResourceMetadata<File> registerInput(File inputFile) {
        return provider.get().registerInput(inputFile);
    }

    @Override
    public ResourceMetadata<URL> registerInput(URL pInputURL)
            throws IOException {
        return provider.get().registerInput(pInputURL);
    }

    @Override
    public ResourceMetadata<URL> registerInput(URL inputFile, Path pCacheDir)
            throws IOException {
        return provider.get().registerInput(inputFile, pCacheDir);
    }

    @Override
    public Iterable<? extends ResourceMetadata<File>> registerInputs(
            File basedir, Collection<String> includes,
            Collection<String> excludes) throws IOException {
        try {
            return provider.get().registerInputs(basedir, includes, excludes);
        } catch (IOException e) {
            throw new IOException(
                    "Failure while registering inputs on the building context.",
                    e);
        }
    }
}
