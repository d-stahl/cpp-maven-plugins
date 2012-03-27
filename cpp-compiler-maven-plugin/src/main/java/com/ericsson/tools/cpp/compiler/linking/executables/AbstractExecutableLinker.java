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

package com.ericsson.tools.cpp.compiler.linking.executables;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractExecutableLinker extends AbstractLinker {
	private final Executable executable;

	public AbstractExecutableLinker(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final Executable executable) {
		super(log, settings, targetEnvironment);
		this.executable = executable;
	}

	@Override
	public void link(Collection<NativeCodeFile> allClasses, Collection<NativeCodeFile> compiledClasses, final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException {
		preBuild();
		
		if(executable.getAllFilesToLink().isEmpty()) {
			log.debug(getTargetEnvironment() + ": No objects to link found for executable " + executable + ". Skipping");
			return;
		}

		if( isBinaryUpToDate(libsToLink) ) {
			log.debug(getTargetEnvironment() + ": Executable " + executable + " is up to date.");
			return;
		}

		buildExecutable(libsToLink);

		postBuild();
		log.info(getTargetEnvironment() + ": " + executable + " linked.");
	}

	private boolean isBinaryUpToDate(final Collection<File> libsToLink) throws MojoFailureException {
		final File preExistingExecutableFile = new File(settings.getExecutablesOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()), executable.getName());
		if( !preExistingExecutableFile.exists() )
			return false;

		if( collectionContainsUpdatedFile(preExistingExecutableFile, executable.getAllFilesToLink()) )
			return false;
		
		if( collectionContainsUpdatedFile(preExistingExecutableFile, libsToLink) )
			return false;
		
		return true;
	}

	private boolean collectionContainsUpdatedFile(final File reference, final Collection<File> collection) {
		for(File file : collection) {
			if( file.lastModified() > reference.lastModified() ) {
				log.debug(getTargetEnvironment() + ": " + file.getName() + " has been updated more recently than " + reference.getName());
				return true;
			}
		}
		
		return false;
	}

	protected void preBuild() throws MojoExecutionException, MojoFailureException {
	}
	
	protected void postBuild() throws MojoExecutionException, MojoFailureException {
	}
	
	protected abstract void buildExecutable(final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException;

	protected Executable getExecutable() {
		return executable;
	}
}
