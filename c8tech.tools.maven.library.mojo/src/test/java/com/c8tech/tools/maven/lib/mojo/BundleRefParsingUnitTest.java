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

import java.net.MalformedURLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.BundleRef;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.P2ArtifactSet;

public class BundleRefParsingUnitTest {

    @Test
    public void testP2ArtifactSetWithNullRepositoryURL()
            throws MojoExecutionException {

        Assertions.assertThrows(Exception.class, () -> {
            P2ArtifactSet p2ArtifactSet = new P2ArtifactSet();
            BundleRef bundleRef1 = new BundleRef("group:artifact:1.0.0");
            p2ArtifactSet.addArtifact(bundleRef1);
        });
    }

    @Test
    public void testValidP2ArtifactSet()
            throws MalformedURLException, MojoExecutionException {

        P2ArtifactSet p2ArtifactSet = new P2ArtifactSet();
        p2ArtifactSet.setRepositoryURL("http://test.org");
        BundleRef bundleRef1 = new BundleRef("group:artifact:1.0.0");
        p2ArtifactSet.addArtifact(bundleRef1);
    }

    @Test
    public void testParseFullGav() {
        String bundleGavStr = "group1:artifact1:jar:1.0@2";
        BundleRef bundleRef = new BundleRef(bundleGavStr);
        assertThat(bundleRef.getGroupId()).isEqualTo("group1");
        assertThat(bundleRef.getArtifactId()).isEqualTo("artifact1");
        assertThat(bundleRef.getVersion()).isEqualTo("1.0");
        assertThat(bundleRef.getType()).isEqualTo("jar");
        assertThat(bundleRef.getStartLevel()).isEqualTo(2);
    }

    @Test
    public void testParseCommonShortGav() {
        String bundleGavStr = "group1:artifact1:1.0";
        BundleRef bundleRef = new BundleRef(bundleGavStr);
        assertThat(bundleRef.getGroupId()).isEqualTo("group1");
        assertThat(bundleRef.getArtifactId()).isEqualTo("artifact1");
        assertThat(bundleRef.getVersion()).isEqualTo("1.0");
        assertThat(bundleRef.getType()).isEqualTo("jar");
        assertThat(bundleRef.getStartLevel()).isEqualTo(0);
    }

    @Test
    public void testParseGavWithType() {
        String bundleGavStr = "group1:artifact1:esa:1.0";
        BundleRef bundleRef = new BundleRef(bundleGavStr);
        assertThat(bundleRef.getGroupId()).isEqualTo("group1");
        assertThat(bundleRef.getArtifactId()).isEqualTo("artifact1");
        assertThat(bundleRef.getVersion()).isEqualTo("1.0");
        assertThat(bundleRef.getType()).isEqualTo("esa");
        assertThat(bundleRef.getStartLevel()).isEqualTo(0);
    }

    @Test
    public void testParseGavWithTypeNoVersion() {
        String bundleGavStr = "group1:artifact1:esa";
        BundleRef bundleRef = new BundleRef(bundleGavStr);
        assertThat(bundleRef.getGroupId()).isEqualTo("group1");
        assertThat(bundleRef.getArtifactId()).isEqualTo("artifact1");
        assertThat(bundleRef.getVersion()).isNull();
        assertThat(bundleRef.getType()).isEqualTo("esa");
        assertThat(bundleRef.getStartLevel()).isEqualTo(0);
    }

    @Test
    public void testParseShortGavWithoutGroup() {
        String bundleGavStr = "artifact1:1.0.0";
        BundleRef bundleRef = new BundleRef(bundleGavStr);
        assertThat(bundleRef.getGroupId()).isNull();
        assertThat(bundleRef.getArtifactId()).isEqualTo("artifact1");
        assertThat(bundleRef.getVersion()).isEqualTo("1.0.0");
        assertThat(bundleRef.getType()).isEqualTo("jar");
        assertThat(bundleRef.getStartLevel()).isEqualTo(0);
    }
}
