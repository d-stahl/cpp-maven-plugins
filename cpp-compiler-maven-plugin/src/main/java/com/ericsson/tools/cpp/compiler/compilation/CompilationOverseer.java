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

package com.ericsson.tools.cpp.compiler.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.classprocessing.CompilationProcessor;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.DirectoryHandler;


public class CompilationOverseer {
	private final List<CompilationProcessor> processors = new ArrayList<CompilationProcessor>();
	private final Log log;
	private final Collection<NativeCodeFile> allCodeFiles;
	private final BlockingQueue<NativeCodeFile> classesToCompile;
	private final ConcurrentLinkedQueue<NativeCodeFile> compiledClasses;
	private final AbstractCompiler compiler;
	private final CompilationSettings settings;
	private final Object monitor = new Object();

	public CompilationOverseer(final CompilationSettings settings, final Log log, final Collection<NativeCodeFile> allClasses, final AbstractCompiler compiler) {
		this.settings = settings;
		this.log = log;
		this.allCodeFiles = allClasses;
		this.compiler = compiler;
		this.classesToCompile = new ArrayBlockingQueue<NativeCodeFile>(allClasses.size());
		this.compiledClasses = new ConcurrentLinkedQueue<NativeCodeFile>();

		final int numberOfCompilerThreads = getNumberOfCompilerThreads();
		for(int i = 0; i < numberOfCompilerThreads; i++)
			processors.add(new CompilationProcessor("Compilation Processor " + i, compiler, log, classesToCompile, compiledClasses, numberOfCompilerThreads, monitor));
	}

	private int getNumberOfCompilerThreads() {
		final int numberOfCores = Runtime.getRuntime().availableProcessors();
		final int numberOfCompilerThreads = numberOfCores;
		log.info("Found " + numberOfCores + " logical cores. Creating " + numberOfCompilerThreads + " compiler threads.");
		return numberOfCompilerThreads;
	}

	public Collection<NativeCodeFile> compile() throws MojoExecutionException {
		new DirectoryHandler(log).create(settings.getObjDirectory(compiler.getTargetEnvironment(), settings.isTestCompilation()));

		if( allCodeFiles.isEmpty()) {
			log.info("List of source files is empty. Skipping compilation.");
			return new ArrayList<NativeCodeFile>();
		}
		
		for(CompilationProcessor processor : processors) {
			final Thread thread = new Thread(processor);
			thread.start();
		}

		populateClassesToCompile(classesToCompile);

		monitorProcessorsUntilAllAreDone();
		
		log.debug(compiler.getTargetEnvironment() + ": " + compiledClasses.size() + " files compiled.");
		return compiledClasses;
	}

	private void monitorProcessorsUntilAllAreDone() throws MojoExecutionException {
		synchronized (monitor) {
			while(processorsAreAlive()) {
				abortIfThereAreProcessorExceptions();
				
				log.debug("Waiting for compilers to finish...");
				try {
					monitor.wait();
				} 
				catch (InterruptedException e) {
					throw new MojoExecutionException("Interrupted while sleeping.", e);
				}
			}
		}
		abortIfThereAreProcessorExceptions();
	}

	private void abortIfThereAreProcessorExceptions() throws MojoExecutionException {
		final AbstractMojoExecutionException caughtException = getAnyExceptionInProcessors();
		if( caughtException != null ) {
			abortAllProcessors();
			throw new MojoExecutionException("Exception caught inside compilation processor.", caughtException);
		}
	}

	private void abortAllProcessors() {
		for(CompilationProcessor processor : processors)
			processor.abort();
	}

	private AbstractMojoExecutionException getAnyExceptionInProcessors() {
		for(CompilationProcessor processor : processors)
			if( processor.getCaughtException() != null )
				return processor.getCaughtException();

		return null;
	}

	private boolean processorsAreAlive() {
		for(CompilationProcessor processor : processors)
			if( !processor.isDone() )
				return true;

		return false;
	}

	private void populateClassesToCompile(final BlockingQueue<NativeCodeFile> classesToCompile) throws MojoExecutionException {
		for(NativeCodeFile codeFile : allCodeFiles) {
			if( compiler.fileNeedsToBeCompiled(codeFile)) {
				try {
					classesToCompile.put(codeFile);
				} 
				catch (InterruptedException e) {
					throw new MojoExecutionException("Failed to add " + codeFile.getSourceFile().toString() + " to list of classes to compile.", e);
				}
			}
		}

		for(CompilationProcessor processor : processors)
			processor.setClassListingIsComplete(true);

		log.debug("Done populating classes to compile.");
	}
}
