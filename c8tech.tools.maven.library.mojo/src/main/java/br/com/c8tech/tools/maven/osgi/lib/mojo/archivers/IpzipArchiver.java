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
package br.com.c8tech.tools.maven.osgi.lib.mojo.archivers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import java.util.zip.CRC32;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.zip.ConcurrentJarCreator;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ContentTypeExtraField;

/**
 *
 * @author Cristiano Gavião
 *
 */
@Named(CommonMojoConstants.OSGI_IPZIP_PACKAGING)
@Typed(Archiver.class)
@Singleton
public class IpzipArchiver extends JarArchiver {

    private File agentConfigurationEntry;

    private File referenceEntry;

    private File startBundleEntry;

    public IpzipArchiver() {
        super();
        archiveType = CommonMojoConstants.OSGI_IPZIP_EXTENSION;
        setEncoding("UTF-8");
        setDuplicateBehavior(DUPLICATES_FAIL);
    }

    private void addAgentConfigurationEntry(File pFile,
            ConcurrentJarCreator pZOut) throws IOException {
        ZipArchiveEntry ae = new ZipArchiveEntry(pFile,
                CommonMojoConstants.OSGI_PROVISIONING_AGENT_CONFIG);
        ZipExtraField zef = new ContentTypeExtraField(
                CommonMojoConstants.MIME_BYTE_ARRAY);
        setupZip(pFile, pZOut, ae, zef);
    }

    private void addReferenceEntry(File pFile,
            ConcurrentJarCreator pZOut) throws IOException {
        ZipArchiveEntry ae = new ZipArchiveEntry(pFile,
                CommonMojoConstants.OSGI_PROVISIONING_REFERENCE);
        ZipExtraField zef = new ContentTypeExtraField(
                CommonMojoConstants.MIME_STRING);
        setupZip(pFile, pZOut, ae, zef);
    }

    private void addStartBundleEntry(File pFile,
            ConcurrentJarCreator pZOut) throws IOException {
        ZipArchiveEntry ae = new ZipArchiveEntry(pFile,
                CommonMojoConstants.OSGI_PROVISIONING_START_BUNDLE);
        ZipExtraField zef = new ContentTypeExtraField(
                CommonMojoConstants.MIME_STRING);

        ae.setMethod(ZipArchiveEntry.STORED);
        ae.setSize(pFile.length());
        byte[] content = Files.readAllBytes(pFile.toPath());
        CRC32 crc = new CRC32();
        crc.update(content);
        ae.setCrc(crc.getValue());
        ae.addExtraField(zef);
        pZOut.addArchiveEntry(ae,
                createInputStreamSupplier(new FileInputStream(pFile)), false);
    }


    private void setupZip(File pFile, ConcurrentJarCreator pZOut,
            ZipArchiveEntry pZipArchiveEntry, ZipExtraField pZipExtraField)
                    throws IOException {
        pZipArchiveEntry.setMethod(ZipArchiveEntry.STORED);
        pZipArchiveEntry.setSize(pFile.length());
        byte[] content = Files.readAllBytes(pFile.toPath());
        CRC32 crc = new CRC32();
        crc.update(content);
        pZipArchiveEntry.setCrc(crc.getValue());
        pZipArchiveEntry.addExtraField(pZipExtraField);
        pZOut.addArchiveEntry(pZipArchiveEntry,
                createInputStreamSupplier(new FileInputStream(pFile)), false);
    }

    @Override
    protected void initZipOutputStream(ConcurrentJarCreator pZOut)
            throws IOException {

        super.initZipOutputStream(pZOut);
        if (!skipWriting) {
            if (startBundleEntry != null) {
                addStartBundleEntry(startBundleEntry, pZOut);
            }
            if (referenceEntry != null) {
                addReferenceEntry(referenceEntry, pZOut);
            }
            if (agentConfigurationEntry != null) {
                addAgentConfigurationEntry(agentConfigurationEntry, pZOut);
            }
        }
    }

    /**
     * Makes this instance reset all attributes to their default values and
     * forget all children.
     *
     * @see #cleanUp
     */
    @Override
    public void reset() {
        super.reset();
        setDestFile(null);
        archiveType = CommonMojoConstants.OSGI_IPZIP_EXTENSION;
    }

    public void setAgentConfigurationEntry(File pFile) {
        agentConfigurationEntry = pFile;
    }

    public void setReferenceEntry(File pFile) {
        referenceEntry = pFile;
    }

    public void setStartBundleEntry(File pFile) {
        startBundleEntry = pFile;
    }

}
