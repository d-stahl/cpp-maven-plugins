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

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.tools.CliExecutor;
import com.ericsson.tools.cpp.tools.environment.Environment;


public abstract class AbstractBundleCompatibilityChecker {
	protected final Log log;

	public AbstractBundleCompatibilityChecker(final Log log) {
		this.log = log;
	}
	
	public boolean supported(final String providerName, final Environment host, final Environment target) {
		if( !hostSupported(host) ) {
			log.debug("Bundle provider " + providerName + " doesn't support host environment: " + host.getName() + ".");
			return false;
		}
		
		if( !targetSupported(target) ) {
			log.debug("Bundle provider " + providerName + " doesn't support target environment: " + target.getName() + ".");
			return false;
		}
		
		log.debug("Bundle provider " + providerName + " supports current host (" + host.getName() + ") and target (" + target.getName() + ") combination.");
		return true;
	}
	
	protected boolean commandIsAvailableOnPath(final String cmd) {
		final CliExecutor executor = new CliExecutor(log);
		executor.initialize(cmd);
		try {
			final int returnValue = executor.execute(false);
			if( returnValue == 127 ) {
				log.debug("Expected tool " + cmd + " is unavailable.");
				return false;
			}
		}
		catch(AbstractMojoExecutionException e) {
			log.debug("Failed to execute \"" + cmd + "\". Expected tool appears to be unavailable.");
			return false;
		} 

		return true;
	}

	protected abstract boolean hostSupported(final Environment host);
	protected abstract boolean targetSupported(final Environment target);
}
