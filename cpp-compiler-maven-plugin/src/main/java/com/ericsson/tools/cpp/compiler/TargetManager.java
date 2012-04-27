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

package com.ericsson.tools.cpp.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import com.ericsson.tools.cpp.compiler.bundle.BundleProviderManager;
import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.compilation.CompilationOverseer;
import com.ericsson.tools.cpp.compiler.dependencies.DependencyExtractor;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.linking.executables.Executable;
import com.ericsson.tools.cpp.compiler.linking.executables.ExecutableCollection;
import com.ericsson.tools.cpp.compiler.linking.staticlib.StaticLinker;
import com.ericsson.tools.cpp.compiler.settings.CompilerPluginSettings;
import com.ericsson.tools.cpp.tools.FileFinder;
import com.ericsson.tools.cpp.tools.environment.Environment;


public class TargetManager {
	private final Log log;
	private final CompilerPluginSettings settings;
	private final Environment targetEnvironment;
	private final Collection<NativeCodeFile> compiledClasses = new ArrayList<NativeCodeFile>();
	private Collection<NativeCodeFile> allClasses;
	private Collection<AbstractLinker> linkers;
	private CompilationOverseer compilationOverseer;
	private final DependencyExtractor dependencyExtractor;
	private final BundleProviderManager bundles;
	private final Environment hostEnvironment;
	private final boolean linkShared;

	public TargetManager(final Log log, final CompilerPluginSettings settings, Environment hostEnvironment, final Environment targetEnvironment, final DependencyExtractor dependencyExtractor, final BundleProviderManager bundles, final boolean linkShared) { 
		this.log = log;
		this.settings = settings;
		this.hostEnvironment = hostEnvironment;
		this.targetEnvironment = targetEnvironment;
		this.dependencyExtractor = dependencyExtractor;
		this.bundles = bundles;
		this.linkShared = linkShared;
	}

	public void compile() throws MojoFailureException, MojoExecutionException {
		if( getAllClasses().isEmpty() ) {
			log.debug(getTargetEnvironment() + ": No native code files to compile. Skipping compilation.");
			return;
		}

		dependencyExtractor.secureAvailabilityOfExtractedDependencies(getTargetEnvironment());
		compiledClasses.addAll(getCompilationOverseer().compile());
	}

	public void link(final ExecutableCollection executables, final Collection<Artifact> dependencies) throws MojoExecutionException, MojoFailureException {
		if(executables == null) {
			log.debug("Executables are undefined, skipping linking.");
			return;
		}
		
		final Collection<File> libsToLink = findLibsToLink();

		for(AbstractLinker linker : getLinkers(executables))
			linker.link(getAllClasses(), compiledClasses, findLibsToLink());

		if(settings.isTestCompilation())
			copySharedLibDependencies(libsToLink, settings.getTestRuntimeDirectory(targetEnvironment));
	}

	public Environment getTargetEnvironment() {
		return targetEnvironment;
	}

	public Collection<NativeCodeFile> getAllClasses() {
		if( allClasses == null )
			allClasses = findAllCodeFiles();

		return allClasses;
	}

	private Collection<AbstractLinker> getLinkers(final ExecutableCollection executables) throws MojoExecutionException, MojoFailureException {
		if( linkers == null )
			linkers = createLinkers(executables);

		return linkers;
	}

	private Collection<AbstractLinker> createLinkers(final ExecutableCollection executables) throws MojoExecutionException, MojoFailureException {
		Collection<AbstractLinker> linkers = new ArrayList<AbstractLinker>();

		linkers.add(new StaticLinker(log, settings, targetEnvironment));

		if(linkShared)
			linkers.add(createSharedLinker());

		for(Executable e : executables.getVerifiedExecutables())
			if(e.shallBeCreatedForTarget(targetEnvironment))
				linkers.add(createExecutableLinker(e));

		return linkers;
	}

	private CompilationOverseer getCompilationOverseer() throws MojoExecutionException {
		if( compilationOverseer == null )
			compilationOverseer = createCompilationOverseer();

		return compilationOverseer;
	}

