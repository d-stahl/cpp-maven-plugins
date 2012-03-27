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

package com.ericsson.tools.cpp.tester;

import java.io.File;

import org.apache.maven.project.MavenProject;
import com.ericsson.tools.cpp.tools.settings.PluginSettingsImpl;


public class TestSettings extends PluginSettingsImpl {

	private final File suppressionFile;

	public TestSettings(final MavenProject project, final File suppressionFile) {
		super(project, null, null);
		this.suppressionFile = suppressionFile;
	}

	public File getSuppressionFile() {
		return suppressionFile;
	}

}
