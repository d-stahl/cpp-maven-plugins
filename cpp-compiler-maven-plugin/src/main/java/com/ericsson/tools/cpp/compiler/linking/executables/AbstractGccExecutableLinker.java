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

import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.DirectoryHandler;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractGccExecutableLinker extends AbstractExecutableLinker {
	public AbstractGccExecutableLinker(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final Executable executable) {
		super(log, settings, targetEnvironment, executable);
	}

	@Override
	public void buildExecutable(final Collection<File> libsToLink) throws MojoExecutionException, MojoFailureException {
		final DirectoryHandler directoryHandler = new DirectoryHandler(log);
		directoryHandler.create(settings.getExecutablesOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()));

		final CliExecutor executor = new CliExecutor(log);
		executor.initialize(settings.getExecutablesOutputDirectory(getTargetEnvironment(), settings.isTestCompilation()), getLinkerExecutable());
		executor.getCommandline().createArg().setLine(getRuntimePathArgument());
		executor.getCommandline().createArg().setValue("-o");
		executor.getCommandline().createArg().setValue(getExecutable().getName());
		executor.getCommandline().createArg().setLine(getMandatoryLinkerArguments());
		
		executor.getCommandline().createArg().setValue(getStartGroupArgument());
		executor.appendFiles(getExecutable().getAllFilesToLink());
		executor.appendFiles(libsToLink);
		executor.getCommandline().createArg().setLine(getDefaultLibraries());
		executor.getCommandline().createArg().setValue(getEndGroupArgument());
		
		executor.getCommandline().createArg().setLine(settings.getLinkerArguments(getTargetEnvironment()));
		executor.execute();
	}

	private String getRuntimePathArgument() {
		String rpathArgument = "-Wl";
		
		if(settings.isTestCompilation()) {
			rpathArgument += ",-rpath," + settings.getTestRuntimeDirectory(getTargetEnvironment());
		}
		else {
			rpathArgument += ",-rpath,\\$ORIGIN";
			rpathArgument += ",-rpath,\\$ORIGIN/../../shared/" + getTargetEnvironment().getCanonicalName();
		}
		
		return rpathArgument;
	}

	protected String getStartGroupArgument() {
		return "-Wl,--start-group";
	}

	protected String getEndGroupArgument() {
		return "-Wl,--end-group";
	}

	protected String getDefaultLibraries() throws MojoFailureException, MojoExecutionException {
		return "-lstdc++";
	}

	protected String getLinkerExecutable() {
		return "gcc";
	}
}
