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

package com.ericsson.tools.cpp.compiler.compilation.gcc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;

import com.ericsson.tools.cpp.compiler.compilation.IncludesAnalyzer;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;


public class GccIncludesAnalyzer implements IncludesAnalyzer {

	@Override
	public List<File> getIncludedFiles(final NativeCodeFile ncf) throws MojoExecutionException {
		final List<File> includedFiles = new ArrayList<File>();

		try {
			Scanner scan = new Scanner(ncf.getDependFile());  
			scan.useDelimiter("\\Z");  

			for(String path : scan.next().split(" ")) {
				if( path.isEmpty() )
					continue;

				if( path.startsWith("\\") )
					continue;

				if( path.equals(ncf.getSourceFile().getName()) )
					continue;

				if( path.equals(ncf.getObjectFile().getName() + ":") )
					continue;

				includedFiles.add(findIncludedFile(ncf, path));
			}
		} 
		catch (FileNotFoundException e) {
			throw new MojoExecutionException("Could not open depend file.", e);
		}

		return includedFiles;
	}

	private File findIncludedFile(final NativeCodeFile ncf, final String path) throws MojoExecutionException {
		final File includedFileAbsolute = new File(path);
		if( includedFileAbsolute.exists() )
			return includedFileAbsolute;

		final File includedFileRelative = new File(ncf.getSourceFile().getParent(), path);
		if( includedFileRelative.exists() )
			return includedFileRelative;

		throw new MojoExecutionException(ncf.getDependFile().getName() + " lists a non-existent dependency (" + path + " could not be resolved as " + includedFileAbsolute.getPath() + " or " + includedFileRelative.getPath() + ".");
	}
}
