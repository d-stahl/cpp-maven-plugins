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

import com.ericsson.tools.cpp.compiler.settings.DependencyExtractionSettings;

public class DependencyIdentifier {
	private static final String MAIN_ARTIFACT_SUBDIRECTORY = "main";
	private final String target;
	private final Artifact artifact;
	private final File destination;

	public DependencyIdentifier(final Artifact artifact, final String target, final DependencyExtractionSettings settings) {
		this.artifact = artifact;
		this.target = target;

		final File artifactExtractionRoot = settings.getDirectoryForDependecyArtifactExtraction(getArtifact().getScope(), getArtifact().getGroupId(), getArtifact().getArtifactId()); 
		if(getArtifact().getClassifier() == null)
			destination = new File(artifactExtractionRoot, MAIN_ARTIFACT_SUBDIRECTORY);
		else
			destination = new File(artifactExtractionRoot, getArtifact().getClassifier());
		
	}
	
	@Override
	public int hashCode() {
		return getArtifact().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DependencyIdentifier))
			return false;
		
		return equals((DependencyIdentifier)obj);
	}
	
	@Override
	public String toString() {
		return getArtifact().getArtifactId() + ":" + getArtifact().getClassifier() + ":" + getArtifact().getVersion() + ":" + getTarget();
	}
	
	private boolean equals(final DependencyIdentifier other) {
		return getTarget().equals(other.getTarget())
			&& getArtifact().equals(other.getArtifact());
	}

	public String getTarget() {
		return target;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public File getDestination() {
		return destination;
	}
}
