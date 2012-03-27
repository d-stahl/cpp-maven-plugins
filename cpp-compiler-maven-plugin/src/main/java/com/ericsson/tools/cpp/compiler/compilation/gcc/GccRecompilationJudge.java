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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.compilation.IncludesAnalyzer;
import com.ericsson.tools.cpp.compiler.compilation.RecompilationJudge;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;


public class GccRecompilationJudge implements RecompilationJudge {
	private final Log log;
	private final IncludesAnalyzer includesAnalyzer;

	public GccRecompilationJudge(final Log log, final IncludesAnalyzer includesAnalyzer) {
		this.log = log;
		this.includesAnalyzer = includesAnalyzer;
	}

	@Override
	public boolean fileNeedsToBeCompiled(final NativeCodeFile ncf) throws MojoExecutionException {
		if( !ncf.getObjectFile().exists() ) {
			log.debug(ncf.getSourceFile().getName() + " has no object file. It needs to be compiled.");
			return true;
		}

		if( !ncf.getDependFile().exists() ) {
			log.debug(ncf.getSourceFile().getName() + " has no depend file. It needs to be compiled.");
			return true;
		}

		if( ncf.getSourceFile().lastModified() > ncf.getObjectFile().lastModified() ) {
			log.debug(ncf.getSourceFile().getName() + " is newer than its object file. It needs to be compiled.");
			return true;
		}

		if( anyIncludedDependencyHasBeenUpdated(ncf) ) {
			log.debug(ncf.getSourceFile().getName() + " includes one or more files that are newer than the object file. It needs to be compiled.");
			return true;
		}

		return false;
	}

	private boolean anyIncludedDependencyHasBeenUpdated(final NativeCodeFile ncf) throws MojoExecutionException {
		for(File includedFile : includesAnalyzer.getIncludedFiles(ncf))
			if( includedFile.lastModified() > ncf.getObjectFile().lastModified() )
				return true;
		
		return false;
	}
}


//public boolean needsToBeCompiled() throws MojoExecutionException {


//
//
//
//log.debug("Class " + getClassName() + " does not need to be compiled.");
//return false;
//}

//private boolean anyIncludedDependencyHasBeenUpdated() throws MojoExecutionException {
//
//List<String> dependencyPaths = getDependencies(getDependFile());
//for(String path : dependencyPaths) {
//	final File includedFile = findIncludedFile(path);
//
//	if( includedFile.lastModified() > getObjectFile().lastModified() ) {
//		log.debug("Class " + className + " depends on " + includedFile + ", which has been updated. It needs to be compiled.");
//		return true;
//	}
//
//}
//
//return false;
//}

//private File findIncludedFile(final String path) throws MojoExecutionException {
//final File includedFileAbsolute = new File(path);
//if( includedFileAbsolute.exists() )
//	return includedFileAbsolute;
//
//final File includedFileRelative = new File(getSourceFile().getParent(), path);
//if( includedFileRelative.exists() )
//	return includedFileRelative;
//
//throw new MojoExecutionException(getDependFile().getName() + " lists a non-existent dependency (" + path + " could not be resolved as " + includedFileAbsolute.getPath() + " or " + includedFileRelative.getPath() + ".");
//}
//
//private List<String> getDependencies(File dependFile) throws MojoExecutionException {
//List<String> filteredPaths = new ArrayList<String>();
//
//try {
//	Scanner scan = new Scanner(dependFile);  
//	scan.useDelimiter("\\Z");  
//
//	for(String path : scan.next().split(" ")) {
//		if( path.isEmpty() )
//			continue;
//
//		if( path.startsWith("\\") )
//			continue;
//
//		if( path.equals(getSourceFile().getName()) )
//			continue;
//
//		if( path.equals(getObjectFile().getName() + ":") )
//			continue;
//
//		filteredPaths.add(path);
//	}
//} 
//catch (FileNotFoundException e) {
//	throw new MojoExecutionException("Could not open depend file.", e);
//}
//
//return filteredPaths;
//}
