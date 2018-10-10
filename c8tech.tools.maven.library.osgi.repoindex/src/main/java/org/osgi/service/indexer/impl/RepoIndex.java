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

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex)
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import static org.osgi.framework.FrameworkUtil.createFilter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.indexer.impl.types.TypedAttribute;
import org.osgi.service.indexer.impl.util.AddOnlyList;
import org.osgi.service.indexer.impl.util.Indent;
import org.osgi.service.indexer.impl.util.Pair;
import org.osgi.service.indexer.impl.util.Tag;
import org.osgi.service.log.LogService;

/**
 * The repository indexer. See OSGi Enterprise Specification 5.0.0, chapter 132.
 */
public class RepoIndex implements ResourceIndexer {

    /**
     * Name of the configuration variable for the increment (if not set then
     * System.currentTimeMillis() is used)
     */
    public static final String REPOSITORY_INCREMENT_OVERRIDE = "-repository.increment.override";

    private static final ThreadLocal<GeneratorState> state = new ThreadLocal<>();

    /**
     * the list of analyzer/filter pairs. The filter determines which resources
     * can be analyzed
     */
    private final List<Pair<ResourceAnalyzer, Filter>> analyzers = new LinkedList<>();

    /** the BluePrint analyzer */
    private final BlueprintAnalyzer blueprintAnalyzer;

    /** the generic bundle analyzer */
    private final BundleAnalyzer bundleAnalyzer;

    /** the OSGi Framework analyzer */
    private final OSGiFrameworkAnalyzer frameworkAnalyzer;

    /** the know bundles analyzer */
//    private final KnownBundleAnalyzer knownBundleAnalyzer;

    /** the logger */
    private final LogService log;

    /** the Declarative Services analyzer */
    private final SCRAnalyzer scrAnalyzer;
    
    private final boolean verbose;

    /**
     * Construct a default instance that uses a console logger.
     */
    public RepoIndex(boolean pVerbose) {
        this(new ConsoleLogSvc(), pVerbose);
    }

    /**
     * Constructor
     *
     * @param log
     *            the log service to use
     */
    public RepoIndex(LogService log, boolean pVerbose) {
        this.log = log;
        this.verbose = pVerbose;
        this.bundleAnalyzer = new BundleAnalyzer(log, pVerbose);
        this.frameworkAnalyzer = new OSGiFrameworkAnalyzer(log, pVerbose);
        this.scrAnalyzer = new SCRAnalyzer(log, pVerbose);
        this.blueprintAnalyzer = new BlueprintAnalyzer(log, pVerbose);
//        this.knownBundleAnalyzer = new KnownBundleAnalyzer(log);

        try {
            Filter allFilter = createFilter("(name=*.jar)");

            addAnalyzer(bundleAnalyzer, allFilter);
            addAnalyzer(frameworkAnalyzer, allFilter);
            addAnalyzer(scrAnalyzer, allFilter);
            addAnalyzer(blueprintAnalyzer, allFilter);
//            addAnalyzer(knownBundleAnalyzer, allFilter);
        } catch (InvalidSyntaxException e) {
            log.log(LogService.LOG_ERROR, "", e);
            throw new ExceptionInInitializerError(
                    "Unexpected internal error compiling filter");
        }
    }

    private static void appendAttributeAndDirectiveTags(Tag parentTag,
            Map<String, Object> attribs, Map<String, String> directives) {
        for (Entry<String, Object> attribEntry : attribs.entrySet()) {
            Tag attribTag = new Tag(Schema.ELEM_ATTRIBUTE);
            attribTag.addAttribute(Schema.ATTR_NAME, attribEntry.getKey());

            TypedAttribute typedAttrib = TypedAttribute
                    .create(attribEntry.getKey(), attribEntry.getValue());
            parentTag.addContent(typedAttrib.toXML());
        }

        for (Entry<String, String> directiveEntry : directives.entrySet()) {
            Tag directiveTag = new Tag(Schema.ELEM_DIRECTIVE);
            directiveTag.addAttribute(Schema.ATTR_NAME,
                    directiveEntry.getKey());
            directiveTag.addAttribute(Schema.ATTR_VALUE,
                    directiveEntry.getValue());
            parentTag.addContent(directiveTag);
        }
    }

    public static GeneratorState getStateLocal() {
        return state.get();
    }

    /**
     * @param analyzer
     *            the analyzer to add
     * @param filter
     *            the filter that determines which resources can be analyzed
     */
    public final void addAnalyzer(ResourceAnalyzer analyzer, Filter filter) {
        synchronized (analyzers) {
            analyzers.add(Pair.create(analyzer, filter));
        }
    }

