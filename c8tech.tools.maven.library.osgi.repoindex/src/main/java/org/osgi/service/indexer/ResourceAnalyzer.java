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
import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * <p>
 * A resource analyzer is responsible for analyzing a resource for some specific
 * purpose, and discovering requirements and capabilities that may not be
 * discovered by the basic BUNDLE Analyzer.
 * </p>
 * 
 * <p>
 * Clients may implement this interface and register instances as services.
 * </p>
 * 
 * @author Neil Bartlett &lt;njbartlett@gmail.com&gt;
 */
@ConsumerType
public interface ResourceAnalyzer {
    /**
     * The service property used to declare a resource filter, so that the
     * analyzer is only invoked on a subset of resources. Example:
     * {@code (&(|(name=foo.jar)(name=*.ear))(lastmodified>=1262217600753))}
     */
    static final String FILTER = "filter";

    /**
     * <p>
     * This method is invoked for each resource that the analyzer is requested
     * to analyze. Implementations add zero or more capabilities and/or
     * requirements to the supplied lists.
     * </p>
     * 
     * <p>
     * Analyzers <b>may</b> examine the lists of already-discovered requirements
     * and capabilities; for example they may wish to add a certain capability
     * if (and only if) it has not already been added.
     * </p>
     * 
     * <p>
     * However, analyzers <b>should not</b> rely on being invoked in any
     * particular order, i.e. either before or after any other analyzer.
     * </p>
     * 
     * <p>
     * Analyzers <b>MUST NOT</b> attempt to remove or replace any capability or
     * requirement from the supplied list. Clients of this method may enforce
     * this by passing List implementations that throw
     * {@link UnsupportedOperationException} upon any attempt to call
     * {@link List#remove(int)}, etc.
     * </p>
     * 
     * 
     * @param resource
     *            The current resource.
     * @param capabilities
     *            The list of capabilities.
     * @param requirements
     *            The list of requirements.
     * @throws AnalyzerException
     *             If something goes wrong. The error will be logged to the OSGi
     *             Log Service (if available) and the next ResourceAnalyzer (if
     *             any) will be asked to analyze the resource.
     */
    void analyzeResource(Resource resource, List<Capability> capabilities,
            List<Requirement> requirements) throws AnalyzerException;
}
