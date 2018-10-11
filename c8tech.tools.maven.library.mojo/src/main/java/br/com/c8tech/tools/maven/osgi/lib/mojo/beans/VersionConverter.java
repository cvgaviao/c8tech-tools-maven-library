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

/**
 * An utility class used to convert the version string from a maven artifact to
 * an OSGi one.
 * <p>
 *
 * It can handle both single version and version range string.
 * 
 * @see <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm">https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm</a>
 *
 * @author Cristiano Gavião
 */
public final class VersionConverter {

    private static class CommonOutputBuilder{
        
        protected String buildNumber;
        protected Integer incremental;
        protected LeftDelimiter leftDelimiter;
        protected String leftVersion;
        protected Integer major;
        protected Integer minor;
        protected String qualifier;
        protected RightDelimiter rightDelimiter;
        protected String rightVersion;


        public String getVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            if (!ZERO.equals(sLeftVersion) && ZERO.equals(sRightVersion)
                    && leftDelimiter.equals(LeftDelimiter.CLOSE)) {
                if (rightDelimiter.equals(RightDelimiter.OPEN)) {
                    return sLeftVersion;
                } else
                    if (RightDelimiter.CLOSE.equals(rightDelimiter)) {
                        sRightVersion = sLeftVersion;
                    }
            }
            return String.format(VERSION_RANGE_OUTPUT_FORMAT,
                    leftDelimiter.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    rightDelimiter.getDelimiterChar());
        }
        
