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
import org.osgi.service.indexer.impl.Schema;
import org.osgi.service.indexer.impl.util.Tag;

public class TypedAttribute {

    private final String name;
    private final Type type;
    private final Object value;

    public TypedAttribute(String name, Type type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public static TypedAttribute create(String name, Object value) {
        return new TypedAttribute(name, Type.typeOf(value), value);
    }

    public Tag toXML() {
        Tag tag = new Tag(Schema.ELEM_ATTRIBUTE);
        tag.addAttribute(Schema.ATTR_NAME, name);

        if (type.isList() || type.getType() != ScalarType.STRING) {
            tag.addAttribute(Schema.ATTR_TYPE, type.toString());
        }

        tag.addAttribute(Schema.ATTR_VALUE, type.convertToString(value));

        return tag;
    }
}
