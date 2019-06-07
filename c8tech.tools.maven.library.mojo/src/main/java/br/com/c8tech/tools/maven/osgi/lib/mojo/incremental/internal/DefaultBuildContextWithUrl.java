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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.BuildContextWithUrl;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DependenciesHelper;
import io.takari.incrementalbuild.ResourceMetadata;
import io.takari.incrementalbuild.spi.AbstractBuildContext;
import io.takari.incrementalbuild.spi.BuildContextEnvironment;
import io.takari.incrementalbuild.spi.BuildContextFinalizer;
import io.takari.incrementalbuild.spi.DefaultOutput;
import io.takari.incrementalbuild.spi.DefaultResource;
import io.takari.incrementalbuild.spi.DefaultResourceMetadata;
import io.takari.incrementalbuild.workspace.Workspace;

public class DefaultBuildContextWithUrl extends AbstractBuildContext
        implements BuildContextWithUrl {

    @Inject
    private DependenciesHelper dependenciesHelper;

    public DefaultBuildContextWithUrl(BuildContextEnvironment configuration) {
        super(configuration);
    }

    public DefaultBuildContextWithUrl(Workspace workspace, File stateFile,
            Map<String, Serializable> configuration,
            BuildContextFinalizer finalizer) {
        super(workspace, stateFile, configuration, finalizer);
    }

    @Override
    public Collection<DefaultResource<File>> registerAndProcessInputs( // NOSONAR
            File basedir, Collection<String> includes,
            Collection<String> excludes) throws IOException {
        return super.registerAndProcessInputs(basedir, includes, excludes);
    }

    @Override
    protected void finalizeContext() throws IOException { // NOSONAR

        // only supports simple input --> output associations
        // outputs are carried over iff their input is carried over

        // TODO harden the implementation // NOSONAR
        //
        // things can get tricky even with such simple model. consider the
        // following
        // build-1: inputA --> outputA
        // build-2: inputA unchanged. inputB --> outputA
        // now outputA has multiple inputs, which is not supported by this
        // context
        //
        // another tricky example
        // build-1: inputA --> outputA
        // build-2: inputA unchanged before the build, inputB --> inputA
        // now inputA is both input and output, which is not supported by this
        // context

        // multi-pass implementation
        // pass 1, carry-over up-to-date inputs and collect all up-to-date
        // outputs
        // pass 2, carry-over all up-to-date outputs
        // pass 3, remove obsolete and orphaned outputs

        Set<File> uptodateOldOutputs = new HashSet<>();
        for (Object resource : oldState.getResources().keySet()) { // NOSONAR
            if (oldState.isOutput(resource)) {
                continue;
            }

            if (isProcessedResource(resource) || isDeletedResource(resource)
                    || !isRegisteredResource(resource)) {
                // deleted or processed resource, nothing to carry over
                continue;
            }

            if (state.isOutput(resource)) {
                // resource flipped from input to output without going through
                // delete
                throw new IllegalStateException(
                        "Inconsistent resource type change " + resource);
            }

            // carry over

            state.putResource(resource, oldState.getResource(resource));
            state.setResourceMessages(resource,
                    oldState.getResourceMessages(resource));
            state.setResourceAttributes(resource,
                    oldState.getResourceAttributes(resource));

            Collection<File> oldOutputs = oldState.getResourceOutputs(resource);
            state.setResourceOutputs(resource, oldOutputs);
            if (oldOutputs != null) {
                uptodateOldOutputs.addAll(oldOutputs);
            }
        }

        for (File output : uptodateOldOutputs) {
            if (state.isResource(output)) {
                // can't carry-over registered resources
                throw new IllegalStateException();
            }

            state.putResource(output, oldState.getResource(output));
            state.addOutput(output);
            state.setResourceMessages(output,
                    oldState.getResourceMessages(output));
            state.setResourceAttributes(output,
                    oldState.getResourceAttributes(output));
        }

        for (File output : oldState.getOutputs()) {
            if (!state.isOutput(output)) {
                deleteOutput(output);
            }
        }
    }

    @Override
    public void markSkipExecution() { // NOSONAR
        super.markSkipExecution();
    }

    @Override
    public DefaultResourceMetadata<File> registerInput(File inputFile) { // NOSONAR
        return super.registerInput(inputFile);
    }

    @Override
    public Collection<DefaultResourceMetadata<File>> registerInputs( // NOSONAR
            File basedir, Collection<String> includes,
            Collection<String> excludes) throws IOException {
        return super.registerInputs(basedir, includes, excludes);
    }

    @Override
    public ResourceMetadata<URL> registerInput(URL inputURL)
            throws IOException {
        return registerInput(inputURL, null);
    }

    @Override
    protected void assertAssociation(DefaultResource<?> resource,
            DefaultOutput output) {
        Object input = resource.getResource();
        File outputFile = output.getResource();

        // input --> output --> output2 is not supported (until somebody
        // provides a usecase)
        if (state.isOutput(input)) {
            throw new UnsupportedOperationException();
        }

        // each output can only be associated with a single input
        Collection<Object> inputs = state.getOutputInputs(outputFile);
        if (inputs != null && !inputs.isEmpty()
                && !DefaultBuildContextWithUrl.containsOnly(inputs, input)) {
            throw new UnsupportedOperationException();
        }
    }

    private static boolean containsOnly(Collection<Object> collection,
            Object element) { 
        for (Object other : collection) {
            if (!element.equals(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceMetadata<URL> registerInput(URL inputURL, Path cacheDir)
            throws IOException {
        if (inputURL == null) {
            throw new IllegalArgumentException();
        }
        return registerNormalizedInput(inputURL, cacheDir);
    }

    protected DefaultResourceMetadata<URL> registerNormalizedInput(
            URL pResourceURL, Path pCacheDir) throws IOException {
        assertOpen();

        if (!state.isResource(
                dependenciesHelper.whichSourceToUse(pResourceURL, pCacheDir))) {
            registerInput(dependenciesHelper
                    .newResourceStateHolder(pResourceURL, pCacheDir));
        }
        return new DefaultURLResourceMetadata(this, oldState, pResourceURL);
    }
}
