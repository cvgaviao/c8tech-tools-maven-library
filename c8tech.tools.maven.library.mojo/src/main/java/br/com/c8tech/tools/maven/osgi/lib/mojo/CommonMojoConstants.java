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
package br.com.c8tech.tools.maven.osgi.lib.mojo;

import java.text.MessageFormat;

public class CommonMojoConstants {

    public static final String CACHED_FILE_PATTERN_CLASSIFIER = "%c";
    public static final String CACHED_FILE_PATTERN_DEFAULT_FINALNAME = "%n-%c-%v.%e";
    public static final String CACHED_FILE_PATTERN_EXTENSION = "%e";
    public static final String CACHED_FILE_PATTERN_NAME = "%n";
    public static final String CACHED_FILE_PATTERN_SYMBOLIC_NAME = "%s";
    public static final String CACHED_FILE_PATTERN_VERSION = "%v";

    public static final String DEFAULT_CACHE_DIR_NAME = "cache";

    public static final String DEFAULT_INCREMENTAL_BUILD_DIR_NAME = "incremental";

    public static final int DEFAULT_TIMEOUT = 0;

    public static final String EMPTY_VALUE = "empty";

    public static final String EMPTY_VERSION = "0.0.0";

    public static final String JAR_EXTENSION = "jar";

    public static final String JAR_MANIFEST_FILE_NAME = "MANIFEST.MF";

    public static final String JAR_MANIFEST_FOLDER = "META-INF/";

    public static final String JAR_MANIFEST_LOCATION = JAR_MANIFEST_FOLDER
            + JAR_MANIFEST_FILE_NAME;

    public static final String LANGUAGE_JAVA = "java";

    public static final String MAVEN_POM = "pom.xml";

    public static final String MAVEN_TARGET_CLASSES_FOLDER = "target/classes";

    /**
     * MIME type to be stored in the extra field of a {@code ZipEntry} object
     * for an installable bundle file. Zip entries of this type will be
     * installed in the framework, but not started. The entry will also not be
     * put into the information dictionary.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String MIME_BUNDLE = "application/vnd.osgi.bundle";

    /**
     * MIME type to be stored in the extra field of a ZipEntry for a String that
     * represents a URL for a bundle. Zip entries of this type will be used to
     * install (but not start) a bundle from the URL. The entry will not be put
     * into the information dictionary.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String MIME_BUNDLE_URL = "text/x-osgi-bundle-url";

    /**
     * MIME type to be stored stored in the extra field of a {@code ZipEntry}
     * object for {@code byte[]} data.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String MIME_BYTE_ARRAY = "application/octet-stream";

    /**
     * MIME type to be stored in the extra field of a {@code ZipEntry} object
     * for String data.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String MIME_STRING = "text/plain;charset=utf-8";

    public static final String MIME_TYPE_ENTRY_NAME = "mimetype";

    public static final MessageFormat MSG_CHOICE_ARTIFACT = new MessageFormat(
            "{0} " + "{0,choice,0#artifacts|1#artifact|1<artifacts}");

    /**
     * Default bundle artifact extension.
     */
    public static final String OSGI_BUNDLE_EXTENSION = "jar";

    public static final String OSGI_BUNDLE_HEADER_DESCRIPTION = "Bundle-Description";

    public static final String OSGI_BUNDLE_HEADER_FRAGMENT_HOST = "Fragment-Host";

    public static final String OSGI_BUNDLE_HEADER_LOCALIZATION = "Bundle-Localization";

    public static final String OSGI_BUNDLE_HEADER_NAME = "Bundle-Name";

    public static final String OSGI_BUNDLE_HEADER_SN = "Bundle-SymbolicName";

    public static final String OSGI_BUNDLE_HEADER_VERSION = "Bundle-Version";

    public static final String OSGI_BUNDLES_DIRECTORY = "plugins";

    public static final String OSGI_BUNDLES_TYPE = "osgi.bundle";