    private Tag generateResource(File file, Map<String, String> config)
            throws IOException {

        JarResource resource = new JarResource(file);
        List<Capability> caps = new AddOnlyList<>();
        List<Requirement> reqs = new AddOnlyList<>();

        try {
            // Read config settings and save in thread local state
            if (config != null) {
                Path rootPath = null;
                Path bundlesCopyPath = null;
                Path subsystemsCopyPath = null;
                Boolean forceAbsolutePath;
                String rootPathStr = config.get(ResourceIndexer.ROOT_DIR);
                if (rootPathStr == null) {
                    rootPathStr = System.getProperty("user.dir");
                }
                if (rootPathStr.startsWith("file:")) {
                    rootPathStr = rootPathStr.substring(5);
                }
                File rootDir = new File(rootPathStr);
                if (rootDir.isDirectory())
                    rootPath = rootDir.toPath();
                else
                    rootPath = rootDir.getParentFile().toPath();

                String bundlesCopyPathStr = config
                        .get(ResourceIndexer.BUNDLES_COPY_DIR);
                if (bundlesCopyPathStr != null) {
                    bundlesCopyPath = Paths.get(bundlesCopyPathStr);
                }
                String subsystemsCopyPathStr = config
                        .get(ResourceIndexer.SUBSYSTEMS_COPY_DIR);
                if (subsystemsCopyPathStr != null) {
                    subsystemsCopyPath = Paths.get(subsystemsCopyPathStr);
                }
                forceAbsolutePath = Boolean.valueOf(
                        config.get(ResourceIndexer.FORCE_ABSOLUTE_PATH));
                String urlTemplate = config.get(ResourceIndexer.URL_TEMPLATE);
                setStateLocal(new GeneratorState(rootPath, bundlesCopyPath,
                        subsystemsCopyPath, urlTemplate, forceAbsolutePath));
            } else {
                setStateLocal(null);
            }

            // Iterate over the analyzers
            try {
                synchronized (analyzers) {
                    for (Pair<ResourceAnalyzer, Filter> entry : analyzers) {
                        ResourceAnalyzer analyzer = entry.getFirst();
                        Filter filter = entry.getSecond();

                        if (filter == null
                                || filter.match(resource.getProperties())) {
                            try {
                                analyzer.analyzeResource(resource, caps, reqs);
                            } catch (Exception e) {
                                log(LogService.LOG_ERROR,
                                        MessageFormat.format(
                                                "Error calling analyzer \"{0}\" on resource {1}.",
                                                analyzer.getClass().getName(),
                                                resource.getLocation()),
                                        e);
                            }
                        }
                    }
                }
            } finally {
                setStateLocal(null);
            }
        } finally

        {
            resource.close();
        }

        Tag resourceTag = new Tag(Schema.ELEM_RESOURCE);
        for (Capability cap : caps)

        {
            Tag capTag = new Tag(Schema.ELEM_CAPABILITY);
            capTag.addAttribute(Schema.ATTR_NAMESPACE, cap.getNamespace());

            appendAttributeAndDirectiveTags(capTag, cap.getAttributes(),
                    cap.getDirectives());

            resourceTag.addContent(capTag);
        }

        for (

        Requirement req : reqs)

        {
            Tag reqTag = new Tag(Schema.ELEM_REQUIREMENT);
            reqTag.addAttribute(Schema.ATTR_NAMESPACE, req.getNamespace());

            appendAttributeAndDirectiveTags(reqTag, req.getAttributes(),
                    req.getDirectives());

            resourceTag.addContent(reqTag);
        }

        return resourceTag;

    }

    /**
     * Get the current analyzers
     * 
     * @return A list of registered analyzers.
     */

    public List<ResourceAnalyzer> getAnalyzers() {
        List<ResourceAnalyzer> list = new ArrayList<>();
        for (Pair<ResourceAnalyzer, Filter> entry : analyzers) {
            list.add(entry.getFirst());
        }
        return list;

    }

    private long getSignature() {
        long value = 97;
        for (Pair<ResourceAnalyzer, Filter> ra : analyzers) {
            value *= 997 * ra.getFirst().getClass().getName().hashCode() + 13;
        }
        return value;
    }

