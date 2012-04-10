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
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractGccSharedLinker extends AbstractSharedLinker {
	public AbstractGccSharedLinker(final Log log, final CompilationSettings settings, final Environment targetEnvironment) {
		super(log, settings, targetEnvironment);
	}

	@Override
	protected void createSharedLibrary(final Collection<NativeCodeFile> allFiles, final File libFile, final String libName) throws MojoFailureException, MojoExecutionException {
		CliExecutor executor = new CliExecutor(log);
		executor.initialize(libFile.getParentFile(), "gcc");
		executor.getCommandline().createArg().setValue("-shared");
		executor.getCommandline().createArg().setValue(getSoName(libName));
		executor.getCommandline().createArg().setLine(getMandatoryLinkerArguments());
		executor.getCommandline().createArg().setValue("-o");
		executor.getCommandline().createArg().setValue(libFile.getName());
		executor.getCommandline().createArg().setLine(getDefaultLibraries());

		for(NativeCodeFile file : allFiles)
			executor.getCommandline().createArg().setValue(file.getObjectFile().getPath());

		executor.execute();
	}

        protected String getSoName( final String libName ) {
                return "-Wl,-soname," + libName;
        }

	protected String getDefaultLibraries() throws MojoFailureException, MojoExecutionException {
		return "-lstdc++";
	}
}