    public static final String OSGI_CONTAINER_ARCHIVE_EXTENSION = "tar.gz";

    public static final String OSGI_CONTAINER_PACKAGING = "osgi.container";

    public static final String OSGI_DP_DIRECTORY = "dps";

    public static final String OSGI_DP_EXTENSION = "dp";

    public static final String OSGI_DP_MANIFEST_CONTACTADDRESS = "DeploymentPackage-ContactAddress";

    public static final String OSGI_DP_MANIFEST_COPYRIGHT = "DeploymentPackage-Copyright";

    public static final String OSGI_DP_MANIFEST_DESCRIPTION = "DeploymentPackage-Description";

    public static final String OSGI_DP_MANIFEST_DOC_URL = "DeploymentPackage-DocURL";

    public static final String OSGI_DP_MANIFEST_FILE_NAME = JAR_MANIFEST_FILE_NAME;

    public static final String OSGI_DP_MANIFEST_FIXPACK = "DeploymentPackage-FixPack";

    public static final String OSGI_DP_MANIFEST_FOLDER = JAR_MANIFEST_FOLDER;

    public static final String OSGI_DP_MANIFEST_ICON = "DeploymentPackage-Icon";

    public static final String OSGI_DP_MANIFEST_LICENSE = "DeploymentPackage-License";

    public static final String OSGI_DP_MANIFEST_LOCATION = JAR_MANIFEST_LOCATION;

    public static final String OSGI_DP_MANIFEST_NAME = "DeploymentPackage-Name";

    public static final String OSGI_DP_MANIFEST_REQUIRED_STORAGE = "DeploymentPackage-RequiredStorage";

    public static final String OSGI_DP_MANIFEST_SECTION_CUSTOMIZER = "DeploymentPackage-Customizer";

    public static final String OSGI_DP_MANIFEST_SECTION_ENTRY_NAME = "entryName";

    public static final String OSGI_DP_MANIFEST_SECTION_MISSING = "DeploymentPackage-Missing";

    public static final String OSGI_DP_MANIFEST_SECTION_RESOURCE_PROCESSOR = "Resource-Processor";

    public static final String OSGI_DP_MANIFEST_SYMBOLIC_NAME = "DeploymentPackage-SymbolicName";

    public static final String OSGI_DP_MANIFEST_VENDOR = "DeploymentPackage-Vendor";

    public static final String OSGI_DP_MANIFEST_VERSION = "DeploymentPackage-Version";

    public static final String OSGI_DP_PACKAGING = "osgi.dp";

    public static final String OSGI_FRAGMENT_TYPE = "osgi.fragment";

    public static final String OSGI_IPZIP_DIRECTORY = "ipzips";

    public static final String OSGI_IPZIP_EXTENSION = "zip";

    public static final String OSGI_IPZIP_HEADER_IP_DESCRIPTION = "InitialProvisioning-Description";

    public static final String OSGI_IPZIP_HEADER_IP_ENTRIES = "InitialProvisioning-Entries";

    public static final String OSGI_IPZIP_HEADER_IP_ENTRIES_ATTR_START_LEVEL = "startLevel";

    public static final String OSGI_IPZIP_HEADER_IP_ENTRIES_ATTR_TYPE = "type";

    public static final String OSGI_IPZIP_HEADER_IP_NAME = "InitialProvisioning-Name";

    public static final String OSGI_IPZIP_HEADER_IP_SYMBOLIC_NAME = "InitialProvisioning-SymbolicName";

    public static final String OSGI_IPZIP_HEADER_IP_VERSION = "InitialProvisioning-Version";

    public static final String OSGI_IPZIP_PACKAGING = "osgi.ipzip";

    public static final String OSGI_IPZIP_REFERENCE_EXTENSION = ".url";

    /**
     * The key to the provisioning information that contains the initial
     * configuration information of the initial Management Agent. The value will
     * be of type byte[].
     *
     */
    public static final String OSGI_PROVISIONING_AGENT_CONFIG = "provisioning.agent.config";

