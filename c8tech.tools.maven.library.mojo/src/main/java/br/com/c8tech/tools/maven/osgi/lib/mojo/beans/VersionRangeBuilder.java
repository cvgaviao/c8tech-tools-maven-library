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

public final class VersionRangeBuilder {

    public static final String LEFT_CLOSED = "[";

    public static final String LEFT_OPEN = "(";

    private static final String RANGE_SEPARATOR = ",";

    public static final String RIGHT_CLOSED = "]";

    public static final String RIGHT_OPEN = ")";

    public static final String STANDARD_MAVEN_VERSION_RANGE = "([\\(|\\[])("
            + VersionBuilder.STANDARD_MAVEN_VERSION + ")?(\\,("
            + VersionBuilder.STANDARD_MAVEN_VERSION + ")?)?([\\)|\\]])";

    public static final Pattern STANDARD_MAVEN_VERSION_RANGE_PATTERN = Pattern
            .compile(STANDARD_MAVEN_VERSION_RANGE, Pattern.DOTALL);

    private static final String ZERO = "0.0.0";

    private String leftDelimiter;

    private String leftVersion;

    private Matcher matcher;

    private String rightDelimiter;
    private String rightVersion;
    private String versionRangeString;

    public VersionRangeBuilder(String mavenVersion) {
        matcher = STANDARD_MAVEN_VERSION_RANGE_PATTERN.matcher(mavenVersion);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The maven version range:"
                    + mavenVersion + " is not a valid one.");
        }

        leftDelimiter = matcher.group(1);
        rightDelimiter = matcher.group(19);
        leftVersion = calculateVersion(matcher.group(2));
        rightVersion = calculateVersion(matcher.group(11));

    }

    private static String calculateVersion(String mavenVersion) {
        return mavenVersion == null || mavenVersion.isEmpty() ? ZERO
                : VersionConverter.fromMavenVersion(mavenVersion)
                        .toString();
    }

    @Override
    public String toString() {
        String s = versionRangeString;
        if (s != null) {
            return s;
        }
        String versionLeft = leftVersion;
        String versionRight = rightVersion;

        if (!ZERO.equals(versionLeft) && ZERO.equals(versionRight)
                && leftDelimiter.equals(LEFT_CLOSED)) {
            if (rightDelimiter.equals(RIGHT_OPEN)) {
                versionRangeString = versionLeft;
                return versionRangeString;
            } else
                if (RIGHT_CLOSED.equals(rightDelimiter)) {
                    versionRight = versionLeft;
                }
        }
        versionRangeString = String.format("%s%s%s%s%s", leftDelimiter,
                versionLeft, RANGE_SEPARATOR, versionRight, rightDelimiter);
        return versionRangeString;
    }
}
