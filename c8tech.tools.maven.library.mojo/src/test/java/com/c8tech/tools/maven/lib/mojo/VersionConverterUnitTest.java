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
package com.c8tech.tools.maven.lib.mojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.VersionConverter;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.VersionConverter.LeftDelimiter;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.VersionConverter.RightDelimiter;

public class VersionConverterUnitTest {

    String mavenVersion = null;
    String osgiVersion = null;

    public VersionConverterUnitTest() {
    }

    @Test
    public void testSingleMavenVersionToOSGiVersion() {

        // With Major Only
        mavenVersion = "1";
        osgiVersion = "1.0.0";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        // With Major and Minor
        mavenVersion = "1.1";
        osgiVersion = "1.1.0";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        // With Major, Minor and Qualifier
        mavenVersion = "1.3-beta-01";
        osgiVersion = "1.3.0.beta-01";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        // Major, Minor and Incremental
        mavenVersion = "1.1.1";
        osgiVersion = "1.1.1";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        // With Major, Minor, Incremental and Qualifier
        mavenVersion = "1.1.1-SomeQualifier";
        osgiVersion = "1.1.1.SomeQualifier";

        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        // With Snapshot Qualifier
        mavenVersion = "1.1.1-SNAPSHOT";
        osgiVersion = "1.1.1.qualifier";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionString()).isEqualTo(osgiVersion);

