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

package com.ericsson.tools.cpp.tools.settings;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;

import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;

public class PluginSettingsImpl {
	private final MavenProject project;
	private final Map<String, File> sources;

	private final File objBaseDirectory;
	private final File testObjBaseDirectory;
	private final File outputDirectory;
	private final File extractedDependenciesDirectory;
	private final File testOutputDirectory;
	private final File attachedOutputDirectory;
	private final File testAttachedOutputDirectory;

	public PluginSettingsImpl(final MavenProject project, final Map<String, String> configuredSources, final File outputDirectory, final File testOutputDirectory) {
		this.project = project;
		this.outputDirectory = outputDirectory;
		this.testOutputDirectory = testOutputDirectory;
		this.sources = createSourcesMapping(configuredSources);

		this.extractedDependenciesDirectory = new File(project.getBuild().getDirectory(), "extracted-dependencies");
		this.objBaseDirectory = new File(project.getBuild().getDirectory(), "obj");
		this.testObjBaseDirectory = new File(project.getBuild().getDirectory(), "test-obj");
		this.attachedOutputDirectory = new File(project.getBuild().getDirectory(), "attached");
		this.testAttachedOutputDirectory = new File(project.getBuild().getDirectory(), "test-attached");
	}

	public PluginSettingsImpl(final MavenProject project, final File outputDirectory, final File testOutputDirectory) {
		this(project, null, outputDirectory, testOutputDirectory);
	}

	private Map<String, File> createSourcesMapping(final Map<String, String> configuredSources) {
		final Map<String, File> sources = new HashMap<String, File>();

		final File srcDirectory = new File(project.getBasedir(), "src");
		sources.put("all", srcDirectory);

		if (configuredSources != null)
			for (String configuredSource : configuredSources.keySet()) {
				final File configuredLocation = new File(project.getBasedir(), configuredSources.get(configuredSource));
				sources.put(configuredSource, configuredLocation);
			}

		return sources;
	}

	public MavenProject getProject() {
		return project;
	}

	public File getCodeDirectory(final Environment environment, final boolean test) {
		return new File(getSourcesDirectory(environment, test), "cpp");
	}

	public File getIncludeDirectory(final Environment environment, final boolean test) {
		return new File(getSourcesDirectory(environment, test), "include");
	}

	public File getResourcesDirectory(final Environment environment, final boolean test) {
		return new File(getSourcesDirectory(environment, test), "resources");
	}

	public File getPreExistingStaticLibDirectory(final Environment environment, final boolean test) {
		return new File(getSourcesDirectory(environment, test), "lib/static");
	}

	public File getPreExistingSharedLibDirectory(final Environment environment, final boolean test) {
		return new File(getSourcesDirectory(environment, test), "lib/shared");
	}

	private synchronized File getSource(final String sourceKey) {
		File dir;
		if ((dir = sources.get(sourceKey)) == null) {
			final File srcDirectory = new File(project.getBasedir(), "src");
			sources.put(sourceKey, dir = new File(srcDirectory, sourceKey));
		}
		return dir;
	}

	public File getSourcesDirectory(final Environment environment, final boolean test) {
		final String sourceKey = environment == null ? "all" : environment.getCanonicalName();
		final String scopeQualifier = test ? "test" : "main";

		return new File(getSource(sourceKey), scopeQualifier);
	}

	public File getObjDirectory(final Environment targetEnvironment) {
		return getObjDirectory(targetEnvironment, false);
	}

	public File getObjDirectory(final Environment targetEnvironment, final boolean test) {
		if (test)
			return new File(testObjBaseDirectory, targetEnvironment.getCanonicalName());
		else
			return new File(objBaseDirectory, targetEnvironment.getCanonicalName());
	}

	public File getOutputDirectory() {
		return getOutputDirectory(false);
	}

	public File getOutputDirectory(final boolean test) {
		if (test)
			return testOutputDirectory;
		else
			return outputDirectory;
	}

