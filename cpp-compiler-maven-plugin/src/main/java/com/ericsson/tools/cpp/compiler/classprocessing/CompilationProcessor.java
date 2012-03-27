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

package com.ericsson.tools.cpp.compiler.classprocessing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;


public class CompilationProcessor extends FilesProcessor {
	private final AbstractCompiler compiler;

	public CompilationProcessor(final String name, final AbstractCompiler compiler, final Log log, final BlockingQueue<NativeCodeFile> classesToProcess, final ConcurrentLinkedQueue<NativeCodeFile> compiledClasses, int numberProcessorThreads, Object monitor) {
		super(name, log, classesToProcess, compiledClasses, numberProcessorThreads, monitor);
		this.compiler = compiler;
	}

	@Override
	protected void processBatch() {
		try {
			info("Compiling " + batch.size() + " files. Remaining in pool: " + getRemainingClassesRepresentation());
			compiler.compile(batch);
			processedClasses.addAll(batch);
			debug("Compiled " + batch.size() + " files.");
		} 
		catch (AbstractMojoExecutionException e) {
			setCaughtException(e);
			abort();
		} 
	}
}
