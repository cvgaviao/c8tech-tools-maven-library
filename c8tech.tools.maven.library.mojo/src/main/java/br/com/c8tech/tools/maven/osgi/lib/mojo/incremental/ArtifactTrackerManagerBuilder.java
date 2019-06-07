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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.MavenArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.MavenArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.P2ArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.PropertiesArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.filters.DefaultFilterFactory;
import br.com.c8tech.tools.maven.osgi.lib.mojo.filters.FilterFactory;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal.DefaultArtifactTrackerManager;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DependenciesHelper;

public class ArtifactTrackerManagerBuilder {

    public static interface M2eWorkspaceSteps {
        MavenSteps endWorkspaceSetup();

        M2eWorkspaceSteps withAssemblyUrlProtocolAllowed(
                boolean pAssemblyUrlProtocolAllowed);

        M2eWorkspaceSteps withPackOnTheFlyAllowed(boolean pPackOnTheFlyAllowed);
    }

    public static interface MavenFilteringSteps {
        MavenSteps endMavenFiltering();

        MavenFilteringSteps withArtifactFilter(ArtifactFilter pArtifactFilter);

        MavenFilteringSteps withExcludedDependencies(
                List<String> pExcludedDependencies);

        MavenFilteringSteps withMavenArtifactSet(
                MavenArtifactSet pMavenArtifactSet);

        MavenFilteringSteps withMavenArtifactSets(
                MavenArtifactSets pMavenArtifactSets);

        MavenFilteringSteps withOptional(boolean pOptional);

        MavenFilteringSteps withPropertiesArtifactSet(
                PropertiesArtifactSet pPropertiesArtifactSet);

        MavenFilteringSteps withScopes(Set<String> pScopes);

        MavenFilteringSteps withTransitive(boolean pTransitive);
    }

    public static interface MavenSteps {
        OperationalSteps endMavenSetup();

        MavenFilteringSteps mavenFiltering();

        /**
         * Sets a pattern that will be used to change the name of the file that
         * will be cached.
         * <p>
         *
         * The following tokens can be used:<br>
         * <ul>
         * <li><b>%n</b> - the artifact name</li>
         * <li><b>%c</b> - the classifier name, when it exists</li>
         * <li><b>%v</b> - the artifact version</li>
         * <li><b>%e</b> - the artifact file extension</li>
         * </ul>
         * <br>
         * Example:
         *
         * <pre>
         * %n-%c_%v.%e
         * </pre>
         * <p>
         * This plugin will use the maven standard file naming format in order
         * to match the tokens against the original file name:
         *
         * <pre>
         * <code>name-classifier-version.extension</code>
         * </pre>
         *
         * @param pCachedFilePatternReplacement
         *                                          The pattern string
         * @return The maven common steps.
         */
        MavenSteps withCachedFileNamePattern(
                String pCachedFilePatternReplacement);

        MavenSteps withDependenciesHelper(
                DependenciesHelper pDependenciesHelper);

        MavenSteps withRepositorySystem(RepositorySystem pRepositorySystem);

        M2eWorkspaceSteps workspaceSetup();
    }

    public static interface OperationalSteps {
        ArtifactTrackerManager build();

        MavenSteps mavenSetup();

        P2Steps p2Setup();

        OperationalSteps withGroupingByTypeDirectory(
                boolean pGroupingByTypeDirectory);

        OperationalSteps withOfflineMode(boolean pOffline);

        OperationalSteps withPreviousCachingRequired(
                boolean pPreviousCachingRequired);

        OperationalSteps withVerbose(boolean pVerbose);

    }

    public static interface P2Steps {

        OperationalSteps endP2Setup();

        P2Steps withDefaultGroupId(String pDefaultGroupId);

        P2Steps withP2ArtifactSets(P2ArtifactSets pP2ArtifactSets);
    }

