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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.LoggingCliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractCompiler {
	protected final Log log;
	private final Environment targetEnvironment;
	protected final CompilationSettings settings;
	private final RecompilationJudge recompilationJudge;
	private final Map<String, Collection<File>> extractedDependencyIncludeDirectories = new HashMap<String, Collection<File>>();

	public AbstractCompiler(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final RecompilationJudge recompilationJudge) {
		this.log = log;
		this.settings = settings;
		this.targetEnvironment = targetEnvironment;
		this.recompilationJudge = recompilationJudge;
	}

	public abstract void compile(final Collection<NativeCodeFile> classes) throws MojoFailureException, MojoExecutionException;
	
	public boolean fileNeedsToBeCompiled(final NativeCodeFile file) throws MojoExecutionException {
		return recompilationJudge.fileNeedsToBeCompiled(file);
	}

	public Environment getTargetEnvironment() {
		return targetEnvironment;
	}

	synchronized protected Collection<File> getExtractedDependencyIncludeDirectories(final String scope) {
		if( !extractedDependencyIncludeDirectories.containsKey(scope) )
			createExtractedDependencyIncludeDirectories(scope);
		
		return extractedDependencyIncludeDirectories.get(scope);
	}
	
	private void createExtractedDependencyIncludeDirectories(final String scope) {
		final Collection<File> includeDirectories = new ArrayList<File>();
		
		for (final File archRoot : settings.getDependencyDirectories(scope, getTargetEnvironment(), true))
		{
			File includeDir = new File(archRoot, "include");
			if (includeDir.exists())
				includeDirectories.add(includeDir);
		}
		
		extractedDependencyIncludeDirectories.put(scope, includeDirectories);
	}
	
	protected CliExecutor getExecutor(final File directory, final String executable) {
		final CliExecutor executor = new LoggingCliExecutor(log);
		executor.initialize(directory, executable);
		return executor;
	}
}
