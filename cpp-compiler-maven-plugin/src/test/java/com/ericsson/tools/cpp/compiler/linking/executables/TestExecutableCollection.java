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

package com.ericsson.tools.cpp.compiler.linking.executables;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;

import static org.mockito.Mockito.*;

public class TestExecutableCollection {
	private Log log;
	private Executable executable;
	private File projectBasedir;
	private Collection<NativeCodeFile> compiledFiles;
	private Collection<NativeCodeFile> matchingNativeCodeFiles;
	private ExecutableCollection c;
	
	
	@Before
	public void setup() {
		log = mock(Log.class);
		executable = mock(Executable.class);
		projectBasedir = mock(File.class);
		
		compiledFiles = new ArrayList<NativeCodeFile>();
		matchingNativeCodeFiles = new ArrayList<NativeCodeFile>();
		when(executable.getNativeCodeFiles()).thenReturn(matchingNativeCodeFiles);
		
		c = new ExecutableCollection(compiledFiles, projectBasedir, log);
	}
	
	@Test
	public void verifyExecutableShouldLogWarningIfNoMatchingSourceFilesFound() {
		c.verifyExecutable(executable);
		verify(log).warn(anyString());
	}
	
	@Test
	public void verifyExecutableShouldNotLogWarningIfMatchingSourceFilesAreFound() {
		matchingNativeCodeFiles.add(new NativeCodeFile(new File(""), new File("")));
		c.verifyExecutable(executable);
		verify(log, times(0)).warn(anyString());
	}
}