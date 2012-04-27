/* 
 *  Copyright 2012 Ericsson AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ericsson.tools.cpp.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.ericsson.tools.cpp.compiler.artifacts.ArtifactFilter;
import com.ericsson.tools.cpp.compiler.artifacts.ArtifactManager;
import com.ericsson.tools.cpp.compiler.bundle.BundleProviderManager;
import com.ericsson.tools.cpp.compiler.dependencies.DependencyExtractor;
import com.ericsson.tools.cpp.compiler.linking.executables.Executable;
import com.ericsson.tools.cpp.compiler.linking.executables.ExecutableCollection;
import com.ericsson.tools.cpp.compiler.publishing.Publisher;
import com.ericsson.tools.cpp.compiler.settings.CompilerPluginSettings;
import com.ericsson.tools.cpp.tools.bundle.BundleLoader;
import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;


public abstract class AbstractCompileMojo extends AbstractMojo
{
	/**
	 * The OS name
	 *
	 * @parameter expression="${os.name}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	protected String osName;

	/**
	 * The OS architecture
	 *
	 * @parameter expression="${os.arch}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	protected String osArch;

	/**
	 * The line separator
	 *
	 * @parameter expression="${line.separator}"
	 * @required
	 * @readonly
	 * @since 0.0.2
	 */
	private String lineSeparator;

	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	protected MavenProject project;

	/**
	 * The component used to create artifacts.
	 * 
	 * @component
	 */
	private ArtifactFactory artifactFactory;	

	/**
	 * The component used to resolve artifacts
	 * 
	 * @component
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * The local repository
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	private ArtifactRepository localRepository;	

	/** 
	 * Remote repositories
	 * 
	 * @parameter default-value="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly 
	 * */
	@SuppressWarnings("unchecked")
	private List remoteRepositories;	

	/**
	 * A comma separated list of target environments to build for.
	 * If undefined it will default to the host environment.
	 *
	 * @parameter expression="${targets}"
	 * @since 0.1.2
	 */
	protected String targetEnvironments;

	/**
	 * The output directory
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @since 0.0.2
	 */
	protected File outputDirectory;	

	/**
	 * The test output directory
	 *
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @since 0.0.2
	 */
	protected File testOutputDirectory;

	/**
	 * The host environment name. If defined, it will be used to determine the current host environment.<br />
	 * If left undefined the plugin will attempt to automatically resolve the host environment.<br />
	 * Can be set via the host.environment system property.
	 * @parameter expression="${host.environment}"
	 * @since 0.4.1
	 */
	private String hostEnvironmentName;

	/**
	 * Linker arguments given as a map with targets as keys.<br />
	 * The key "all" is applied to all linkers.<br />
	 * <br />
	 * Default:<br />
	 * &lt;linkerArguments&gt;<br />
	 * &nbsp;&nbsp;&lt;all&gt;-lpthread -lm -lstdc++&lt;/all&gt;<br />
	 * &lt;/linkerArguments&gt;<br />
	 * <br />
	 * Example:<br />
	 * &lt;linkerArguments&gt;<br />
	 * &nbsp;&nbsp;&lt;all&gt;-lsomelib&lt;/all&gt;<br />
	 * &nbsp;&nbsp;&lt;linux_64&gt;-lsomelinuxlib&lt;/linux_64&gt;<br />
	 * &lt;/linkerArguments&gt;
	 * 
	 * @parameter
	 * @since 0.3.1
	 */
	private Map<String, String> linkerArguments;

	/**
	 * Compiler arguments given as a map with targets as keys.<br />
	 * The key "all" is applied to all compilers.<br />
	 * <br />
	 * Example:<br />
	 * &lt;compilerArguments&gt;<br />
	 * &nbsp;&nbsp;&lt;all&gt;-O3&lt;/all&gt;<br />
	 * &nbsp;&nbsp;&lt;linux_64&gt;-DMYDEF&lt;/linux_64&gt;<br />
	 * &lt;/compilerArguments&gt;
	 * 
	 * @parameter
	 * @since 0.2.6
	 */
	private Map<String, String> compilerArguments;

	/**
	 * Definition of where sources can be found.<br/>
	 * This is given as a map with each element representing
	 * a directory under which "main/cpp", "main/include",
	 * "test/cpp" and/or "test/include" is assumed to exist.
	 * There are default values for "all" and each supported
	 * environment. Any value defined with this parameter will
	 * overwrite that default value, while leaving all other
	 * default values intact.<br/>
	 * <br/>
	 * Default:<br/>
	 * <br/>
	 * &lt;sources&gt;<br/>
	 * &nbsp;&nbsp;&lt;all&gt;src&lt;/all&gt;<br/>
	 * &nbsp;&nbsp;&lt;linux_32&gt;src/linux_32&lt;/linux_32&gt;<br/>
	 * &nbsp;&nbsp;&lt;linux_64&gt;src/linux_64&lt;/linux_64&gt;<br/>
	 * &nbsp;&nbsp;[...]<br/>
	 * &lt;/sources&gt;<br/>
	 * <br/>
	 * The following example specifies a custom location
	 * that will be used for both linux_32 and linux_64 sources, 
	 * but leaves all other locations (including "all") at 
	 * their default values:<br/>
	 * <br/>
	 * &lt;sources&gt;<br/>
	 * &nbsp;&nbsp;&lt;linux_64&gt;src/linux&lt;/linux_64&gt;<br/>
	 * &nbsp;&nbsp;&lt;linux_32&gt;src/linux&lt;/linux_32&gt;<br/>
	 * &lt;/sources&gt;<br/>
	 * <br/>
	 * Note:<br/>
	 * As of current version only header files handling
	 * supports multiple source directories according to this
	 * scheme. Files to compile will only be searched for in
	 * the "all" directory (e.g. src/main/cpp and src/test/cpp).
	 * 
	 * @parameter
	 * @since 0.2.7
	 */
	private Map<String, String> sources;

	protected EnvironmentManager environmentManager;
	protected BundleProviderManager bundles;

	protected Environment hostEnvironment;

	public void initialize() throws MojoExecutionException {
		environmentManager = new EnvironmentManager(getLog());

		try {
			bundles = new BundleProviderManager(new BundleLoader(getClass().getClassLoader(), environmentManager, getLog()), getLog());
		} 
		catch (Exception e) {
			throw new MojoExecutionException("Unable to load compiler plugin descriptions", e);
		}

		if (hostEnvironmentName != null)
		{
			this.hostEnvironment = environmentManager.getEnvironmentByName(hostEnvironmentName);
		}
		else
		{
			this.hostEnvironment = bundles.determineHostEnvironment();
		}

		if (hostEnvironment == null)
			throw new MojoExecutionException("Unable to determine host environment.");

		getLog().debug("Determined host environment: " + this.hostEnvironment + "(" + this.hostEnvironment.getCanonicalName() + ")");
		System.setProperty("host.environment", this.hostEnvironment.getCanonicalName());
	}

	protected void run(final boolean testCompilation, final Executable[] executables, final Environment... targetEnvironments) throws MojoExecutionException, MojoFailureException {
		final CompilerPluginSettings settings = new CompilerPluginSettings(project, sources, outputDirectory, testOutputDirectory, linkerArguments, compilerArguments, testCompilation);
		final ArtifactManager artifactManager = new ArtifactManager(getLog(), project, artifactFactory, artifactResolver, localRepository, remoteRepositories);
		final TargetCurrencyVerifier targetCurrencyVerifier = new TargetCurrencyVerifier(artifactManager, project, getLog());
		final Publisher publisher = new Publisher(getLog(), settings);

		targetCurrencyVerifier.ensureCurrency();

		publisher.publish(null);
		for(TargetManager targetManager : createTargetManagers(settings, targetEnvironments, new DependencyExtractor(getLog(), settings, artifactManager), bundles)) {
			getLog().info(targetManager.getTargetEnvironment() + ": Starting.");

			final long startTime = Calendar.getInstance().getTimeInMillis();
			targetManager.compile();
			final long compilationDoneTime = Calendar.getInstance().getTimeInMillis();
			
			final ExecutableCollection executableCollection = new ExecutableCollection(targetManager.getAllClasses(), project.getBasedir(), getLog());
			executableCollection.addExecutables(executables);
			getLog().debug(executableCollection.getExecutablesDescriptonString());
			targetManager.link(executableCollection, new ArtifactFilter(getLog(), "car").filter(project.getArtifacts()));
			publisher.publish(targetManager.getTargetEnvironment());
			final long linkingDoneTime = Calendar.getInstance().getTimeInMillis();

			getLog().info(targetManager.getTargetEnvironment() + ": Done.");
			getLog().debug(targetManager.getTargetEnvironment() + ": Time spent in:" + lineSeparator +
					"    Compilation:        " + (compilationDoneTime - startTime) + " ms" + lineSeparator +
					"    Building artifacts: " + (linkingDoneTime - compilationDoneTime) + " ms");
		}
	}

	protected List<TargetManager> createTargetManagers(final CompilerPluginSettings settings, final Environment[] targetEnvironments, final DependencyExtractor dependencyExtractor, final BundleProviderManager bundles) {
		List<TargetManager> list = new ArrayList<TargetManager>();

		for(Environment targetEnvironment : targetEnvironments)
			list.add(new TargetManager(getLog(), settings, hostEnvironment, targetEnvironment, dependencyExtractor, bundles, sharedShallBeLinked()));

		return list;
	}

	protected Environment[] determineTargetEnvironments() throws MojoExecutionException {
		final String targetsString = determineTargetsString();
		if( targetsString != null )
			return environmentManager.getEnvironmentsByName(targetsString.split(","));

		return new Environment[] { this.hostEnvironment };
	}

	protected String determineTargetsString() {
		final String targetsSysProp = System.getProperty("targets"); 
		if( targetsSysProp != null && !targetsSysProp.isEmpty() )
			return targetsSysProp;

		if( targetEnvironments != null && !targetEnvironments.isEmpty() )
			return targetEnvironments;

		return null;
	}

	protected abstract boolean sharedShallBeLinked();
}
