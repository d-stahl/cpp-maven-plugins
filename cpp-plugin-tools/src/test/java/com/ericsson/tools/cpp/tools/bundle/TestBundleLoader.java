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

package com.ericsson.tools.cpp.tools.bundle;

import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import com.ericsson.tools.cpp.tools.bundle.BundleLoader;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestBundleLoader {
	
	@Test
	public void testLoad() throws Exception {
		Log log = mock(Log.class);
		BundleLoader loader = new BundleLoader(getClass().getClassLoader(), new EnvironmentManager(log), log);
		Collection<DummyParent> dummies = loader.activate("test", DummyParent.class);
		assertNotNull(dummies);
		assertEquals(1, dummies.size());
		for (DummyParent dummy: dummies) {
			assertTrue(dummy.getTrue());
		}
	}
}