    /**
     * The key to the provisioning information that contains the location of the
     * provision data provider. The value must be of type {@code String}.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String OSGI_PROVISIONING_REFERENCE = "provisioning.reference";

    /**
     * The key to the provisioning information that contains the location of the
     * bundle to start with {@code AllPermission}. The bundle must have be
     * previously installed for this entry to have any effect.
     * <p>
     * Copied from org.osgi.service.provisioning.ProvisioningService
     */
    public static final String OSGI_PROVISIONING_START_BUNDLE = "provisioning.start.bundle";

    public static final String OSGI_REPO_COMPRESSED_XML_GZ = ".gz";

    public static final String OSGI_REPOSITORIES_DIRECTORY = "repository";

    public static final String OSGI_REPOSITORY_ARCHIVE_EXTENSION = "zip";

    public static final String OSGI_REPOSITORY_PACKAGING = "osgi.repository";

    public static final String OSGI_SUBSYSTEM_ARCHIVER = "osgi.subsystem";

    public static final String OSGI_SUBSYSTEM_DIRECTORY = "subsystems";

    /**
     * Default subsystem artifact extension.
     */
    public static final String OSGI_SUBSYSTEM_EXTENSION = "esa";

    public static final String OSGI_SUBSYSTEM_MANIFEST_FOLDER = "OSGI-INF/";

    public static final String OSGI_SUBSYSTEM_MANIFEST_LOCATION = OSGI_SUBSYSTEM_MANIFEST_FOLDER
            + CommonMojoConstants.OSGI_SUBSYSTEM_MANIFEST_XML_NAME;

    public static final String OSGI_SUBSYSTEM_MANIFEST_XML_NAME = "SUBSYSTEM.MF";

    public static final String OSGI_SUBSYSTEM_MIME_TYPE = "application/vnd.osgi.subsystem";

    public static final String OSGI_SUBSYSTEM_PACKAGING_APPLICATION = "osgi.subsystem.application";

    public static final String OSGI_SUBSYSTEM_PACKAGING_COMPOSITE = "osgi.subsystem.composite";

    public static final String OSGI_SUBSYSTEM_PACKAGING_FEATURE = "osgi.subsystem.feature";

    public static final String OSGI_SUBSYSTEM_SN = "Subsystem-SymbolicName";

    public static final String OSGI_SUBSYSTEM_TYPE = "Subsystem-Type";

    public static final String OSGI_SUBSYSTEM_VERSION = "Subsystem-Version";

    public static final String SCHEMA_SYNTAX_FILE = "file:<bundle-symbolic-name>['/'<bundle-version>]";

    public static final String SCHEMA_SYNTAX_HTTP = "http:Host[:Port]/[Path][#AnchorName][?Query]";

    public static final String SCHEMA_SYNTAX_HTTPS = "https:Host[:Port]/[Path][#AnchorName][?Query]";

    public static final String SCHEMA_SYNTAX_MVN = "mvn:[repositoryUrl!]groupId/artifactId[/[version][/[packaging][/[classifier]]]]";

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    public static final String SCHEMA_SYNTAX_OBR = "obr:<bundle-symbolic-name>['/'<bundle-version>]";

    public static final String SCHEMA_SYNTAX_RSH = "rsh:Host[:Port]/[Path][#AnchorName][?Query]";

    public static final String URL_SCHEME_ASSEMBLY = "assembly://";

    public static final String URL_SCHEME_FILE = "file://";

    public static final String URL_SCHEME_HTTP = "http://";

    public static final String URL_SCHEME_HTTPS = "https://";

    public static final String URL_SCHEME_MVN = "mvn://";
    
    public static final String URL_SCHEME_OBR = "obr://";
    
    public static final String URL_SCHEME_RSH = "rsh://";

    private CommonMojoConstants() {
    }

}
