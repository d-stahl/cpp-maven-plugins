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

package com.ericsson.tools.cpp.compiler.publishing;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.DirectoryHandler;
import com.ericsson.tools.cpp.tools.DirectoryHandler.OverwriteStyle;
import com.ericsson.tools.cpp.tools.environment.Environment;

public class Publisher {
	private final Log log;
	private final CompilationSettings settings;

	public Publisher(final Log log, final CompilationSettings settings) {
		this.log = log;
		this.settings = settings;
	}
	
	public void publish(final Environment environment) throws MojoFailureException, MojoExecutionException {
		publishHeaders(environment);
		
		if( environment != null ) {
			publishPreExistingStaticLibs(environment);
			publishPreExistingSharedLibs(environment);
		}
	}
	
	private void publishHeaders(final Environment environment) throws MojoFailureException, MojoExecutionException {
		publish(settings.getIncludeDirectory(environment, settings.isTestCompilation()), settings.getOutputHeaderDirectory(environment, settings.isTestCompilation()));
	}
	
	private void publishPreExistingStaticLibs(final Environment environment) throws MojoFailureException, MojoExecutionException {
		publish(settings.getPreExistingStaticLibDirectory(environment, settings.isTestCompilation()), settings.getStaticOutputDirectory(environment, settings.isTestCompilation()));
	}

	private void publishPreExistingSharedLibs(final Environment environment) throws MojoFailureException, MojoExecutionException {
		publish(settings.getPreExistingSharedLibDirectory(environment, settings.isTestCompilation()), settings.getSharedOutputDirectory(environment, settings.isTestCompilation()));
	}

	private void publish(final File sourceDirectory, final File destinationDirectory) throws MojoFailureException, MojoExecutionException {
		log.debug("Copying " + sourceDirectory + " to " + destinationDirectory);
		new DirectoryHandler(log).copyRecursively(sourceDirectory, destinationDirectory, OverwriteStyle.OVERWRITE_IF_NEWER);
	}
}
