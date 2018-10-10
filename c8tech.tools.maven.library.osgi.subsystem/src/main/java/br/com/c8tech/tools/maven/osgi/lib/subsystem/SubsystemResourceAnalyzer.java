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
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.aries.subsystem.core.archive.SubsystemManifest;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Namespaces;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.impl.GeneratorState;
import org.osgi.service.indexer.impl.MimeType;
import org.osgi.service.indexer.impl.RepoIndex;
import org.osgi.service.indexer.impl.Util;
import org.osgi.service.indexer.impl.util.Hex;
import org.osgi.service.indexer.impl.util.OSGiHeader;
import org.osgi.service.indexer.impl.util.Yield;

public class SubsystemResourceAnalyzer implements ResourceAnalyzer {

    /**
     * 
     */
    private static final String SHA_256 = "SHA-256";

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

    private static void buildFromHeader(String headerStr,
            Yield<Builder> output) {
        if (headerStr == null)
            return;
        Map<String, Map<String, String>> header = OSGiHeader
                .parseHeader(headerStr);

        for (Entry<String, Map<String, String>> entry : header.entrySet()) {
            String namespace = OSGiHeader.removeDuplicateMarker(entry.getKey());
            Builder builder = new Builder().setNamespace(namespace);

            Map<String, String> attribs = entry.getValue();
            Util.copyAttribsToBuilder(builder, attribs);
            output.yield(builder);
        }
    }

    /**
     * Method borrowed from org.osgi.service.indexer.impl.BundleAnalyzer.
     * 
     * @param resource
     * @return
     * @throws IOException
     */
    private static String calculateLocation(Resource resource)
            throws IOException {
        Path resultPath;
        Path resourcePath = Paths.get(resource.getLocation());
        GeneratorState state = RepoIndex.getStateLocal();
        if (state != null) {
            Path rootPath = state.getRootPath();
            Path artifactCopyDirPath = state.getSubsystemCopyDirPath();
            if (artifactCopyDirPath != null) {
                resourcePath = artifactCopyDirPath.normalize()
                        .resolve(resourcePath.getFileName());
            }
            if (state.isForceAbsolutePath()) {
                resultPath = resourcePath.toAbsolutePath().normalize();
            } else {
                resultPath = rootPath.normalize().relativize(resourcePath);
            }
        } else {
            resultPath = resourcePath.toAbsolutePath().normalize();
        }
        return resultPath.toString();
    }

    /**
     * Method borrowed from org.osgi.service.indexer.impl.BundleAnalyzer.
     * 
     * @param resource
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static String calculateSHA(Resource resource)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA_256);
        byte[] buf = new byte[1024];

        try (InputStream stream = resource.getStream()) {

            while (true) {
                int bytesRead = stream.read(buf, 0, 1024);
                if (bytesRead < 0)
                    break;

                digest.update(buf, 0, bytesRead);
            }
        }

        return Hex.toHexString(digest.digest());
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
        buildFromHeader(capsStr, builder -> caps.add(builder.buildCapability()));
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

        Builder builder = new Builder().setNamespace(Namespaces.NS_CONTENT);

        String sha = calculateSHA(resource);
        builder.addAttribute(Namespaces.NS_CONTENT, sha);

        String location = calculateLocation(resource);
        builder.addAttribute(Namespaces.ATTR_CONTENT_URL, location);

        long size = resource.getSize();
        if (size > 0L)
            builder.addAttribute(Namespaces.ATTR_CONTENT_SIZE, size);

        builder.addAttribute(Namespaces.ATTR_CONTENT_MIME, mimeType.toString());

        capabilities.add(builder.buildCapability());
    }

    private static void doRequirements(SubsystemManifest subsystemManifest,
            final List<? super Requirement> reqs) {
        if (subsystemManifest.getRequireCapabilityHeader() == null) {
            return;
        }
        String reqsStr = subsystemManifest.getRequireCapabilityHeader()
                .getValue();
        buildFromHeader(reqsStr, builder -> reqs.add(builder.buildRequirement()));
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
