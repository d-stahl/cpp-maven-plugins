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

package com.ericsson.tools.cpp.compiler.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;

public class ColocatedNativeCodeFileBatch {
	private File directory = null;
	private final Collection<NativeCodeFile> codeFiles = new ArrayList<NativeCodeFile>();
	private final Log log;
	
	public ColocatedNativeCodeFileBatch(final Log log) {
		this.log = log;
	}

	public Collection<NativeCodeFile> getCodeFiles() {
		return codeFiles;
	}
	
	public void clear() {
		codeFiles.clear();
		directory = null;
	}

	public File getDirectory() {
		return directory;
	}

	public void drainDispersedCollection(final Collection<NativeCodeFile> dispersedFiles) {
		for(NativeCodeFile file : dispersedFiles) {
			if( directory == null )
				directory = file.getSourceFile().getParentFile();
			
			if( file.getSourceFile().getParentFile().equals(directory) ) {
				codeFiles.add(file);
				log.debug(file + " is in " + directory + ", adding to batch.");
			}
			else {
				log.debug(file + " is NOT in " + directory + ", saving for later.");
			}

			log.debug("Colocated collection contains " + codeFiles.size() + " code files in " + directory);
		}
		
		dispersedFiles.removeAll(codeFiles);
	}
}
