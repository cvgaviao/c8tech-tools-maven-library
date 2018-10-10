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
package br.com.c8tech.tools.maven.osgi.lib.mojo.services;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.Archiver;

import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.BundleRef;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.URLResourceBySizeHolder;

public interface DependenciesHelper {

    /**
     *
     * @param pType
     *                  the artifact type.
     * @return the associated extended artifact handler.
     */
    ExtendedArtifactHandler getArtifactHandler(String pType);

    /**
     *
     * @param pArtifact
     *                      the artifact used to search for a maven project.
     * @param pSession
     *                      the injected maven session.
     * @return the associated maven project or null.
     * @throws MojoExecutionException
     *                                    When a problem occurs.
     */
    MavenProject getMavenProject(Artifact pArtifact, MavenSession pSession)
            throws MojoExecutionException;

    /**
     *
     * @param pFileUrl
     *                     the file to check.
     * @return if the file is present.
     */
    boolean isFilePresent(URL pFileUrl);

    /**
     * Check whether the specified URL belongs to a server using a <b>p2
     * Composite Repository</b> or not.
     *
     * @param pRepository
     *                        The URL of a p2 repository.
     * @return if it is a composite repository.
     */
    boolean isP2CompositeRepository(URL pRepository);

    /**
     *
     * @param pResourceURL
     *                         The URL of a resource to check.
     * @return if the resource is present.
     */
    boolean isResourcePresent(URL pResourceURL);

    /**
     *
     * @param pHttpConnection
     *                            The HTTP Connection object.
     * @return if the resource is present in the web server.
     */
    boolean isResourcePresentOnWebServer(HttpURLConnection pHttpConnection);

    /**
     * Tries to construct project for the provided artifact.
     *
     * @param pArtifact
     *                                 The artifact to search for a project.
     * @param pSession
     *                                 The inject maven session.
     * @param pResolveDependencies
     *                                 Whether plugin must resolve associated
     *                                 artifact dependencies.
     * @return The found maven project or null.
     * @throws ProjectBuildingException
     *                                      When a problem occurs while
     *                                      searching for a project.
     */
    MavenProject loadProject(Artifact pArtifact, MavenSession pSession,
            boolean pResolveDependencies) throws ProjectBuildingException;

    /**
     *
     * @param pPackaging
     *                       The packaging to search for an archiver.
     * @return The archiver related to the packaging when one are found or null.
     */
    Archiver lookupArchiver(String pPackaging);

    /**
     *
     * @param pResourceURL
     *                         The URL of the resource.
     * @param pCacheDir
     *                         The cache directory.
     * @return The resource state holder.
     * @throws IOException
     *                         When no resouce if found.
     */
    URLResourceBySizeHolder newResourceStateHolder(URL pResourceURL,
            Path pCacheDir) throws IOException;

    /**
     *
     * @param pArtifact
     *                                the artifact to be resolved.
     * @param pRepositorySystem
     *                                the injected repository system object.
     * @param pRemoteRepositories
     *                                the injected remote repositories object.
     * @param pLocalRepository
     *                                the injected local repository.
     * @return the File representing the found artifact.
     * @throws IOException
     *                         when it can't resolve the artifact.
     */
    File resolveArtifact(Artifact pArtifact, RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException;

    /**
     *
     * @param pArtifactId
     *                                The artifact ID.
     * @param pGroupId
     *                                The group ID.
     * @param pVersion
     *                                The version of the artifact.
     * @param pType
     *                                The packaging type.
     * @param pClassifier
     *                                The Classifier
     * @param pRepositorySystem
     *                                The injected repository system object.
     * @param pRemoteRepositories
     *                                The injected remote repositories object.
     * @param pLocalRepository
     *                                The local repository.
     * @return The resolved artifact or null.
     * @throws IOException
     *                         When an error occurs while resolving the
     *                         artifact.
     */
    Artifact resolveArtifact(String pArtifactId, String pGroupId,
            String pVersion, String pType, String pClassifier,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException;

    /**
     * 
     * @param pBundleRef
     *                                The artifact created from a String.
     * @param pRepositorySystem
     *                                The injected repository system object.
     * @param pRemoteRepositories
     *                                The injected remote repositories object.
     * @param pLocalRepository
     *                                The local repository.
     * @return The resolved artifact or null.
     * @throws IOException
     *                         When an error occurs while resolving the
     *                         artifact.
     */
    Artifact resolveArtifact(BundleRef pBundleRef,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException;

    /**
     *
     * @param pArtifacts
     *                       The set of artifacts to be saved.
     * @param pFilePath
     *                       The place where the artifacts must be saved
     * @throws IOException
     *                         When something goes wrong.
     */
    void saveToFile(Set<Artifact> pArtifacts, File pFilePath)
            throws IOException;

    /**
     * This method is aimed to search the local repository for the latest
     * package file that was generated for one dependency artifact.
     * <p>
     * It should be used by mojos in situations where current project's
     * lifecycle are being processed (before package phase) but some of its
     * dependencies have its source code project either opened in the eclipse
     * workspace.<br>
     * When mojo's request the artifact's associated packaged file it will get a
     * null because m2e do not process package phase by default, so the file
     * wasn't created yet.<br>
     *
     * @param pArtifact
     *                                The artifact to search for its associated
     *                                file.
     * @param pRepositorySystem
     *                                The injected repository system.
     * @param pRemoteRepositories
     *                                The injected remote repositories.
     * @param pLocalRepository
     *                                The injected local repository.
     * @return The file associate to the artifact, if one is found, or null.
     * @throws IOException
     *                         When something goes wrong.
     */
    File searchForLatestArtifactFile(Artifact pArtifact,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException;

    /**
     *
     * @param pDependency
     *                            The artifact to be validated.
     * @param pArtifactFilter
     *                            The artifact filter to use in the validation
     *                            process.
     * @return Whether the maven dependency is valid.
     */
    boolean validateMavenDependency(Artifact pDependency,
            ArtifactFilter pArtifactFilter);

    /**
     *
     * @param pUrl
     *                      The URL whene the artifact comes from.
     * @param pCacheDir
     *                      The cache directory to search for the artifact.
     * @return The URL pointing to an artifact file.
     * @throws IOException
     *                         When an error occurs while evaluating the
     *                         artifacts.
     */
    URL whichSourceToUse(URL pUrl, Path pCacheDir) throws IOException;

}
