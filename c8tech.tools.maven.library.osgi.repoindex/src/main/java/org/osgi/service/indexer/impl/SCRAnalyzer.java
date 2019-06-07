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
package org.osgi.service.indexer.impl;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.indexer.AnalyzerException;
import org.osgi.service.indexer.Builder;
import org.osgi.service.indexer.Namespaces;
import org.osgi.service.indexer.Resource;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.impl.types.VersionKey;
import org.osgi.service.indexer.impl.types.VersionRange;
import org.osgi.service.log.LogService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SCRAnalyzer implements ResourceAnalyzer {
    public static final String NS_1_0 = Namespaces.NS_OSGI + "/scr/v1.0.0";
    public static final String NS_1_1 = Namespaces.NS_OSGI + "/scr/v1.1.0";
    public static final String NS_1_2 = Namespaces.NS_OSGI + "/scr/v1.2.0";
    public static final String NS_1_3 = Namespaces.NS_OSGI + "/scr/v1.3.0";
    public static final String NS_1_4 = Namespaces.NS_OSGI + "/scr/v1.4.0";

    public static final String ELEMENT_COMPONENT = "component";
    public static final String ELEMENT_SERVICE = "service";
    public static final String ELEMENT_PROVIDE = "provide";
    public static final String ELEMENT_REFERENCE = "reference";
    public static final String ELEMENT_PROPERTY = "property";

    public static final String ATTRIB_INTERFACE = "interface";
    public static final String ATTRIB_CARDINALITY = "cardinality";
    public static final String ATTRIB_NAME = "name";
    public static final String ATTRIB_TYPE = "type";
    public static final String ATTRIB_VALUE = "value";
    public static final String ATTRIB_TARGET = "target";

    private static final String LONG_STR = Long.class.getSimpleName();
    private static final String INTEGER_STR = Integer.class.getSimpleName();
    private static final String SHORT_STR = Short.class.getSimpleName();
    private static final String BYTE_STR = Byte.class.getSimpleName();
    private static final String DOUBLE_STR = Double.class.getSimpleName();
    private static final String FLOAT_STR = Float.class.getSimpleName();

    private final LogService log;
    
    private final boolean verbose;


    public SCRAnalyzer(LogService log, boolean pVerbose) {
        this.log = log;
        this.verbose = pVerbose;
    }

    @Override
    public void analyzeResource(Resource resource, List<Capability> caps,
            List<Requirement> reqs) throws AnalyzerException {
        String header;
        try {
            header = resource.getManifest().getMainAttributes()
                    .getValue(ComponentConstants.SERVICE_COMPONENT);
            if (header == null)
                return;

            StringTokenizer tokenizer = new StringTokenizer(header, ",");
            Version highest = null;
            while (tokenizer.hasMoreTokens()) {
                String pattern = tokenizer.nextToken().trim();
                List<String> paths = Util.findMatchingPaths(resource, pattern);
                if (paths == null) {
                    continue;
                }
                for (String path : paths) {
                    Version version = processScrXml(resource, path, caps, reqs);
                    if (highest == null || (version != null
                            && version.compareTo(highest) > 0))
                        highest = version;
                }
            }

            if (highest != null) {
                Version lower = new Version(highest.getMajor(),
                        highest.getMinor(), 0);
                Version upper = new Version(highest.getMajor() + 1, 0, 0);
                Requirement requirement = createRequirement(
                        new VersionRange(true, lower, upper, false));
                reqs.add(requirement);
            }
        } catch (IOException e) {
            throw new AnalyzerException("", e);
        }
    }

    private Version processScrXml(Resource resource, String path,
            List<Capability> caps, List<Requirement> reqs) throws IOException {
        Resource childResource = resource.getChild(path);
        if (childResource == null) {
            if (log != null && verbose)
                log.log(LogService.LOG_WARNING, MessageFormat.format(
                        "Cannot analyse SCR requirement version: resource {0} does not contain path {1} referred from Service-Component header.",
                        resource.getLocation(), path));
            return null;
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        try {
            SAXParser parser = spf.newSAXParser();
            SCRContentHandler handler = new SCRContentHandler(caps, reqs);
            parser.parse(childResource.getStream(), handler);

            return handler.highest;
        } catch (Exception e) {
            if (log != null)
                log.log(LogService.LOG_ERROR, MessageFormat.format(
                        "Processing error: failed to parse child resource {0} in resource {1}.",
                        path, resource.getLocation()), e);
            return null;
        }
    }

    private static Requirement createRequirement(VersionRange range) {
        Builder builder = new Builder().setNamespace(Namespaces.NS_EXTENDER);

        StringBuilder filter = new StringBuilder();
        filter.append('(').append(Namespaces.NS_EXTENDER).append('=')
                .append(Namespaces.EXTENDER_SCR).append(')');

        filter.insert(0, "(&");
        Util.addVersionFilter(filter, range, VersionKey.PACKAGEVERSION);
        filter.append(')');

        builder.addDirective(Namespaces.DIRECTIVE_FILTER, filter.toString())
                .addDirective(Namespaces.DIRECTIVE_EFFECTIVE,
                        Namespaces.EFFECTIVE_ACTIVE);
        return builder.buildRequirement();
    }

    private static class SCRContentHandler extends DefaultHandler {
        private List<Capability> caps;
        private List<Requirement> reqs;

        private Version highest = null;

        private List<String> provides = null;
        private List<Requirement> references = null;

        private Map<String, Object> properties = null;
        private String currentPropertyName = null;
        private String currentPropertyType = null;
        private String currentPropertyAttrib = null;
        private StringBuilder currentPropertyText = null;

        public SCRContentHandler(List<Capability> caps,
                List<Requirement> reqs) {
            super();
            this.caps = caps;
            this.reqs = reqs;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attribs) throws SAXException {
            super.startElement(uri, localName, qName, attribs);

            String localNameLowerCase = localName.toLowerCase();
            if (ELEMENT_COMPONENT.equals(localNameLowerCase)) {
                provides = new LinkedList<>();
                properties = new LinkedHashMap<>();
                references = new LinkedList<>();

                switch (uri) {
                case NS_1_4:
                    setVersion(new Version(1, 4, 0));
                    break;
                case NS_1_3:
                    setVersion(new Version(1, 3, 0));
                    break;
                case NS_1_2:
                    setVersion(new Version(1, 2, 0));
                    break;
                case NS_1_1:
                    setVersion(new Version(1, 1, 0));
                    break;
                case NS_1_0:
                    setVersion(new Version(1, 0, 0));
                    break;

                default:
                    setVersion(new Version(1, 0, 0));
                    break;
                }

            } else
                if (ELEMENT_PROVIDE.equals(localNameLowerCase)) {
                    String objectClass = attribs.getValue(ATTRIB_INTERFACE);
                    provides.add(objectClass);
                }

                else
                    if (ELEMENT_PROPERTY.equals(localNameLowerCase)) {
                        currentPropertyName = attribs.getValue(ATTRIB_NAME);
                        if (currentPropertyName == null)
                            throw new SAXException(
                                    "Missing required attribute '" + ATTRIB_NAME
                                            + "'.");
                        currentPropertyType = attribs.getValue(ATTRIB_TYPE);
                        if (currentPropertyType == null)
                            currentPropertyType = String.class.getSimpleName();
                        String value = attribs.getValue(ATTRIB_VALUE);
                        if (value != null) {
                            currentPropertyAttrib = value;
                        } else {
                            currentPropertyText = new StringBuilder();
                        }
                    }

                    else
                        if (ELEMENT_REFERENCE.equals(localNameLowerCase)) {
                            references.add(createServiceRequirement(attribs));
                        }
        }

        @Override
        public void characters(char[] chars, int start, int length)
                throws SAXException {
            if (currentPropertyName != null && currentPropertyText != null)
                currentPropertyText.append(chars, start, length);
        }

        private void handleProperty() {

            if (currentPropertyAttrib != null) {
                Object value = readTyped(currentPropertyType,
                        currentPropertyAttrib);
                properties.put(currentPropertyName, value);
            } else
                if (currentPropertyText != null) {
                    String[] lines = currentPropertyText.toString().split("\n");
                    List<Object> values = new ArrayList<>(lines.length);
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (line.length() > 0) {
                            Object value = readTyped(currentPropertyType, line);
                            values.add(value);
                        }
                    }
                    properties.put(currentPropertyName, values);
                }
            currentPropertyName = null;
            currentPropertyType = null;
            currentPropertyText = null;

        }

        private void handleComponent() throws SAXException {

            if (provides != null && !provides.isEmpty()) {
                Builder builder = new Builder()
                        .setNamespace(Namespaces.NS_SERVICE);
                builder.addAttribute(Constants.OBJECTCLASS, provides);
                for (Entry<String, Object> entry : properties.entrySet()) {
                    builder.addAttribute(entry.getKey(), entry.getValue());
                }

                StringBuilder uses = new StringBuilder();
                boolean first = true;
                for (String objectClass : provides) {
                    if (!first)
                        uses.append(',');
                    first = false;

                    int dotindex = objectClass.lastIndexOf('.');
                    if (dotindex < 0)
                        throw new SAXException(
                                "Service interface in default package.");
                    String pkgName = objectClass.substring(0, dotindex);
                    uses.append(pkgName);
                }
                builder.addDirective(Constants.USES_DIRECTIVE, uses.toString());

                caps.add(builder.buildCapability());
            }

            if (references != null && !references.isEmpty()) {
                reqs.addAll(references);
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            String localNameLowerCase = localName.toLowerCase();

            if (ELEMENT_PROPERTY.equals(localNameLowerCase)) {
                handleProperty();
            }

            if (ELEMENT_COMPONENT.equals(localNameLowerCase)) {
                handleComponent();
            }

            super.endElement(uri, localName, qName);
        }

        private void setVersion(Version version) {
            if (highest == null || (version.compareTo(highest) > 0))
                highest = version;
        }

        private static Requirement createServiceRequirement(Attributes attribs)
                throws SAXException {
            String interfaceClass = attribs.getValue(ATTRIB_INTERFACE);
            if (interfaceClass == null || interfaceClass.length() == 0) {
                throw new SAXException(
                        "Missing required " + ATTRIB_INTERFACE + " attribute");
            }

            Builder builder = new Builder().setNamespace(Namespaces.NS_SERVICE);

            String filter;
            String target = attribs.getValue(ATTRIB_TARGET);
            if (target != null)
                filter = String.format("(&(%s=%s)%s)", Constants.OBJECTCLASS,
                        interfaceClass, target);
            else
                filter = String.format("(%s=%s)", Constants.OBJECTCLASS,
                        interfaceClass);
            builder.addDirective(Namespaces.DIRECTIVE_FILTER, filter);

            String cardinality = attribs.getValue(ATTRIB_CARDINALITY);
            if (cardinality != null) {
                cardinality = cardinality.trim().toLowerCase();
                if (cardinality.length() > 0) {
                    if ('0' == cardinality.charAt(0))
                        builder.addDirective(Namespaces.DIRECTIVE_RESOLUTION,
                                Namespaces.RESOLUTION_OPTIONAL);
                    if ('n' == cardinality.charAt(cardinality.length() - 1))
                        builder.addDirective(Namespaces.DIRECTIVE_CARDINALITY,
                                Namespaces.CARDINALITY_MULTIPLE);
                }
            }

            builder.addDirective(Namespaces.DIRECTIVE_EFFECTIVE,
                    Namespaces.EFFECTIVE_ACTIVE);
            return builder.buildRequirement();
        }

        private static Object readTyped(String type, String string) {
            if (type.equals(LONG_STR) || type.equals(INTEGER_STR)
                    || type.equals(SHORT_STR) || type.equals(BYTE_STR))
                return Long.parseLong(string);

            if (type.equals(FLOAT_STR) || type.equals(DOUBLE_STR))
                return Double.parseDouble(string);

            return string;
        }
    }
}
