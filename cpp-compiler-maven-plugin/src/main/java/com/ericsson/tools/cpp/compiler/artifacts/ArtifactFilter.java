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

package com.ericsson.tools.cpp.compiler.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

public class ArtifactFilter {
	private final String classifier;
	private final Log log;

	public ArtifactFilter(final Log log, final String requiredType) {
		this.classifier = requiredType;
		this.log = log;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Artifact> filter(Set artifacts) {
		List<Artifact> filteredArtifacts = new ArrayList<Artifact>();
		
		for(Object artifactObj : artifacts) {
			if( !(artifactObj instanceof Artifact) )
				throw new IllegalArgumentException("Received a non-Artifact object of type " + artifactObj.getClass().getName());
			
			Artifact artifact = (Artifact)artifactObj;
			if( artifact.getType().equals(classifier) ) {
				filteredArtifacts.add(artifact);
				log.debug("Artifact \"" + artifact.getArtifactId() + ":" + artifact.getClassifier() + "\" (type " + artifact.getType() + ") passed the filter.");
			}
			else {
				log.debug("Artifact \"" + artifact.getArtifactId() + ":" + artifact.getClassifier() + "\" (type " + artifact.getType() + ") was filtered out.");
			}
		}
		
		return filteredArtifacts;
	}
}