	public File getAttachedOutputDirectory() {
		return getAttachedOutputDirectory(false);
	}

	public File getAttachedOutputDirectory(final boolean test) {
		if (test)
			return testAttachedOutputDirectory;
		else
			return attachedOutputDirectory;
	}
	
	public File getOutputHeaderDirectory(final Environment environment, final boolean test) {
		final String environmentQualifier = environment == null ? "noarch" : environment.getCanonicalName();
		return new File(getMainOutputDirectory(test), environmentQualifier + "/include");
	}

	protected File getExtractedDependenciesDirectory(final String scope) {
		return new File(extractedDependenciesDirectory, scope);
	}

	public File getMainOutputDirectory() {
		return getMainOutputDirectory(false);
	}

	public File getMainOutputDirectory(final boolean test) {
		return getOutputDirectory(test);
	}

	public File getExecutablesOutputDirectory() {
		return getExecutablesOutputDirectory(false);
	}

	public File getExecutablesOutputDirectory(final boolean test) {
		return new File(getAttachedOutputDirectory(test), "executables");
	}

	public File getExecutablesOutputDirectory(
			final Environment targetEnvironment) {
		return getExecutablesOutputDirectory(targetEnvironment, false);
	}

	public File getExecutablesOutputDirectory(final Environment targetEnvironment, final boolean test) {
		return new File(getExecutablesOutputDirectory(test), targetEnvironment.getCanonicalName());
	}

	public File getStaticOutputDirectory() {
		return getStaticOutputDirectory(false);
	}

	public File getStaticOutputDirectory(final boolean test) {
		return new File(getAttachedOutputDirectory(test), "static");
	}

	public File getStaticOutputDirectory(final Environment targetEnvironment) {
		return getStaticOutputDirectory(targetEnvironment, false);
	}

	public File getStaticOutputDirectory(final Environment targetEnvironment, final boolean test) {
		return new File(getStaticOutputDirectory(test), targetEnvironment.getCanonicalName());
	}

	public File getSharedOutputDirectory() {
		return getSharedOutputDirectory(false);
	}

	public File getSharedOutputDirectory(final boolean test) {
		return new File(getAttachedOutputDirectory(test), "shared");
	}

	public File getSharedOutputDirectory(final Environment targetEnvironment) {
		return getSharedOutputDirectory(targetEnvironment, false);
	}

	public File getSharedOutputDirectory(final Environment targetEnvironment, final boolean test) {
		return new File(getSharedOutputDirectory(test), targetEnvironment.getCanonicalName());
	}

	public Collection<File> getDependencyDirectories(final String scope) {
		return getDependencyDirectories(scope, null);
	}

	public Collection<File> getDependencyDirectories(final String scope, Environment targetEnvironment) {
		return getDependencyDirectories(scope, targetEnvironment, targetEnvironment == null);
	}

	public Collection<File> getDependencyDirectories(final String scope, Environment targetEnvironment, boolean noArch) {
		final FileFilter dirFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		final File dependDir = getExtractedDependenciesDirectory(scope);
		final Collection<File> dependencies = new ArrayList<File>();
		if (dependDir.isDirectory()) {
			final File[] groups = dependDir.listFiles(dirFilter);
			for (File group : groups) {
				final File[] artifacts = group.listFiles(dirFilter);
				for (File artifact : artifacts) {
					final File[] classifiers = artifact.listFiles(dirFilter);
					for(File classifier : classifiers ) {

					if (targetEnvironment != null) {
						for (String candidate : targetEnvironment .getCandidateNames()) {
							File archDir = new File(classifier, candidate);
							if (archDir.exists() && archDir.isDirectory()) {
								dependencies.add(archDir);
							}
						}
					}
					if (noArch) {
						File noArchDir = new File(classifier, EnvironmentManager.NOARCH_NAME);
						if (noArchDir.exists())
							dependencies.add(noArchDir);
					}
					} 
				}
			}
		}
		return dependencies;
	}
}