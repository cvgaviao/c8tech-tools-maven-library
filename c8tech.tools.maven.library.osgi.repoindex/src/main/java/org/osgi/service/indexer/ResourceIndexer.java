/**
 * ==========================================================================
 * Copyright © 2015-2018 OSGi Alliance, Cristiano Gavião.
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
package org.osgi.service.indexer;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex)
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * ResourceIndexer is an OSGi service that creates a Repository XML
 * representation by indexing resource capabilities and requirements.
 */
@ProviderType
public interface ResourceIndexer {

    class IndexResult {
        private List<Capability> capabilities = new ArrayList<>();
        private List<Requirement> requirements = new ArrayList<>();
        private Resource resource;

        /**
         * A unique signature for this indexer. It should be some kind of hash
         * that changes when the set of analyzers changes, or the results of
         * this parse are no longer compatible with other parse results. The
         * intention of this method is to allow caching of results and
         * invalidate the cache when the version has changed.
         */
        private long signature;

        public List<Capability> getCapabilities() {
            return capabilities;
        }

        public List<Requirement> getRequirements() {
            return requirements;
        }

        public Resource getResource() {
            return resource;
        }

        public long getSignature() {
            return signature;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public void setSignature(long signature) {
            this.signature = signature;
        }
    }

    /**
     * Index a set of input files and write the Repository XML representation to
     * the stream
     *
     * @param files
     *            a set of input files
     * @param out
     *            the stream to write the XML representation to
     * @param config
     *            a set of optional parameters (use the interface constants as
     *            keys)
     * @throws AnalyzerException
     *             in case of an error
     */
    void index(Set<File> files, OutputStream out, Map<String, String> config)
            throws AnalyzerException;

    /**
     * Return a Resource from a file
     *
     * @param file
     *            a bundle to index
     * @throws AnalyzerException
     *             in case of an error
     * @return The resource, caps, and reqs for that file
     */
    IndexResult indexFile(File file) throws AnalyzerException;

    /**
     * <p>
     * Index a set of input files and write a Repository XML fragment to the
     * given writer.
     * </p>
     * <p>
     * Note that the result will be one or more XML <code>resource</code>
     * elements <em>without</em> a top-level surrounding <code>repository</code>
     * element. The resulting XML is therefore not well-formed.
     * </p>
     * <p>
     * This method may be useful for repository managers that wish to (re-)index
     * individual resources and assemble the XML fragments into a complete
     * repository document later.
     * </p>
     *
     * @param files
     *            a set of input files
     * @param out
     *            the writer to write the Repository XML representation to
     * @param config
     *            a set of optional parameter (use the interface constants as
     *            keys)
     * @throws AnalyzerException
     *             in case of an error
     */
    void indexFragment(Set<File> files, Writer out, Map<String, String> config)
            throws AnalyzerException;

    void setKnownBundlesExtraProperties(Properties props);
}
