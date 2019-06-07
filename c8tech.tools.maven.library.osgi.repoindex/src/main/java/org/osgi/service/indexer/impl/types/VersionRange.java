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
package org.osgi.service.indexer.impl.types;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Version;

public class VersionRange implements Comparable<VersionRange> {

    final Version high;
    final Version low;

    char start = '[';
    char end = ']';

    static final String V = "[0-9]+(\\.[0-9]+(\\.[0-9]+(\\.[a-zA-Z0-9_-]+)?)?)?";
    static final Pattern RANGE = Pattern.compile(
            "(\\(|\\[)\\s*(" + V + ")\\s*,\\s*(" + V + ")\\s*(\\)|\\])");

    public VersionRange(boolean lowInclusive, Version low, Version high,
            boolean highInclusive) {
        if (low.compareTo(high) > 0)
            throw new IllegalArgumentException(
                    "Low Range is higher than High Range: " + low + "-" + high);

        this.low = low;
        this.high = high;
        this.start = lowInclusive ? '[' : '(';
        this.end = highInclusive ? ']' : ')';
    }

    public VersionRange(String string) {
        String lstring = string.trim();
        Matcher m = RANGE.matcher(lstring);
        if (m.matches()) {
            start = m.group(1).charAt(0);
            low = new Version(m.group(2));
            high = new Version(m.group(6));
            end = m.group(10).charAt(0);
            if (low.compareTo(high) > 0)
                throw new IllegalArgumentException(
                        "Low Range is higher than High Range: " + low + "-"
                                + high);

        } else {
            start = '[';
            high = low = new Version(lstring);
            end = ']';
        }
    }

    public boolean isRange() {
        return high != low;
    }

    public boolean includeLow() {
        return start == '[';
    }

    public boolean includeHigh() {
        return end == ']';
    }

    @Override
    public String toString() {
        if (high == low)
            return high.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(start);
        sb.append(low);
        sb.append(',');
        sb.append(high);
        sb.append(end);
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (this.getClass() != other.getClass())
            return false;
        return compareTo((VersionRange) other) == 0;
    }

    @Override
    public int hashCode() {
        return low.hashCode() * high.hashCode();
    }

    @Override
    public int compareTo(VersionRange range) {
        VersionRange a = this;
        VersionRange b = range;
        if (range.isRange()) {
            a = range;
            b = this;
        } else {
            if (!isRange())
                return low.compareTo(range.high);
        }
        int l = a.low.compareTo(b.low);
        boolean ll;
        if (a.includeLow())
            ll = l <= 0;
        else
            ll = l < 0;

        if (!ll)
            return -1;

        int h = a.high.compareTo(b.high);
        if (a.includeHigh())
            ll = h >= 0;
        else
            ll = h > 0;

        if (ll)
            return 0;
        return 1;
    }

    public Version getHigh() {
        return high;
    }

    public Version getLow() {
        return low;
    }

    public boolean match(Version version) {
        int lowmatch = version.compareTo(low);
        if (lowmatch < 0)
            return false;
        if (lowmatch == 0 && !includeLow())
            return false;

        int highmatch = version.compareTo(high);
        if (highmatch > 0)
            return false;
        return !(highmatch == 0 && !includeHigh());

    }
}
