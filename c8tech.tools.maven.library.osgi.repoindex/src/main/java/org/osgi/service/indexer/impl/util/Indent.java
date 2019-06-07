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
package org.osgi.service.indexer.impl.util;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Represents an indent in a file
 */
public final class Indent {
    /** the platform specific EOL */
    private static String eol = String.format("%n");

    /** true when a newline must be printed before the indent */
    private final boolean newLine;

    /** the level of the indent */
    private final int level;

    /** the increment for the next indent */
    private final int increment;

    /** the indent string */
    private char[] indentAttribute;

    /** no indentAttribute */
    public static final Indent NONE = new Indent(false, 0, 0);

    /** default indent (2 spaces) */
    public static final Indent PRETTY = new Indent(true, 0, 2);

    /**
     * Constructor
     * 
     * @param newLine
     *            true when a newline must be printed before the indent
     * @param level
     *            the level of the indent
     * @param increment
     *            the increment for the next indent
     */
    private Indent(boolean newLine, int level, int increment) {
        this.newLine = newLine;
        this.level = level;
        this.increment = increment;
        this.indentAttribute = new char[level];
        Arrays.fill(this.indentAttribute, ' ');
    }

    /**
     * Print the indent
     * 
     * @param pw
     *            the writer to print to
     */
    public void print(PrintWriter pw) {
        if (newLine)
            pw.print(eol);
        pw.print(indentAttribute);
    }

    /**
     * @return the next indent when the increment is larger than zero, this
     *         indent otherwise
     */
    public Indent next() {
        return (increment <= 0) ? this
                : new Indent(newLine, level + increment, increment);
    }
}
