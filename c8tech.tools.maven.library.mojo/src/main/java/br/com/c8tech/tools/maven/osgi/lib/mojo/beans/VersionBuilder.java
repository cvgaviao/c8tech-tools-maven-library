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
package br.com.c8tech.tools.maven.osgi.lib.mojo.beans;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionBuilder {

    private static final String MAVEN_SNAPSHOT = "SNAPSHOT";

    private static final String OSGI_SNAPSHOT = "qualifier";

    public static final String QUALIFIER_RULE = "[A-Z|a-z|0-9|_|-]*";

    public static final Pattern QUALIFIER_PATTERN = Pattern
            .compile(QUALIFIER_RULE, Pattern.DOTALL);

    private static final String SEPARATOR = ".";

    public static final String STANDARD_MAVEN_VERSION = "(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([-|.]([^\\,]*))?";

    public static final Pattern STANDARD_MAVEN_VERSION_PATTERN = Pattern
            .compile(STANDARD_MAVEN_VERSION, Pattern.DOTALL);

    private int incremental;

    private int major;

    private int minor;

    private String qualifier;

    private String versionString;

    public VersionBuilder(String mavenVersion) {
        Matcher matcher;

        if (mavenVersion == null) {
            throw new IllegalArgumentException(
                    "The maven version cannot be null.");
        }

        matcher = STANDARD_MAVEN_VERSION_PATTERN.matcher(mavenVersion);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The maven version:"
                    + mavenVersion + " is not a valid one.");
        }
        major(matcher.group(1)).minor(matcher.group(3)).patch(matcher.group(5))
                .qualifier(matcher.group(7));
    }

    public VersionBuilder major(String majorVersion) {
        if (majorVersion == null || majorVersion.isEmpty()) {
            this.major = 0;
            return this;
        }
        int majorl = Integer.parseInt(majorVersion);
        if (majorl < 0) {
            throw new IllegalArgumentException("invalid value for major.");
        }
        this.major = majorl;
        return this;
    }

    public VersionBuilder minor(String minorVersion) {
        if (minorVersion == null || minorVersion.isEmpty()) {
            this.minor = 0;
            return this;
        }
        int minorl = Integer.parseInt(minorVersion);
        if (minorl < 0) {
            throw new IllegalArgumentException("invalid value for minor.");
        }
        this.minor = minorl;
        return this;
    }

    public VersionBuilder patch(String patchVersion) {
        if (patchVersion == null || patchVersion.isEmpty()) {
            this.incremental = 0;
            return this;
        }
        int patch = Integer.parseInt(patchVersion);
        if (patch < 0) {
            throw new IllegalArgumentException("invalid value for patch.");
        }
        this.incremental = patch;
        return this;
    }

    VersionBuilder qualifier(String qualifierArg) {
        Matcher matcher;

        if (qualifierArg == null) {
            return this;
        }
        if (qualifierArg.equalsIgnoreCase(MAVEN_SNAPSHOT)) {
            this.qualifier = OSGI_SNAPSHOT;
            return this;
        }
        matcher = QUALIFIER_PATTERN.matcher(qualifierArg);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "invalid qualifier \"" + qualifierArg + "\"");
        }
        this.qualifier = qualifierArg;
        return this;
    }

    @Override
    public String toString() {
        String s = versionString;
        if (s != null) {
            return s;
        }
        versionString = String.format("%s%s%s%s%s", major,
                SEPARATOR, minor, SEPARATOR, incremental);
        
         if (qualifier != null) {
             versionString = versionString.concat(SEPARATOR).concat(qualifier);
        }
        return versionString;
    }
}
