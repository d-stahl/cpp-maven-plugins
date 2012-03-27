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

package com.ericsson.tools.cpp.compiler.linking.sharedlib;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.DirectoryHandler;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractSharedLinker extends AbstractLinker {
	public AbstractSharedLinker(final Log log, final CompilationSettings settings, final Environment targetEnvironment) {
		super(log, settings, targetEnvironment);
	}

	@Override
	public void link(final Collection<NativeCodeFile> allFiles, final Collection<NativeCodeFile> compiledClasses, final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException {
		new DirectoryHandler(log).create(settings.getSharedOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()));

		final String libName = "lib" + settings.getProject().getArtifactId() + getTargetEnvironment().getSharedLibraryIdentifier(); 
		final File libFile = getLibFile(libName);
		createSharedLibrary(allFiles, libFile, libName);
		log.info(getTargetEnvironment() + ": " + libFile.getName() + " linked.");
	}

	private File getLibFile(final String name) {
		final File path = settings.getSharedOutputDirectory(getTargetEnvironment(), settings.isTestCompilation());
		final String versionSuffix = "." + settings.getProject().getVersion();
		final File libFile = new File(path, name + versionSuffix);
		if(libFile.exists())
			libFile.delete();
		
		return libFile;
	}
	
	protected abstract void createSharedLibrary(final Collection<NativeCodeFile> allFiles, final File libFile, final String libName) throws MojoExecutionException, MojoFailureException;
}
