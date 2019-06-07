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
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Tag class represents a minimal XML tree. It consist of a named element
 * with a hashtable of named attributes. Methods are provided to walk the tree
 * and get its constituents. The content of a Tag is a list that contains STRING
 * objects or other Tag objects.
 */
public class Tag {

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    private static final String XMLNS = "xmlns";

    private static final String XMLNS_SCHEME = "xmlns:";
    Map<String, String> attributes = new TreeMap<>();
    ArrayList<Object> content = new ArrayList<>();
    String name;

    Tag parent;

    /**
     * Construct a new Tag with a name.
     * 
     * @param name
     *                 The name to be used by the tag.
     */
    public Tag(String name) {
        this.name = name;
    }

    /**
     * Construct a new Tag with a name.
     * 
     * @param name
     *                       The name to be used by the tag.
     * @param attributes
     *                       The tag attributes.
     */
    public Tag(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    /**
     * Construct a new Tag with a single string as content.
     * 
     * @param name
     *                    The name to be used by the tag.
     * @param content
     *                    The tag content.
     */
    public Tag(String name, String content) {
        this.name = name;
        addContent(content);
    }

    /**
     * Construct a new Tag with a name and a set of attributes. The attributes
     * are given as ( name, value )
     * 
     * @param name
     *                       The name to be used by the tag.
     * @param attributes
     *                       The tag attributes.
     */
    public Tag(String name, String[] attributes) {
        this.name = name;
        for (int i = 0; i < attributes.length; i += 2)
            addAttribute(attributes[i], attributes[i + 1]);
    }

    public static void convert(Collection<Map<String, String>> c, String type,
            Tag parent) {
        for (Map<String, String> map : c) {
            parent.addContent(new Tag(type, map));
        }
    }

    /**
     * Add a new date attribute. The date is formatted as the SimpleDateFormat
     * describes at the top of this class.
     * 
     * @param key
     *                  The attribute key.
     * @param value
     *                  The attribute value.
     *
     */
    public void addAttribute(String key, Date value) {
        attributes.put(key, format.format(value));
    }

    /**
     * Add a new attribute.
     * 
     * @param key
     *                  The attribute key.
     * @param value
     *                  The attribute value.
     */
    public void addAttribute(String key, int value) {
        attributes.put(key, Integer.toString(value));
    }

    /**
     * Add a new attribute.
     * 
     * @param key
     *                  The attribute key.
     * @param value
     *                  The attribute value.
     */
    public void addAttribute(String key, Object value) {
        if (value == null)
            return;
        attributes.put(key, value.toString());
    }

    /**
     * Add a new attribute.
     * 
     * @param key
     *                  The attribute key.
     * @param value
     *                  The attribute value.
     */
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * Add a new content string.
     * 
     * @param string
     *                   The content to be added.
     */
    public void addContent(String string) {
        content.add(string);
    }

    /**
     * Add a new content tag.
     * 
     * @param tag
     *                The content to be added.
     */
    public void addContent(Tag tag) {
        content.add(tag);
        tag.parent = this;
    }

    /**
     * Escape a string, do entity conversion.
     * 
     * @param s
     *              The escape string.
     */
    String escape(String s) {
        if (s == null)
            return "?null?";

        StringBuilder sb = new StringBuilder(); //NOSONAR
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    public String findRecursiveAttribute(String pName) {
        String value = getAttribute(pName);
        if (value != null)
            return value;
        if (parent != null)
            return parent.findRecursiveAttribute(pName);
        return null;
    }

    /**
     * Convenience method to print a string nicely and does character conversion
     * to entities.
     * 
     * @param pw
     *                   The print writer object.
     * @param indent
     *                   The indent object.
     * @param width
     *                   The width.
     * @param s
     *                   The string to be printed.
     */
    void formatted(PrintWriter pw, Indent indent, int width, String s) {
        int pos = width + 1;
        s = s.trim();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i == 0 || (Character.isWhitespace(c) && pos > width - 3)) {
                indent.print(pw);
                pos = 0;
            }
            switch (c) {
            case '<':
                pw.print("&lt;");
                pos += 4;
                break;
            case '>':
                pw.print("&gt;");
                pos += 4;
                break;
            case '&':
                pw.print("&amp;");
                pos += 5;
                break;
            default:
                pw.print(c);
                pos++;
                break;
            }

        }
    }

    /**
     * Return the attribute value.
     * 
     * @param key
     *                The attribute's key
     * @return A string containing the attribute's value.
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Return the attribute value or a default if not defined.
     * 
     * @param key
     *                  The attribute's key
     * @param deflt
     *                  The attribute's default value.
     * @return A string containing the attribute's value.
     */
    public String getAttribute(String key, String deflt) {
        String answer = getAttribute(key);
        return answer == null ? deflt : answer;
    }

    /**
     * Answer the attributes as a Dictionary object.
     * 
     * @return A map containing all attributes.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Return the contents.
     * 
     * @return A Vector of objects representing the contents.
     */
    public List<Object> getContents() {
        return content;
    }

    /**
     * Return only the tags of the first level of descendants that match the
     * name.
     * 
     * @param tag
     *                The first level tag
     * 
     * @return A Vector of objects representing the contents.
     */
    public List<Object> getContents(String tag) {
        List<Object> out = new ArrayList<>();
        for (Object o : content) {
            if (o instanceof Tag && ((Tag) o).getName().equals(tag))
                out.add(o);
        }
        return out;
    }

    /**
     * Return the whole contents as a STRING (no tag info and attributes).
     * 
     * @return A string representing the contents.
     */
    public String getContentsAsString() {
        StringBuilder sb = new StringBuilder();
        getContentsAsString(sb);
        return sb.toString();
    }

