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
package br.com.c8tech.tools.maven.osgi.lib.subsystem;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.aries.subsystem.core.archive.SubsystemManifest;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Namespaces;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.impl.BundleAnalyzer;
import org.osgi.service.indexer.impl.MimeType;

public class SubsystemResourceAnalyzer implements ResourceAnalyzer {


    @Override
    public void analyzeResource(Resource resource,
            List<Capability> capabilities, List<Requirement> requirements)
                    throws AnalyzerException {
        MimeType mimeType = MimeType.SUBSYSTEM;
        try {
            Resource io = resource.getChild("OSGI-INF/SUBSYSTEM.MF");
            if (io == null) {
                return;
            }
            SubsystemManifest subsystemManifest = new SubsystemManifest(
                    io.getStream());

            doSubsystemIdentity(subsystemManifest, capabilities);
            doContent(resource, mimeType, capabilities);
            doCapabilities(subsystemManifest, capabilities);
            doRequirements(subsystemManifest, requirements);

        } catch (Exception e) {
            throw new AnalyzerException(
                    "An error occurred while analyzing a subsystem archive.",
                    e);
        }
    }





    /**
     * Method borrowed from org.osgi.service.indexer.impl.BundleAnalyzer.
     * 
     * @param subsystemManifest
     * @param caps
     * @throws Exception
     */
    private static void doCapabilities(SubsystemManifest subsystemManifest,
            final List<? super Capability> caps) {
        if (subsystemManifest.getProvideCapabilityHeader() == null) {
            return;
        }
        String capsStr = subsystemManifest.getProvideCapabilityHeader()
                .getValue();
        BundleAnalyzer.buildFromHeader(capsStr, builder -> caps.add(builder.buildCapability()));
    }

    /**
     * Method borrowed from org.osgi.service.indexer.impl.BundleAnalyzer.
     * 
     * @param resource
     * @param mimeType
     * @param capabilities
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws Exception
     */
    private static void doContent(Resource resource, MimeType mimeType,
            List<? super Capability> capabilities)
                    throws IOException, NoSuchAlgorithmException {
        
        BundleAnalyzer.doContent(resource, mimeType, capabilities);

    }

    private static void doRequirements(SubsystemManifest subsystemManifest,
            final List<? super Requirement> reqs) {
        if (subsystemManifest.getRequireCapabilityHeader() == null) {
            return;
        }
        String reqsStr = subsystemManifest.getRequireCapabilityHeader()
                .getValue();
        BundleAnalyzer.buildFromHeader(reqsStr, builder -> reqs.add(builder.buildRequirement()));
    }

    private static void doSubsystemIdentity(SubsystemManifest subsystemManifest,
            List<? super Capability> caps) {
        Builder builder = new Builder().setNamespace(Namespaces.NS_IDENTITY)
                .addAttribute(Namespaces.NS_IDENTITY,
                        subsystemManifest.getSubsystemSymbolicNameHeader()
                                .getSymbolicName())
                .addAttribute(Namespaces.ATTR_IDENTITY_TYPE,
                        subsystemManifest.getSubsystemTypeHeader().getType())
                .addAttribute(Namespaces.ATTR_VERSION, subsystemManifest
                        .getSubsystemVersionHeader().getVersion());
        caps.add(builder.buildCapability());

    }

}
