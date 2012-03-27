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

package com.ericsson.tools.cpp.tools;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractHelpMojo extends AbstractMojo {
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if( getGoal() == null || getGoal().isEmpty() ) {
			printIntroduction();
			printAllGoals();
		}
		else if( !printGoal( getGoal() ) ) {
			printIntroduction();
			printUnrecognizedGoal(getGoal());
			printAllGoals();
		}
	}

	protected abstract String getGoal();
	protected abstract boolean getDetail();
	protected abstract void printIntroduction();
	protected abstract void printAllGoals();
	protected abstract boolean printGoal(final String goal);
	
	private void printUnrecognizedGoal(final String unrecognizedGoal) {
		getLog().warn("Unrecognized goal \"" + unrecognizedGoal + "\". You may either specify one of the available goals or view the info on all available goals by omitting the goal parameter.");
		getLog().info("");
	}

	protected void printHelpGoal() {
		getLog().info("  help:       Display this help.");
		getLog().info("");
	}
}
