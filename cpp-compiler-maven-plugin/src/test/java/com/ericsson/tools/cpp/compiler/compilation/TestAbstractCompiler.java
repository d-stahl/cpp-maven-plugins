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

package com.ericsson.tools.cpp.compiler.compilation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.tools.cpp.compiler.compilation.AbstractCompiler;
import com.ericsson.tools.cpp.compiler.compilation.RecompilationJudge;
import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;
import com.ericsson.tools.cpp.compiler.settings.CompilationSettings;
import com.ericsson.tools.cpp.tools.environment.Environment;


public class TestAbstractCompiler {
	private RecompilationJudge recompilationJudge = null;
	private AbstractCompiler compiler = null;
	
	@Before
	public void setup() {
		recompilationJudge = mock(RecompilationJudge.class);
		compiler = new CompilerImpl(mock(Log.class), mock(CompilationSettings.class), mock(Environment.class), recompilationJudge);
	}
	
	@Test
	public void compilerUsesRecompilerJudge() throws Exception {
		final NativeCodeFile file = mock(NativeCodeFile.class);
		when(recompilationJudge.fileNeedsToBeCompiled(file)).thenReturn(true).thenReturn(false);
		final boolean judgement1 = compiler.fileNeedsToBeCompiled(file);
		final boolean judgement2 = compiler.fileNeedsToBeCompiled(file);
		
		verify(recompilationJudge, times(2)).fileNeedsToBeCompiled(file);
		
		assertTrue("Compiler should pass positive judgments from the judge.", judgement1);
		assertFalse("Compiler should pass negative judgments from the judge.", judgement2);
	}
	
	
	public static class CompilerImpl extends AbstractCompiler {
		public CompilerImpl(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final RecompilationJudge recompilationJudge) {
			super(log, settings, targetEnvironment, recompilationJudge);
		}

		@Override
		public void compile(Collection<NativeCodeFile> classes) throws MojoFailureException, MojoExecutionException {
		}
	}
}
