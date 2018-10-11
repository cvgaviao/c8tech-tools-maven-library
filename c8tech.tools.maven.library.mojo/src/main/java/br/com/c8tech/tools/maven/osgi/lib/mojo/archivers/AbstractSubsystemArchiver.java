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

import static org.codehaus.plexus.archiver.util.Streams.bufferedOutputStream;
import static org.codehaus.plexus.archiver.util.Streams.fileOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.CRC32;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ConcurrentJarCreator;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

/**
 * 
 * @author Cristiano Gavião
 * 
 */
@Named(CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION)
@Typed(Archiver.class)
@Singleton
public class AbstractSubsystemArchiver extends AbstractZipArchiver {

    /**
     * the name of the meta-inf dir
     */
    private static final String OSGI_INF_NAME = "OSGI-INF/";

    /**
     * The manifest file name.
     */
    private static final String MANIFEST_NAME = OSGI_INF_NAME + "SUBSYSTEM.MF";

    private boolean generateEsaMimeEntry = false;

    private File manifestFile;

    public AbstractSubsystemArchiver() {
        super();
        archiveType = CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION;
        setEncoding("UTF-8");
    }

    private void writeManifest(ConcurrentJarCreator zOut, File manifest)
            throws IOException {
        zipDir(null, zOut, OSGI_INF_NAME, DEFAULT_DIR_MODE, getEncoding());

        byte[] data = Files.readAllBytes(manifest.toPath());
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        super.zipFile(createInputStreamSupplier(bais), zOut, MANIFEST_NAME,
                System.currentTimeMillis(), null, DEFAULT_FILE_MODE, null,
                false);
        super.initZipOutputStream(zOut);
    }

    @Override
    protected boolean hasVirtualFiles() {
        getLogger().debug(
                "\n\n\nChecking for subsytem manifest virtual files...\n\n\n");
        System.out.flush(); //NOSONAR

        return (manifestFile != null) || super.hasVirtualFiles();
    }

    @Override
    protected void initZipOutputStream(ConcurrentJarCreator pZOut)
            throws IOException {

        if (!skipWriting) {
            if (generateEsaMimeEntry) {
                byte[] mimetypeBytes = CommonMojoConstants.OSGI_SUBSYSTEM_MIME_TYPE
                        .getBytes(StandardCharsets.UTF_8);
                CRC32 crc = new CRC32();
                crc.update(mimetypeBytes);
                ZipArchiveEntry mimetypeZipEntry = new ZipArchiveEntry(
                        CommonMojoConstants.MIME_TYPE_ENTRY_NAME);
                mimetypeZipEntry.setMethod(ZipArchiveEntry.STORED);
                mimetypeZipEntry.setSize(mimetypeBytes.length);
                mimetypeZipEntry.setCrc(crc.getValue());
                pZOut.addArchiveEntry(mimetypeZipEntry,
                        createInputStreamSupplier(
                                new ByteArrayInputStream(mimetypeBytes)),
                        false);

            }
            if (manifestFile == null) {
                throw new ArchiverException(
                        "Subsystem Manifest file does not exist.");
            }
            writeManifest(pZOut, manifestFile);
        }
    }

    public void setManifest(File manifestFile) {
        if (!manifestFile.exists()) {
            throw new ArchiverException("Subsystem Manifest file: "
                    + manifestFile + " does not exist.");
        }

        this.manifestFile = manifestFile;
    }

    @Override
    protected boolean createEmptyZip(File zipFile) {

        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(
                bufferedOutputStream(fileOutputStream(getDestFile(),
                        CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION)));) {
            getLogger().debug("Building MANIFEST-only subsystem archive: "
                    + getDestFile().getAbsolutePath());

            zipArchiveOutputStream.setEncoding(getEncoding());
            if (isCompress()) {
                zipArchiveOutputStream
                        .setMethod(ZipArchiveOutputStream.DEFLATED);
            } else {
                zipArchiveOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            }
            ConcurrentJarCreator ps = new ConcurrentJarCreator(
                    Runtime.getRuntime().availableProcessors());
            initZipOutputStream(ps);
            finalizeZipOutputStream(ps);
        } catch (IOException ioe) {
            throw new ArchiverException(
                    "Could not create almost empty Zip archive ("
                            + ioe.getMessage() + ")",
                    ioe);
        } finally {
            getLogger().debug("Finished writing file.");
        }
        return true;
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
        archiveType = CommonMojoConstants.OSGI_SUBSYSTEM_EXTENSION;

        manifestFile = null;
    }

    public void setGenerateEsaMimeEntry(boolean skipMimeEntry) {
        this.generateEsaMimeEntry = skipMimeEntry;

    }
}
