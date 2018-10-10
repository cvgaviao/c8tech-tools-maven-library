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
package org.osgi.service.indexer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.FrameworkUtil;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.log.LogService;

public class IndexerUnitTest {

    private static void assertFragmentMatch(RepoIndex indexer,
            String expectedPath, String jarPath) throws Exception {
        StringWriter writer = new StringWriter();
        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                IndexerUnitTest.class.getResource("/").getPath());
        indexer.indexFragment(Collections.singleton(new File(
                IndexerUnitTest.class.getResource("/" + jarPath).getPath())),
                writer, props);

        String expected = Utils
                .readStream(new FileInputStream(IndexerUnitTest.class
                        .getResource("/" + expectedPath).getPath()));
        String actual = writer.toString().trim();
        assertEquals(expected, actual);
    }

    private static void assertFragmentMatch(String expectedPath, String jarPath)
            throws Exception {
        RepoIndex indexer = new RepoIndex(true);
        assertFragmentMatch(indexer, expectedPath, jarPath);
    }

    @Test
    public void testAddAnalyzer() throws Exception {
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new WibbleAnalyzer(), null);
        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/").getPath());
        StringWriter writer = new StringWriter();
        LinkedHashSet<File> files = new LinkedHashSet<File>();
        files.add(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
        files.add(new File(getClass()
                .getResource("/testdata/02-localization.jar").getPath()));

        indexer.indexFragment(files, writer, props);
        String expected = Utils.readStream(new FileInputStream(getClass()
                .getResource("/testdata/fragment-wibble.txt").getPath()));

        assertEquals(expected, writer.toString().trim());
    }

    @Test
    public void testAddAnalyzerWithFilter() throws Exception {
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new WibbleAnalyzer(),
                FrameworkUtil.createFilter("(location=*sion.jar)"));
        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/").getPath());
        StringWriter writer = new StringWriter();
        LinkedHashSet<File> files = new LinkedHashSet<File>();
        files.add(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath()));
        files.add(new File(getClass()
                .getResource("/testdata/02-localization.jar").getPath()));

        indexer.indexFragment(files, writer, props);
        String expected = Utils.readStream(new FileInputStream(
                getClass().getResource("/testdata/fragment-wibble-filtered.txt")
                        .getPath()));

        assertEquals(expected, writer.toString().trim());
    }

    @Test
    public void testBundleOutsideRootDirectory() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(log, true);

        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/testdata/subdir").getPath());

        StringWriter writer = new StringWriter();
        indexer.indexFragment(Collections.singleton(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath())),
                writer, props);

        String expected = Utils.readStream(new FileInputStream(getClass()
                .getResource("/testdata/fragment-subdir-absolute-path.txt")
                .getPath()));
        assertEquals(expected, writer.toString().trim());
    }

    @Test
    public void testEmptyIndex() throws Exception {
        RepoIndex indexer = new RepoIndex(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Set<File> files = Collections.emptySet();

        Map<String, String> config = new HashMap<String, String>();
        config.put(RepoIndex.REPOSITORY_INCREMENT_OVERRIDE, "0");
        config.put(ResourceIndexer.REPOSITORY_NAME, "empty");
        config.put(ResourceIndexer.PRETTY, "true");
        indexer.index(files, out, config);

        String expected = Utils.readStream(new FileInputStream(
                getClass().getResource("/testdata/empty.txt").getPath()));
        assertEquals(expected, out.toString());
    }

    @Test
    public void testFragmentBREE() throws Exception {
        assertFragmentMatch("testdata/fragment-13.txt", "testdata/13-bree.jar");
    }

    @Test
    public void testFragmentBsnVersion() throws Exception {
        assertFragmentMatch("testdata/fragment-01.txt",
                "testdata/01-bsn+version.jar");
    }

    @Test
    public void testFragmentBundleNativeCode() throws Exception {
        assertFragmentMatch("testdata/fragment-19.txt",
                "testdata/19-bundlenativecode.jar");
    }

    @Test
    public void testFragmentBundleNativeCodeOptional() throws Exception {
        assertFragmentMatch("testdata/fragment-20.txt",
                "testdata/20-bundlenativecode-optional.jar");
    }

    @Test
    public void testFragmentExport() throws Exception {
        assertFragmentMatch("testdata/fragment-03.txt",
                "testdata/03-export.jar");
    }

    @Test
    public void testFragmentExportService() throws Exception {
        assertFragmentMatch("testdata/fragment-10.txt",
                "testdata/10-exportservice.jar");
    }

    @Test
    public void testFragmentExportWithUses() throws Exception {
        assertFragmentMatch("testdata/fragment-04.txt",
                "testdata/04-export+uses.jar");
    }

    @Test
    public void testFragmentForbidFragments() throws Exception {
        assertFragmentMatch("testdata/fragment-12.txt",
                "testdata/12-nofragments.jar");
    }

    @Test
    public void testFragmentFragmentHost() throws Exception {
        assertFragmentMatch("testdata/fragment-08.txt",
                "testdata/08-fragmenthost.jar");
    }

    @Test
    public void testFragmentImport() throws Exception {
        assertFragmentMatch("testdata/fragment-05.txt",
                "testdata/05-import.jar");
    }

    @Test
    public void testFragmentImportService() throws Exception {
        assertFragmentMatch("testdata/fragment-11.txt",
                "testdata/11-importservice.jar");
    }

    @Test
    public void testFragmentLocalization() throws Exception {
        assertFragmentMatch("testdata/fragment-02.txt",
                "testdata/02-localization.jar");
    }

    @Test
    public void testFragmentOptionalImport() throws Exception {
        assertFragmentMatch("testdata/fragment-07.txt",
                "testdata/07-optionalimport.jar");
    }

    @Test
    public void testFragmentOptionalRequireBundle() throws Exception {
        assertFragmentMatch("testdata/fragment-16.txt",
                "testdata/16-optionalrequirebundle.jar");
    }

    @Test
    public void testFragmentPlainJar() throws Exception {
        LogService mockLog = Mockito.mock(LogService.class);
        RepoIndex indexer = new RepoIndex(mockLog,true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(mockLog,true),
                FrameworkUtil.createFilter("(name=*)"));

        assertFragmentMatch(indexer, "testdata/fragment-plainjar.txt",
                "testdata/jcip-annotations.jar");
        // Mockito.verifyZeroInteractions(mockLog);
    }

    @Test
    public void testFragmentPlainJarWithVersion() throws Exception {
        assertFragmentMatch("testdata/fragment-plainjar-versioned.txt",
                "testdata/jcip-annotations-2.5.6.wibble.jar");
    }

    @Test
    public void testFragmentProvideRequireCap() throws Exception {
        assertFragmentMatch("testdata/fragment-14.txt",
                "testdata/14-provide-require-cap.jar");
    }

    public void testFragmentRequireBlueprint() throws Exception {
        assertFragmentMatch("testdata/fragment-17.txt",
                "testdata/17-blueprint1.jar");
    }

    @Test
    public void testFragmentRequireBlueprintUsingHeader() throws Exception {
        assertFragmentMatch("testdata/fragment-18.txt",
                "testdata/18-blueprint2.jar");
    }

    @Test
    public void testFragmentRequireBundle() throws Exception {
        assertFragmentMatch("testdata/fragment-06.txt",
                "testdata/06-requirebundle.jar");
    }

    @Test
    public void testFragmentRequireSCR() throws Exception {
        assertFragmentMatch("testdata/fragment-15.txt", "testdata/15-scr.jar");
    }

    @Test
    public void testFragmentRequireSCR1_0() throws Exception {
        assertFragmentMatch("testdata/fragment-scr1_0.txt",
                "testdata/scr1_0.jar");
    }

    @Test
    public void testFragmentRequireSCR1_1() throws Exception {
        assertFragmentMatch("testdata/fragment-scr1_1.txt",
                "testdata/scr1_1.jar");
    }

    @Test
    public void testFragmentRequireSCR1_2() throws Exception {
        assertFragmentMatch("testdata/fragment-scr1_2.txt",
                "testdata/scr1_2.jar");
    }

    @Test
    public void testFragmentSCRServices() throws Exception {
        assertFragmentMatch("testdata/fragment-scr_services.txt",
                "testdata/scr_services.jar");
    }

    @Test
    public void testFragmentSingletonBundle() throws Exception {
        assertFragmentMatch("testdata/fragment-09.txt",
                "testdata/09-singleton.jar");
    }

    @Test
    public void testFullIndex() throws Exception {
        RepoIndex indexer = new RepoIndex(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Set<File> files = new LinkedHashSet<File>();
        files.add(new File(
                getClass().getResource("/testdata/03-export.jar").getPath()));
        files.add(new File(getClass()
                .getResource("/testdata/06-requirebundle.jar").getPath()));

        Map<String, String> config = new HashMap<String, String>();
        config.put(RepoIndex.REPOSITORY_INCREMENT_OVERRIDE, "0");
        config.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/").getPath());
        config.put(ResourceIndexer.REPOSITORY_NAME, "full-c+f");
        indexer.index(files, out, config);

        String unpackedXML = Utils.readStream(new FileInputStream(
                getClass().getResource("/testdata/unpacked.xml").getPath()));
        String expected = unpackedXML.replaceAll("[\\n\\t]*", "");
        assertEquals(expected, Utils.decompress(out.toByteArray()));
    }

    @Test
    public void testFullIndexPrettyCompressedPermutations() throws Exception {
        Boolean pretties[] = { null, Boolean.FALSE, Boolean.TRUE };
        Boolean compressions[] = { null, Boolean.FALSE, Boolean.TRUE };

        boolean outPretties[] = { false, false, false, true, false, false, true,
                true, true };
        boolean outCompressions[] = { true, false, true, false, false, true,
                false, false, true };

        String expectedPretty = Utils.readStream(new FileInputStream(
                getClass().getResource("/testdata/full-03+06.txt").getPath()));
        String expectedNotPretty = Utils.readStream(new FileInputStream(
                getClass().getResource("/testdata/full-03+06-not-pretty.txt")
                        .getPath()));

        RepoIndex indexer = new RepoIndex(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Set<File> files = new LinkedHashSet<File>();
        files.add(new File(
                getClass().getResource("/testdata/03-export.jar").getPath()));
        files.add(new File(getClass()
                .getResource("/testdata/06-requirebundle.jar").getPath()));

        Map<String, String> config = new HashMap<String, String>();
        config.put(ResourceIndexer.ROOT_DIR,
                new File(getClass().getResource("/").getPath())
                        .getAbsoluteFile().toURI().toURL().toString());
        int outIndex = 0;
        for (Boolean pretty : pretties) {
            for (Boolean compression : compressions) {
                config.put(RepoIndex.REPOSITORY_INCREMENT_OVERRIDE, "0");
                config.put(ResourceIndexer.ROOT_DIR,
                        getClass().getResource("/").getPath());
                config.put(ResourceIndexer.REPOSITORY_NAME, "full-c+f");
                if (pretty != null) {
                    config.put(ResourceIndexer.PRETTY,
                            pretty.toString().toLowerCase());
                }
                if (compression != null) {
                    config.put(ResourceIndexer.COMPRESSED,
                            compression.toString().toLowerCase());
                }
                indexer.index(files, out, config);

                String expected = outPretties[outIndex] ? expectedPretty
                        : expectedNotPretty;
                if (!outCompressions[outIndex]) {
                    assertEquals(expected, out.toString(),
                            "pretty/compression = " + pretty + "/"
                                    + compression);
                } else {
                    assertEquals(

                            expected, Utils.decompress(out.toByteArray()),
                            "pretty/compression = " + pretty + "/"
                                    + compression);
                }

                config.clear();
                out.reset();
                outIndex++;
            }
        }
    }

    @Test
    public void testImportServiceOptional() throws Exception {
        assertFragmentMatch("testdata/org.apache.felix.eventadmin-1.3.2.xml",
                "testdata/org.apache.felix.eventadmin-1.3.2.jar");
    }

    @Test
    public void testLogErrorsFromAnalyzer() throws Exception {
        ResourceAnalyzer badAnalyzer = new ResourceAnalyzer() {
            @Override
            public void analyzeResource(Resource resource,
                    List<Capability> capabilities,
                    List<Requirement> requirements) throws AnalyzerException {
                throw new AnalyzerException("Bang!");
            }
        };
        ResourceAnalyzer goodAnalyzer = mock(ResourceAnalyzer.class);

        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(log,true);
        indexer.addAnalyzer(badAnalyzer, null);
        indexer.addAnalyzer(goodAnalyzer, null);

        // Run the indexer
        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR, getClass()
                .getResource("/testdata/02-localization.jar").getPath());
        StringWriter writer = new StringWriter();
        indexer.indexFragment(Collections.singleton(new File(getClass()
                .getResource("/testdata/subdir/01-bsn+version.jar").getPath())),
                writer, props);

        // The "good" analyzer should have been called
        verify(goodAnalyzer).analyzeResource(any(Resource.class), anyList(),
                anyList());

        // The log service should have been notified about the exception
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor
                .forClass(Exception.class);
        verify(log).log(eq(LogService.LOG_ERROR), any(String.class),
                exceptionCaptor.capture());
        assertEquals("Bang!", exceptionCaptor.getValue().getMessage());
    }

    @Test
    public void testMacroExpansion() throws Exception {
        LogService log = mock(LogService.class);
        Properties props = new Properties();
        props.load(new FileInputStream(getClass()
                .getResource("/testdata/known-bundles.properties").getPath()));

        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(props, log,true),
                FrameworkUtil.createFilter("(name=*)"));
        assertFragmentMatch(indexer,
                "testdata/org.apache.felix.eventadmin-1.2.14.xml",
                "testdata/org.apache.felix.eventadmin-1.2.14.jar");
    }

    @Test
    public void testRecogniseAriesBlueprint() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(log,true),
                FrameworkUtil.createFilter("(name=*)"));
        assertFragmentMatch(indexer,
                "testdata/org.apache.aries.blueprint-1.0.0.xml",
                "testdata/org.apache.aries.blueprint-1.0.0.jar");
    }

    @Test
    public void testRecogniseFelixJetty() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(log,true),
                FrameworkUtil.createFilter("(name=*)"));
        assertFragmentMatch(indexer,
                "testdata/org.apache.felix.http.jetty-2.2.0.xml",
                "testdata/org.apache.felix.http.jetty-2.2.0.jar");
    }

    @Test
    public void testRecogniseFelixSCR() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(log,true),
                FrameworkUtil.createFilter("(name=*)"));
        assertFragmentMatch(indexer, "testdata/org.apache.felix.scr-1.6.0.xml",
                "testdata/org.apache.felix.scr-1.6.0.jar");
    }

    @Test
    public void testRecogniseGeminiBlueprint() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(true);
        indexer.addAnalyzer(new KnownBundleAnalyzer(log,true),
                FrameworkUtil.createFilter("(name=*)"));
        assertFragmentMatch(indexer,
                "testdata/gemini-blueprint-extender-1.0.0.RELEASE.xml",
                "testdata/gemini-blueprint-extender-1.0.0.RELEASE.jar");
    }

    @Test
    public void testRemoveDisallowed() throws Exception {
        LogService log = mock(LogService.class);
        RepoIndex indexer = new RepoIndex(log,true);
        indexer.addAnalyzer(new NaughtyAnalyzer(), null);

        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/testdata/").getPath());

        StringWriter writer = new StringWriter();
        indexer.indexFragment(Collections.singleton(new File(getClass()
                .getResource("/testdata/subdir/01-bsn+version.jar").getPath())),
                writer, props);

        verify(log).log(eq(LogService.LOG_ERROR), anyString(),
                isA(UnsupportedOperationException.class));
    }

    @Test
    public void testRootInSubdirectory() throws Exception {
        RepoIndex indexer = new RepoIndex(true);

        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/testdata").getPath());

        StringWriter writer = new StringWriter();
        indexer.indexFragment(Collections.singleton(new File(getClass()
                .getResource("/testdata/01-bsn+version.jar").getPath())),
                writer, props);

        String expected = Utils.readStream(new FileInputStream(getClass()
                .getResource("/testdata/fragment-subdir1.txt").getPath()));
        assertEquals(expected, writer.toString().trim());
    }

    @Test
    public void testRootInSubSubdirectory() throws Exception {
        RepoIndex indexer = new RepoIndex(true);

        Map<String, String> props = new HashMap<String, String>();
        props.put(ResourceIndexer.ROOT_DIR,
                getClass().getResource("/testdata").getPath());

        StringWriter writer = new StringWriter();
        indexer.indexFragment(Collections.singleton(new File(getClass()
                .getResource("/testdata/subdir/01-bsn+version.jar").getPath())),
                writer, props);

        String expected = Utils.readStream(new FileInputStream(getClass()
                .getResource("/testdata/fragment-subdir2.txt").getPath()));
        assertEquals(expected, writer.toString().trim());
    }
}