	private AbstractCompiler createCompiler() throws MojoExecutionException {
		AbstractCompiler compiler = bundles.selectCompiler(hostEnvironment, targetEnvironment, settings);
		if (compiler == null)
			throw new MojoExecutionException("Don't know of any compiler for target environment " + targetEnvironment.getName() + " compatible with current host environment (" + hostEnvironment.getName() + "). See debug printouts for details of the compatibility check.");
		return compiler;
	}

	private CompilationOverseer createCompilationOverseer() throws MojoExecutionException {
		return new CompilationOverseer(settings, log, getAllClasses(), createCompiler());
	}

	private AbstractLinker createExecutableLinker(final Executable executable) throws MojoExecutionException, MojoFailureException {
		AbstractLinker linker = bundles.selectExecutableLinker(hostEnvironment, targetEnvironment, settings, executable, dependencyExtractor);
		if (linker == null)
			throw new MojoExecutionException("Don't know of any executable linker for target environment " + targetEnvironment.getName() + " compatible with current host environment (" + hostEnvironment.getName() + "). See debug printouts for details of the compatibility check.");
		return linker;
	}

	private AbstractLinker createSharedLinker() throws MojoExecutionException, MojoFailureException {
		AbstractLinker builder = bundles.selectSharedLinker(hostEnvironment, targetEnvironment, settings);
		if (builder == null)
			throw new MojoExecutionException("Don't know of any shared linker for target environment " + targetEnvironment.getName() + " compatible with current host environment (" + hostEnvironment.getName() + "). See debug printouts for details of the compatibility check.");
		return builder;
	}

	private Collection<NativeCodeFile> findAllCodeFiles() {
		Collection<NativeCodeFile> list = new ArrayList<NativeCodeFile>();
		collectNativeCodeFiles(settings.getCodeDirectory(null, settings.isTestCompilation()), list);
		collectNativeCodeFiles(settings.getCodeDirectory(targetEnvironment, settings.isTestCompilation()), list);
		return list;
	}

	private void collectNativeCodeFiles(final File sourceDirectory, final Collection<NativeCodeFile> fileList) {
		if( !sourceDirectory.exists() ) {
			log.debug("Source directory " + sourceDirectory + " doesn't exist.");
		}

		for(String suffix : NativeCodeFile.SOURCE_SUFFIXES)
			for(String fileName : new FileFinder(sourceDirectory, "**/*" + suffix).getFilenames())
				fileList.add(new NativeCodeFile(fileName, sourceDirectory, settings.getObjDirectory(getTargetEnvironment(), settings.isTestCompilation())));

		if( fileList.isEmpty() )
			log.debug("Found no classes in " + sourceDirectory);
	}

	private Collection<File> findLibsToLink() {
		final Collection<File> libs = findLibsToLinkForScope("compile");

		if( settings.isTestCompilation() )
			libs.addAll(findLibsToLinkForScope("test"));

		return libs;
	}

	private Collection<File> findLibsToLinkForScope(final String scope) {
		final String pattern = "lib*";
		final Collection<File> libs = new ArrayList<File>();

		final Collection<File> dependencyDirectories = settings.getDependencyDirectories(scope, getTargetEnvironment());
		for (final File dir : dependencyDirectories) {
			log.debug("Looking for libs matching " + pattern + " in " + dir);
			libs.addAll(new FileFinder(dir, pattern).getFiles());
		}

		log.debug(getTargetEnvironment() + ": " + libs.size() + " libs matching pattern \"" + pattern + "\" found for scope " + scope + " in " + dependencyDirectories.size() + " dependency directories.");
		libs.addAll(new FileFinder(settings.getStaticOutputDirectory(getTargetEnvironment(), scope.equals("test")), "*.a").getFiles());

		return libs;
	}

	private void copySharedLibDependencies(final Collection<File> libs, final File destination) throws MojoExecutionException {
		try {
			if( !destination.exists() )
				destination.mkdir();
			
			for(File lib : libs) {
				if(!lib.getName().contains(targetEnvironment.getSharedLibraryIdentifier()))
					continue;

				final String fullName = lib.getName();
				final String truncatedName = fullName.substring(0, fullName.indexOf(targetEnvironment.getSharedLibraryIdentifier()) + targetEnvironment.getSharedLibraryIdentifier().length()); 
				FileUtils.copyFile(lib, new File(destination, truncatedName));
			}
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to copy shared lib dependencies to " + destination + ".", e);
		}
	}
}
