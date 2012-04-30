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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import com.ericsson.tools.cpp.compiler.linking.executables.Executable;
import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.settings.PluginSettingsImpl;


/**
 * Checks whether the current execution environment is ok. If it isn't the build will fail.
 * Compiles the source into a static library and any specified executables for the current
 * execution environment.
 * 
 * @goal testCompile
 * @phase test-compile
 * @threadSafe
 * @requiresDependencyResolution test
 * @since 0.1.3
 */
public class TestCompileMojo extends AbstractCompileMojo {
	/**
	 * Specifies one or more test executables to link.<br/>
	 * Each executable takes the following arguments:<br/>
	 * &nbsp;&nbsp;name:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Mandatory name of the executable.<br/>
	 * &nbsp;&nbsp;The name "[]" causes one executable to be linked for every<br/>
	 * &nbsp;&nbsp;source file found matching the pattern, with the name of.<br/>
	 * &nbsp;&nbsp;that source file.<br/>
	 * <br/>    
	 * &nbsp;&nbsp;sourcesPattern:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Comma separated list of source file patterns to include.<br/> 
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "src/main/cpp/*.cpp"<br/>
	 * <br/>
	 * &nbsp;&nbsp;targets:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Comma separated list of targets for which to create the executable.<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "all"<br/>
	 * <br/>
	 * &nbsp;&nbsp;rpath:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;rpath argument (used for dynamic library looking at runtime).<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "-rpath,\\$ORIGIN"<br/>
	 * <br/>
	 * Example:<br/>
	 * <br/>
	 * &lt;testExecutables&gt;<br/>
	 * &nbsp;&nbsp;&lt;testExecutable&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;myExec&lt;/name&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;sourcesPattern&gt;src/test/cpp/*.cc&lt;/sourcesPattern&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;targets&gt;all&lt;/target&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;rpath&gt;-rpath,\\&ORIGIN/lib&lt;/rpath&gt;<br/>
	 * &nbsp;&nbsp;&lt;/testExecutable&gt;<br/>
	 * &nbsp;&nbsp;&lt;testExecutable&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;defaultExec&lt;/name&gt;<br/>
	 * &nbsp;&nbsp;&lt;/testExecutable&gt;<br/>
	 * &lt;/testExecutables&gt;<br/>
	 * 
	 * Default:<br/>
	 * <br/>
	 * &lt;testExecutables&gt;<br/>
	 * &nbsp;&nbsp;&lt;testExecutable&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;[]&lt;/name&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;sourcesPattern&gt;src/test/cpp/*.cc&lt;/sourcesPattern&gt;<br/>
	 * &nbsp;&nbsp;&lt;/testExecutable&gt;<br/>
	 * &lt;/testExecutables&gt;<br/>
	 * 
	 * @parameter
	 * @since 1.0.0
	 */
	private Executable[] testExecutables;

	/**
	 * Set this to "true" to bypass unit test compilation.
	 *
	 * @parameter default-value="false" expression="${maven.test.skip}"
	 * @since 1.1.0
	 */
	private boolean skip;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if( skip ) {
			getLog().info("Skipping test compilation.");
			return;
		}
		
		initialize();

		if(!isExecutionEnvironmentAmongTargetEnvironments(determineTargetEnvironments(), hostEnvironment)) {
			getLog().warn("The current execution environment (" + hostEnvironment.getName() + ") is not among the target environments. Hence test compilation will be skipped, as it will be impossible to link test executables for " + hostEnvironment.getName() + ". The test output directory will be cleaned to avoid test result ambiguities.");
			cleanTestOutput();
		}
		else {
			if(testExecutables == null)
				testExecutables = new Executable[] { new Executable(Executable.ENUMERATION_SYMBOL, "src/test/cpp/*", null, null) };

			run(true, testExecutables, hostEnvironment);
		}
	}

	private void cleanTestOutput() throws MojoExecutionException {
		final PluginSettingsImpl settings = new PluginSettingsImpl(project, null, outputDirectory, testOutputDirectory);
		final File testOutputDirectory = settings.getOutputDirectory(true);
		try {
			FileUtils.deleteDirectory(testOutputDirectory);
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to delete test output directory.", e);
		}
	}

	private boolean isExecutionEnvironmentAmongTargetEnvironments(final Environment[] targetEnvironments, final Environment executionEnvironment) {
		for(Environment targetEnvironment : targetEnvironments)
			if( targetEnvironment == executionEnvironment )
				return true;

		return false;
	}

	@Override
	protected boolean sharedShallBeLinked() {
		return false;
	}
}
