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
package br.com.c8tech.tools.maven.osgi.lib.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.rtinfo.RuntimeInformation;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;
import br.com.c8tech.tools.maven.osgi.lib.mojo.filters.FilterFactory;
import br.com.c8tech.tools.maven.osgi.lib.mojo.incremental.ArtifactTracker;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DependenciesHelper;
import br.com.c8tech.tools.maven.osgi.lib.mojo.services.DirectoryHelper;
import io.takari.incrementalbuild.Incremental;
import io.takari.incrementalbuild.Incremental.Configuration;
import io.takari.incrementalbuild.aggregator.AggregatorBuildContext;
import io.takari.incrementalbuild.aggregator.InputSet;

/**
 *
 * @author Cristiano Gavião
 *
 */
public abstract class AbstractCustomPackagingMojo
        extends org.apache.maven.plugin.AbstractMojo {

    private final boolean aggregator;

    /**
     * Base directory of the project.
     */
    @Parameter(required = true, property = "basedir", readonly = true)
    private File basedir;

    /**
     * Base directory of the project.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}",
            readonly = true)
    private File buildDir;

    /**
     * A naming pattern that will be used to rename an artifact's file that is
     * about to be cached when it doesn't has a standard name.
     * <p>
     * 
     * A standard maven name is formatted as:
     * <i>artifactId-classifier-version.extension</i>
     */
    @Parameter(required = false,
            defaultValue = CommonMojoConstants.CACHED_FILE_PATTERN_DEFAULT_FINALNAME)
    private String cachedFilePatternReplacement;

    /**
     * A classifier string to be used to compose the project's generated
     * artifact file name.
     */
    @Parameter()
    private String classifier;

    @Inject
    private DependenciesHelper dependenciesHelper;

    /**
     * The component that help with directory handling.
     */
    @Inject
    private DirectoryHelper directoryHelper;

    /**
     * A list of packaging types that a project must have in order to be allowed
     * to run this plugin.
     * <p>
     * Normally only the packaging types provided by the plugin is allowed.
     */
    @Parameter()
    private List<String> extraSupportedPackagings = new ArrayList<>();

    @Inject
    private FilterFactory filterFactory;

    /**
     * The filename of the generated artifact file.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true,
            readonly = true)
    private String finalName;

    @Parameter(required = true, property = "plugin", readonly = true)
    @Incremental(configuration = Configuration.ignore)
    // for Maven 3 only
    private PluginDescriptor pluginDescriptor;

    /**
     * The Maven project.
     */
    private final MavenProject project;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    @Inject
    private RepositorySystem repositorySystem;

    /**
     * The runtime information for Maven, used to retrieve Maven's version
     * number.
     */
    @Inject
    private RuntimeInformation runtimeInformation;

    /**
     * The Maven reactor session.
     * <p>
     */
    @Parameter(required = true, property = "session", readonly = true)
    @Incremental(configuration = Configuration.ignore)
    private MavenSession session;

    /**
     * A list of supported package type that this plugin must accept.
     * 
     * @see DEFAULT_SUPPORTED_PACKAGING
     */
    private final List<String> supportedPackagings = new ArrayList<>();

    /**
     * Set this to <code>true</code> to allow more information to be displayed
     * during the plugin execution.
     */
    @Parameter(defaultValue = "false")
    @Incremental(configuration = Configuration.ignore)
    private boolean verbose;

    /**
     * A path pointing to the plugin's work directory.
     */
    @Parameter(required = true,
            defaultValue = "${project.build.directory}/work")
    private File workDirectory;

    /**
     * The default constructor.
     *
     * @param project
     *                           the current project.
     * @param aggregatorMojo
     *                           Whether the concrete mojo is an aggregator one.
     * @param packagings
     *                           the packagings supported by the plugin.
     */
    protected AbstractCustomPackagingMojo(final MavenProject project,
            boolean aggregatorMojo, final String... packagings) {
        this.project = project;
        this.aggregator = aggregatorMojo;
        if (packagings != null) {
            this.supportedPackagings.addAll(Arrays.asList(packagings));
        }
    }

    /**
     * The default constructor.
     *
     * @param project
     *                       the current project.
     * @param packagings
     *                       the packagings supported by the plugin.
     */
    protected AbstractCustomPackagingMojo(final MavenProject project,
            final String... packagings) {
        this(project, false, packagings);
    }

    public static MessageFormat getMsgChoiceArtifact() {
        return CommonMojoConstants.MSG_CHOICE_ARTIFACT;
    }

    public final void addExtraSupportedPackaging(
            String extraSupportedPackagingStr) {
        extraSupportedPackagings.add(extraSupportedPackagingStr);
    }

    /**
     *
     * @throws IOException
     *                         When any IO related error have occurred.
     */
    protected final void cleanWorkDirectory() throws IOException {
        if (!getWorkDirectory().toFile().exists()) {
            throw new IOException(
                    "Root work directory was not properly initialized.");
        }
        getDirectoryHelper().cleanDirectory(getWorkDirectory());
    }

    /**
     *
     * @param pScopes1
     *                     The list of scopes one.
     * @param pScopes2
     *                     The list of scopes two.
     * @return A set of the combined scopes.
     */
    protected final Set<String> combineScopes(List<String> pScopes1,
            List<String> pScopes2) {
        Set<String> allScopes = new HashSet<>(pScopes1);
        allScopes.addAll(pScopes2);
        return allScopes;
    }

    protected final void copyInternalFileToProjectDir(String pSourceInternalDir,
            String pFileName, Path pTargetDir) throws IOException {
        if (pTargetDir == null) {
            throw new IOException("Target directory can't be null");
        }
        if (!pSourceInternalDir.endsWith("/")) {
            pSourceInternalDir = pSourceInternalDir.concat("/");
        }
        InputStream is = getClass()
                .getResourceAsStream(pSourceInternalDir + pFileName);

        if (is != null) {
            Files.createDirectories(pTargetDir);
            Files.copy(is, pTargetDir.resolve(pFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new IOException("Source file was not found in"
                    + pSourceInternalDir + pFileName);
        }

    }

    protected final void copyInternalFileToProjectDir(String pSourceInternalDir,
            String pFileName, String pTargetDir) throws IOException {
        copyInternalFileToProjectDir(pSourceInternalDir, pFileName,
                Paths.get(pTargetDir));
    }

    protected void createDefaultDirectories() throws MojoExecutionException {
        try {
            if (getWorkDirectory() != null) {
                Files.createDirectories(getWorkDirectory());
            } else {
                throw new MojoExecutionException(
                        "Work directory was not properly configured.");
            }
            if (getCacheDirectory() != null) {
                Files.createDirectories(getCacheDirectory());
            }
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Fail to create the plugin directories.", e);
        }

    }

    /**
     * Used to specify any action that must be executed when the mojo are being
     * skipped by maven reactor.
     *
     * @throws MojoExecutionException
     *                                    When the skipping process had any
     *                                    problem
     */
    protected abstract void doBeforeSkipMojo() throws MojoExecutionException;

    protected boolean doInitialValidation() throws MojoExecutionException {

        if (isSkip()) {
            getLog().info(String.format("Skipping goal %s for project %s",
                    getGoalName(getClass().getName()), getProject().getId()));
            doBeforeSkipMojo();
            return false;
        }

        getSupportedPackagings().addAll(getExtraSupportedPackagings());
        if (!aggregator && !getSupportedPackagings().isEmpty()
                && !getSupportedPackagings()
                        .contains(getProject().getPackaging())) {
            throw new MojoExecutionException(String.format(
                    "The project '%s' has a packaging not allowed by this plugin. Allowed packagings are '%s'.",
                    getProject().getId(), getSupportedPackagings()));
        }

        if (aggregator && !getProject().isExecutionRoot()) {
            getLog().info(String.format("Skipping goal %s for project %s",
                    getGoalName(getClass().getName()), getProject().getId()));
            return false;
        }
        return true;
    }

    @Override
    public final void execute()
            throws MojoExecutionException, MojoFailureException {

        if (!runtimeInformation.isMavenVersion("[3.3.3,)")) {
            throw new UnsupportedOperationException(
                    "C8Tech Maven OSGi Tools requires Maven runtime with version 3.3.3 or higher.");
        }

        if (!doInitialValidation()) {
            return;
        }

        createDefaultDirectories();

        executeExtraInitializationSteps();

        executeMojo();
    }

    protected abstract void executeExtraInitializationSteps()
            throws MojoExecutionException, MojoFailureException;

    /**
     * @throws MojoExecutionException
     *                                    Thrown when the plugin execution had
     *                                    any problem
     * @throws MojoFailureException
     *                                    Thrown when the projects being built
     *                                    had any problem
     */
    protected void executeMojo()
            throws MojoExecutionException, MojoFailureException {
    }

    public final File getBasedir() {
        return basedir;
    }

    public File getBuildDir() {
        return this.buildDir;
    }

    protected final String getCachedFileNamePattern() {
        return cachedFilePatternReplacement;
    }

    protected abstract Path getCacheDirectory();

    /**
     * Creates or return an existent sub-directory of the already set up cache
     * directory.
     *
     * @param name
     *                 the name of the directory to be resolved against the main
     *                 cache directory.
     * @return a non-null path of the sub-directory
     * @throws MojoExecutionException
     *                                      When any IO related error have
     *                                      occurred or.
     * @throws IllegalArgumentException
     *                                      When a null name is specified.
     */
    protected final Path getCacheSubDirectory(String name)
            throws MojoExecutionException {
        if (!getCacheDirectory().toFile().exists()) {
            throw new MojoExecutionException(
                    "Cache directory was not properly initialized.");
        }
        Path sub = getCacheDirectory().resolve(name);
        if (sub.toFile().exists()) {
            return sub;
        }
        if (!sub.toFile().mkdirs()) {
            throw new MojoExecutionException(
                    "Error when creating cache sub-directory.");
        }
        return sub;
    }

    protected final String getClassifier() {
        return classifier;
    }

    protected final DependenciesHelper getDependenciesHelper() {
        return dependenciesHelper;
    }

    protected final DirectoryHelper getDirectoryHelper() {
        return directoryHelper;
    }

    protected final List<String> getExtraSupportedPackagings() {
        return extraSupportedPackagings;
    }

    protected final FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public final String getFinalName() {
        return finalName;
    }

    protected final String getGoalName(String mojoClassName) {
        String goalName = null;
        List<MojoDescriptor> mojoDescriptorList = pluginDescriptor.getMojos();
        for (MojoDescriptor mojoDescriptor : mojoDescriptorList) {
            if (mojoDescriptor.getImplementation().equals(mojoClassName)) {
                goalName = mojoDescriptor.getGoal();
                break;
            }
        }
        return goalName;
    }

    public final ArtifactRepository getLocalRepository() {
        return getMavenSession().getLocalRepository();
    }

    public final MavenSession getMavenSession() {
        return session;
    }

    protected final PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }

    public final MavenProject getProject() {
        return project;
    }

    public final MavenProjectHelper getProjectHelper() {
        return this.projectHelper;
    }

    public final List<ArtifactRepository> getRemoteRepositories() {
        return getProject().getRemoteArtifactRepositories();
    }

    public final RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    protected final RuntimeInformation getRuntimeInformation() {
        return runtimeInformation;
    }

    protected final List<String> getSupportedPackagings() {
        return supportedPackagings;
    }

    protected final Path getWorkDirectory() {
        return workDirectory.toPath();
    }

    /**
     * Creates or return an existent sub-directory of the already set up work
     * directory.
     *
     * @param name
     *                 the name of the directory to be resolved against the main
     *                 work directory.
     * @return a non-null path of the sub-directory
     * @throws MojoExecutionException
     *                                      When any IO related error have
     *                                      occurred or.
     * @throws IllegalArgumentException
     *                                      When a null name is specified.
     */
    protected final Path getWorkSubDirectory(String name)
            throws MojoExecutionException {
        if (!getWorkDirectory().toFile().exists()) {
            throw new MojoExecutionException(
                    "Root work directory was not properly initialized.");
        }
        Path sub = getWorkDirectory().resolve(name);
        if (sub.toFile().exists()) {
            return sub;
        }
        if (!sub.toFile().mkdirs()) {
            throw new MojoExecutionException(
                    "Error when creating work sub-directory.");
        }
        return sub;
    }

    protected final boolean isAggregator() {
        return aggregator;
    }

    protected abstract boolean isSkip();

    protected final boolean isVerbose() {
        return verbose;
    }

    protected MavenProject loadProject(
            org.eclipse.aether.artifact.Artifact pArtifact,
            boolean pResolveDependencies) throws ProjectBuildingException {

        return getDependenciesHelper().loadProject(
                RepositoryUtils.toArtifact(pArtifact), getMavenSession(),
                pResolveDependencies);
    }

    protected MavenProject loadProject(String groupId, String artifactId,
            String pVersion, String pPackaging, boolean pResolveDependencies)
            throws ProjectBuildingException {

        Artifact pomArtifact = getRepositorySystem().createArtifact(groupId,
                artifactId, pVersion, pPackaging);
        return getDependenciesHelper().loadProject(pomArtifact,
                getMavenSession(), pResolveDependencies);
    }

    protected final InputSet registerArtifactsIntoAggregatorBuildContext(
            Set<ArtifactTracker> pArtifactTrackerSet,
            AggregatorBuildContext pAggregatorBuildContext,
            boolean pWorkspaceArtifactAllowed) throws MojoExecutionException {
        if (pArtifactTrackerSet == null) {
            throw new IllegalArgumentException("No artifacts were specified.");
        }
        try {

            InputSet inputSet = pAggregatorBuildContext.newInputSet();
            for (ArtifactTracker artifactTracker : pArtifactTrackerSet) {

                if ((artifactTracker.isWorkspaceProject()
                        && !pWorkspaceArtifactAllowed)) {
                    getLog().warn("    Ignoring artifact from workspace "
                            + artifactTracker.getArtifactId());
                    continue;
                }
                inputSet.addInput(artifactTracker.isCached()
                        ? artifactTracker.getCachedFilePath().toFile()
                        : artifactTracker.getOriginalFile());
            }
            return inputSet;
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Failure registering an artifact into an aggregator build context.",
                    e);
        }
    }

    public void setBuildDir(File pBuildDir) {
        this.buildDir = pBuildDir;
    }

    public final void setClassifier(String pClassifier) {
        classifier = pClassifier;
    }

    public final void setExtraSupportedPackagings(
            List<String> extraSupportedPackagings) {
        for (String packaging : extraSupportedPackagings) {
            addExtraSupportedPackaging(packaging);
        }
    }

    public final void setFinalName(final String finalName) {
        this.finalName = finalName;
    }

    public final void setWorkDirectory(File pWorkDirectory) {
        workDirectory = pWorkDirectory;
    }
}
