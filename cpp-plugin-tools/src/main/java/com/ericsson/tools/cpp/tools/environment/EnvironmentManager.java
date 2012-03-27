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

package com.ericsson.tools.cpp.tools.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class EnvironmentManager {
	public static final String NOARCH_NAME = "noarch";
	public static final Environment LINUX_32 = new Environment("Linux_32", ".so");
	public static final Environment LINUX_64 = new Environment("Linux_64", ".so");
	public static final Environment WIN_32 = new Environment("Win_32", ".dll");
	public static final Environment WIN_64 = new Environment("Win_64", ".dll");
	public static final Environment SOLARIS_32 = new Environment("Solaris_32", ".so");
	public static final Environment SOLARIS_64 = new Environment("Solaris_64", ".so");
	public static final Environment SOLARIS_SPARC = new Environment("Solaris_sparc", ".so");

	private final Map<String, Environment> environments = new HashMap<String, Environment>();
	private final Log log;

	public EnvironmentManager(Log log) throws MojoExecutionException {
		this.log = log;
		addEnvironment(LINUX_32, LINUX_64, WIN_32, WIN_64, SOLARIS_32, SOLARIS_64, SOLARIS_SPARC);
		addAlias("linux_i386", LINUX_32);
		addAlias("linux_x64", LINUX_64);
	}

	private boolean addAlias(String alias, Environment env) {
		log.debug("Added environment alias: " + alias + " = " + env.getName());
		return environments.put(alias.toLowerCase(), env) == null;
	}

	public void addAlias(String alias, String name) throws MojoExecutionException {
		Environment env = lookupEnvironment(name);
		
		if (env == null)
			throw new MojoExecutionException("Alias '"+ alias +"' provided for non-existing environment '"+ name +"'.");
		else if (! addAlias(alias, env))
			throw new MojoExecutionException("Duplicate environment name '" + alias + "' provided as alias for '" + name + "'.");
	}

	public Environment[] getEnvironmentsByName(final String... targetEnvironmentNames) throws MojoExecutionException {
		final List<Environment> matchingEnvironments = new ArrayList<Environment>();

		for (String targetEnvironmentName : targetEnvironmentNames)
			matchingEnvironments.add(getEnvironmentByName(targetEnvironmentName));

		return matchingEnvironments.toArray(new Environment[matchingEnvironments.size()]);
	}

	public Environment getEnvironmentByName(String environmentName) throws MojoExecutionException {
		Environment e = lookupEnvironment(environmentName);
		if (e == null)
			throw new MojoExecutionException("Couldn't find any environment matching '" + environmentName + "'.");
		return e;
	}

	public void addEnvironment(Environment... es) throws MojoExecutionException {
		for (Environment e : es) {
			if (environments.put(e.getCanonicalName(), e) != null) {
				throw new MojoExecutionException("Duplicate environment '" + e.getName() + "' provided.");
			}
			log.debug("Added environment: " + e.getName());
		}
	}

	public Environment lookupEnvironment(String name) {
		return environments.get(name.toLowerCase());
	}
}
