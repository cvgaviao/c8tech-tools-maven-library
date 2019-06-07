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
package br.com.c8tech.tools.maven.osgi.lib.mojo.beans;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

public final class FileSet {

    public static final String FILE_SET_STRUCTURE_RULE = "((\\||\\s)?([^:|;\\s]+)((:)([^:|]+)((:)([^:|]+))?)?)";

    public static final Pattern FILE_SET_PATTERN = Pattern
            .compile(FILE_SET_STRUCTURE_RULE);

    /**
     * 
     */
    private Path directory = null;

    /**
     * A list of path patterns pointing to files and directories that must be
     * excluded by the file finder.
     * <p>
     * Example: {@literal "*.{html,txt}"}
     * <p>
     * The pattern must follow the rules specified in
     * {@link FileSystem#getPathMatcher(String)}.
     */
    private List<String> excludes = new ArrayList<>();

    /**
     * A list of path patterns pointing to files and directories that must be
     * included by file finder.
     * <p>
     * Example: {@literal "*.{esa,jar}"}
     * <p>
     * The pattern must follow the rules specified in
     * {@link FileSystem#getPathMatcher(String)}.
     */
    private List<String> includes = new ArrayList<>();

    public FileSet() {
        // needed for DI
    }

    public FileSet(String filesetStr) {
        set(filesetStr);
    }

    public void addExclude(String exclude) {
        excludes.add(exclude);
    }

    public void addInclude(String include) {
        includes.add(include);
    }

    public static List<FileSet> createCollectionFromString(
            String fileSetCollectionStr) {
        String[] tokens = fileSetCollectionStr.split(",");
        ArrayList<FileSet> list = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            FileSet nFileSet = new FileSet(token);
            list.add(nFileSet);
        }
        return list;
    }

    public Path getDirectory() {
        return directory;
    }

    public List<String> getExcludes() {
        return ImmutableList.copyOf(excludes);
    }

    public List<String> getIncludes() {
        return ImmutableList.copyOf(includes);
    }

    public void set(String fileSetString) {
        Matcher m = FILE_SET_PATTERN.matcher(fileSetString);

        if (m.find()) {
            String directoryStr = m.group(3);
            String includesStr = m.group(6);
            String excludesStr = m.group(9);
            setDirectory(Paths.get(directoryStr));
            if (includesStr != null) {
                includes.addAll(Arrays.asList(includesStr.split(";")));
            }
            if (excludesStr != null) {
                excludes.addAll(Arrays.asList(excludesStr.split(";")));
            }
        }
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(128);

        sb.append("FileSet {directory: " + getDirectory() + ", ");
        sb.append("[includes: {");
        for (Iterator<String> i = getIncludes().iterator(); i.hasNext();) {
            String str = i.next();
            sb.append(str).append(", ");
        }
        if (", ".equals(sb.substring(sb.length() - 2)))
            sb.delete(sb.length() - 2, sb.length());

        sb.append("}, excludes: {");
        for (Iterator<String> i = getExcludes().iterator(); i.hasNext();) {
            String str = i.next();
            sb.append(str).append(", ");
        }
        if (", ".equals(sb.substring(sb.length() - 2)))
            sb.delete(sb.length() - 2, sb.length());

        sb.append("}]}");
        return sb.toString();

    }

}
