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
package br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.internal;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;
import br.com.c8tech.tools.maven.osgi.lib.mojo.archivers.AbstractSubsystemArchiver;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.ArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.BundleRef;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.MavenArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.MavenArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.P2ArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.P2ArtifactSets;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.PropertiesArtifactSet;
import br.com.c8tech.tools.maven.osgi.lib.mojo.beans.VersionConverter;
import br.com.c8tech.tools.maven.osgi.lib.mojo.handlers.ExtendedArtifactHandler;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.ArtifactTracker;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.ArtifactTrackerManager;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.ArtifactTrackerManagerBuilder;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DependenciesHelper;
import io.takari.incrementalbuild.BuildContext;
import io.takari.incrementalbuild.Output;
import io.takari.incrementalbuild.Resource;
import io.takari.incrementalbuild.ResourceMetadata;
import io.takari.incrementalbuild.ResourceStatus;

public final class DefaultArtifactTrackerManager
        implements ArtifactTrackerManager {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultArtifactTrackerManager.class);

    private ArtifactFilter artifactFilter;

    private final boolean assemblyUrlProtocolAllowed;

    private final String cachedFileNamePattern;

    private final Path cacheDirPath;

    private final boolean considerOptionalDependencies;

    private final boolean considerTransitiveDependencies;

    private final DependenciesHelper dependenciesHelper;

    private final List<String> excludedDependencies;

    private final boolean groupingByTypeDirectory;

    private final MavenArtifactSets mavenArtifactSets;

    private final MavenProject mavenProject;

    private final MavenSession mavenSession;

    private final boolean offline;

    private final P2ArtifactSets p2ArtifactSets;

    private final boolean packOnTheFlyAllowed;

    private final boolean previousCachingRequired;

    private final PropertiesArtifactSet propertiesArtifactSet;

    private final RepositorySystem repositorySystem;

    private final Set<String> scopes;

    private final Set<ArtifactTracker> toBeProcessed = new TreeSet<>();

    private final boolean verbose;

    public DefaultArtifactTrackerManager(
            ArtifactTrackerManagerBuilder pBuilder) {
        dependenciesHelper = pBuilder.getDependenciesHelper();
        cacheDirPath = pBuilder.getCacheDirPath();
        repositorySystem = pBuilder.getRepositorySystem();
        mavenSession = pBuilder.getMavenSession();
        mavenProject = pBuilder.getMavenProject();
        assemblyUrlProtocolAllowed = pBuilder.isAssemblyUrlProtocolAllowed();
        groupingByTypeDirectory = pBuilder.isGroupingByTypeDirectory();
        scopes = pBuilder.getScopes();
        excludedDependencies = pBuilder.getExcludedDependencies();
        verbose = pBuilder.isVerbose();
        offline = pBuilder.isOffline();
        previousCachingRequired = pBuilder.isPreviousCachingRequired();
        packOnTheFlyAllowed = pBuilder.isPackOnTheFlyAllowed();
        p2ArtifactSets = pBuilder.getP2ArtifactSets();
        mavenArtifactSets = pBuilder.getMavenArtifactSets();
        propertiesArtifactSet = pBuilder.getPropertiesArtifactSet();
        considerTransitiveDependencies = pBuilder
                .isConsiderTransitiveDependencies();
        considerOptionalDependencies = pBuilder
                .isConsiderOptionalDependencies();
        cachedFileNamePattern = pBuilder.getCachedFileNamePattern();
        artifactFilter = pBuilder.getArtifactFilter();
    }

    private void addP2ArtifactTracker(P2ArtifactTracker pArtifactTracker) {
        toBeProcessed.add(pArtifactTracker);
    }

    /**
     * This method will check if the name of the file that will be cached will
     * need any adjustment.
     * 
     */
    private String adjustedArtifactFilePath(String pArtifactId,
            String pClassifier, String pManifestSN, String pVersion,
            String pExtension, String pCachedFileNamePattern) {
        String result;
        if (pCachedFileNamePattern == null
                || pCachedFileNamePattern.isEmpty()) {
            result = CommonMojoConstants.CACHED_FILE_PATTERN_DEFAULT_FINALNAME;
        } else {
            result = pCachedFileNamePattern;
        }

        if (pClassifier == null || pClassifier.isEmpty()) {
            int idx = result.indexOf(
                    CommonMojoConstants.CACHED_FILE_PATTERN_CLASSIFIER);
            if (idx != -1 && idx > 0) {
                char car = result.charAt(idx - 1);
                result = result.replaceAll(car
                        + CommonMojoConstants.CACHED_FILE_PATTERN_CLASSIFIER,
                        "");
            }
        } else {
            result = result.replaceAll(
                    CommonMojoConstants.CACHED_FILE_PATTERN_CLASSIFIER,
                    pClassifier);
        }
        result = result.replaceAll(CommonMojoConstants.CACHED_FILE_PATTERN_NAME,
                pArtifactId);
        result = result.replaceAll(
                CommonMojoConstants.CACHED_FILE_PATTERN_SYMBOLIC_NAME,
                pManifestSN);

        if (pVersion.endsWith("-SNAPSHOT")) {
            long instant = System.currentTimeMillis();
            pVersion = pVersion.replace("-SNAPSHOT",
                    "." + Long.toString(instant));
        }
        result = result.replaceAll(
                CommonMojoConstants.CACHED_FILE_PATTERN_VERSION, pVersion);
        result = result.replaceAll(
                CommonMojoConstants.CACHED_FILE_PATTERN_EXTENSION, pExtension);

        return result;

    }

    private Path calculateArtifactCachedFilePath(Path pCacheDir,
            ExtendedArtifactHandler pExtendedArtifactHandler,
            Artifact pDependency, BundleRef pBundleConfig, String pManifestSN,
            String pManifestVersion) {
        String adjustedCachedFilePath;

        Path baseDir = isGroupingByTypeDirectory()
                ? pCacheDir.resolve(pExtendedArtifactHandler.getDirectory())
                : pCacheDir;
        if (pBundleConfig != null && pBundleConfig.getCopyName() != null
                && !pBundleConfig.getCopyName().isEmpty()) {
            adjustedCachedFilePath = pBundleConfig.getCopyName();
        } else {

            adjustedCachedFilePath = adjustedArtifactFilePath(
                    pDependency.getArtifactId(), pDependency.getClassifier(),
                    pManifestSN,
                    pManifestVersion != null ? pManifestVersion
                            : pDependency.getVersion(),
                    pExtendedArtifactHandler.getExtension(),
                    getCachedFileNamePattern());
        }
        return baseDir.resolve(adjustedCachedFilePath);
    }

    private Path calculateWorkspaceArtifactOriginalFilePath(
            ExtendedArtifactHandler pExtendedArtifactHandler,
            Artifact pDependency, BundleRef pBundleConfig, String pManifestSN,
            String pManifestVersion) {
        String adjustedOriginalFilePath;

        File baseDir = pExtendedArtifactHandler
                .getOutputDirectory(pDependency.getFile());
        String adjustedVersion;
        if (pManifestVersion != null) {
            if (VersionConverter.isOSGiVersion(pManifestVersion)) {
                adjustedVersion = VersionConverter
                        .fromOsgiVersion(pManifestVersion).toMaven()
                        .getVersionString();

            } else {
                if (pManifestVersion.endsWith("-SNAPSHOT")) {
                    long instant = System.currentTimeMillis();
                    adjustedVersion = pManifestVersion.replace("-SNAPSHOT",
                            "." + Long.toString(instant));
                } else

                    adjustedVersion = pManifestVersion;
            }
        } else {
            adjustedVersion = pDependency.getVersion();
        }

        adjustedOriginalFilePath = adjustedArtifactFilePath(
                pDependency.getArtifactId(), pDependency.getClassifier(),
                pManifestSN, adjustedVersion,
                pExtendedArtifactHandler.getExtension(),
                getCachedFileNamePattern());
        return baseDir.toPath().resolve(adjustedOriginalFilePath);
    }

    protected boolean checkURL(URL pURL) {

        try {
            URLConnection connection = pURL.openConnection();
            if (connection instanceof HttpsURLConnection
                    || connection instanceof HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection) connection;
                // huc.setInstanceFollowRedirects(false); // NOSONAR
                huc.setRequestMethod("HEAD");
                huc.connect();
                return huc.getResponseCode() >= 200
                        && huc.getResponseCode() < 299;
            } else
                if (pURL.getProtocol().startsWith("file")) {
                    File dir = new File(pURL.getPath());
                    return dir.exists();
                }

        } catch (IOException e) {
            LOG.warn("Failure validating the URL", e);
            return false;
        }
        return false;
    }

    /**
     * Copy files from maven repository to cache directory.
     *
     * @param pBuildContext
     *                          The build context object.
     * @throws MojoExecutionException
     *                                    When any problem is found.
     */
    public final void copyMavenArtifactsToCache(BuildContext pBuildContext) // NOSONAR
            throws MojoExecutionException {
        Path sourcePath;
        Path targetPath;
        int count = 0;
        Set<ArtifactTracker> artifactsToCopy = new HashSet<>();

        artifactsToCopy.addAll(getMavenArtifactTrackers());

        artifactsToCopy.addAll(getPropertiesArtifactTrackers());

        if (artifactsToCopy.isEmpty()) {
            LOG.warn(
                    "Skipping downloading artifacts from maven repositories for project "
                            + getMavenProject().getArtifactId()
                            + " since there are any artifact declared in pom.");
            return;
        }
        LOG.info("Preparing for caching artifacts from maven repositories...");

        for (ArtifactTracker artifactToEmbed : artifactsToCopy) {
            if (artifactToEmbed.isWorkspaceProject()) {
                sourcePath = Paths.get(artifactToEmbed.getDownloadUrl());
                try {
                    if (packOnTheFlyAllowed
                            && artifactToEmbed.getTypeHandler()
                                    .isPackOnTheFlyAllowed()
                            && (!sourcePath.toFile().exists()
                                    || Files.deleteIfExists(sourcePath))) {

                        packWorkspaceProjectOnTheFly(artifactToEmbed,
                                sourcePath);

                    }
                } catch (IOException e) {
                    throw new MojoExecutionException(
                            "Failure while caching artifact", e);
                }
            } else {
                sourcePath = artifactToEmbed.getOriginalFile().toPath();

            }

            if (artifactToEmbed.isToBeCached()) {
                targetPath = artifactToEmbed.getCachedFilePath();
                if (copyNormalizedArtifact(pBuildContext, sourcePath,
                        targetPath, artifactToEmbed.isWorkspaceProject()))
                    count++;
            } else {
                LOG.warn("    Skipping caching non-cacheable artifact '"
                        + artifactToEmbed.getArtifactId() + "'");
            }
        }

        LOG.info("Finished copying of {} from maven repositories.", // NOSONAR
                CommonMojoConstants.MSG_CHOICE_ARTIFACT // NOSONAR
                        .format(new Object[] { count }));// NOSONAR
    }

    private boolean copyNormalizedArtifact(BuildContext pBuildContext,
            Path pSourcePath, Path pTargetPath, boolean pWorkspaceProject) {
        ResourceMetadata<File> resourceMetadata = pBuildContext
                .registerInput(pSourcePath.toFile());

        if (resourceMetadata.getStatus() != ResourceStatus.UNMODIFIED
                || pWorkspaceProject) {
            Resource<File> meta = resourceMetadata.process();
            Output<File> output = meta.associateOutput(pTargetPath.toFile());
            try {
                Files.createDirectories(pTargetPath.getParent());
                Files.copy(pSourcePath, output.newOutputStream());
                if (isVerbose()) {
                    LOG.info(
                            "    Copied maven artifact file from '{}' to '{}'.",
                            pSourcePath, pTargetPath);
                }
            } catch (IOException e) {
                LOG.warn(
                        "An error have occurred while copying the file:'{}'. {}",
                        pTargetPath, e);
                return false;
            }
            return true;
        } else
            return false;

    }

    private PropertiesArtifactTracker createPropertyArtifactTracker(
            Artifact pArtifact, BundleRef pBundleRef)
            throws MojoExecutionException {
        String url = null;
        try {
            url = pArtifact.getFile().toURI().toURL().toString();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        return PropertiesArtifactTracker
                .builder(getCacheDirPath(), isGroupingByTypeDirectory(),
                        isPreviousCachingRequired())
                .withArtifactId(pArtifact.getArtifactId())
                .withGroupId(pArtifact.getGroupId())
                .withVersion(pArtifact.getVersion())
                .withCachedFilePath(getCacheDirPath()
                        .resolve((pBundleRef.getCopyName() != null
                                && !pBundleRef.getCopyName().isEmpty())
                                        ? pBundleRef.getCopyName()
                                        : pArtifact.getFile().getName()))
                .withOriginalFile(pArtifact.getFile()).withDownloadUrl(url)
                .withType(pArtifact.getType()).withToBeCached(true)
                .withToBeEmbedded(false)
                .withClassifier(pArtifact.getClassifier()).build();
    }

    private BundleRef findArtifact(String pGroupId, String pArtifactId) {
        BundleRef found = null;
        if (mavenArtifactSets == null || mavenArtifactSets.isEmpty()) {
            return null;
        }
        for (MavenArtifactSet mavenArtifactSet : getMavenArtifactSets()
                .getMavenArtifactSets()) {
            found = mavenArtifactSet.findArtifact(pGroupId, pArtifactId);
            if (found != null)
                break;
        }
        return found;
    }

    @Override
    public Set<ArtifactTracker> getAllArtifactTrackers() {
        return ImmutableSet.copyOf(toBeProcessed);
    }

    @Override
    public ArtifactFilter getArtifactFilter() {
        return artifactFilter;
    }

    @Override
    public String getCachedFileNamePattern() {
        return cachedFileNamePattern;
    }

    @Override
    public Path getCacheDirPath() {
        return cacheDirPath;
    }

    protected DependenciesHelper getDependenciesHelper() {
        return dependenciesHelper;
    }

    @Override
    public List<String> getExcludedDependencies() {
        return excludedDependencies;
    }

    @Override
    public ArtifactRepository getLocalRepository() {
        return mavenSession.getLocalRepository();
    }

    @Override
    public MavenArtifactSets getMavenArtifactSets() {
        return mavenArtifactSets;
    }

    @Override
    public Set<ArtifactTracker> getMavenArtifactTrackers() {
        Set<ArtifactTracker> set = Sets.filter(toBeProcessed,
                Predicates.instanceOf(MavenArtifactTracker.class));
        if (!set.isEmpty()) {
            return ImmutableSet.copyOf(set);
        }
        return ImmutableSet.of();
    }

    @Override
    public MavenProject getMavenProject() {
        return mavenProject;
    }

    @Override
    public MavenSession getMavenSession() {
        return mavenSession;
    }

    @Override
    public P2ArtifactSets getP2ArtifactSets() {
        return p2ArtifactSets;
    }

    @Override
    public Set<ArtifactTracker> getP2ArtifactTrackers() {
        Set<ArtifactTracker> set = Sets.filter(toBeProcessed,
                Predicates.instanceOf(P2ArtifactTracker.class));
        if (!set.isEmpty()) {
            return ImmutableSet.copyOf(set);
        }
        return ImmutableSet.of();
    }

    @Override
    public PropertiesArtifactSet getPropertiesArtifactSet() {
        return propertiesArtifactSet;
    }

    @Override
    public Set<ArtifactTracker> getPropertiesArtifactTrackers() {
        Set<ArtifactTracker> set = Sets.filter(toBeProcessed,
                Predicates.instanceOf(PropertiesArtifactTracker.class));
        if (!set.isEmpty()) {
            return ImmutableSet.copyOf(set);
        }
        return ImmutableSet.of();
    }

    @Override
    public List<ArtifactRepository> getRemoteRepositories() {
        return mavenProject.getRemoteArtifactRepositories();
    }

    @Override
    public RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public boolean isAssemblyUrlProtocolAllowed() {
        return assemblyUrlProtocolAllowed;
    }

    @Override
    public boolean isConsiderOptionalDependencies() {
        return considerOptionalDependencies;
    }

    @Override
    public boolean isConsiderTransitiveDependencies() {
        return considerTransitiveDependencies;
    }

    @Override
    public boolean isGroupingByTypeDirectory() {
        return groupingByTypeDirectory;
    }

    public boolean isOffline() {
        return offline;
    }

    @Override
    public boolean isPackOnTheFlyAllowed() {
        return packOnTheFlyAllowed;
    }

    @Override
    public boolean isPreviousCachingRequired() {
        return previousCachingRequired;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public Set<ArtifactTracker> lookupEmbeddableArtifactTrackers() {
        return lookupEmbeddableArtifactTrackers(null);
    }

    @Override
    public Set<ArtifactTracker> lookupEmbeddableArtifactTrackers(
            List<String> pExcludedPackagingTypes) {
        Set<ArtifactTracker> result = new HashSet<>();
        for (ArtifactTracker artifactDetail : toBeProcessed) {
            if (!artifactDetail.isToBeEmbedded()) {
                continue;
            }
            if (pExcludedPackagingTypes != null) {
                if (!pExcludedPackagingTypes
                        .contains(artifactDetail.getType())) {
                    result.add(artifactDetail);
                }
            } else
                result.add(artifactDetail);
        }
        return ImmutableSet.copyOf(result);
    }

    @Override
    public Set<ArtifactTracker> lookupNotEmbeddableArtifactTrackers() {
        return lookupNotEmbeddableArtifactTrackers(null);
    }

    @Override
    public final Set<ArtifactTracker> lookupNotEmbeddableArtifactTrackers(
            List<String> pExcludedPackagingTypes) {
        Set<ArtifactTracker> result = new HashSet<>();
        for (ArtifactTracker artifactDetail : toBeProcessed) {
            if (!artifactDetail.isToBeEmbedded()
                    && (pExcludedPackagingTypes != null
                            ? !pExcludedPackagingTypes
                                    .contains(artifactDetail.getType())
                            : true)) {
                result.add(artifactDetail);
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private void packWorkspaceProjectOnTheFly(ArtifactTracker pArtifactTracker,
            Path pArtifactPath) throws MojoExecutionException {
        try {
            Files.createDirectories(pArtifactPath.getParent());
            File wdir = pArtifactTracker.getTypeHandler()
                    .getWorkspaceDirectory(pArtifactTracker.getOriginalFile());

            Archiver archiver = getDependenciesHelper().lookupArchiver(
                    pArtifactTracker.getTypeHandler().getPackaging());
            File mf = pArtifactTracker.getTypeHandler()
                    .lookupManifestFileInProjectDirectory(
                            pArtifactTracker.toArtifact())
                    .toFile();
            if (archiver instanceof AbstractSubsystemArchiver) {
                ((AbstractSubsystemArchiver) archiver).setManifest(mf);
            } else
                if (archiver instanceof JarArchiver) {
                    ((JarArchiver) archiver).setManifest(mf);
                }

            archiver.setDestFile(pArtifactPath.toFile());
            archiver.addFileSet(new DefaultFileSet(wdir));
            archiver.createArchive();
            pArtifactTracker.setCached();
        } catch (ArchiverException | IOException e) {
            throw new MojoExecutionException(
                    "Failure while packaging a workspace project.", e);
        }
    }

    private ArtifactTracker resolve(Artifact dependencyArtifact,
            Set<String> pEmbeddableScopes) throws MojoExecutionException {
        if (!getDependenciesHelper().validateMavenDependency(dependencyArtifact,
                artifactFilter)) {
            return null;
        }
        if (isVerbose())
            LOG.info(" Resolving artifact '{}'.",
                    dependencyArtifact.getArtifactId());

        ExtendedArtifactHandler handler = getDependenciesHelper()
                .getArtifactHandler(dependencyArtifact.getType());
        if (handler == null) {
            throw new MojoExecutionException(
                    "Couldn't find artifact handler for "
                            + dependencyArtifact.getType());
        }
        MavenArtifactTracker artifactTracker = null;
        try {
            File inputFile = dependencyArtifact.getFile();
            if (inputFile != null
                    && handler.isTypeValid(dependencyArtifact.getType())) {
                BundleRef bundleConfig = findArtifact(
                        dependencyArtifact.getGroupId(),
                        dependencyArtifact.getArtifactId());

                if (isPackOnTheFlyAllowed()
                        && handler.isWorkspaceProject(inputFile)) {

                    artifactTracker = resolveMavenArtifactOnWorkspace(
                            dependencyArtifact, bundleConfig, pEmbeddableScopes,
                            handler);
                } else
                    if (handler.isExtensionValid(inputFile.toPath())) {
                        artifactTracker = resolveMavenArtifact(
                                dependencyArtifact, bundleConfig,
                                pEmbeddableScopes, handler);
                    }

            } else {
                LOG.warn("The artifact " + dependencyArtifact.getArtifactId()
                        + " is not valid since it was not resolved.");
            }
        } catch (IOException e) {
            LOG.warn("An error was found while resolving the artifact '{}' {}.",
                    dependencyArtifact.getArtifactId(), e);
        }
        return artifactTracker;

    }

    private int resolveCustomMavenArtifactSet(Set<Artifact> pAllDependencies, // NOSONAR
            ArtifactSet pArtifactSet, Set<String> pEmbeddableScopes)
            throws MojoExecutionException {
        int count = 0;
        if (pArtifactSet == null || pArtifactSet.isEmpty()) {
            return 0;
        }

        for (BundleRef bundleRef : pArtifactSet.getImmutableArtifactSet()) {
            Optional<Artifact> found = Optional.empty();

            if (bundleRef.getVersion() != null) {
                found = pAllDependencies.stream().filter(p -> p.getGroupId()
                        .equals(bundleRef.getGroupId())
                        && p.getArtifactId().equals(bundleRef.getArtifactId())
                        && p.getVersion().equals(bundleRef.getVersion()))
                        .findFirst();
            }
            if (!found.isPresent()) {
                found = pAllDependencies.stream().filter(p -> p.getGroupId()
                        .equals(bundleRef.getGroupId())
                        && p.getArtifactId().equals(bundleRef.getArtifactId()))
                        .findFirst();
            }

            if (found.isPresent()) {
                Artifact art = found.get();
                ArtifactTracker artifactTracker = resolve(art,
                        pEmbeddableScopes);
                if (artifactTracker != null) {
                    toBeProcessed.add(artifactTracker);
                    count++;
                    pAllDependencies.remove(art);
                }
                continue;
            } else
                if (bundleRef.getVersion() == null) {
                    throw new MojoExecutionException(
                            "You must declare a dependency in the POM for the '"
                                    + bundleRef.getArtifactId() + "'");
                }

            Artifact artifact = null;
            try {
                artifact = getDependenciesHelper().resolveArtifact(
                        bundleRef.getArtifactId(), bundleRef.getGroupId(),
                        bundleRef.getVersion(), bundleRef.getType(),
                        bundleRef.getClassifier(), repositorySystem,
                        getRemoteRepositories(), getLocalRepository());
                if (artifact != null) {
                    ArtifactTracker artifactTracker = resolve(artifact,
                            pEmbeddableScopes);
                    toBeProcessed.add(artifactTracker);

                    pArtifactSet.getResolvedArtifacts().add(artifact);
                    count++;
                }
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failure occurred while resolving an artifact defined in mavenArtifactSets",
                        e);
            }
        }
        return count;
    }

    private MavenArtifactTracker resolveMavenArtifact(Artifact pDependency, // NOSONAR
            BundleRef pBundleConfig, Set<String> pEmbeddableScopes,
            ExtendedArtifactHandler pExtendedArtifactHandler)
            throws MojoExecutionException, IOException {
        MavenArtifactTracker result = null;
        boolean valid = false;
        Map<String, String> jarManifestHeaders;
        String msn = null;
        String mversion = null;
        Path adjustedCachedFilePath;

        Path cacheDir = pBundleConfig != null ? pBundleConfig.getCachePath()
                : getCacheDirPath();

        if (pExtendedArtifactHandler.isManifestFileRequired()) {
            jarManifestHeaders = pExtendedArtifactHandler
                    .extractManifestHeadersFromArchive(pDependency.getFile());
            valid = pExtendedArtifactHandler
                    .isArtifactManifestValid(jarManifestHeaders);
            if (valid) {
                msn = jarManifestHeaders.get(
                        pExtendedArtifactHandler.defaultSymbolicNameHeader());

                int idx = msn.indexOf(';');
                if (idx != -1) {
                    msn = msn.substring(0, idx);
                }

                mversion = jarManifestHeaders
                        .get(pExtendedArtifactHandler.defaultVersionHeader());

                if (mversion.endsWith("-SNAPSHOT")) {
                    long instant = System.currentTimeMillis();
                    mversion = mversion.replace("-SNAPSHOT",
                            "." + Long.toString(instant));
                }

            } else {
                LOG.warn("Invalid manifest for artifact {}",
                        pDependency.getArtifactId());
                return null;
            }
        } else {
            jarManifestHeaders = Collections.emptyMap();
        }

        adjustedCachedFilePath = calculateArtifactCachedFilePath(cacheDir,
                pExtendedArtifactHandler, pDependency, pBundleConfig, msn,
                mversion);

        if (mversion == null) {
            if (pBundleConfig != null && pBundleConfig.getVersion() != null
                    && !pBundleConfig.getVersion().isEmpty()) {
                mversion = pBundleConfig.getVersion();
            } else
                mversion = pDependency.getVersion();
        }

        result = MavenArtifactTracker
                .builder(cacheDir, isGroupingByTypeDirectory(),
                        isPreviousCachingRequired())
                .withManifestMap(jarManifestHeaders)
                .withArtifactId(pDependency.getArtifactId())
                .withClassifier(pDependency.getClassifier())
                .withGroupId(pDependency.getGroupId())
                .withOptional(pDependency.isOptional())
                .withType(pDependency.getType())
                .withCachedFilePath(adjustedCachedFilePath)
                .withExtendedArtifactHandler(getDependenciesHelper()
                        .getArtifactHandler(pDependency.getType()))
                .withScope(pDependency.getScope())
                .withOriginalFile(pDependency.getFile()).withVersion(mversion)
                .withSymbolicName(msn).withWorkspaceProject(false)
                .withStartLevel(
                        pBundleConfig != null ? pBundleConfig.getStartLevel()
                                : 0)
                .withToBeCached(scopes.contains(pDependency.getScope())
                        || pEmbeddableScopes.contains(pDependency.getScope()))
                .withToBeEmbedded(pEmbeddableScopes.isEmpty() ? false
                        : pEmbeddableScopes.contains(pDependency.getScope()))
                .build();
        result.setCached();

        return result;
    }

    private MavenArtifactTracker resolveMavenArtifactOnWorkspace(
            Artifact pDependencyArtifact, BundleRef pBundleConfig,
            Set<String> pEmbeddableScopes,
            ExtendedArtifactHandler pExtendedArtifactHandler)
            throws MojoExecutionException, IOException {
        MavenArtifactTracker result = null;
        Map<String, String> jarManifestHeaders = null;
        Path adjustedCachedFilePath = null;
        Path adjustedOriginalFilePath = null;
        String msn = null;
        String mversion = null;
        if (isVerbose())
            LOG.info("   Found workspace project:'{}'",
                    pDependencyArtifact.getArtifactId());
        if (pExtendedArtifactHandler.isManifestFileRequired()) {
            Path manifestFile = pExtendedArtifactHandler
                    .lookupManifestFileInProjectDirectory(pDependencyArtifact);
            if (manifestFile == null || !manifestFile.toFile().exists()) {
                LOG.warn("    A manifest file was not found for '{}'",
                        pDependencyArtifact.getArtifactId());
                return null;
            }
            jarManifestHeaders = pExtendedArtifactHandler
                    .extractHeadersFromManifestFile(manifestFile.toFile());
            msn = jarManifestHeaders
                    .get(pExtendedArtifactHandler.defaultSymbolicNameHeader());
            mversion = jarManifestHeaders
                    .get(pExtendedArtifactHandler.defaultVersionHeader());
        }

        if (mversion == null) {
            mversion = pBundleConfig != null
                    && !pBundleConfig.getVersion().isEmpty()
                            ? pBundleConfig.getVersion()
                            : pDependencyArtifact.getVersion();
        }
        if (msn == null) {
            msn = pBundleConfig != null
                    && !pBundleConfig.getArtifactId().isEmpty()
                            ? pBundleConfig.getArtifactId()
                            : pDependencyArtifact.getArtifactId();
        }

        Path cacheDir = pBundleConfig != null ? pBundleConfig.getCachePath()
                : getCacheDirPath();
        adjustedCachedFilePath = calculateArtifactCachedFilePath(cacheDir,
                pExtendedArtifactHandler, pDependencyArtifact, pBundleConfig,
                msn, mversion);
        adjustedOriginalFilePath = calculateWorkspaceArtifactOriginalFilePath(
                pExtendedArtifactHandler, pDependencyArtifact, pBundleConfig,
                msn, mversion);

        result = MavenArtifactTracker
                .builder(cacheDir, isGroupingByTypeDirectory(),
                        isPreviousCachingRequired())
                .withManifestMap(jarManifestHeaders)
                .withCachedFilePath(adjustedCachedFilePath)
                .withArtifactId(pDependencyArtifact.getArtifactId())
                .withClassifier(pDependencyArtifact.getClassifier())
                .withGroupId(pDependencyArtifact.getGroupId())
                .withOptional(pDependencyArtifact.isOptional())
                .withType(pDependencyArtifact.getType())
                .withExtendedArtifactHandler(getDependenciesHelper()
                        .getArtifactHandler(pDependencyArtifact.getType()))
                .withScope(pDependencyArtifact.getScope())
                .withOriginalFile(pDependencyArtifact.getFile())
                .withDownloadUrl(adjustedOriginalFilePath.toString())
                .withVersion(mversion).withWorkspaceProject(true)
                .withSymbolicName(msn).withToBeCached(true)
                .withStartLevel(
                        pBundleConfig != null ? pBundleConfig.getStartLevel()
                                : 0)
                .withToBeEmbedded(pEmbeddableScopes.isEmpty() ? false
                        : pEmbeddableScopes
                                .contains(pDependencyArtifact.getScope()))
                .build();

        result.setCached();

        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int resolveMavenArtifacts(Set<Artifact> pResolvedDependencies,
            Set<String> pEmbeddableScopes)
            throws MojoExecutionException, MojoFailureException {
        int count = 0;

        if (pResolvedDependencies == null) {
            throw new MojoFailureException(
                    "pResolvedDependencies can't be null");
        }

        Set<Artifact> allDependencies = new TreeSet<>(pResolvedDependencies);
        if (considerTransitiveDependencies
                && getMavenProject().getArtifacts() != null) {
            allDependencies.addAll(getMavenProject().getArtifacts());
        }
        if (!considerTransitiveDependencies
                && getMavenProject().getDependencyArtifacts() != null) {// NOSONAR
            allDependencies.addAll(getMavenProject().getDependencyArtifacts()); // NOSONAR
        }

        // traverse the maven artifact set from pom and resolve those that was
        // not declared as dependency
        for (MavenArtifactSet mavenArtifactSet : getMavenArtifactSets()
                .getMavenArtifactSets()) {

            if (mavenArtifactSet.isEmpty()) {
                continue;
            }
            if (mavenArtifactSet.getCacheDirectory() == null) {
                mavenArtifactSet.setCacheDirectory(getCacheDirPath());
            }
            count = count + resolveCustomMavenArtifactSet(allDependencies,
                    mavenArtifactSet, pEmbeddableScopes);
        }

        if (allDependencies.isEmpty()) {
            return count;
        }

        for (Artifact dependency : allDependencies) {

            ArtifactTracker artifactTracker = resolve(dependency,
                    pEmbeddableScopes);

            if (artifactTracker == null) {
                LOG.warn("   Ignoring " + dependency.getArtifactId()
                        + " because it is no valid.");
            } else {
                toBeProcessed.add(artifactTracker);
                count++;
            }
        }

        return count;
    }

    @Override
    public int resolveMavenArtifacts(Set<String> pEmbeddableScopes) // NOSONAR
            throws MojoExecutionException, MojoFailureException {

        return resolveMavenArtifacts(Collections.emptySet(), pEmbeddableScopes);
    }

    @Override
    public int resolveP2Artifacts(URL pP2LocalPoolUrl) // NOSONAR
            throws MojoExecutionException {
        if (p2ArtifactSets == null
                || p2ArtifactSets.getP2ArtifactSets().isEmpty()) {
            if (isVerbose()) {
                LOG.info("Skipping resolution of p2 artifacts for project "
                        + mavenProject.getArtifactId()
                        + " since no one was declared in its pom.xml.");
            }
            return 0;
        } else
            if (isVerbose()) {
                if (isOffline()) {
                    LOG.info(
                            "Resolving in offline mode the p2 artifacts for project "
                                    + mavenProject.getArtifactId());

                } else {
                    if (isVerbose())
                        LOG.info("Resolving p2 artifacts for project "
                                + mavenProject.getArtifactId());
                }
            }

        int count = 0;
        URL url;
        for (P2ArtifactSet p2ArtifactSet : p2ArtifactSets.getP2ArtifactSets()) {

            if (p2ArtifactSet.getCacheDirectory() == null) {
                p2ArtifactSet.setCacheDirectory(getCacheDirPath());
            }
            if (pP2LocalPoolUrl != null) {
                url = pP2LocalPoolUrl;
            } else
                if (isOffline()) {
                    try {
                        String path = p2ArtifactSet.getCacheDirectory()
                                .toString();
                        if (path.startsWith("/")) {
                            url = new URL("file://" + path);
                        } else {
                            url = new URL(path);
                        }
                    } catch (MalformedURLException e) {
                        throw new MojoExecutionException(
                                "Failure to find cache directory.", e);
                    }
                } else {
                    url = p2ArtifactSet.getRepositoryURL();
                }
            if (url == null || !checkURL(url)) {
                throw new MojoExecutionException(
                        "Invalid URL for p2 repository " + url);
            }
            try {
                p2ArtifactSet.setRepositoryURL(url.toString());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException(
                        "Failure to set the repository URL.", e);
            }
            count += resolveP2ArtifactSet(p2ArtifactSet);
        }
        if (isVerbose()) {
            String number = CommonMojoConstants.MSG_CHOICE_ARTIFACT
                    .format(new Object[] { count });
            LOG.info("   Resolved {} from p2 repositories.", number);
        }
        return count;
    }

    private int resolveP2ArtifactSet(P2ArtifactSet aP2ArtifactSet)
            throws MojoExecutionException {
        int count = 0;
        for (BundleRef bundleRef : aP2ArtifactSet.getImmutableArtifactSet()) {
            P2ArtifactTracker artifactTracker;
            artifactTracker = P2ArtifactTracker
                    .builder(aP2ArtifactSet.getDefaultGroupId(),
                            aP2ArtifactSet.getCacheDirectory(),
                            groupingByTypeDirectory, previousCachingRequired)
                    .withBundleRef(bundleRef).withToBeEmbedded(true).build();
            try {
                if (checkURL(new URL(artifactTracker.getDownloadUrl()))) {
                    addP2ArtifactTracker(artifactTracker);
                    artifactTracker.setCached();
                    count++;
                }
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failure occurred while resolving P2 artifacts.", e);
            }
        }
        return count;
    }

    @Override
    public int resolvePropertiesArtifactSet() throws MojoExecutionException {
        int count = 0;

        if (getPropertiesArtifactSet().size() == 0)
            return 0;

        for (BundleRef bundleRef : getPropertiesArtifactSet()
                .getImmutableArtifactSet()) {

            Artifact artifact;
            try {
                artifact = getDependenciesHelper().resolveArtifact(
                        bundleRef.getArtifactId(), bundleRef.getGroupId(),
                        bundleRef.getVersion(), bundleRef.getType(),
                        bundleRef.getClassifier(), repositorySystem,
                        getRemoteRepositories(), getLocalRepository());
                if (artifact != null) {
                    PropertiesArtifactTracker propertiesArtifactTracker = createPropertyArtifactTracker(
                            artifact, bundleRef);
                    toBeProcessed.add(propertiesArtifactTracker);
                    count++;
                }
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failure occurred while resolving an artifact defined in mavenArtifactSets",
                        e);
            }
        }
        return count;
    }

    @Override
    public ArtifactTracker searchByArtifactId(String pArtifactId) {
        for (ArtifactTracker artifactDetail : toBeProcessed) {
            if (artifactDetail.getArtifactId().equals(pArtifactId)) {
                return artifactDetail;
            }
        }
        return null;
    }

    @Override
    public ArtifactTracker searchByPath(String pPath) {
        for (ArtifactTracker artifactDetail : toBeProcessed) {
            if (artifactDetail.getCachedFilePath().startsWith(pPath)
                    || artifactDetail.getOriginalFile().toPath()
                            .startsWith(pPath)) {
                return artifactDetail;
            }
        }
        return null;
    }

    @Override
    public ArtifactTracker searchByType(String pPackageType) {
        for (ArtifactTracker artifactDetail : toBeProcessed) {
            if (artifactDetail.getType().equalsIgnoreCase(pPackageType)) {
                return artifactDetail;
            }
        }
        return null;
    }

}
