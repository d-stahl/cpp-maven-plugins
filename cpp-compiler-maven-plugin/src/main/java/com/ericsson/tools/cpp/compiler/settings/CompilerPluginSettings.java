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

package com.ericsson.tools.cpp.compiler.settings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;

import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.settings.PluginSettingsImpl;



public class CompilerPluginSettings extends PluginSettingsImpl implements DependencyExtractionSettings, CompilationSettings {
	private final boolean testCompilation;
	private final Map<String, String> compilerArguments;
	private final Map<String, String> linkerArguments;

	public CompilerPluginSettings(final MavenProject project, final Map<String, String> sources, final File outputDirectory, final File testOutputDirectory, final Map<String, String> linkerArguments, final Map<String, String> compilerArguments, final boolean testCompilation) {
		super(project, sources, outputDirectory, testOutputDirectory);

		this.linkerArguments = addDefaultLinkerArguments(linkerArguments);
		this.compilerArguments = compilerArguments;
		this.testCompilation = testCompilation;
	}

	public boolean isTestCompilation() {
		return testCompilation;
	}

	public String getCompilerArguments(final Environment targetEnvironment) {
		return getEnvironmentSpecificArguments(targetEnvironment, compilerArguments);
	}

	public String getLinkerArguments(final Environment targetEnvironment) {
		return getEnvironmentSpecificArguments(targetEnvironment, linkerArguments);
	}
	
	public String getEnvironmentSpecificArguments(final Environment environment, final Map<String, String> argumentMap) {
		String arguments = "";

		if( argumentMap != null ) {
			final String argumentsForAllEnvironments = argumentMap.get("all");
			final String argumentsForThisEnvironment = argumentMap.get(environment.getCanonicalName());
			
			if(argumentsForAllEnvironments != null)
				arguments += " " + argumentsForAllEnvironments;

			if(argumentsForThisEnvironment != null)
				arguments += " " + argumentsForThisEnvironment;
		}		
		return arguments.replaceAll("[\\t\\n\\x0B\\f\\r]+", " ").trim();
	}

	private Map<String, String> addDefaultLinkerArguments(Map<String, String> configuredLinkerArguments) {
		Map<String, String> linkerArguments = configuredLinkerArguments;
		
		if( linkerArguments == null )
			linkerArguments = new HashMap<String, String>();
		
		return linkerArguments;
	}

	@Override
	public File getDirectoryForDependecyArtifactExtraction(final String scope, final String groupId, final String artifactId) {
		return new File(getExtractedDependenciesDirectory(scope), groupId + "/" + artifactId);
	}

	@Override
	public File getTestRuntimeDirectory(Environment environment) {
		return new File(getProject().getBuild().getDirectory(), "test-runtime/" + environment.getCanonicalName());
	}
}

