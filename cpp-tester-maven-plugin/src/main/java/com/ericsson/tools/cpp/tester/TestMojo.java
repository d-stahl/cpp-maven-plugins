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

package com.ericsson.tools.cpp.tester;

import java.io.File;
import java.io.FileFilter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;
import com.ericsson.tools.cpp.tools.settings.PluginSettingsImpl;

import com.ericsson.tools.cpp.tester.execution.RawTestExecutor;
import com.ericsson.tools.cpp.tester.execution.TestExecutor;
import com.ericsson.tools.cpp.tester.execution.ValgrindTestExecutor;


/**
 * Looks for test binaries and executes them.<br />
 * Success statistics are presented and if there are test failures the build is failed.
 * 
 * @goal test
 * @phase test
 * @threadSafe
 * @since 0.0.1
 */
public class TestMojo extends AbstractMojo
{
	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	private MavenProject project;

	/**
	 * The line separator
	 *
	 * @parameter expression="${line.separator}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	private String lineSeparator;

	/**
	 * File containing Valgrind suppressions. If it exists it will be used
	 * by Valgrind to suppress warnings.<br />
	 * Can be set via the suppresions.file system property. 
	 *
	 * @parameter
	 * 		expression="${suppressions.file}" 
	 * 		default-value="${basedir}/src/test/cpp/valgrind.supp"
	 * @since 0.1.2
	 */
	private File suppressionsFile;

	/**
	 * Whether to try to execute Valgrind or not.<br />
	 * If true, Valgrind is expected to be present on the path.
	 * Can be set via the run.valgrind system property. 
	 *
	 * @parameter
	 * 		expression="${run.valgrind}"
	 * 		default-value="false"
	 * @since 0.1.2
	 */
	private boolean runValgrind;

	/**
	 * The host environment name. If invoked via the car lifecycle,
	 * this property will be set by the cpp-compiler-maven-plugin.
	 * Can be set via the host.environment system property.
	 * 
	 * @parameter
	 * 		expression="${host.environment}"
	 */
	private String hostEnvironmentName;

	/**
	 * Set this to "true" to skip test execution.
	 * The test cases will still be compiled by cpp-compiler-maven-plugin.
	 *
	 * @parameter default-value="false" expression="${skipTests}"
	 * @since 1.1.0
	 */
	private boolean skipTests;

	/**
	 * Set this to "true" to skip test execution.
	 * This parameter will also prevent test compilation by cpp-compiler-maven-plugin.
	 *
	 * @parameter default-value="false" expression="${maven.test.skip}"
	 */
	private boolean skip;	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		if( skip || skipTests ) {
			getLog().info("Skipping test execution.");
			return;
		}
		
		final long startTime = Calendar.getInstance().getTimeInMillis();

		final Environment hostEnvironment = determineHostEnvironment();
		final TestSettings settings = new TestSettings(project, suppressionsFile);
		final TestExecutor executor = getTestExecutor(settings);

		final Map<File, Integer> testResults = new HashMap<File, Integer>();
		final List<File> testBinaries = findTestBinaries(hostEnvironment, settings);

		for(File testBinary : testBinaries)
			testResults.put(testBinary, executor.execute(testBinary));

		final long doneTime = Calendar.getInstance().getTimeInMillis();
		report(testResults, doneTime - startTime);
	}

	private Environment determineHostEnvironment() throws MojoExecutionException {
		if (hostEnvironmentName == null) {
			hostEnvironmentName = System.getProperty("host.environment");
		}

		if (hostEnvironmentName == null) {
			throw new MojoExecutionException("Unable to determine host environment");
		}

		return new EnvironmentManager(getLog()).getEnvironmentByName(hostEnvironmentName);
	}

	private TestExecutor getTestExecutor(TestSettings settings) throws MojoFailureException, MojoExecutionException {
		if( runValgrind ) {
			if( isValgrindAvailable() )
				return new ValgrindTestExecutor(getLog(), settings);
			else
				getLog().warn("Valgrind is unavailable. Falling back to raw test execution.");
		}

		return new RawTestExecutor(getLog());
	}

	private void report(final Map<File, Integer> testResults, final long timeSpent) throws MojoFailureException {
		if( testResults.isEmpty() ) {
			getLog().info("No test binaries found.");
			return;
		}

		String failureMessage = "Tests failed!";
		int successes = 0;

		for(Entry<File, Integer> result : testResults.entrySet()) {
			final String paddedName = String.format("%1$-25s", result.getKey().getName() + ":");

			if(result.getValue() == 0)
				successes++;
			else
				failureMessage += lineSeparator + "    " + paddedName + " FAILED (returned " + result.getValue() + ")";
		}

		getLog().info("Successfully executed test binaries: " + successes + " / " + testResults.size() + " (" + getPercentageString(successes, testResults.size()) + "). " + timeSpent + "ms elapsed.");

		if( successes != testResults.size() )
			throw new MojoFailureException(failureMessage);
	}

	private String getPercentageString(double numerator, double denominator) {
		final double flooredPercentage = Math.floor((numerator / denominator) * 100) / 100;
		return NumberFormat.getPercentInstance().format(flooredPercentage);
	}

	private boolean isValgrindAvailable() throws MojoFailureException, MojoExecutionException {
		final CliExecutor executor = new CliExecutor(getLog());
		executor.initialize(project.getBasedir(), "valgrind");
		return executor.execute(false) != 127;
	}

	@SuppressWarnings("unchecked")
	private List<File> findTestBinaries(final Environment environment, final PluginSettingsImpl settings) {
		final File directory = settings.getExecutablesOutputDirectory(environment, true);
		final List<File> files = directory.isDirectory() ?
				Arrays.asList(directory.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return !file.isDirectory() && file.canExecute();
					}
				}))
				: (List<File>) Collections.EMPTY_LIST;
				getLog().debug("Found " + files.size() + " test binaries in " + directory);
				return files;
	}
}