        public String getVersionString() {
            String versionString = String.format(VERSION_OUTPUT_FORMAT, major,
                    VERSION_SEPARATOR, minor, VERSION_SEPARATOR, incremental);

            if (qualifier != null) {
                versionString = versionString.concat(SEPARATOR_Q)
                        .concat(qualifier.equals(OSGI_SNAPSHOT) ? MAVEN_SNAPSHOT
                                : qualifier);
            }
            if (buildNumber != null) {
                versionString = versionString.concat(SEPARATOR_Q)
                        .concat(buildNumber);
            }
            return versionString;
        }
    }

    public enum LeftDelimiter {
        CLOSE('['), OPEN('(');

        private char character;

        LeftDelimiter(char pCharacter) {
            character = pCharacter;
        }

        public static LeftDelimiter parseEnum(String pRightDelimiterChar) {

            switch (pRightDelimiterChar) {
            case "(":
                return OPEN;
            case "[":
                return CLOSE;
            default:
                return null;
            }
        }

        public char getDelimiterChar() {
            return character;
        }
    }

    private static class MavenVersionInputBuilder
            implements MavenVersionInputSteps, MavenVersionSplittedInputSteps {

        private static final String A_QUALIFIER_RULE = "[A-Z|a-z|0-9|_|-]*";

        private static final String A_STANDARD_MAVEN_VERSION = "(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:-([\\w]+)(?:-(\\d+))?)?(?![\\d.])";

        private static final Pattern B_QUALIFIER_PATTERN = Pattern
                .compile(A_QUALIFIER_RULE, Pattern.DOTALL);

        private static final Pattern B_STANDARD_MAVEN_VERSION_PATTERN = Pattern
                .compile(A_STANDARD_MAVEN_VERSION, Pattern.DOTALL);

        private String buildNumber;

        private int incremental;

        private int major;

        private int minor;

        private String qualifier;

        private MavenVersionInputBuilder() {
        }

        public static boolean isMavenVersion(String pMavenVersionString) {
            Matcher matcher = B_STANDARD_MAVEN_VERSION_PATTERN
                    .matcher(pMavenVersionString);
            return matcher.matches();
        }

        @Override
        public MavenVersionSplittedInputSteps buildNumber(
                String pMavenBuildNumber) {
            if (pMavenBuildNumber == null) {
                return this;
            }
            this.buildNumber = pMavenBuildNumber;
            return this;
        }

        @Override
        public MavenVersionInputSteps fromFullString(
                String pMavenVersionString) {
            if (pMavenVersionString == null) {
                throw new IllegalArgumentException(
                        "The maven version cannot be null.");
            }
            if (pMavenVersionString.charAt(0) == LeftDelimiter.CLOSE
                    .getDelimiterChar()
                    || pMavenVersionString.charAt(0) == LeftDelimiter.OPEN
                            .getDelimiterChar()) {
                return new MavenVersionRangeInputBuilder()
                        .fromFullString(pMavenVersionString);
            }
            Matcher matcher = B_STANDARD_MAVEN_VERSION_PATTERN
                    .matcher(pMavenVersionString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("The version:"
                        + pMavenVersionString + " is not a valid maven one.");
            }

            this.major(matcher.group(1));
            this.minor(matcher.group(2));
            this.incremental(matcher.group(3));
            this.qualifier(matcher.group(4));
            this.buildNumber(matcher.group(5));
            return this;
        }

        @Override
        public MavenVersionSplittedInputSteps incremental(
                Integer pMavenIncrementalVersion) {
            if (pMavenIncrementalVersion == null) {
                this.incremental = 0;
                return this;
            }
            int patch = pMavenIncrementalVersion;
            if (patch < 0) {
                throw new IllegalArgumentException("invalid value for patch.");
            }
            this.incremental = patch;
            return this;
        }

        protected MavenVersionSplittedInputSteps incremental(
                String pMavenIncrementalVersion) {
            if (pMavenIncrementalVersion != null) {
                return incremental(Integer.parseInt(pMavenIncrementalVersion));
            }
            this.incremental = 0;
            return this;

        }

        @Override
        public MavenVersionSplittedInputSteps major(
                Integer pMavenMajorVersion) {
            if (pMavenMajorVersion == null) {
                this.major = 0;
                return this;
            }
            int majorl = pMavenMajorVersion;
            if (majorl < 0) {
                throw new IllegalArgumentException("invalid value for major.");
            }
            this.major = majorl;
            return this;
        }

        protected MavenVersionSplittedInputSteps major(
                String pMavenMajorVersion) {
            if (pMavenMajorVersion != null) {
                return major(Integer.parseInt(pMavenMajorVersion));
            }
            this.major = 0;
            return this;

        }

        @Override
        public MavenVersionSplittedInputSteps minor(
                Integer pMavenMinorVersion) {
            if (pMavenMinorVersion == null) {
                this.minor = 0;
                return this;
            }
            int minorl = pMavenMinorVersion;
            if (minorl < 0) {
                throw new IllegalArgumentException("invalid value for minor.");
            }
            this.minor = minorl;
            return this;
        }

        protected MavenVersionSplittedInputSteps minor(
                String pMavenMinorVersion) {
            if (pMavenMinorVersion != null) {
                return minor(Integer.parseInt(pMavenMinorVersion));
            }
            this.minor = 0;
            return this;

        }

        @Override
        public MavenVersionSplittedInputSteps qualifier(
                String pMavenQualifierVersion) {

            if (pMavenQualifierVersion == null) {
                return this;
            }
            if (pMavenQualifierVersion.equalsIgnoreCase(MAVEN_SNAPSHOT)
                    || pMavenQualifierVersion
                            .equalsIgnoreCase("-" + MAVEN_SNAPSHOT)) {
                this.qualifier = OSGI_SNAPSHOT;
                return this;
            }
            Matcher matcher = B_QUALIFIER_PATTERN
                    .matcher(pMavenQualifierVersion);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "invalid qualifier \"" + pMavenQualifierVersion + "\"");
            }
            this.qualifier = pMavenQualifierVersion;
            return this;
        }

        @Override
        public MavenVersionOutput toMaven() {
            return new MavenVersionOutputBuilder(major, minor, incremental,
                    qualifier, buildNumber);
        }

        @Override
        public OSGiVersionOutput toOSGi() {
            return new OSGiVersionOutputBuilder(major, minor, incremental,
                    qualifier, buildNumber);
        }

    }

    public static interface MavenVersionInputSteps {

        MavenVersionInputSteps fromFullString(String pMavenVersionRangeString);

        MavenVersionOutput toMaven();

        OSGiVersionOutput toOSGi();

    }

    
    public interface MavenVersionOutput {

        String getFixedVersionRangeString();

        String getOpenVersionRangeString();

        String getVersionRangeString();

        String getVersionString();
    }
    
    private static class MavenVersionOutputBuilder extends CommonOutputBuilder
            implements MavenVersionOutput {


        public MavenVersionOutputBuilder(int pMajor, int pMinor,
                int pIncremental, String pQualifier, String pBuildNumber) {
            major = pMajor;
            minor = pMinor;
            incremental = pIncremental;
            qualifier = pQualifier;
            buildNumber = pBuildNumber;
        }

        public MavenVersionOutputBuilder(LeftDelimiter pLeftDelimiter,
                String pLeftVersion, String pRightVersion,
                RightDelimiter pRightDelimiter) {
            leftDelimiter = pLeftDelimiter;
            leftVersion = pLeftVersion;
            rightDelimiter = pRightDelimiter;
            rightVersion = pRightVersion;
        }

        @Override
        public String getFixedVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            return String.format(VERSION_RANGE_OUTPUT_FORMAT,
                    LeftDelimiter.CLOSE.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    RightDelimiter.CLOSE.getDelimiterChar());
        }

        @Override
        public String getOpenVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            return String.format(VERSION_RANGE_OUTPUT_FORMAT,
                    LeftDelimiter.CLOSE.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    RightDelimiter.OPEN.getDelimiterChar());
        }

        @Override
        public String getVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            if (!ZERO.equals(sLeftVersion) && ZERO.equals(sRightVersion)
                    && leftDelimiter.equals(LeftDelimiter.CLOSE)) {
                if (rightDelimiter.equals(RightDelimiter.OPEN)) {
                    return sLeftVersion;
                } else
                    if (RightDelimiter.CLOSE.equals(rightDelimiter)) {
                        sRightVersion = sLeftVersion;
                    }
            }
            return String.format(VERSION_RANGE_OUTPUT_FORMAT,
                    leftDelimiter.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    rightDelimiter.getDelimiterChar());
        }


    }

    private static class MavenVersionRangeInputBuilder implements
            MavenVersionInputSteps, MavenVersionRangeSplittedInputSteps {

        private static final String STANDARD_MAVEN_VERSION_RANGE = "([\\(|\\[])("
                + MavenVersionInputBuilder.A_STANDARD_MAVEN_VERSION + ")?(\\,("
                + MavenVersionInputBuilder.A_STANDARD_MAVEN_VERSION
                + ")?)?([\\)|\\]])";

        private static final Pattern STANDARD_MAVEN_VERSION_RANGE_PATTERN = Pattern
                .compile(STANDARD_MAVEN_VERSION_RANGE, Pattern.DOTALL);

        private LeftDelimiter leftDelimiter;

        private String leftVersion;

        private RightDelimiter rightDelimiter;

        private String rightVersion;

        private boolean splitted;

        private MavenVersionRangeInputBuilder() {
        }

        private MavenVersionRangeInputBuilder(boolean pSplitted) {
            splitted = pSplitted;
        }

        private String calculateVersion(String pMavenVersion) {
            return pMavenVersion == null || pMavenVersion.isEmpty() ? ZERO
                    : VersionConverter.fromMavenVersion(pMavenVersion).toOSGi()
                            .getVersionString();
        }

        @Override
        public MavenVersionInputSteps fromFullString(
                String pMavenVersionRangeString) {
            Matcher matcher = STANDARD_MAVEN_VERSION_RANGE_PATTERN
                    .matcher(pMavenVersionRangeString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "The version range:" + pMavenVersionRangeString
                                + " is not a valid maven one.");
            }

            leftDelimiter = LeftDelimiter.parseEnum(matcher.group(1));
            rightDelimiter = RightDelimiter.parseEnum(matcher.group(15));
            leftVersion = calculateVersion(matcher.group(2));
            rightVersion = calculateVersion(matcher.group(9));

            return this;

        }

        @Override
        public MavenVersionRangeSplittedInputSteps leftDelimiter(
                LeftDelimiter pLeftDelimiter) {
            leftDelimiter = pLeftDelimiter;
            return this;
        }

        @Override
        public MavenVersionRangeSplittedInputSteps leftVersion(
                String pLeftVersionString) {
            leftVersion = calculateVersion(pLeftVersionString);
            return this;
        }

        @Override
        public MavenVersionRangeSplittedInputSteps rightDelimiter(
                RightDelimiter pRightDelimiter) {
            rightDelimiter = pRightDelimiter;
            return this;
        }

        @Override
        public MavenVersionRangeSplittedInputSteps rightVersion(
                String pRightVersionString) {
            rightVersion = calculateVersion(pRightVersionString);
            return this;
        }

        @Override
        public MavenVersionOutput toMaven() {
            if (splitted && (leftVersion == null || leftVersion.isEmpty())) {
                throw new IllegalArgumentException(
                        "The Maven version range informed is not a valid one.");
            }
            return new MavenVersionOutputBuilder(leftDelimiter, leftVersion,
                    rightVersion, rightDelimiter);
        }

        @Override
        public OSGiVersionOutput toOSGi() {
            if (splitted && (leftVersion == null || leftVersion.isEmpty())) {
                throw new IllegalArgumentException(
                        "The Maven version range informed is not a valid one.");
            }
            return new OSGiVersionOutputBuilder(leftDelimiter, leftVersion,
                    rightVersion, rightDelimiter);
        }

    }

    public static interface MavenVersionRangeSplittedInputSteps {

        MavenVersionRangeSplittedInputSteps leftDelimiter(
                LeftDelimiter pLeftDelimiter);

        MavenVersionRangeSplittedInputSteps leftVersion(
                String pLeftVersionString);

        MavenVersionRangeSplittedInputSteps rightDelimiter(
                RightDelimiter pRightDelimiter);

        MavenVersionRangeSplittedInputSteps rightVersion(
                String pRightVersionString);

        MavenVersionOutput toMaven();

        OSGiVersionOutput toOSGi();

    }

    public static interface MavenVersionSplittedInputSteps {

        MavenVersionSplittedInputSteps buildNumber(String pMavenBuildNumber);

        MavenVersionSplittedInputSteps incremental(
                Integer pMavenIncrementalVersion);

        MavenVersionSplittedInputSteps major(Integer pMavenMajorVersion);

        MavenVersionSplittedInputSteps minor(Integer pMavenMinorVersion);

        MavenVersionSplittedInputSteps qualifier(String pMavenQualifierVersion);

        MavenVersionOutput toMaven();

        OSGiVersionOutput toOSGi();

    }

    private static class OSGiVersionInputBuilder
            implements OSGiVersionInputSteps, OSGiVersionSplittedInputSteps {

        private static final String A_QUALIFIER_RULE = "[A-Z|a-z|0-9|_|-]*";

        private static final String A_STANDARD_OSGI_VERSION = "(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:\\.([\\w]+)(?:-(\\d+))?)?(?![\\d.])";

        private static final Pattern B_QUALIFIER_PATTERN = Pattern
                .compile(A_QUALIFIER_RULE, Pattern.DOTALL);

        private static final Pattern B_STANDARD_OSGI_VERSION_PATTERN = Pattern
                .compile(A_STANDARD_OSGI_VERSION, Pattern.DOTALL);

        private String buildNumber;

        private int major;

        private int micro;

        private int minor;

        private String qualifier;

        private OSGiVersionInputBuilder() {
        }

        public static boolean isOSGiVersion(String pOSGiVersionString) {
            Matcher matcher = B_STANDARD_OSGI_VERSION_PATTERN
                    .matcher(pOSGiVersionString);
            return matcher.matches();
        }

        @Override
        public OSGiVersionSplittedInputSteps buildNumber(
                String pOSGiBuildNumber) {
            if (pOSGiBuildNumber == null) {
                return this;
            }
            this.buildNumber = pOSGiBuildNumber;
            return this;
        }

        @Override
        public OSGiVersionInputSteps fromFullString(String pOSGiVersionString) {
            if (pOSGiVersionString == null) {
                throw new IllegalArgumentException(
                        "The OSGi version cannot be null.");
            }
            if (pOSGiVersionString.charAt(0) == LeftDelimiter.CLOSE
                    .getDelimiterChar()
                    || pOSGiVersionString.charAt(0) == LeftDelimiter.OPEN
                            .getDelimiterChar()) {
                return new OSGiVersionRangeInputBuilder()
                        .fromFullString(pOSGiVersionString);
            }

            Matcher matcher = B_STANDARD_OSGI_VERSION_PATTERN
                    .matcher(pOSGiVersionString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("The OSGi version:"
                        + pOSGiVersionString + " is not a valid one.");
            }

            this.major(matcher.group(1));
            this.minor(matcher.group(2));
            this.micro(matcher.group(3));
            this.qualifier(matcher.group(4));
            this.buildNumber(matcher.group(5));

            return this;
        }

        @Override
        public OSGiVersionSplittedInputSteps major(Integer pOSGiMajorVersion) {
            if (pOSGiMajorVersion == null) {
                this.major = 0;
                return this;
            }
            int majorl = pOSGiMajorVersion;
            if (majorl < 0) {
                throw new IllegalArgumentException(
                        "invalid value for major version.");
            }
            this.major = majorl;
            return this;
        }

        protected OSGiVersionSplittedInputSteps major(
                String pOSGiMajorVersion) {
            if (pOSGiMajorVersion != null) {
                return major(Integer.parseInt(pOSGiMajorVersion));
            }
            this.major = 0;
            return this;

        }

        @Override
        public OSGiVersionSplittedInputSteps micro(Integer pOSGiMicroVersion) {
            if (pOSGiMicroVersion == null) {
                this.micro = 0;
                return this;
            }
            int patch = pOSGiMicroVersion;
            if (patch < 0) {
                throw new IllegalArgumentException(
                        "invalid value for incremental version.");
            }
            this.micro = patch;
            return this;
        }

        protected OSGiVersionSplittedInputSteps micro(
                String pOSGiMicroVersion) {
            if (pOSGiMicroVersion != null) {
                return micro(Integer.parseInt(pOSGiMicroVersion));
            }
            this.micro = 0;
            return this;

        }

        @Override
        public OSGiVersionSplittedInputSteps minor(Integer pOSGiMinorVersion) {
            if (pOSGiMinorVersion == null) {
                this.minor = 0;
                return this;
            }
            int minorl = pOSGiMinorVersion;
            if (minorl < 0) {
                throw new IllegalArgumentException(
                        "invalid value for minor version.");
            }
            this.minor = minorl;
            return this;
        }

        protected OSGiVersionSplittedInputSteps minor(
                String pOSGiMinorVersion) {
            if (pOSGiMinorVersion != null) {
                return minor(Integer.parseInt(pOSGiMinorVersion));
            }
            this.minor = 0;
            return this;

        }

        @Override
        public OSGiVersionSplittedInputSteps qualifier(
                String pOSGiQualifierVersion) {

            if (pOSGiQualifierVersion == null) {
                return this;
            }

            Matcher matcher = B_QUALIFIER_PATTERN
                    .matcher(pOSGiQualifierVersion);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "invalid qualifier \"" + pOSGiQualifierVersion + "\"");
            }
            this.qualifier = pOSGiQualifierVersion;
            return this;
        }

        @Override
        public MavenVersionOutput toMaven() {
            String squalifier = qualifier;
            if (qualifier != null && (qualifier.equalsIgnoreCase(OSGI_SNAPSHOT)
                    || qualifier.equalsIgnoreCase("." + OSGI_SNAPSHOT))) {
                squalifier = MAVEN_SNAPSHOT;
            }
            return new MavenVersionOutputBuilder(major, minor, micro, squalifier,
                    buildNumber);
        }

        @Override
        public OSGiVersionOutput toOSGi() {
            return new OSGiVersionOutputBuilder(major, minor, micro, qualifier,
                    buildNumber);
        }

    }

    public static interface OSGiVersionInputSteps {
        OSGiVersionInputSteps fromFullString(String pOSGiVersionString);

        MavenVersionOutput toMaven();

        OSGiVersionOutput toOSGi();

    }

    public interface OSGiVersionOutput {

        String getFixedVersionRangeString();

        String getOpenVersionRangeString();

        String getVersionRangeString();

        String getVersionString();

    }

    private static class OSGiVersionOutputBuilder extends CommonOutputBuilder implements OSGiVersionOutput {

        private Integer micro;

        public OSGiVersionOutputBuilder(Integer pMajor, Integer pMinor,
                Integer pMicro, String pQualifier, String pBuildNumber) {
            major = pMajor;
            minor = pMinor;
            micro = pMicro;
            qualifier = pQualifier;
            buildNumber = pBuildNumber;
        }

        public OSGiVersionOutputBuilder(LeftDelimiter pLeftDelimiter,
                String pLeftVersion, String pRightVersion,
                RightDelimiter pRightDelimiter) {
            leftDelimiter = pLeftDelimiter;
            leftVersion = pLeftVersion;
            rightDelimiter = pRightDelimiter;
            rightVersion = pRightVersion;
        }

        @Override
        public String getFixedVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            return String.format(VERSION_OUTPUT_FORMAT,
                    LeftDelimiter.CLOSE.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    RightDelimiter.CLOSE.getDelimiterChar());
        }

        @Override
        public String getOpenVersionRangeString() {
            String sLeftVersion = leftVersion == null ? getVersionString()
                    : leftVersion;
            String sRightVersion = rightVersion == null ? sLeftVersion
                    : rightVersion;

            return String.format(VERSION_OUTPUT_FORMAT,
                    LeftDelimiter.CLOSE.getDelimiterChar(), sLeftVersion,
                    RANGE_SEPARATOR, sRightVersion,
                    RightDelimiter.OPEN.getDelimiterChar());
        }


        @Override
        public String getVersionString() {
            String versionString = String.format(VERSION_OUTPUT_FORMAT, major,
                    VERSION_SEPARATOR, minor, VERSION_SEPARATOR, micro);

            if (qualifier != null) {
                versionString = versionString.concat(VERSION_SEPARATOR)
                        .concat(qualifier.equals(MAVEN_SNAPSHOT) ? OSGI_SNAPSHOT
                                : qualifier);
            }
            if (buildNumber != null) {
                versionString = versionString.concat(SEPARATOR_Q)
                        .concat(buildNumber);
            }
            return versionString;
        }
    }

    private static class OSGiVersionRangeInputBuilder implements
            OSGiVersionInputSteps, OSGiVersionRangeSplittedInputSteps {

        private static final String STANDARD_OSGI_VERSION_RANGE = "([\\(|\\[])("
                + OSGiVersionInputBuilder.A_STANDARD_OSGI_VERSION + ")?(\\,("
                + OSGiVersionInputBuilder.A_STANDARD_OSGI_VERSION
                + ")?)?([\\)|\\]])";

        private static final Pattern STANDARD_OSGI_VERSION_RANGE_PATTERN = Pattern
                .compile(STANDARD_OSGI_VERSION_RANGE, Pattern.DOTALL);

        private LeftDelimiter leftDelimiter;

        private String leftVersion;

        private RightDelimiter rightDelimiter;

        private String rightVersion;

        private boolean splitted;

        public OSGiVersionRangeInputBuilder() {

        }

        public OSGiVersionRangeInputBuilder(boolean pSplitted) {
            splitted = pSplitted;
        }

        private String calculateVersion(String pOSGiVersion) {
            return pOSGiVersion == null || pOSGiVersion.isEmpty() ? ZERO
                    : VersionConverter.fromOsgiVersion(pOSGiVersion).toOSGi()
                            .getVersionString();
        }

        @Override
        public OSGiVersionInputSteps fromFullString(
                String pOSGiVersionRangeString) {

            if (pOSGiVersionRangeString == null) {
                throw new IllegalArgumentException(
                        "The OSGi version range can't be null.");
            }
            if (pOSGiVersionRangeString.charAt(0) != LeftDelimiter.CLOSE
                    .getDelimiterChar()
                    && pOSGiVersionRangeString.charAt(0) != LeftDelimiter.OPEN
                            .getDelimiterChar()) {
                return new OSGiVersionInputBuilder()
                        .fromFullString(pOSGiVersionRangeString);
            }

            Matcher matcher = STANDARD_OSGI_VERSION_RANGE_PATTERN
                    .matcher(pOSGiVersionRangeString);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("The OSGi version range:"
                        + pOSGiVersionRangeString + " is not a valid one.");
            }

            leftDelimiter = LeftDelimiter.parseEnum(matcher.group(1));
            rightDelimiter = RightDelimiter.parseEnum(matcher.group(15));
            leftVersion = calculateVersion(matcher.group(2));
            rightVersion = calculateVersion(matcher.group(9));

            return this;
        }

        @Override
        public OSGiVersionRangeSplittedInputSteps leftDelimiter(
                LeftDelimiter pLeftDelimiter) {
            leftDelimiter = pLeftDelimiter;
            return this;
        }

        @Override
        public OSGiVersionRangeSplittedInputSteps leftVersion(
                String pLeftVersionString) {
            leftVersion = calculateVersion(pLeftVersionString);
            return this;
        }

        @Override
        public OSGiVersionRangeSplittedInputSteps rightDelimiter(
                RightDelimiter pRightDelimiter) {
            rightDelimiter = pRightDelimiter;
            return this;
        }

        @Override
        public OSGiVersionRangeSplittedInputSteps rightVersion(
                String pRightVersionString) {
            rightVersion = calculateVersion(pRightVersionString);
            return this;
        }

        @Override
        public MavenVersionOutput toMaven() {
            if (splitted && (leftVersion == null || leftVersion.isEmpty())) {
                throw new IllegalArgumentException(
                        "The OSGi version range informed is not a valid one.");
            }

            String sL = new OSGiVersionInputBuilder()
                    .fromFullString(leftVersion).toMaven().getVersionString();
            String sR = new OSGiVersionInputBuilder()
                    .fromFullString(rightVersion).toMaven().getVersionString();

            return new MavenVersionOutputBuilder(leftDelimiter, sL, sR,
                    rightDelimiter);
        }

        @Override
        public OSGiVersionOutput toOSGi() {
            if (splitted && (leftVersion == null || leftVersion.isEmpty())) {
                throw new IllegalArgumentException(
                        "The OSGi version range informed is not a valid one.");
            }
            return new OSGiVersionOutputBuilder(leftDelimiter, leftVersion,
                    rightVersion, rightDelimiter);
        }
    }

    public static interface OSGiVersionRangeSplittedInputSteps {

        OSGiVersionRangeSplittedInputSteps leftDelimiter(
                LeftDelimiter pLeftDelimiter);

        OSGiVersionRangeSplittedInputSteps leftVersion(
                String pLeftVersionString);

        OSGiVersionRangeSplittedInputSteps rightDelimiter(
                RightDelimiter pRightDelimiter);

        OSGiVersionRangeSplittedInputSteps rightVersion(
                String pRightVersionString);

        MavenVersionOutput toMaven();

        OSGiVersionOutput toOSGi();
    }

    public static interface OSGiVersionSplittedInputSteps {

        OSGiVersionSplittedInputSteps buildNumber(String pOSGiBuildNumbe);

        OSGiVersionSplittedInputSteps major(Integer pOSGiMajorVersion);

        OSGiVersionSplittedInputSteps micro(Integer pOSGiMicroVersion);

        OSGiVersionSplittedInputSteps minor(Integer pOSGiMinorVersion);

        OSGiVersionSplittedInputSteps qualifier(String pOSGiQualifierVersion);

        MavenVersionOutput toMaven();
    }

    public enum RightDelimiter {
        CLOSE(']'), OPEN(')');

        char character;

        RightDelimiter(char pCharacter) {
            character = pCharacter;
        }

        public static RightDelimiter parseEnum(String pRightDelimiterChar) {

            switch (pRightDelimiterChar) {
            case ")":
                return OPEN;
            case "]":
                return CLOSE;
            default:
                return null;
            }
        }

        public char getDelimiterChar() {
            return character;
        }
    }

    private static final String MAVEN_SNAPSHOT = "SNAPSHOT";

    private static final String OSGI_SNAPSHOT = "qualifier";

    private static final String RANGE_SEPARATOR = ",";

    private static final String SEPARATOR_Q = "-";

    public static final String VERSION_OUTPUT_FORMAT = "%s%s%s%s%s";

    public static final String VERSION_RANGE_OUTPUT_FORMAT = "%s%s%s%s%s";

    private static final String VERSION_SEPARATOR = ".";

    public static final String ZERO = "0.0.0";

    private VersionConverter() {

    }

    public static MavenVersionSplittedInputSteps fromMavenVersion() {
        return new MavenVersionInputBuilder();
    }

    public static MavenVersionInputSteps fromMavenVersion(
            String pMavenVersionString) {
        return new MavenVersionInputBuilder()
                .fromFullString(pMavenVersionString);
    }

    public static MavenVersionRangeSplittedInputSteps fromMavenVersionRange() {
        return new MavenVersionRangeInputBuilder(true);
    }

    public static MavenVersionInputSteps fromMavenVersionRange(
            String pMavenVersionRangeString) {
        return new MavenVersionRangeInputBuilder()
                .fromFullString(pMavenVersionRangeString);
    }

    public static OSGiVersionSplittedInputSteps fromOsgiVersion() {
        return new OSGiVersionInputBuilder();
    }

    public static OSGiVersionInputSteps fromOsgiVersion(
            String pOSGiVersionString) {
        return new OSGiVersionInputBuilder().fromFullString(pOSGiVersionString);
    }

    public static OSGiVersionRangeSplittedInputSteps fromOsgiVersionRange() {
        return new OSGiVersionRangeInputBuilder(true);
    }

    public static OSGiVersionInputSteps fromOsgiVersionRange(
            String pOSGiVersionRangeString) {
        return new OSGiVersionRangeInputBuilder()
                .fromFullString(pOSGiVersionRangeString);
    }

    public static boolean isMavenVersion(String pMavenVersionString) {
        return MavenVersionInputBuilder.isMavenVersion(pMavenVersionString);
    }

    public static boolean isOSGiVersion(String pOSGiVersionString) {
        return OSGiVersionInputBuilder.isOSGiVersion(pOSGiVersionString);
    }

}