    private static class Steps implements MavenSteps, MavenFilteringSteps,
            OperationalSteps, M2eWorkspaceSteps, P2Steps {

        private final ArtifactTrackerManagerBuilder artifactTrackerManagerBuilder;

        private Steps(
                ArtifactTrackerManagerBuilder pArtifactTrackerManagerBuilder) {
            this.artifactTrackerManagerBuilder = pArtifactTrackerManagerBuilder;
        }

        private Path getCachePath() {
            return artifactTrackerManagerBuilder.cacheDirPath;
        }

        @Override
        public ArtifactTrackerManager build() {
            if (artifactTrackerManagerBuilder.artifactFilter == null
                    && artifactTrackerManagerBuilder.aMavenSetup) {
                artifactTrackerManagerBuilder.artifactFilter = artifactTrackerManagerBuilder
                        .getDefaultArtifactFilter(null);
            }
            return new DefaultArtifactTrackerManager(
                    artifactTrackerManagerBuilder);
        }

        @Override
        public MavenSteps endMavenFiltering() {
            return this;
        }

        @Override
        public OperationalSteps endMavenSetup() {
            artifactTrackerManagerBuilder.aMavenSetup = true;
            return this;
        }

        @Override
        public OperationalSteps endP2Setup() {
            artifactTrackerManagerBuilder.aP2Setup = true;
            return this;
        }

        @Override
        public MavenSteps endWorkspaceSetup() {
            return this;
        }

        @Override
        public MavenFilteringSteps mavenFiltering() {
            return this;
        }

        @Override
        public MavenSteps mavenSetup() {
            return this;
        }

        @Override
        public P2Steps p2Setup() {
            return this;
        }

        @Override
        public MavenFilteringSteps withArtifactFilter(
                ArtifactFilter pArtifactFilter) {
            artifactTrackerManagerBuilder.artifactFilter = artifactTrackerManagerBuilder
                    .getDefaultArtifactFilter(pArtifactFilter);

            return this;
        }

        @Override
        public M2eWorkspaceSteps withAssemblyUrlProtocolAllowed(
                boolean pAssemblyUrlProtocolAllowed) {
            artifactTrackerManagerBuilder.assemblyUrlProtocolAllowed = pAssemblyUrlProtocolAllowed;
            return this;
        }

        @Override
        public MavenSteps withCachedFileNamePattern(
                String pCachedFileNamePattern) {
            artifactTrackerManagerBuilder.cachedFileNamePattern = pCachedFileNamePattern;
            return this;
        }

        @Override
        public P2Steps withDefaultGroupId(String pDefaultGroupId) {
            artifactTrackerManagerBuilder.defaultGroupId = pDefaultGroupId;
            return this;
        }

        @Override
        public MavenSteps withDependenciesHelper(
                DependenciesHelper pDependenciesHelper) {
            artifactTrackerManagerBuilder.dependenciesHelper = pDependenciesHelper;
            return this;
        }

        @Override
        public MavenFilteringSteps withExcludedDependencies(
                List<String> pExcludedDependencies) {
            artifactTrackerManagerBuilder.excludedDependencies = pExcludedDependencies;
            return this;
        }

        @Override
        public OperationalSteps withGroupingByTypeDirectory(
                boolean pGroupingByTypeDirectory) {
            artifactTrackerManagerBuilder.groupingByTypeDirectory = pGroupingByTypeDirectory;
            return this;
        }

        @Override
        public MavenFilteringSteps withMavenArtifactSet(
                MavenArtifactSet pMavenArtifactSet) {
            if (pMavenArtifactSet == null) {
                MavenArtifactSet set = new MavenArtifactSet();
                set.setCacheDirectory(getCachePath());
                artifactTrackerManagerBuilder.getMavenArtifactSets()
                        .addMavenArtifactSet(set);
            } else {
                artifactTrackerManagerBuilder.getMavenArtifactSets()
                        .addMavenArtifactSet(pMavenArtifactSet);
            }
            return this;
        }

        @Override
        public MavenFilteringSteps withMavenArtifactSets(
                MavenArtifactSets pMavenArtifactSets) {
            if (pMavenArtifactSets == null) {
                MavenArtifactSet mas = new MavenArtifactSet();
                mas.setCacheDirectory(getCachePath());
                artifactTrackerManagerBuilder.getMavenArtifactSets()
                        .addMavenArtifactSet(mas);
            }
            artifactTrackerManagerBuilder.mavenArtifactSets = pMavenArtifactSets;
            return this;
        }

        @Override
        public OperationalSteps withOfflineMode(boolean pOffline) {
            artifactTrackerManagerBuilder.offline = pOffline;
            return this;
        }

        @Override
        public MavenFilteringSteps withOptional(boolean pOptional) {
            artifactTrackerManagerBuilder.considerOptionalDependencies = pOptional;
            return this;
        }

        @Override
        public P2Steps withP2ArtifactSets(P2ArtifactSets pP2ArtifactSets) {
            artifactTrackerManagerBuilder.p2ArtifactSets = pP2ArtifactSets;
            return this;
        }

        @Override
        public M2eWorkspaceSteps withPackOnTheFlyAllowed(
                boolean pPackOnTheFlyAllowed) {
            artifactTrackerManagerBuilder.packOnTheFlyAllowed = pPackOnTheFlyAllowed;
            return this;
        }

        @Override
        public OperationalSteps withPreviousCachingRequired(
                boolean pPreviousCachingRequired) {
            artifactTrackerManagerBuilder.previousCachingRequired = pPreviousCachingRequired;
            return this;
        }

        @Override
        public MavenFilteringSteps withPropertiesArtifactSet(
                PropertiesArtifactSet pPropertiesArtifactSet) {
            artifactTrackerManagerBuilder.propertiesArtifactSet = pPropertiesArtifactSet;
            return this;
        }

        @Override
        public MavenSteps withRepositorySystem(
                RepositorySystem pRepositorySystem) {
            artifactTrackerManagerBuilder.repositorySystem = pRepositorySystem;
            return this;
        }

        @Override
        public final MavenFilteringSteps withScopes(Set<String> pScopes) {
            artifactTrackerManagerBuilder.scopes.addAll(pScopes);
            return this;
        }

        @Override
        public MavenFilteringSteps withTransitive(boolean pTransitive) {
            artifactTrackerManagerBuilder.considerTransitiveDependencies = pTransitive;
            return this;
        }

        @Override
        public OperationalSteps withVerbose(boolean pVerbose) {
            artifactTrackerManagerBuilder.verbose = pVerbose;
            return this;
        }

        @Override
        public M2eWorkspaceSteps workspaceSetup() {
            return this;
        }
    }

