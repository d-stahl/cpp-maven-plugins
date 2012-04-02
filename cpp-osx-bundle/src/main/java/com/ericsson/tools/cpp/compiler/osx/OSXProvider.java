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

package com.ericsson.tools.cpp.compiler.osx;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import com.ericsson.tools.cpp.compiler.bundle.BundleProvider;
import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.compilation.gcc.GccIncludesAnalyzer;
import com.ericsson.tools.cpp.compiler.compilation.gcc.GccRecompilationJudge;
import com.ericsson.tools.cpp.compiler.dependencies.DependencyExtractor;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.linking.executables.Executable;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

import com.ericsson.tools.cpp.compiler.osx.compiler.OSX32Compiler;
import com.ericsson.tools.cpp.compiler.osx.compiler.OSX64Compiler;
import com.ericsson.tools.cpp.compiler.osx.linking.executables.OSX32ExecutableLinker;
import com.ericsson.tools.cpp.compiler.osx.linking.executables.OSX64ExecutableLinker;
import com.ericsson.tools.cpp.compiler.osx.linking.sharedlib.OSX32SharedLinker;
import com.ericsson.tools.cpp.compiler.osx.linking.sharedlib.OSX64SharedLinker;
import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;


public class OSXProvider implements BundleProvider {

	private final Log log;
	private final CompatibilityChecker compatibilityChecker;

	public OSXProvider(EnvironmentManager em, Log log) {
		this.log = log;
		this.compatibilityChecker = new CompatibilityChecker(log);

		log.debug("Loaded OS X provider");
	}

	@Override
	public AbstractCompiler selectCompiler(Environment host, Environment target, CompilationSettings settings) {
		if(!compatibilityChecker.supported(getClass().getName(), host, target))
			return null;

		if (target.equals(EnvironmentManager.OSX_32))
			return new OSX32Compiler(log, settings, target, new GccRecompilationJudge(log, new GccIncludesAnalyzer()));

		if (target.equals(EnvironmentManager.OSX_64))
			return new OSX64Compiler(log, settings, target, new GccRecompilationJudge(log, new GccIncludesAnalyzer()));

		log.warn(getClass().getSimpleName() + " failed to find a compiler for target " + target.getName() + ", even though it's supposedly supported!");
		return null;
	}

	@Override
	public AbstractLinker selectExecutableLinker(final Environment host, final Environment target, final CompilationSettings settings, Executable executable, final DependencyExtractor extractor) {
		if(!compatibilityChecker.supported(getClass().getName(), host, target))
			return null;

		if (target.equals(EnvironmentManager.OSX_32))
			return new OSX32ExecutableLinker(log, settings, target, executable, extractor);

		if (target.equals(EnvironmentManager.OSX_64))
			return new OSX64ExecutableLinker(log, settings, target, executable, extractor);

		log.warn(getClass().getSimpleName() + " failed to find an executable linker for target " + target.getName() + ", even though it's supposedly supported!");
		return null;
	}

	@Override
	public AbstractLinker selectSharedLinker(final Environment host, final Environment target, final CompilationSettings settings) {
		if(!compatibilityChecker.supported(getClass().getName(), host, target))
			return null;

		if (target.equals(EnvironmentManager.OSX_32))
			return new OSX32SharedLinker(log, settings, target);

		if (target.equals(EnvironmentManager.OSX_64))
			return new OSX64SharedLinker(log, settings, target);

		log.warn(getClass().getSimpleName() + " failed to find a shared linker for target " + target.getName() + ", even though it's supposedly supported!");
		return null;
	}

	@Override
	public Environment determineHostEnvironment(Properties systemProperties) {
	    
	    log.debug("Checking OS X host environment.");
		String osName = systemProperties.getProperty("os.name","");

		log.debug( "osName: " + osName );

		if (!osName.equals("Mac OS X"))
		    {
			log.debug("This is not OS X");
			return null;
		    }

		log.debug("Checking OS X hardware platform");
		final CliExecutor cli = new CliExecutor(log);
		cli.initialize("uname");
		/* The OS X flavor of uname does not provide an -i switch; use -m instead. */
		cli.getCommandline().createArg().setValue("-m");
		try {
			cli.execute();
			final String output = ((StringStreamConsumer)cli.getStdOutConsumer()).getOutput();

			if(output.contains("i386"))
			    {
				log.debug("Got i386");
				return EnvironmentManager.OSX_32;
			    }
			if(output.contains("x86_64"))
			    {
				log.debug("Got x86_64");
				return EnvironmentManager.OSX_64;
			    }

			log.warn("Got neither i386 nor x86_64 architecture for OS X.");
		} 
		catch (Exception e) {
			log.warn("Failed to check 32/64 architecture. Assuming none is supported. " + e);
		}

		return null;
	}
}
