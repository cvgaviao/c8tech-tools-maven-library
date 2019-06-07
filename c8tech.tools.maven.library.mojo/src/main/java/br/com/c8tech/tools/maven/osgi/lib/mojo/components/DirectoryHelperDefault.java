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
package br.com.c8tech.tools.maven.osgi.lib.mojo.components;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.FileSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DirectoryHelper;

/**
 *
 * @author cvgaviao
 *
 */
@Named()
public class DirectoryHelperDefault implements DirectoryHelper {

    public static class FilesFinderVisitor extends SimpleFileVisitor<Path> {

        private final List<PathMatcher> excludeMatchers;
        private final Set<File> filesFound;
        private final List<PathMatcher> includeMatchers;
        private int numMatches = 0;

        public FilesFinderVisitor(List<String> includeGlobPatterns,
                List<String> excludeGlobPatterns, Set<File> filesFound) {
            this.filesFound = filesFound;
            this.includeMatchers = buildFileMatchers(includeGlobPatterns);
            this.excludeMatchers = buildFileMatchers(excludeGlobPatterns);
        }

        private static List<PathMatcher> buildFileMatchers(
                List<String> pFilePatterns) {
            List<PathMatcher> matchers = new ArrayList<>(pFilePatterns.size());
            for (String pattern : pFilePatterns) {
                PathMatcher matcher = FileSystems.getDefault()
                        .getPathMatcher("glob:" + pattern);
                matchers.add(matcher);
            }
            return matchers;
        }

        /**
         * returns the number of matches.
         *
         * @return The number of matches.
         */
        public int count() {
            return numMatches;
        }

        private boolean isExcluded(Path pPath) {
            if (!excludeMatchers.isEmpty()) {
                for (PathMatcher pathMatcher : excludeMatchers) {
                    if (pathMatcher.matches(pPath)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isIncluded(Path path) {
            if (!includeMatchers.isEmpty()) {
                for (PathMatcher pathMatcher : includeMatchers) {
                    if (pathMatcher.matches(path)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) {
            if (dir.startsWith(".")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (isExcluded(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (isExcluded(file)) {
                return FileVisitResult.CONTINUE;
            }
            if (isIncluded(file)) {
                numMatches++;
                filesFound.add(file.toFile());
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return CONTINUE;
        }

    }

    private static final Logger LOG = LoggerFactory
            .getLogger(DirectoryHelperDefault.class);

    public static String getFileNameFromUrl(String pUrlString)
            throws MalformedURLException {
        return getFileNameFromUrl(new URL(pUrlString));
    }

    /**
     * This function will take an URL as input and return the file name.
     * <p>
     * Examples :
     * </p>
     * <ul>
     * <li>http://example.com/a/b/c/test.txt -- test.txt</li>
     * <li>http://example.com/ -- an empty string</li>
     * <li>http://example.com/test.txt?param=value -- test.txt</li>
     * <li>http://example.com/test.txt#anchor -- test.txt</li>
     * </ul>
     *
     * @param pUrl
     *                The input URL
     * @return The URL file name
     */
    public static String getFileNameFromUrl(URL pUrl) {

        String urlString = pUrl.getFile();

        return urlString.substring(urlString.lastIndexOf('/') + 1)
                .split("\\?")[0].split("#")[0];
    }

    @Override
    public void cleanDirectory(final Path pDirectoryPath) throws IOException {
        try {
            Files.walkFileTree(pDirectoryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc) throws IOException {
                    if (pDirectoryPath.compareTo(dir) == 0) {
                        return FileVisitResult.CONTINUE;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOException("Error cleaning work directory.", e);
        }

    }

    @Override
    public void copyDirectory(final Path pSourcePath, final Path pTargetPath)
            throws IOException {

        try {
            Files.walkFileTree(pSourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(
                            pTargetPath.resolve(pSourcePath.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    Files.copy(file,
                            pTargetPath.resolve(pSourcePath.relativize(file)),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOException("Error copying directory.", e);
        }
    }

    @Override
    public File copyResourceFromWebServerToDirectory(URL pResourceUrl,
            Path pTargetDirectory) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) pResourceUrl
                .openConnection();
        String fileName = "";
        String disposition = httpConn.getHeaderField("Content-Disposition");
        int contentLength = httpConn.getContentLength();
        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 9,
                        disposition.length());
            }
        } else {
            // extracts file name from URL
            fileName = getFileNameFromUrl(pResourceUrl);
        }
        Path targetFilePath = pTargetDirectory.resolve(fileName);
        try (InputStream inputStream = httpConn.getInputStream();) {
            long size = Files.copy(inputStream, targetFilePath,
                    StandardCopyOption.REPLACE_EXISTING);
            if (contentLength != size) {
                throw new IOException(
                        "Downloaded bytes was different from the content lenght");
            }
        }
        return targetFilePath.toFile();
    }

    @Override
    public File copyResourceToDirectory(URL pResourceUrl, // NOSONAR
            final Path pTargetDirectory) throws IOException {
        if (pTargetDirectory == null) {
            throw new IOException("Target directory can't be null.");
        }
        Files.createDirectories(pTargetDirectory);
        if (pResourceUrl.toExternalForm().startsWith("file:")) {
            File file;
            try {
                file = new File(pResourceUrl.toURI());
                if (!file.exists()) {
                    throw new IOException(
                            "Do not exists source file at " + file.toString());
                }
            } catch (URISyntaxException e) {
                LOG.warn("error converting artifact URL into a file.", e);
                file = new File(pResourceUrl.getPath());
            }
            Path targetFile = pTargetDirectory.resolve(file.getName());
            Path copied = Files.copy(file.toPath(), targetFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            if (!copied.equals(targetFile)) {
                throw new IOException();
            }
            return targetFile.toFile();
        } else
            if (pResourceUrl.toExternalForm().startsWith("http:")
                    || pResourceUrl.toExternalForm().startsWith("https:")) {
                return copyResourceFromWebServerToDirectory(pResourceUrl,
                        pTargetDirectory);
            }
        throw new IllegalArgumentException("Inexistent directory.");
    }

    @Override
    public Set<File> findFiles(FileSet... fileSets) throws IOException {
        Set<File> foundFiles = new HashSet<>();
        for (FileSet fileSet : fileSets) {
            foundFiles.addAll(findFiles(fileSet.getDirectory(),
                    fileSet.getIncludes(), fileSet.getExcludes()));
        }
        return foundFiles;
    }

    @Override
    public Set<File> findFiles(List<FileSet> fileSetCollection)
            throws IOException {
        FileSet[] fileSetsArray = new FileSet[fileSetCollection.size()];
        return findFiles(fileSetCollection.toArray(fileSetsArray));
    }

    @Override
    public Set<File> findFiles(Path directory, List<String> includeGlobPatterns,
            List<String> excludeGlobPatterns) throws IOException {
        Set<File> foundFiles = new HashSet<>();
        try {
            Files.walkFileTree(directory, new FilesFinderVisitor(
                    includeGlobPatterns, excludeGlobPatterns, foundFiles));
        } catch (Exception e) {
            throw new IOException("Error searching for files to index.", e);
        }
        return foundFiles;
    }
}