    /**
     * convenient method to get the contents in a StringBuilder.
     * 
     * @param sb
     *               A string buffer.
     */
    public void getContentsAsString(StringBuilder sb) {
        for (Object o : content) {
            if (o instanceof Tag)
                ((Tag) o).getContentsAsString(sb);
            else
                sb.append(o.toString());
        }
    }

    public String getLocalName() {
        int index = name.indexOf(':');
        if (index <= 0)
            return name;

        return name.substring(index + 1);
    }

    /**
     * Return the name of the tag.
     * 
     * @return A string representing the name.
     */
    public String getName() {
        return name;
    }

    public String getNameSpace() {
        return getNameSpace(name);
    }

    public String getNameSpace(String pName) {
        int index = pName.indexOf(':');
        if (index > 0) {
            String ns = pName.substring(0, index);
            return findRecursiveAttribute(XMLNS_SCHEME + ns);
        }
        return findRecursiveAttribute(XMLNS);
    }

    public String getString(String path) {
        String attribute = null;
        int index = path.indexOf('@');
        if (index >= 0) {
            // attribute
            attribute = path.substring(index + 1);

            if (index > 0) {
                // prefix path
                path = path.substring(index - 1); // skip -1
            } else
                path = "";
        }
        Tag[] tags = select(path);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            if (attribute == null)
                tags[i].getContentsAsString(sb);
            else
                sb.append(tags[i].getAttribute(attribute));
        }
        return sb.toString();
    }

    public String getStringContent() {
        StringBuilder sb = new StringBuilder();
        for (Object c : content) {
            if (!(c instanceof Tag))
                sb.append(c);
        }
        return sb.toString();
    }

    public boolean match(String search, Tag child, Tag mapping) {
        String target = child.getName();
        String sn = null;
        String tn = null;

        if ("*".equals(search))
            return true;

        int s = search.indexOf(':');
        if (s > 0) {
            sn = search.substring(0, s);
            search = search.substring(s + 1);
        }
        int t = target.indexOf(':');
        if (t > 0) {
            tn = target.substring(0, t);
            target = target.substring(t + 1);
        }

        if (!search.equals(target)) // different tag names
            return false;

        if (mapping == null) {
            return (tn == null && sn == null) || (sn != null && sn.equals(tn));
        }
        String suri = sn == null ? mapping.getAttribute(XMLNS)
                : mapping.getAttribute(XMLNS_SCHEME + sn);
        String turi = tn == null ? child.findRecursiveAttribute(XMLNS)
                : child.findRecursiveAttribute(XMLNS_SCHEME + tn);
        return turi == suri
                || (turi != null && suri != null && turi.equals(suri));
    }

    /**
     * Print the tag formatted to a PrintWriter.
     * 
     * @param indent
     *                   The indent.
     * @param pw
     *                   The print writer object.
     */
    public void print(Indent indent, PrintWriter pw) {
        boolean empty = content.isEmpty();

        printOpen(indent, pw, empty);
        if (!empty) {
            printContents(indent, pw);
            printClose(indent, pw);
        }
    }

    public void printClose(Indent indent, PrintWriter pw) {
        indent.print(pw);
        pw.print("</");
        pw.print(name);
        pw.print('>');
    }

    public void printContents(Indent indent, PrintWriter pw) {
        for (Object lcontent : this.content) {
            Indent nextIndent = indent.next();
            if (lcontent instanceof String) {
                formatted(pw, nextIndent, 60, escape((String) lcontent));
            } else
                if (lcontent instanceof Tag) {
                    Tag tag = (Tag) lcontent;
                    tag.print(nextIndent, pw);
                }
        }
    }

    public void printOpen(Indent indent, PrintWriter pw, boolean andClose) {
        indent.print(pw);
        pw.print('<');
        pw.print(name);

        String quote = "\"";
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            String key = e.getKey();
            String value = escape(e.getValue());
            pw.print(' ');
            pw.print(key);
            pw.print("=");
            pw.print(quote);
            pw.print(value.replaceAll("\"", "&quot;"));
            pw.print(quote);
        }

        if (andClose)
            pw.print("/>");
        else
            pw.print('>');
    }

    public void rename(String string) {
        name = string;
    }

    /**
     * root/preferences/native/os
     * 
     * @param path
     *                 The path.
     * @return An array of tags.
     */
    public Tag[] select(String path) {
        return select(path, (Tag) null);
    }

    public Tag[] select(String path, Tag mapping) {
        List<Object> v = new ArrayList<>();
        select(path, v, mapping);
        Tag[] result = new Tag[v.size()];
        result = v.toArray(result);
        return result;
    }

    void processRelative(String path, List<Object> results, Tag mapping) {

        int i = path.indexOf('/', 2);
        String lname = path.substring(2, i < 0 ? path.length() : i);

        for (Object o : content) {
            if (o instanceof Tag) {
                Tag child = (Tag) o;
                if (match(lname, child, mapping))
                    results.add(child);
                child.select(path, results, mapping);
            }
        }
    }
    
    
    void select(String path, List<Object> results, Tag mapping) {
        if (path.startsWith("//")) {
            
            processRelative(path, results, mapping);
            return;
        }

        if (path.length() == 0) {
            results.add(this);
            return;
        }

        int i = path.indexOf('/');
        String elementName = path;
        String remainder = "";
        if (i > 0) {
            elementName = path.substring(0, i);
            remainder = path.substring(i + 1);
        }

        for (Object o : content) {
            if (o instanceof Tag) {
                Tag child = (Tag) o;
                if (child.getName().equals(elementName)
                        || "*".equals(elementName))
                    child.select(remainder, results, mapping);
            }
        }
    }

    /**
     * Return a string representation of this Tag and all its children
     * recursively.
     */
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        print(Indent.NONE, new PrintWriter(sw));
        return sw.toString();
    }

}
