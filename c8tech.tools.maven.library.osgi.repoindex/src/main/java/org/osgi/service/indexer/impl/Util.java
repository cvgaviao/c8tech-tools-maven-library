/**
 * ======================================================================
 * Copyright © 2015-2019, OSGi Alliance, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package org.osgi.service.indexer.impl;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex)
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.impl.types.ScalarType;
import org.osgi.service.indexer.impl.types.SymbolicName;
import org.osgi.service.indexer.impl.types.VersionKey;
import org.osgi.service.indexer.impl.types.VersionRange;
import org.osgi.service.indexer.impl.util.OSGiHeader;
import org.osgi.service.indexer.impl.util.QuotedTokenizer;

public class Util {

    private static final Pattern MACRO_PATTERN = Pattern
            .compile("\\$\\{([^\\{\\}]*)\\}");

    public static void addVersionFilter(StringBuilder filter,
            VersionRange version, VersionKey key) {
        if (version.isRange()) {
            if (version.includeLow()) {
                filter.append("(").append(key.getKey());
                filter.append(">=");
                filter.append(version.getLow());
                filter.append(")");
            } else {
                filter.append("(!(").append(key.getKey());
                filter.append("<=");
                filter.append(version.getLow());
                filter.append("))");
            }

            if (version.includeHigh()) {
                filter.append("(").append(key.getKey());
                filter.append("<=");
                filter.append(version.getHigh());
                filter.append(")");
            } else {
                filter.append("(!(").append(key.getKey());
                filter.append(">=");
                filter.append(version.getHigh());
                filter.append("))");
            }
        } else {
            filter.append("(").append(key.getKey()).append(">=");
            filter.append(version);
            filter.append(")");
        }
    }

    public static void copyAttribsToBuilder(Builder builder,
            Map<String, String> attribs) {
        for (Entry<String, String> attrib : attribs.entrySet()) {
            String key = attrib.getKey();

            if (key.endsWith(":")) {
                String directiveName = key.substring(0, key.length() - 1);
                builder.addDirective(directiveName, attrib.getValue());
            } else {
                int colonIndex = key.lastIndexOf(':');

                String name;
                String typeStr;
                if (colonIndex > -1) {
                    name = key.substring(0, colonIndex);
                    typeStr = key.substring(colonIndex + 1);
                } else {
                    name = key;
                    typeStr = ScalarType.STRING.getKey();
                }

                Object value = Util.parseValue(attrib.getValue(), typeStr);
                builder.addAttribute(name, value);
            }
        }
    }

    /**
     * Returns a list of resource paths matching the glob pattern, e.g.
     * {@code OSGI-INF/blueprint/*.xml}. Wildcards only permitted in the final
     * path segment.
     *
     * @param resource
     *                        The root resource.
     * @param globPattern
     *                        The pattern to be used.
     * @return A list of matching paths.
     * @throws IOException
     *                         When something was wrong.
     */
    public static final List<String> findMatchingPaths(Resource resource,
            String globPattern) throws IOException {
        String prefix;
        String suffix;

        int index = globPattern.lastIndexOf('/');
        if (index == -1) {
            prefix = "";
            suffix = globPattern;
        } else {
            int next = index + 1;
            prefix = globPattern.substring(0, next);
            if (globPattern.length() <= next)
                suffix = "";
            else
                suffix = globPattern.substring(next);
        }

        String regexp = suffix.replaceAll("\\*", ".*");
        Pattern pattern = Pattern.compile(regexp);

        List<String> children = resource.listChildren(prefix);
        if (children == null)
            return Collections.emptyList();

        List<String> result = new ArrayList<>(children.size());

        for (String child : children) {
            if (pattern.matcher(child).matches())
                result.add(prefix + child);
        }

        return result;
    }

    public static MimeType getMimeType(Resource resource) throws IOException {
        Manifest manifest = resource.getManifest();
        if (manifest == null)
            return MimeType.JAR;
        String bsn = manifest.getMainAttributes()
                .getValue(Constants.BUNDLE_SYMBOLICNAME);
        if (bsn == null)
            return MimeType.JAR;

        String fragmentHost = manifest.getMainAttributes()
                .getValue(Constants.FRAGMENT_HOST);
        if (fragmentHost != null)
            return MimeType.FRAGMENT;

        return MimeType.BUNDLE;
    }

    public static SymbolicName getSymbolicName(Resource resource)
            throws IOException {
        Manifest manifest = resource.getManifest();
        if (manifest == null)
            throw new IllegalArgumentException(String.format(
                    "Cannot identify symbolic name for resource %s: manifest unavailable",
                    resource.getLocation()));

        String header = manifest.getMainAttributes()
                .getValue(Constants.BUNDLE_SYMBOLICNAME);
        if (header == null)
            throw new IllegalArgumentException(
                    "Not an OSGi R4+ bundle: missing 'BUNDLE-SymbolicName' entry from manifest.");

        Map<String, Map<String, String>> map = OSGiHeader.parseHeader(header);
        if (map.size() != 1)
            throw new IllegalArgumentException(
                    "Invalid format for BUNDLE-SymbolicName header.");

        Entry<String, Map<String, String>> entry = map.entrySet().iterator()
                .next();
        return new SymbolicName(entry.getKey(), entry.getValue());
    }

    public static Version getVersion(Resource resource) throws IOException {
        Manifest manifest = resource.getManifest();
        if (manifest == null)
            throw new IllegalArgumentException(String.format(
                    "Cannot identify version for resource %s: manifest unavailable",
                    resource.getLocation()));
        String versionStr = manifest.getMainAttributes()
                .getValue(Constants.BUNDLE_VERSION);
        return (versionStr != null) ? new Version(versionStr)
                : Version.emptyVersion;
    }

    private static List<?> parseListValue(String value, String typeStr) {

        QuotedTokenizer tokenizer = new QuotedTokenizer(value, ",");
        String[] tokens = tokenizer.getTokens();
        List<Object> result = new ArrayList<>(tokens.length);
        for (String token : tokens)
            result.add(parseScalarValue(token, typeStr));

        return result;
    }

    private static Object parseScalarValue(String value, String typeStr) {
        ScalarType type = ScalarType.fromString(typeStr);
        switch (type) {
        case STRING:
            return value;
        case LONG:
            return Long.valueOf(value);
        case DOUBLE:
            return Double.valueOf(value);
        case VERSION:
            return new Version(value);
        default:
            throw new IllegalArgumentException(typeStr);
        }
    }

    public static Object parseValue(String value, String typeStr) {
        Object result;

        if (typeStr.startsWith("List<")) {
            typeStr = typeStr.substring("List<".length(), typeStr.length() - 1);
            result = parseListValue(value, typeStr);
        } else {
            result = parseScalarValue(value, typeStr);
        }

        return result;
    }

    public static String readProcessedProperty(String propName,
            Properties... propsList) {
        String value = null;
        for (Properties props : propsList) {
            value = props.getProperty(propName);
            if (value != null)
                break;
        }
        if (value == null)
            return null;

        StringBuilder builder = new StringBuilder(value);
        Matcher matcher = MACRO_PATTERN.matcher(builder);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String varName = matcher.group(1);
            String processed = readProcessedProperty(varName, propsList);
            if (processed != null) {
                builder.replace(start, end, processed);
                matcher.reset(builder);
            }
        }

        return builder.toString();
    }

    private Util() {

    }
}
