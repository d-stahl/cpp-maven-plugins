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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

public class FileFinder {
	
	private final DirectoryScanner ccScanner;

	public FileFinder(final File baseDirectory, final String pattern) {
		ccScanner = new DirectoryScanner();
		ccScanner.setBasedir(baseDirectory);
		ccScanner.setIncludes(new String[] { pattern });
	}
	
	public String[] getFilenames() {
		if( !ccScanner.getBasedir().exists() )
			return new String[0];

		ccScanner.scan();
		return ccScanner.getIncludedFiles();
	}

	public List<File> getFiles() {
		List<File> files = new ArrayList<File>();
		
		for(String filename : getFilenames())
			files.add(new File(ccScanner.getBasedir(), filename));
		
		return files;
	}

}
