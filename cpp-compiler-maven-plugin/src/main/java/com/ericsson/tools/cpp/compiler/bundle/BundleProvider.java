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

package com.ericsson.tools.cpp.compiler.bundle;

import java.util.Properties;


import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.dependencies.DependencyExtractor;
import com.ericsson.tools.cpp.compiler.linking.AbstractLinker;
import com.ericsson.tools.cpp.compiler.linking.executables.Executable;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.environment.Environment;


public interface BundleProvider {
	public AbstractCompiler selectCompiler(Environment execution, Environment target, CompilationSettings settings);
	public AbstractLinker selectExecutableLinker(Environment execution, Environment target, CompilationSettings settings, Executable executable, DependencyExtractor extractor);
	public AbstractLinker selectSharedLinker(Environment execution, Environment target, CompilationSettings settings);
	public Environment determineHostEnvironment(Properties systemProperties);
}