    /*
     * See ResourceIndexer interface
     */
    @Override
    public void index(final Set<File> files, final OutputStream out,
            final Map<String, String> configMap) throws AnalyzerException {
        Map<String, String> config;
        if (configMap == null) {
            config = new HashMap<>(0);
        } else {
            config = new HashMap<>(configMap);
        }

        Set<File> filesToIndex = new TreeSet<>();
        if (files != null && !files.isEmpty()) {
            resolveDirectories(files, filesToIndex);
        }

        Indent indent;
        PrintWriter pw = null;
        try {
            String prettySetting = config.get(ResourceIndexer.PRETTY);
            String compressedSetting = config.get(ResourceIndexer.COMPRESSED);
            /**
             * <pre>
             * pretty   compressed         out-pretty     out-compressed
             *   null         null        Indent.NONE               true*
             *   null        false        Indent.NONE              false
             *   null         true        Indent.NONE               true
             *  false         null      Indent.PRETTY              false*
             *  false        false        Indent.NONE              false
             *  false         true        Indent.NONE               true
             *   true         null      Indent.PRETTY              false*
             *   true        false      Indent.PRETTY              false
             *   true         true      Indent.PRETTY               true
             *
             *   * = original behaviour, before compressed was introduced
             * </pre>
             */
            indent = (prettySetting == null
                    || (!Boolean.parseBoolean(prettySetting)
                            && compressedSetting != null)) ? Indent.NONE
                                    : Indent.PRETTY;
            boolean compressed = (prettySetting == null
                    && compressedSetting == null)
                    || Boolean.parseBoolean(compressedSetting);
            try {
                if (!compressed) {
                    pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
                } else {
                    pw = new PrintWriter(new GZIPOutputStream(out));
                }
            } catch (IOException e) {
                throw new AnalyzerException("", e);
            }

            pw.print(Schema.XML_PROCESSING_INSTRUCTION);
            Tag repoTag = new Tag(Schema.ELEM_REPOSITORY);

            String repoName = config.get(REPOSITORY_NAME);
            if (repoName == null)
                repoName = REPOSITORYNAME_DEFAULT;
            repoTag.addAttribute(Schema.ATTR_NAME, repoName);

            String increment = config.get(REPOSITORY_INCREMENT_OVERRIDE);
            if (increment == null)
                increment = Long.toString(System.currentTimeMillis());
            repoTag.addAttribute(Schema.ATTR_INCREMENT, increment);

            repoTag.addAttribute(Schema.ATTR_XML_NAMESPACE, Schema.NAMESPACE);

            repoTag.printOpen(indent, pw, false);
            for (File file : filesToIndex) {
                try {
                    Tag resourceTag = generateResource(file, config);
                    resourceTag.print(indent.next(), pw);
                } catch (Exception e) {
                    log(LogService.LOG_WARNING,
                            MessageFormat.format(
                                    "Could not index {0}, skipped ({1}).", file,
                                    e.getMessage()),
                            e);
                }
            }
            repoTag.printClose(indent, pw);
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

    @Override
    public IndexResult indexFile(File file) throws AnalyzerException {
        IndexResult result = new IndexResult();
        try {
            result.setResource(new JarResource(file));
        } catch (IOException e) {
            throw new AnalyzerException("", e);
        }
        result.setSignature(getSignature());
        synchronized (analyzers) {
            for (Pair<ResourceAnalyzer, Filter> entry : analyzers) {
                ResourceAnalyzer analyzer = entry.getFirst();
                Filter filter = entry.getSecond();

                if (filter != null
                        && filter.match(result.getResource().getProperties())) {
                    analyzer.analyzeResource(result.getResource(),
                            result.getCapabilities(), result.getRequirements());
                }
            }
        }
        return result;
    }

    @Override
    public void indexFragment(Set<File> files, Writer out,
            Map<String, String> config) throws AnalyzerException {
        PrintWriter pw;
        if (out instanceof PrintWriter)
            pw = (PrintWriter) out;
        else
            pw = new PrintWriter(out);

        for (File file : files) {
            try {
                Tag resourceTag = generateResource(file, config);
                resourceTag.print(Indent.PRETTY, pw);
            } catch (Exception e) {
                log(LogService.LOG_WARNING,
                        MessageFormat.format(
                                "Could not index {0}, skipped ({1}).", file,
                                e.getMessage()),
                        e);
            }
        }
    }

    /*
     * Index a file and return a resource for it.
     */

    private void log(int level, String message, Throwable t) {
        if (log != null)
            log.log(level, message, t);
    }

    /**
     * @param analyzer
     *            the analyzer to add
     * @param filter
     *            the filter that determines which resources can be analyzed
     */
    public final void removeAnalyzer(ResourceAnalyzer analyzer, Filter filter) {
        synchronized (analyzers) {
            analyzers.remove(Pair.create(analyzer, filter));
        }
    }

    private void resolveDirectories(Set<File> files, Set<File> filesToIndex) {
        for (File file : files) {
            if (!file.isDirectory()) {
                filesToIndex.add(file);
            } else {
                File[] dirFiles = file.listFiles();
                if (dirFiles.length > 0) {
                    Set<File> dirFilesSet = new LinkedHashSet<>(
                            Arrays.asList(dirFiles));
                    resolveDirectories(dirFilesSet, filesToIndex);
                }
            }
        }
    }

    void setStateLocal(GeneratorState state) {
        RepoIndex.state.set(state);
    }

    @Override
    public void setKnownBundlesExtraProperties(final Properties props) {
//        knownBundleAnalyzer.setKnownBundlesExtra(props);
    }
}
