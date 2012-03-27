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

package com.ericsson.tools.cpp.compiler.dependencies;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.tools.cpp.compiler.artifacts.ArtifactManager;
import com.ericsson.tools.cpp.compiler.dependencies.DependencyExtractor;
import com.ericsson.tools.cpp.compiler.settings.DependencyExtractionSettings;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestDependencyExtractor {
	private Log log;
	private DependencyExtractionSettings settings;
	private ArtifactManager artifactManager;
	private File destination;
	private Artifact artifact;
	private File artifactFile;
	private DependencyExtractor de;
	
	
	@Before
	public void setup() {
		log = mock(Log.class);
		settings = mock(DependencyExtractionSettings.class);
		artifactManager = mock(ArtifactManager.class);
		destination = mock(File.class);
		artifactFile = mock(File.class);
		artifact = mock(Artifact.class);
		when(artifact.getFile()).thenReturn(artifactFile);
		
		de = new DependencyExtractor(log, settings, artifactManager);
	}
	
	@Test
	public void isUpdatedSnapshotShouldDetectNonSnapshotArtifacts() throws Exception {
		when(artifact.isSnapshot()).thenReturn(false);
		assertFalse(de.isUpdatedSnapshot(artifact, destination));
	}

	@Test
	public void isUpdatedSnapshotShouldCompareTimestamps() throws Exception {
		when(artifact.isSnapshot()).thenReturn(true);
		when(destination.lastModified()).thenReturn(1l);
		when(artifactFile.lastModified()).thenReturn(0l);
		assertFalse("Artifact file older than extraction destination should not be considered updated.", de.isUpdatedSnapshot(artifact, destination));

		when(destination.lastModified()).thenReturn(0l);
		when(artifactFile.lastModified()).thenReturn(1l);
		assertTrue("Artifact file newer than extraction destination should be considered updated.", de.isUpdatedSnapshot(artifact, destination));
	}
}