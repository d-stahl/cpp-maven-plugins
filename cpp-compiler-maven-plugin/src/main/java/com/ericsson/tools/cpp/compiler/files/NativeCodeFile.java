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

public class NativeCodeFile {
	public static final String[] SOURCE_SUFFIXES = { ".cc", ".c", ".cpp", ".cxx", ".c++" };
	public static final String OBJECT_SUFFIX = ".o";
	public static final String DEPEND_SUFFIX = ".d";


	private final String sourceFilename;
	private final File sourceDirectory;
	private final File objectDirectory;

	private String className;
	private File sourceFile;
	private File objectFile;
	private File dependFile;

	public NativeCodeFile(final File sourceFile, final File objectDirectory) {
		this(sourceFile.getName(), sourceFile.getParentFile(), objectDirectory);
	}

	public NativeCodeFile(final String sourceFilename, final File sourceDirectory, final File objectDirectory) {
		this.sourceFilename = sourceFilename;
		this.sourceDirectory = sourceDirectory;
		this.objectDirectory = objectDirectory;
	}

	public synchronized String getClassName() {
		if( className == null )
			className = sourceFilename.substring(0, sourceFilename.lastIndexOf("."));

		return className;
	}

	public File getSourceFile() {
		if( sourceFile == null )
			sourceFile = new File(sourceDirectory, sourceFilename);

		return sourceFile;
	}

	public synchronized File getObjectFile() {
		if( objectFile == null )
			objectFile = new File(objectDirectory, getClassName() + OBJECT_SUFFIX);

		return objectFile;
	}

	public synchronized File getDependFile() {
		if( dependFile == null )
			dependFile = new File(objectDirectory, getClassName() + DEPEND_SUFFIX);

		return dependFile;
	}
}
