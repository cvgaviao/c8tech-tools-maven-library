/**
 * ==========================================================================
 * Copyright © 2015-2018 OSGi Alliance, Cristiano Gavião.
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
package org.osgi.service.indexer.impl.util;

public class Hex {

    private Hex() {
    }
    
    private static final char[] HEX_VALUES = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        append(sb, data);
        return sb.toString();
    }

    public static final void append(StringBuilder sb, byte[] data) {
        for (int i = 0; i < data.length; i++) {
            sb.append(nibble(data[i] >> 4));
            sb.append(nibble(data[i]));
        }
    }

    private static final char nibble(int i) {
        return HEX_VALUES[i & 0xF];
    }
}
