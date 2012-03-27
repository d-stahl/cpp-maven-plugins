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

package com.ericsson.tools.cpp.compiler.linking.staticlib;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.DirectoryHandler;
import com.ericsson.tools.cpp.tools.environment.Environment;


public class StaticLinker extends AbstractLinker {
	private static final int ARCHIVING_BATCH_SIZE = 250;

	public StaticLinker(final Log log, final CompilationSettings settings, final Environment targetEnvironment) {
		super(log, settings, targetEnvironment);
	}

	@Override
	public void link(final Collection<NativeCodeFile> allClasses, final Collection<NativeCodeFile> compiledClasses, final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException {
		new DirectoryHandler(log).create(settings.getStaticOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()));

		createStaticLibrary(allClasses, compiledClasses);
	}

	private void createStaticLibrary(final Collection<NativeCodeFile> allClasses, final Collection<NativeCodeFile> compiledClasses) throws MojoExecutionException, MojoFailureException {
		final File libFile = new File(settings.getStaticOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()), "lib" + settings.getProject().getArtifactId() + ".a");
		final Collection<NativeCodeFile> classesToArchive = determineClassesToArchive(libFile.exists(), allClasses, compiledClasses);

		if( classesToArchive.isEmpty() ) {
			log.debug("No files to archive - static library will not be updated.");
			return;
		}
		
		for(NativeCodeFile[] batch : createClassBatches(classesToArchive)) 
			archiveBatch(libFile, batch);

		log.info(getTargetEnvironment() + ": " + classesToArchive.size() + " files archived to " + libFile.getName() + ".");
	}

	private NativeCodeFile[][] createClassBatches(final Collection<NativeCodeFile> classes) {
		final NativeCodeFile[] allClasses = classes.toArray(new NativeCodeFile[classes.size()]);
		final int numberOfBatches = (allClasses.length + ARCHIVING_BATCH_SIZE - 1) / ARCHIVING_BATCH_SIZE;
		final NativeCodeFile[][] batches = new NativeCodeFile[numberOfBatches][];

		for(int i = 0; i < numberOfBatches; i++) {
			int start = i * ARCHIVING_BATCH_SIZE;
			int stop = Math.min(start + ARCHIVING_BATCH_SIZE, allClasses.length);

			batches[i] = Arrays.copyOfRange(allClasses, start, stop);
		}

		log.debug("Split classes to archive into " + batches.length + " batches of up to " + ARCHIVING_BATCH_SIZE + " each.");

		return batches;
	}

	private void archiveBatch(final File libFile, final NativeCodeFile[] batch) throws MojoFailureException, MojoExecutionException {
		CliExecutor executor = new CliExecutor(log);
		executor.initialize(libFile.getParentFile(), "ar");
		executor.getCommandline().createArg().setValue("rc");
		executor.getCommandline().createArg().setValue(libFile.getName());

		for(NativeCodeFile classToArchive : batch)
			executor.getCommandline().createArg().setValue(classToArchive.getObjectFile().getPath());

		executor.execute();

		log.debug("Archived batch of " + batch.length + " files.");
	}

	private Collection<NativeCodeFile> determineClassesToArchive(boolean libFileExists, Collection<NativeCodeFile> allClasses, Collection<NativeCodeFile> compiledClasses) {
		if( libFileExists )
			return compiledClasses;
		else
			return allClasses;
	}
}
