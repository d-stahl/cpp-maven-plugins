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
import com.ericsson.tools.cpp.tools.FileFinder;
import com.ericsson.tools.cpp.tools.environment.Environment;


public class Executable {
	private String name;
	private String entryPointPattern;
	private String targets;
	private String rpath;

	private Collection<NativeCodeFile> nativeCodeFiles;

	public Executable() {

	}

	public Executable(final String name, final String entryPointPattern, final String targets, final String rpath) {
		this.name = name;
		this.entryPointPattern = entryPointPattern;
		this.targets = targets;
		this.rpath = rpath;
	}

	public void initialize(final Log log, final File projectBasedir, final Collection<NativeCodeFile> compiledFiles) throws MojoExecutionException {
		if( name == null )
			throw new MojoExecutionException("Mandatory executable parameter \"name\" has not been set.");

		if( entryPointPattern == null )
			entryPointPattern = "src/main/cpp/*.c*";

		if( targets == null )
			targets = "all";

		if( rpath == null )
			rpath = "-rpath,\\$ORIGIN";

		nativeCodeFiles = createNativeCodeFilesCollection(log, projectBasedir, compiledFiles);
	}

	public String getName() {
		return name;
	}

	public String getEntryPointPattern() {
		return entryPointPattern;
	}

	public String getTargets() {
		return targets;
	}

	public String getRpath() {
		return rpath;
	}

	@Override
	public String toString() {
		if( name == null )
			return "<unnamed>";
		else
			return name;
	}

	public Collection<File> getAllFilesToLink() throws MojoFailureException {
		if( nativeCodeFiles == null )
			throw new MojoFailureException("Executable " + toString() + " is uninitialized - there is no list of native code files.");

		final Collection<File> objectFiles = new ArrayList<File>();

		for(NativeCodeFile nativeCodeFile : nativeCodeFiles)
			objectFiles.add(nativeCodeFile.getObjectFile());

		return objectFiles;
	}

	private Collection<NativeCodeFile> createNativeCodeFilesCollection(final Log log, final File projectBasedir, final Collection<NativeCodeFile> compiledFiles) {
		log.debug("Creative native code file list matching pattern " + entryPointPattern + " for executable " + name + ".");

		final Collection<NativeCodeFile> allCppFiles = new ArrayList<NativeCodeFile>();

		if( entryPointPattern != null ) {
			final Collection<File> matchingRawFiles = findMatchingSourceFiles(projectBasedir);
			allCppFiles.addAll(translateRawFilesToCompiledFiles(log, matchingRawFiles, compiledFiles));
		}

		if( allCppFiles.isEmpty() )
			log.warn("Found no compiled files matching the pattern " + entryPointPattern + " for executable " + name + ".");

		return allCppFiles;
	}

	public Collection<File> findMatchingSourceFiles(final File projectBasedir) {
		final Collection<File> l = new ArrayList<File>();
		for(String patternElement : entryPointPattern.split(",")) {
			File f = new File(patternElement);
			if( f.exists() )
				l.add(f);
			else
				l.addAll(new FileFinder(projectBasedir, patternElement.trim()).getFiles());
		}

		return l;
	}

	private Collection<NativeCodeFile> translateRawFilesToCompiledFiles(final Log log, final Collection<File> rawFiles, final Collection<NativeCodeFile> compiledFiles) {
		final Collection<NativeCodeFile> matchingCompiledFiles = new ArrayList<NativeCodeFile>();

		for(File rawFile : rawFiles) {
			boolean matchFound = false;
			for(NativeCodeFile compiledFile : compiledFiles) {
				if( compiledFile.getSourceFile().equals(rawFile)) {
					matchingCompiledFiles.add(compiledFile);
					matchFound = true;
					break;
				}
			}

			if( !matchFound )
				log.warn("Could not find " + rawFile + " among the list of compiled files.");
		}
		return matchingCompiledFiles;
	}

	public boolean shallBeCreatedForTarget(final Environment env) {
		if( targets.equals("all") )
			return true;

		for( String token: targets.split(",") )
			if(token.equals(env.getCanonicalName()))
				return true;

		return false;
	}
}
