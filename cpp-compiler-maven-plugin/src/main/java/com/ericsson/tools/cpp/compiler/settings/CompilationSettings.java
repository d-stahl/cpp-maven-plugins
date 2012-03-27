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

package com.ericsson.tools.cpp.compiler.settings;

import java.io.File;
import java.util.Collection;

import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.settings.MavenProjectContainer;


public interface CompilationSettings extends MavenProjectContainer {
	public boolean isTestCompilation();
	public File getObjDirectory(final Environment targetEnvironment);
	public File getObjDirectory(final Environment targetEnvironment, final boolean test);
	public File getCodeDirectory(final Environment environment, final boolean test);
	public File getIncludeDirectory(final Environment environment, final boolean test);
	public File getStaticOutputDirectory(final Environment environment, final boolean testCompilation);
	public File getSharedOutputDirectory(final Environment environment, final boolean testCompilation);
	public File getPreExistingStaticLibDirectory(final Environment environment, final boolean testCompilation);
	public File getPreExistingSharedLibDirectory(final Environment environment, final boolean testCompilation);
	public File getOutputHeaderDirectory(final Environment environment, final boolean testCompilation);
	public File getExecutablesOutputDirectory(final Environment targetEnvironment, final boolean testCompilation);
	public File getTestRuntimeDirectory(final Environment environment);
	public File getOutputDirectory();
	public Collection<File> getDependencyDirectories(final String scope, final Environment targetEnvironment);
	public Collection<File> getDependencyDirectories(final String scope, final Environment targetEnvironment, final boolean noArch);
	public String getLinkerArguments(final Environment targetEnvironment);
	public String getCompilerArguments(final Environment targetEnvironment);
}
