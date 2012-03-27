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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.ericsson.tools.cpp.compiler.linking.executables.Executable;


/**
 * Compiles the source into static and/or shared libraries and 
 * any specified executables for each of the listed target environments.
 * 
 * @goal compile
 * @phase compile
 * @threadSafe
 * @requiresDependencyResolution compile
 * @since 0.0.1
 */

public class CompileMojo extends AbstractCompileMojo {
	/**
	 * Specifies one or more executables to link.<br/>
	 * Each executable takes the following arguments:<br/>
	 * &nbsp;&nbsp;name:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Mandatory name of the executable.<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;The name "[]" causes one executable to be linked for every<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;source file found matching the pattern, with the name of.<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;that source file.<br/>
	 * <br/>    
	 * &nbsp;&nbsp;sourcesPattern:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Comma separated list of source file patterns to include.<br/> 
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "src/main/cpp/*.cpp"<br/>
	 * <br/>
	 * &nbsp;&nbsp;targets:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Comma separated list of targets for which to create the executable.<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;If set to "all" it will be created for all targets.<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "all"<br/>
	 * <br/>
	 * &nbsp;&nbsp;rpath:<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;rpath argument (used for dynamic library looking at runtime).<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Default: "-rpath,\\$ORIGIN"<br/>
	 * <br/>
	 * Example:<br/>
	 * <br/>
	 * &lt;executables&gt;<br/>
	 * &nbsp;&nbsp;&lt;executable&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;myExec&lt;/name&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;sourcesPattern&gt;src/test/cpp/*.cc&lt;/sourcesPattern&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;targets&gt;all&lt;/target&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;rpath&gt;-rpath,\\&ORIGIN/lib&lt;/rpath&gt;<br/>
	 * &nbsp;&nbsp;&lt;/executable&gt;<br/>
	 * &nbsp;&nbsp;&lt;executable&gt;<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;defaultExec&lt;/name&gt;<br/>
	 * &nbsp;&nbsp;&lt;/executable&gt;<br/>
	 * &lt;/executables&gt;<br/>
	 * 
	 * @parameter
	 * @since 1.0.0
	 */
	private Executable[] executables;
	
	/**
	 * Whether to link a shared library of all the objects.<br/> 
	 *
	 * @parameter default-value="false"
	 * @since 1.0.0
	 */
	private boolean linkShared;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();

		run(false, executables, determineTargetEnvironments());
	}

	@Override
	protected boolean sharedShallBeLinked() {
		return linkShared;
	}
}
