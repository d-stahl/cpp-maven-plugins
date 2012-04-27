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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;

public class ExecutableCollection {
	private final Collection<Executable> verifiedExecutables = new ArrayList<Executable>();
	private final Log log;
	private final Collection<NativeCodeFile> compiledFiles;
	private final File projectBasedir;

	public ExecutableCollection(final Collection<NativeCodeFile> compiledFiles, final File projectBasedir, final Log log) {
		this.compiledFiles = compiledFiles;
		this.projectBasedir = projectBasedir;
		this.log = log;
	}

	public void addExecutables(final Executable[] executables) throws MojoExecutionException {
		if(executables != null) {
			for(Executable e: executables) {
				e.initialize(log, projectBasedir, compiledFiles);
			
				if(e.getName().equals(Executable.ENUMERATION_SYMBOL))
					verifiedExecutables.addAll(expandEnumerator(e));
				else
					verifyExecutable(e);
			}
		}
	}

	protected void verifyExecutable(final Executable e) {
		if( e.getNativeCodeFiles().isEmpty() )
			log.warn("Executable " + e.getName() + " will be skipped, because no code files matching the entry point pattern \"" + e.getEntryPointPattern() + "\" could be found in \"" + projectBasedir + "\".");
		else
			verifiedExecutables.add(e);
	}

	private Collection<Executable> expandEnumerator(final Executable enumerator) {
		final Collection<Executable> expanded = new ArrayList<Executable>();
		
		for(NativeCodeFile ncf : enumerator.getNativeCodeFiles()) {
			final Executable e = new Executable(ncf.getClassName(), ncf.getSourceFile().getAbsolutePath(), enumerator.getTargets(), enumerator.getRpath());
			e.addNativeCodeFile(ncf);
			expanded.add(e);
		}
		
		return expanded;
	}

	public String getExecutablesDescriptonString() throws MojoExecutionException, MojoFailureException {
		if( verifiedExecutables.isEmpty() )
			return "No executables identified.";

		final String eol = System.getProperty("line.separator");
		String descriptionString = "" + verifiedExecutables.size() + " executables are defined:" + eol; 

		for(Executable e : verifiedExecutables) {
			descriptionString += "  Executable: " + e.getName() + eol;
			descriptionString += "    Entrypoint : " + e.getEntryPointPattern() + eol;
			descriptionString += "    Targets    : " + e.getTargets() + eol;
			descriptionString += "    Rpath      : " + e.getRpath() + eol;
			descriptionString += "    Files      :" + eol;
			for(File f : e.getAllFilesToLink())
				descriptionString += "      " + f.getPath()  + eol;
		}

		return descriptionString;
	}

	public Collection<Executable> getVerifiedExecutables() {
		return verifiedExecutables;
	}
}
