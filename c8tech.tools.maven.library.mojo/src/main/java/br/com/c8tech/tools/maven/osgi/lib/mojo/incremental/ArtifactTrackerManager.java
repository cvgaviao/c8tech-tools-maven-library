/**
 * ==========================================================================
 * Copyright © 2015-2018 Cristiano Gavião, C8 Technology ME.
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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.MavenArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.P2ArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.PropertiesArtifactSet;
import io.takari.incrementalbuild.BuildContext;

public interface ArtifactTrackerManager {

    /**
     * 
     * @param pBuildContext
     *                          The incremental building context registered in
     *                          the mojo.
     * @throws MojoExecutionException
     *                                    When an error occurs while copying.
     */
    void copyMavenArtifactsToCache(BuildContext pBuildContext)
            throws MojoExecutionException;

    /**
     * 
     * @return a set of artifact tracker objects.
     */
    Set<ArtifactTracker> getAllArtifactTrackers();

    /**
     * 
     * @return the artifact filter object.
     */
    ArtifactFilter getArtifactFilter();

    /**
     * 
     * @return the pattern used to compute the cache file path.
     */
    String getCachedFileNamePattern();

    /**
     * 
     * @return the cache path.
     */
    Path getCacheDirPath();

    /**
     * 
     * @return the excluded dependencies.
     */
    List<String> getExcludedDependencies();

    /**
     * 
     * @return the maven local repository;
     */
    ArtifactRepository getLocalRepository();

    /**
     * 
     * @return the custom maven artifact sets.
     */
    MavenArtifactSets getMavenArtifactSets();

    /**
     * 
     * @return the set of maven artifact trackers.
     */
    Set<ArtifactTracker> getMavenArtifactTrackers();

    /**
     * 
     * @return the injected maven project object.
     */
    MavenProject getMavenProject();

    /**
     * 
     * @return the injected maven session.
     */
    MavenSession getMavenSession();

    /**
     * 
     * @return the custom P2 artifact sets.
     */
    P2ArtifactSets getP2ArtifactSets();

    /**
     * 
     * @return the set of P2 artifact trackers.
     */
    Set<ArtifactTracker> getP2ArtifactTrackers();

    /**
     * 
     * @return the set of properties artifacts.
     */
    PropertiesArtifactSet getPropertiesArtifactSet();

    /**
     * 
     * @return the set of properties artifact trackers.
     */
    Set<ArtifactTracker> getPropertiesArtifactTrackers();

    /**
     * 
     * @return the remote repositories.
     */
    List<ArtifactRepository> getRemoteRepositories();

    /**
     * 
     * @return the injected repository system object.
     */
    RepositorySystem getRepositorySystem();

    /**
     * 
     * @return the defined scopes.
     */
    Set<String> getScopes();

    boolean isAssemblyUrlProtocolAllowed();

    boolean isConsiderOptionalDependencies();

    boolean isConsiderTransitiveDependencies();

    boolean isGroupingByTypeDirectory();

    boolean isPackOnTheFlyAllowed();

    boolean isPreviousCachingRequired();

    boolean isVerbose();

    Set<ArtifactTracker> lookupEmbeddableArtifactTrackers();

    Set<ArtifactTracker> lookupEmbeddableArtifactTrackers(
            List<String> pExcludedPackagingTypes);

    Set<ArtifactTracker> lookupNotEmbeddableArtifactTrackers();

    /**
     * 
     * @param pExcludedPackagingTypes
     *                                    the list of packaging type to be
     *                                    unconsidered.
     * @return a set of found artifact trackers.
     */
    Set<ArtifactTracker> lookupNotEmbeddableArtifactTrackers(
            List<String> pExcludedPackagingTypes);

    /**
     * 
     * @param pEmbeddableScopes
     *                              a set of scopes to be considered.
     * @return the number of resolved artifacts.
     * @throws MojoExecutionException
     *                                    when any problem occurs while
     *                                    resolving artifacts.
     * @throws MojoFailureException
     *                                    when a null set of artifacts are
     *                                    provided.
     */
    int resolveMavenArtifacts(Set<String> pEmbeddableScopes)
            throws MojoExecutionException, MojoFailureException;

    /**
     * 
     * @param pResolvedDependencies
     *                                  a previous resolved set of artifacts
     *                                  that will be merged to the result of
     *                                  this method.
     * @param pEmbeddableScopes
     *                                  a set of scopes to be considered.
     * @return the number of resolved artifacts.
     * @throws MojoExecutionException
     *                                    when any problem occurs while
     *                                    resolving artifacts.
     * @throws MojoFailureException
     *                                    when a null set of artifacts are
     *                                    provided.
     */
    int resolveMavenArtifacts(Set<Artifact> pResolvedDependencies,
            Set<String> pEmbeddableScopes)
            throws MojoExecutionException, MojoFailureException;

    /**
     * This method will check the existence of declared p2 repositories and
     * resolve the artifacts declared inside the <b>P2ArtifactSet</b>
     * configuration tag in the project pom.xml.
     * 
     * @param pP2LocalPoolUrl
     *                            the location of a bundle pool.
     *
     * @return the number of resolved artifacts.
     * @throws MojoExecutionException
     *                                    when an error occurs while resolving
     *                                    p2 artifacts.
     */
    int resolveP2Artifacts(URL pP2LocalPoolUrl) throws MojoExecutionException;

    int resolvePropertiesArtifactSet() throws MojoExecutionException;

    /**
     * 
     * @param pArtifactId
     *                        the artifactID to search for.
     * @return the found artifact tracker or null;
     */
    ArtifactTracker searchByArtifactId(String pArtifactId);

    /**
     * 
     * @param pPath
     *                  the artifact path to search for an artifact.
     * @return the found artifact tracker or null;
     */
    ArtifactTracker searchByPath(String pPath);

    /**
     * 
     * @param pPackagingType
     *                           The packaging type.
     * @return the found artifact tracker or null;
     */
    ArtifactTracker searchByType(String pPackagingType);

}
