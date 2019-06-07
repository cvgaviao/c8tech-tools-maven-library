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
package br.com.c8tech.tools.maven.osgi.lib.mojo.components;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.Archiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.BundleRef;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.BundleArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.DefaultExtendedArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.URLResourceBySizeHolder;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DependenciesHelper;

/**
 *
 * @author cvgaviao
 *
 */
@Named
@Singleton
public class DefaultDependenciesHelper implements DependenciesHelper {

    private static final String COMPOSITE_INDEX = "compositeArtifacts.xml";

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultDependenciesHelper.class);

    @Inject
    private Map<String, Archiver> archiversMap;

    @Inject
    private ArtifactHandlerManager artifactHandlerManager;

    @Inject
    private ProjectBuilder projectBuilder;

    @Override
    public ExtendedArtifactHandler getArtifactHandler(String pType) {

        ExtendedArtifactHandler extendedArtifactHandler;
        ArtifactHandler art = artifactHandlerManager.getArtifactHandler(pType);
        if (art instanceof ExtendedArtifactHandler) {
            extendedArtifactHandler = (ExtendedArtifactHandler) art;
        } else
            if ("jar".equals(art.getPackaging())) {
                extendedArtifactHandler = new BundleArtifactHandler(art);
            } else {
                extendedArtifactHandler = new DefaultExtendedArtifactHandler(
                        art);
            }
        return extendedArtifactHandler;
    }

    @Override
    public MavenProject getMavenProject(Artifact pArtifact,
            MavenSession pSession) throws MojoExecutionException {
        try {
            List<ArtifactRepository> repos = pSession.getCurrentProject()
                    .getRemoteArtifactRepositories();
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest(
                    pSession.getProjectBuildingRequest())
                            // We don't want to execute any plugin here
                            .setProcessPlugins(false)
                            // It's not this plugin job to validate this pom.xml
                            .setValidationLevel(
                                    ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                            // Use the repositories configured for the built
                            // project instead of the global Maven ones
                            .setRemoteRepositories(repos);
            // Note: build() will automatically get the POM artifact
            // corresponding to the passed artifact.
            ProjectBuildingResult result = projectBuilder.build(pArtifact,
                    request);
            return result.getProject();
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(String
                    .format("Failed to build project for [%s]", pArtifact), e);
        }
    }

    @Override
    public boolean isFilePresent(URL fileUrl) {
        File file;
        try {
            file = new File(fileUrl.toURI());
        } catch (URISyntaxException e) {
            LOG.debug("", e);
            file = new File(fileUrl.getPath());
        }
        return file.isFile() && file.canRead();
    }

    @Override
    public boolean isP2CompositeRepository(URL repository) {

        URL compositeUrl;
        try {
            compositeUrl = new URL(
                    repository.toExternalForm() + "/" + COMPOSITE_INDEX);
        } catch (MalformedURLException e) {
            LOG.warn("", e);
            return false;
        }
        return isResourcePresent(compositeUrl);
    }

    @Override
    public boolean isResourcePresent(URL resourceURL) {
        try {
            URLConnection connection = resourceURL.openConnection();
            if (connection instanceof HttpURLConnection) {
                return isResourcePresentOnWebServer(
                        (HttpURLConnection) connection);
            } else
                return isFilePresent(resourceURL);
        } catch (IOException exception) {
            LOG.debug("Resource wasn't found " + resourceURL, exception);
            return false;
        }
    }

    @Override
    public boolean isResourcePresentOnWebServer(
            HttpURLConnection httpConnection) {
        try {
            httpConnection
                    .setConnectTimeout(CommonMojoConstants.DEFAULT_TIMEOUT);
            httpConnection.setReadTimeout(CommonMojoConstants.DEFAULT_TIMEOUT);
            httpConnection.setRequestMethod("GET");
            int responseCode = httpConnection.getResponseCode();
            return 200 <= responseCode && responseCode <= 399;
        } catch (IOException exception) {
            LOG.debug("", exception);
            return false;
        }
    }

    @Override
    public MavenProject loadProject(Artifact artifact, MavenSession session,
            boolean pResolveDependencies) throws ProjectBuildingException {
        ProjectBuildingRequest request = session.getProjectBuildingRequest();
        request.setResolveDependencies(pResolveDependencies);
        request.getInactiveProfileIds().add("when-building-java-using-java8");
        return projectBuilder.build(artifact, request).getProject();
    }

    @Override
    public Archiver lookupArchiver(String pPackaging) {
        return archiversMap.get(pPackaging);
    }

    @Override
    public URLResourceBySizeHolder newResourceStateHolder(URL pResourceURL,
            Path pCacheDir) throws IOException {
        if (!isResourcePresent(pResourceURL)) {
            throw new IllegalArgumentException(
                    "URL does not exist or cannot be read " + pResourceURL);
        }
        try {
            URL url = whichSourceToUse(pResourceURL, pCacheDir);
            return new URLResourceBySizeHolder(url);
        } catch (IOException e) {
            throw new IOException(
                    "URL does not exist or cannot be read " + pResourceURL, e);
        }
    }

    @Override
    public File resolveArtifact(Artifact pArtifact,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException {

        return resolveArtifact(pArtifact.getArtifactId(),
                pArtifact.getGroupId(), pArtifact.getVersion(),
                pArtifact.getType(), pArtifact.getClassifier(),
                pRepositorySystem, pRemoteRepositories, pLocalRepository)
                        .getFile();
    }

    @Override
    public Artifact resolveArtifact(BundleRef pBundleRef,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException {
        return resolveArtifact(pBundleRef.getArtifactId(),
                pBundleRef.getGroupId(), pBundleRef.getVersion(),
                pBundleRef.getType(), pBundleRef.getClassifier(),
                pRepositorySystem, pRemoteRepositories, pLocalRepository);
    }

    @Override
    public Artifact resolveArtifact(String pArtifactId, String pGroupId,
            String pVersion, String pType, String pClassifiers,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException {
        DefaultArtifact artifact = new DefaultArtifact(pGroupId, pArtifactId,
                pVersion, "compile", pType, pClassifiers,
                getArtifactHandler(pType));
        ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                .setArtifact(artifact)
                .setRemoteRepositories(pRemoteRepositories)
                .setLocalRepository(pLocalRepository);
        ArtifactResolutionResult resolutionResult = pRepositorySystem
                .resolve(request);
        if (resolutionResult.hasExceptions()) {
            LOG.warn("Error have occurred while resolvin the artifact: {}",
                    artifact, resolutionResult.getExceptions().get(0));
            return null;
        }
        if (resolutionResult.hasMissingArtifacts()) {
            LOG.warn("Could not resolve artifact: {}", artifact);
            return null;
        }
        return resolutionResult.getArtifacts().iterator().next();
    }

    @Override
    public void saveToFile(Set<Artifact> artifacts, File filePath)
            throws IOException {
        List<String> buffer = new ArrayList<>(artifacts.size());
        for (Artifact artifact : artifacts) {
            buffer.add(artifact.toString());
        }
        try {
            Files.write(filePath.toPath(), buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Error when loading the artifact set file at "
                    + filePath.getPath(), e);
        }
    }

    @Override
    public File searchForLatestArtifactFile(Artifact artifact,
            RepositorySystem pRepositorySystem,
            List<ArtifactRepository> pRemoteRepositories,
            ArtifactRepository pLocalRepository) throws IOException {
        LOG.info("Searching for latest built artifact on local repository for '"
                + artifact.getArtifactId() + "'.");

        File inputFile = resolveArtifact(artifact, pRepositorySystem,
                pRemoteRepositories, pLocalRepository);
        if (inputFile == null || inputFile.isDirectory()) {
            LOG.info("A valid artifact for " + artifact.getArtifactId()
                    + " was not found in local repository.");
            return null;
        }
        LOG.info("Found the following latest artifact file: "
                + inputFile.getPath());
        return inputFile;
    }

    @Override
    public boolean validateMavenDependency(Artifact pDependency,
            ArtifactFilter pArtifactFilter) {
        if ("pom".equals(pDependency.getType())) {
            LOG.warn("POM project is not allowed, ignoring '"
                    + pDependency.getArtifactId() + "'.");
            return false;
        }
        if (pDependency.getClassifier() != null
                && "third-party".equals(pDependency.getClassifier())) {
            return false;
        }

        if (pDependency.getFile() == null) {
            LOG.debug(
                    "Artifact {} was ignored from dependency selection since it was not resolved.",
                    pDependency.getArtifactId());
            return false;
        }

        return pArtifactFilter.include(pDependency);
    }

    @Override
    public URL whichSourceToUse(URL pUrl, Path pCacheDir) // NOSONAR
            throws IOException {
        if (pUrl.getProtocol().endsWith(CommonMojoConstants.URL_SCHEME_FILE)) {
            return pUrl;
        }
        if (pCacheDir == null || !pCacheDir.toFile().exists()) {
            return pUrl;
        }
        String fileName = pUrl.getFile()
                .substring(pUrl.getFile().lastIndexOf('/') + 1);
        Path cachedFile = pCacheDir.resolve(fileName);
        if (!cachedFile.toFile().exists()) {
            return pUrl;
        }
        try {
            if (Arrays.equals(URLResourceBySizeHolder.hash(pUrl),
                    URLResourceBySizeHolder
                            .hash(cachedFile.toFile().toURI().toURL()))) {
                return cachedFile.toFile().toURI().toURL();
            }
        } catch (IOException e) {
            throw new IOException(
                    "Failure while resolving the source artifact URL.", e);
        }
        return pUrl;
    }

}
