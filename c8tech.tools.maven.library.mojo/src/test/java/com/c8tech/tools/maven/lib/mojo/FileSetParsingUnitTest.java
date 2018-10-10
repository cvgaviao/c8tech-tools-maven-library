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
package com.c8tech.tools.maven.lib.mojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.FileSet;

public class FileSetParsingUnitTest {

    @Test
    public void testParseOneDirectory() {
        String filesetStr = "/dir/sub/sub";
        FileSet fileSet = new FileSet(filesetStr);
        assertEquals(filesetStr, fileSet.getDirectory().toString());

        assertEquals(
                "FileSet {directory: /dir/sub/sub, [includes: {}, excludes: {}]}",
                fileSet.toString());
    }

    @Test
    public void testParseDirectoryPlusIncludes() {
        String filesetStr1 = "/dir1/sub1:*.{esa,jar};*.txt";
        FileSet fileSet = new FileSet(filesetStr1);
        assertEquals("/dir1/sub1", fileSet.getDirectory().toString());
        assertThat(fileSet.getIncludes()).contains("*.{esa,jar}", "*.txt");
        assertEquals(
                "FileSet {directory: /dir1/sub1, [includes: {*.{esa,jar}, *.txt}, excludes: {}]}",
                fileSet.toString());
    }

    @Test
    public void testParseDirectoryPlusIncludesAndExcludes() {
        String filesetStr = "/dir/sub/sub:*.{esa,jar};*.war:*.txt";
        FileSet fileSet = new FileSet(filesetStr);
        fileSet.addExclude("*.html");
        fileSet.addInclude("*.bash");
        assertEquals("/dir/sub/sub", fileSet.getDirectory().toString());
        assertThat(fileSet.getIncludes()).contains("*.{esa,jar}", "*.war", "*.bash");
        assertThat(fileSet.getExcludes()).contains("*.txt", "*.html");
        assertEquals(
                "FileSet {directory: /dir/sub/sub, [includes: {*.{esa,jar}, *.war, *.bash}, excludes: {*.txt, *.html}]}",
                fileSet.toString());
    }

    @Test
    public void testParseMultipleFileSets() {
        String filesetStr1 = "/dir1/sub1:*.jar:*.txt";
        String filesetStr2 = "/dir2/sub2:*.esa:*.war";
        List<FileSet> fileSetCollection = FileSet
                .createCollectionFromString(filesetStr1 + "," + filesetStr2);

        assertEquals(fileSetCollection.size(), 2);

        FileSet fileSet1 = fileSetCollection.get(0);
        assertThat(fileSet1.getDirectory().toString()).isEqualTo("/dir1/sub1");
        assertThat(fileSet1.getIncludes()).contains("*.jar");
        assertThat(fileSet1.getExcludes()).contains("*.txt");

        FileSet fileSet2 = fileSetCollection.get(1);
        assertThat(fileSet2.getDirectory().toString()).isEqualTo("/dir2/sub2");
        assertThat(fileSet2.getIncludes()).contains("*.esa");
        assertThat(fileSet2.getExcludes()).contains("*.war");
    }

}
