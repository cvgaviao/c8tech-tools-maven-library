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
package br.com.c8tech.tools.maven.osgi.lib.mojo.beans;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BundleRef {

    private static final Pattern GAV_PATTERN = Pattern.compile(
            "\\b((\\D[a-zA-Z_0-9\\.\\-]+):)?(\\D[a-zA-Z_0-9\\.\\-]+)(:(\\D+))?(:(\\d[a-zA-Z_0-9\\.\\-]+))?(@(\\d))?$");

    private String artifactId;

    private Path cachePath;

    private String classifier;

    private String copyName;

    private String groupId;

    private URL locationURL;

    private int startLevel;

    private String type;

    private String version;

    public BundleRef() {
        super();
    }

    /**
     * The string must be in GAV format.
     * <p>
     * ex: group:name:version:type@startlevel
     *
     * @param pArtifactString
     *                            an GAV based artifact string expression.
     *
     * @see #GAV_PATTERN
     */
    public BundleRef(String pArtifactString) {
        set(pArtifactString);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BundleRef))
            return false;
        BundleRef other = (BundleRef) obj;
        return Objects.equals(this.groupId, other.groupId) // NOSONAR
                && Objects.equals(this.artifactId, other.artifactId)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.version, other.version)
                && Objects.equals(this.classifier, other.classifier);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Path getCachePath() {
        return cachePath;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getCopyName() {
        return copyName;
    }

    public String getGroupId() {
        return groupId;
    }

    public URL getLocationURL() {
        return locationURL;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public boolean isValid() {
        return getArtifactId() != null && !getArtifactId().isEmpty()
                && getVersion() != null && !getVersion().isEmpty()
                && getGroupId() != null && !getGroupId().isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getGroupId(), this.getArtifactId(),
                this.getVersion(), this.getClassifier());
    }

    public void set(String artifactString) {
        Matcher m = GAV_PATTERN.matcher(artifactString);

        if (m.matches()) {
            this.setGroupId(m.group(2));
            this.setArtifactId(m.group(3));
            this.setVersion(m.group(7));
            this.setType(m.group(5));
            this.setStartLevel(
                    Integer.parseInt(m.group(9) != null ? m.group(9) : "0"));
        }
    }

    public void setArtifactId(String pId) {
        artifactId = pId;
    }

    public void setCachePath(Path cachePath) {
        this.cachePath = cachePath;
    }

    public void setClassifier(String pClassifier) {
        classifier = pClassifier;
    }

    public void setCopyName(String pCopyName) {
        copyName = pCopyName;
    }

    public void setGroupId(String pGroupId) {
        groupId = pGroupId;
    }

    public void setLocationURL(URL pLocationURL) {
        locationURL = pLocationURL;
    }

    public void setStartLevel(int pStartLevel) {
        startLevel = pStartLevel;
    }

    public void setType(String pType) {
        type = pType == null ? "jar" : pType;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s(%s-%s)", groupId, artifactId, version,
                type, classifier);
    }
}