        assertThat(VersionConverter.fromMavenVersion().major(1).minor(1)
                .incremental(1).qualifier("-SNAPSHOT").toOSGi()
                .getVersionString()).isEqualTo(osgiVersion);
    }

    @Test
    public void testSingleOSGiVersionToMavenVersion() {

        // With Major, Minor and Qualifier
        mavenVersion = "1.3.0-beta-01";
        osgiVersion = "1.3.0.beta-01";
        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getVersionString()).isEqualTo(mavenVersion);

        // Major, Minor and Incremental
        mavenVersion = "1.1.1";
        osgiVersion = "1.1.1";
        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getVersionString()).isEqualTo(mavenVersion);

        // With Major, Minor, Incremental and Qualifier
        mavenVersion = "1.1.1-SomeQualifier";
        osgiVersion = "1.1.1.SomeQualifier";

        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getVersionString()).isEqualTo(mavenVersion);

        // With Snapshot Qualifier
        mavenVersion = "1.1.1-SNAPSHOT";
        osgiVersion = "1.1.1.qualifier";
        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getVersionString()).isEqualTo(mavenVersion);

        assertThat(VersionConverter.fromOsgiVersion().major(1).minor(1).micro(1)
                .qualifier("qualifier").toMaven().getVersionString())
                        .isEqualTo(mavenVersion);
    }

    @Test
    public void testOSGiVersionRangeToMaven() {

        // With Snapshot Qualifier
        osgiVersion = "[0.1.1.20170424014114,0.1.1.20170424014114]";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion).toOSGi()
                .getVersionRangeString()).isEqualTo(osgiVersion);

        // With Snapshot Qualifier
        String mavenVersion1 = "1.1.1.qualifier";
        String mavenVersion2 = "2.0.0";
        osgiVersion = "(1.1.1-SNAPSHOT,2.0.0]";
        assertThat(VersionConverter.fromOsgiVersionRange()
                .leftDelimiter(LeftDelimiter.OPEN).leftVersion(mavenVersion1)
                .rightVersion(mavenVersion2)
                .rightDelimiter(RightDelimiter.CLOSE).toMaven()
                .getVersionRangeString()).isEqualTo(osgiVersion);
        
        // x <= 1.0
        mavenVersion = "(0.0.0,1.0.0]";
        osgiVersion = "(0.0.0,1.0.0]";
        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // 0.0.0 >= x < 1.0
        mavenVersion = "[0.0.0,1.0.0)";
        osgiVersion = "[0.0.0,1.0.0)";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // x < 1.0
        mavenVersion = "(0.0.0,1.0.0)";
        osgiVersion = "(0.0.0,1.0.0)";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // Exactly 1.0
        mavenVersion = "[1.0.0,1.0.0]";
        osgiVersion = "[1.0.0,1.0.0]";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // 1.2 <= x <= 1.3
        mavenVersion = "[1.2.0,1.3.0]";
        osgiVersion = "[1.2.0,1.3.0]";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // 1.0 <= x < 2.0
        mavenVersion = "[1.0.0,2.0.0)";
        osgiVersion = "[1.0.0,2.0.0)";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

        // x >= 1.5
        mavenVersion = "[1.5.0,1.5.0)";
        osgiVersion = "1.5.0";
        assertThat(VersionConverter.fromOsgiVersion(osgiVersion)
                .toMaven().getOpenVersionRangeString())
                        .isEqualTo(mavenVersion);

        // 1.0 < x < 2.0
        mavenVersion = "(1.0.0,2.0.0)";
        osgiVersion = "(1.0.0,2.0.0)";
        assertThat(VersionConverter.fromOsgiVersionRange(osgiVersion)
                .toMaven().getVersionRangeString()).isEqualTo(mavenVersion);

    }

    @Test
    public void testExceptionWhenNotInformedValidInputVersion() {
        assertThat(
                VersionConverter.fromOsgiVersion().toMaven().getVersionString())
                        .isEqualTo("0.0.0");

    }

    @Test
    public void testFromOpenToFixedRange() {
        assertThat(VersionConverter.fromOsgiVersionRange("[1.5.0,1.5.0)")
                .toOSGi().getFixedVersionRangeString())
                        .isEqualTo("[1.5.0,1.5.0]");

        assertThat(VersionConverter.fromOsgiVersionRange("1.5.0")
                .toOSGi().getFixedVersionRangeString())
                        .isEqualTo("[1.5.0,1.5.0]");

    }

    @Test
    public void testExceptionWhenNotInformedValidInputVersionRange() {

        assertThatThrownBy(() -> {
            VersionConverter.fromOsgiVersionRange().toMaven()
                    .getVersionRangeString();
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
                "The OSGi version range informed is not a valid one.");

        assertThatThrownBy(() -> {
            VersionConverter.fromMavenVersionRange().toOSGi()
                    .getVersionString();
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
                "The Maven version range informed is not a valid one.");
    }

    @Test
    public void testMavenVersionRangeToOSGi() {

        // With Snapshot Qualifier
        String mavenVersion1 = "1.1.1-SNAPSHOT";
        String mavenVersion2 = "2.0.0";
        osgiVersion = "(1.1.1.qualifier,2.0.0]";
        assertThat(VersionConverter.fromMavenVersionRange()
                .leftDelimiter(LeftDelimiter.OPEN).leftVersion(mavenVersion1)
                .rightVersion(mavenVersion2)
                .rightDelimiter(RightDelimiter.CLOSE).toOSGi()
                .getVersionRangeString()).isEqualTo(osgiVersion);

        // x <= 1.0
        mavenVersion = "(,1.0]";
        osgiVersion = "(0.0.0,1.0.0]";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // x <= 1.0
        mavenVersion = "[,1.0]";
        osgiVersion = "[0.0.0,1.0.0]";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // 0.0.0 >= x < 1.0
        mavenVersion = "[,1.0)";
        osgiVersion = "[0.0.0,1.0.0)";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // x < 1.0
        mavenVersion = "(,1.0)";
        osgiVersion = "(0.0.0,1.0.0)";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // Exactly 1.0
        mavenVersion = "[1.0]";
        osgiVersion = "[1.0.0,1.0.0]";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // 1.2 <= x <= 1.3
        mavenVersion = "[1.2,1.3]";
        osgiVersion = "[1.2.0,1.3.0]";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // 1.0 <= x < 2.0
        mavenVersion = "[1.0,2.0)";
        osgiVersion = "[1.0.0,2.0.0)";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // x >= 1.5
        mavenVersion = "[1.5,)";
        osgiVersion = "1.5.0";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

        // 1.0 < x < 2.0
        mavenVersion = "(1.0,2.0)";
        osgiVersion = "(1.0.0,2.0.0)";
        assertThat(VersionConverter.fromMavenVersion(mavenVersion)
                .toOSGi().getVersionRangeString()).isEqualTo(osgiVersion);

    }
}
