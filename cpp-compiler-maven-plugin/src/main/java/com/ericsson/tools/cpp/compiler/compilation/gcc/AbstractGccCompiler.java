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

package com.ericsson.tools.cpp.compiler.compilation.gcc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.compilation.RecompilationJudge;
import com.ericsson.tools.cpp.compiler.files.ColocatedNativeCodeFileBatch;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractGccCompiler extends AbstractCompiler {

	public AbstractGccCompiler(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final RecompilationJudge recompilationJudge) {
		super(log, settings, targetEnvironment, recompilationJudge);
	}

	@Override
	public void compile(final Collection<NativeCodeFile> codeFiles) throws MojoFailureException, MojoExecutionException {
		final Collection<NativeCodeFile> dispersedCodeFiles = new ArrayList<NativeCodeFile>(codeFiles);
		final ColocatedNativeCodeFileBatch colocatedBatch = new ColocatedNativeCodeFileBatch(log);
		while(!dispersedCodeFiles.isEmpty()) {
			colocatedBatch.clear();
			colocatedBatch.drainDispersedCollection(dispersedCodeFiles);
			compileColocatedBatch(colocatedBatch);
		}
		
		placeFilesInCorrectDirectories(codeFiles);
	}
	
	protected String getCompilerExecutable() {
		return "gcc";
	}

	protected String getMandatoryCompilerArguments() {
		return "-c -MMD -fPIC";
	}

	private void compileColocatedBatch(final ColocatedNativeCodeFileBatch batch) throws MojoFailureException, MojoExecutionException {
		final CliExecutor executor = getExecutor(batch.getDirectory(), getCompilerExecutable());
		executor.getCommandline().createArg().setLine(getMandatoryCompilerArguments());
		executor.getCommandline().createArg().setLine(settings.getCompilerArguments(getTargetEnvironment()));
		executor.getCommandline().createArg().setValue("-I" + settings.getIncludeDirectory(null, settings.isTestCompilation()));
		executor.getCommandline().createArg().setValue("-I" + settings.getIncludeDirectory(getTargetEnvironment(), settings.isTestCompilation()));
		for(File includeDirectory : getExtractedDependencyIncludeDirectories("compile"))
			executor.getCommandline().createArg().setValue("-I" + includeDirectory);

		if( settings.isTestCompilation() ) {
			executor.getCommandline().createArg().setValue("-I" + settings.getIncludeDirectory(null, false));
			for(File includeDirectory : getExtractedDependencyIncludeDirectories("test"))
				executor.getCommandline().createArg().setValue("-I" + includeDirectory);
		}

		for(NativeCodeFile codeFile : batch.getCodeFiles())
			executor.getCommandline().createArg().setValue(codeFile.getSourceFile().getName());

		executor.execute();
	}

	private void placeFilesInCorrectDirectories(final Collection<NativeCodeFile> batch) throws MojoExecutionException {
		for(NativeCodeFile codeFile : batch) {
			final File destDir = codeFile.getObjectFile().getParentFile();
			if(!destDir.exists())
				destDir.mkdirs();
			
			final File actualObjectFile = new File(codeFile.getSourceFile().getParentFile(), codeFile.getObjectFile().getName());
			final File actualDependFile = new File(codeFile.getSourceFile().getParentFile(), codeFile.getDependFile().getName());
			moveFile(actualObjectFile, codeFile.getObjectFile());
			moveFile(actualDependFile, codeFile.getDependFile());
		}
	}

	private void moveFile(File source, File target) throws MojoExecutionException {
		if(!source.renameTo(target))
			throw new MojoExecutionException("Failed to move " + source + " to " + target);
	}
}
