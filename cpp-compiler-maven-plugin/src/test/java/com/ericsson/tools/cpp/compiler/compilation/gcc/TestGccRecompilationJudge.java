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
import org.junit.Before;
import org.junit.Test;

import com.ericsson.tools.cpp.compiler.compilation.IncludesAnalyzer;
import com.ericsson.tools.cpp.compiler.compilation.RecompilationJudge;
import com.ericsson.tools.cpp.compiler.compilation.gcc.GccRecompilationJudge;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;


import edu.emory.mathcs.backport.java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestGccRecompilationJudge {
	private IncludesAnalyzer includesAnalyzer = null;
	private RecompilationJudge recompilationJudge = null;
	private NativeCodeFile ncf = null;
	private File sourceFile = null;
	private File objectFile = null;
	private File dependFile = null;
	private File includedFile = null;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		includesAnalyzer = mock(IncludesAnalyzer.class);
		recompilationJudge = new GccRecompilationJudge(mock(Log.class), includesAnalyzer);
		sourceFile = mock(File.class);
		objectFile = mock(File.class);
		dependFile = mock(File.class);
		includedFile = mock(File.class);
		when(sourceFile.exists()).thenReturn(true);
		when(objectFile.exists()).thenReturn(true);
		when(dependFile.exists()).thenReturn(true);
		when(includedFile.exists()).thenReturn(true);
		ncf = mock(NativeCodeFile.class);
		when(ncf.getSourceFile()).thenReturn(sourceFile);
		when(ncf.getObjectFile()).thenReturn(objectFile);
		when(ncf.getDependFile()).thenReturn(dependFile);
		try {
			when(includesAnalyzer.getIncludedFiles(ncf)).thenReturn(Arrays.asList(new File[] { includedFile }));
		} 
		catch (MojoExecutionException e) {
			fail();
		}
	}
	
	@Test
	public void ifObjectFileDoesntExistThenCodeShouldBeRecompiled() throws Exception {
		when(objectFile.exists()).thenReturn(false);
		assertTrue(recompilationJudge.fileNeedsToBeCompiled(ncf));
	}

	@Test
	public void ifDependFileDoesntExistThenCodeShouldBeRecompiled() throws Exception {
		when(dependFile.exists()).thenReturn(false);
		assertTrue(recompilationJudge.fileNeedsToBeCompiled(ncf));
	}

	@Test
	public void ifSourceFileIsNewerThanObjectFileThenCodeShouldBeRecompiled() throws Exception {
		when(sourceFile.lastModified()).thenReturn(2l);
		when(objectFile.lastModified()).thenReturn(1l);
		assertTrue(recompilationJudge.fileNeedsToBeCompiled(ncf));
	}

	@Test
	public void ifIncludedFileIsNewerThanObjectFileThenCodeShouldBeRecompiled() throws Exception {
		when(includedFile.lastModified()).thenReturn(2l);
		when(objectFile.lastModified()).thenReturn(1l);
		
		assertTrue(recompilationJudge.fileNeedsToBeCompiled(ncf));
	}

	@Test
	public void ifObjectFileIsUpToDateThenCodeShouldntBeRecompiled() throws Exception {
		assertFalse(recompilationJudge.fileNeedsToBeCompiled(ncf));
	}
}