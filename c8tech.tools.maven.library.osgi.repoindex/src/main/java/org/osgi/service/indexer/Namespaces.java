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
package org.osgi.service.indexer;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
/**
 * Predefined namespaces
 */
public final class Namespaces {
    /** Root of the OSGi namespaces */
    public static final String NS_OSGI = "http://www.osgi.org/xmlns";

    /** Basic namespaces */
    public static final String NS_IDENTITY = "osgi.identity";
    public static final String NS_CONTENT = "osgi.content";

    /** Wiring namespaces */
    public static final String NS_WIRING_PACKAGE = "osgi.wiring.package";
    public static final String NS_WIRING_BUNDLE = "osgi.wiring.bundle";
    public static final String NS_WIRING_HOST = "osgi.wiring.host";
    public static final String NS_EE = "osgi.ee";

    /** Non-core namespaces */
    public static final String NS_EXTENDER = "osgi.extender";
    public static final String NS_SERVICE = "osgi.service";
    public static final String NS_CONTRACT = "osgi.contract";
    public static final String NS_NATIVE = "osgi.native";

    /** Generic attributes */
    public static final String ATTR_VERSION = "version";

    /** Identity attributes */
    public static final String ATTR_IDENTITY_TYPE = "type";

    /** Resource types */
    public static final String RESOURCE_TYPE_BUNDLE = "osgi.bundle";
    public static final String RESOURCE_TYPE_FRAGMENT = "osgi.fragment";
    public static final String RESOURCE_TYPE_PLAIN_JAR = "jarfile";

    /** Content attributes */
    public static final String ATTR_CONTENT_URL = "url";
    public static final String ATTR_CONTENT_SIZE = "size";
    public static final String ATTR_CONTENT_MIME = "mime";

    /** Package export attributes */
    public static final String ATTR_BUNDLE_SYMBOLIC_NAME = "bundle-symbolic-name";
    public static final String ATTR_BUNDLE_VERSION = "bundle-version";

    /** Native Attributes */
    public static final String ATTR_NATIVE_OSNAME = NS_NATIVE + ".osname";
    public static final String ATTR_NATIVE_OSVERSION = NS_NATIVE + ".osversion";
    public static final String ATTR_NATIVE_PROCESSOR = NS_NATIVE + ".processor";
    public static final String ATTR_NATIVE_LANGUAGE = NS_NATIVE + ".language";

    /** Common directives */
    public static final String DIRECTIVE_SINGLETON = "singleton";
    public static final String DIRECTIVE_FILTER = "filter";
    public static final String DIRECTIVE_EFFECTIVE = "effective";
    public static final String DIRECTIVE_MANDATORY = "mandatory";
    public static final String DIRECTIVE_USES = "uses";
    public static final String DIRECTIVE_RESOLUTION = "resolution";
    public static final String DIRECTIVE_CARDINALITY = "cardinality";

    public static final String RESOLUTION_OPTIONAL = "optional";
    public static final String CARDINALITY_MULTIPLE = "multiple";

    public static final String EFFECTIVE_RESOLVE = "resolve";
    public static final String EFFECTIVE_ACTIVE = "active";

    /** Known contracts and extenders */
    public static final String CONTRACT_OSGI_FRAMEWORK = "OSGiFramework";
    public static final String EXTENDER_SCR = "osgi.ds";
    public static final String EXTENDER_BLUEPRINT = "osgi.blueprint";

    private Namespaces() {
    }
}