    protected boolean aMavenSetup;

    protected boolean aP2Setup;

    private ArtifactFilter artifactFilter;

    private boolean assemblyUrlProtocolAllowed;

    private String cachedFileNamePattern;

    private final Path cacheDirPath;

    private boolean considerOptionalDependencies;

    private boolean considerTransitiveDependencies;

    private String defaultGroupId;

    private DependenciesHelper dependenciesHelper;

    private List<String> excludedDependencies;

    private boolean groupingByTypeDirectory;

    private MavenArtifactSets mavenArtifactSets;

    private final MavenSession mavenSession;

    private boolean offline;

    private P2ArtifactSets p2ArtifactSets;

    private boolean packOnTheFlyAllowed;

    private boolean previousCachingRequired;

    private PropertiesArtifactSet propertiesArtifactSet;

    private RepositorySystem repositorySystem;

    private Set<String> scopes = new HashSet<>();

    private Set<ArtifactTracker> toBeProcessed = new HashSet<>();

    private boolean verbose;

    private ArtifactTrackerManagerBuilder(MavenSession pMavenSession,
            Path pCacheDirPath) {
        mavenSession = pMavenSession;
        cacheDirPath = pCacheDirPath;
    }

    public static OperationalSteps newBuilder(MavenSession pMavenSession,
            Path pCacheDirPath) {
        return new Steps(new ArtifactTrackerManagerBuilder(pMavenSession,
                pCacheDirPath));
    }

    public ArtifactFilter getArtifactFilter() {
        return artifactFilter;
    }

    public String getCachedFileNamePattern() {
        return cachedFileNamePattern;
    }

    public Path getCacheDirPath() {
        return cacheDirPath;
    }

    public ArtifactFilter getDefaultArtifactFilter(
            ArtifactFilter pCustomArtifactFilter) {
        FilterFactory filterFactory = new DefaultFilterFactory();
        ArtifactFilter filterOptionality = isConsiderOptionalDependencies()
                ? filterFactory.newFilterAllowOptionalArtifacts()
                : filterFactory.newFilterDenyOptionalArtifacts();
        ArtifactFilter filterScopes = filterFactory
                .newFilterAnyOfTheseScopes(getScopes());
        ArtifactFilter filterExcludes = filterFactory
                .newFilterExcludeTheseArtifacts(getExcludedDependencies());
        if (pCustomArtifactFilter == null) {
            return new AndArtifactFilter(Arrays.asList(filterExcludes,
                    filterOptionality, filterScopes));
        } else {
            return new AndArtifactFilter(Arrays.asList(pCustomArtifactFilter,
                    filterExcludes, filterOptionality, filterScopes));

        }
    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public DependenciesHelper getDependenciesHelper() {
        return dependenciesHelper;
    }

    public List<String> getExcludedDependencies() {
        if (excludedDependencies == null) {
            excludedDependencies = Collections.emptyList();
        }
        return excludedDependencies;
    }

    public MavenArtifactSets getMavenArtifactSets() {
        if (mavenArtifactSets == null) {
            mavenArtifactSets = new MavenArtifactSets();
        }
        return mavenArtifactSets;
    }

    public MavenProject getMavenProject() {
        return mavenSession.getCurrentProject();
    }

    public MavenSession getMavenSession() {
        return mavenSession;
    }

    public P2ArtifactSets getP2ArtifactSets() {

        return p2ArtifactSets;
    }

    public PropertiesArtifactSet getPropertiesArtifactSet() {
        return propertiesArtifactSet;
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public Set<ArtifactTracker> getToBeProcessed() {
        return toBeProcessed;
    }

    public boolean isAssemblyUrlProtocolAllowed() {
        return assemblyUrlProtocolAllowed;
    }

    public boolean isConsiderOptionalDependencies() {
        return considerOptionalDependencies;
    }

    public boolean isConsiderTransitiveDependencies() {
        return considerTransitiveDependencies;
    }

    public boolean isGroupingByTypeDirectory() {
        return groupingByTypeDirectory;
    }

    public boolean isOffline() {
        return offline;
    }

    public boolean isPackOnTheFlyAllowed() {
        return packOnTheFlyAllowed;
    }

    public boolean isPreviousCachingRequired() {
        return previousCachingRequired;
    }

    public boolean isVerbose() {
        return verbose;
    }

}
