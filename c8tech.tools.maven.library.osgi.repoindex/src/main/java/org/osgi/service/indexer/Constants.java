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

public class Constants {

    /**
     * Name of the configuration variable for the location where <b>bundles</b>
     * which will be embedded with the repository index file are stored.
     * <p>
     * The copy process will happen after the creation of the index, so the
     * value hold by this variable will be used to calculate the resource
     * relative location.
     */
    public static final String BUNDLES_COPY_DIR = "bundles.dir";
    /**
     * Name of the configuration variable to enable compression: gzipped XML
     */
    public static final String COMPRESSED = "compressed";
    /**
     * The service property used to declare a resource filter, so that the
     * analyzer is only invoked on a subset of resources. Example:
     * {@code (&(|(name=foo.jar)(name=*.ear))(lastmodified>=1262217600753))}
     */
    public static final String FILTER = "filter";
    /**
     * Name of the configuration variable to enable the use of absolute paths to
     * point to indexed contents.
     */
    public static final String FORCE_ABSOLUTE_PATH = "force.absolute.path";
    /**
     * Name of the configuration variable to enable the use of absolute paths to
     * point to indexed contents.
     */
    public static final String FORCE_BASE_URL = "force.base.url";
    /**
     * Name of the configuration variable for the index file name.
     */
    public static final String INDEX_FILE_NAME = "index.file.name";
    /** the name of the lastmodified attribute */
    public static final String LAST_MODIFIED = "lastmodified";
    /**
     * Name of the configuration variable for the license URL of the repository
     */
    public static final String LICENSE_URL = "license.url";
    /** the name of the location attribute */
    public static final String LOCATION = "location";
    /** the name of the name attribute */
    public static final String NAME = "name";
    /**
     * Name of the configuration variable to enable pretty-printing: indented
     * XML
     */
    public static final String PRETTY = "pretty";
    /** Name of the configuration variable for the repository name */
    public static final String REPOSITORY_NAME = "repository.name";
    /** the default repository name */
    public static final String REPOSITORYNAME_DEFAULT = "Untitled";
    /**
     * Name of the configuration variable for the root path of the repository.
     * <p>
     * The value hold by this variable will be used to calculate the resource
     * relative location.
     */
    public static final String ROOT_DIR = "root.dir";
    /** the name of the size attribute */
    public static final String SIZE = "size";
    /**
     * Name of the configuration variable for the stylesheet of the XML
     * representation
     */
    public static final String STYLESHEET = "stylesheet";
    /** the default stylesheet for the XML representation */
    public static final String STYLESHEET_DEFAULT = "http://www.osgi.org/www/obr2html.xsl";
    /**
     * Name of the configuration variable for the location where
     * <b>subsystems</b> which will be embedded with the repository index file
     * are stored.
     * <p>
     * The copy process will happen after the creation of the index, so the
     * value hold by this variable will be used to calculate the resource
     * relative location.
     */
    public static final String SUBSYSTEMS_COPY_DIR = "subsystems.dir";
    /**
     * Name of the configuration variable for the template for the URLs in the
     * XML representation. A template can contain the following symbols:
     * <ul>
     * <li>%s is the symbolic name</li>
     * <li>%v is the version number</li>
     * <li>%f is the filename</li>
     * <li>%p is the directory path</li>
     * </ul>
     */
    public static final String URL_TEMPLATE = "url.template";
    /** Name of the configuration variable for the verbose mode */
    public static final String VERBOSE = "verbose";

    private Constants() {
    }

}
