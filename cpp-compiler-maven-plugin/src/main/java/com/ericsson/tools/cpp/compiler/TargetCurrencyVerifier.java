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

package com.ericsson.tools.cpp.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.ericsson.tools.cpp.compiler.artifacts.ArtifactManager;


public class TargetCurrencyVerifier {
	private final Log log;
	private final MavenProject project;
	private final ArtifactManager artifactManager;

	public TargetCurrencyVerifier(final ArtifactManager artifactManager, final MavenProject project, final Log log) {
		this.artifactManager = artifactManager;
		this.project = project;
		this.log = log;
	}

	public void ensureCurrency() throws MojoExecutionException {
		final File targetDirectory = new File(project.getBuild().getDirectory());

		if( !targetDirectory.exists() )
			return;

		try {
			if( configurationFilesHaveBeenModified(targetDirectory.lastModified()) ) {
				log.debug("Cleaning " + targetDirectory + " due to configuration changes.");
				FileUtils.deleteDirectory(targetDirectory);
			}
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to clean " + targetDirectory + ".", e);
		}

		targetDirectory.setLastModified(new Date().getTime());
	}

	private boolean configurationFilesHaveBeenModified(long latestAllowedModification) {
		for( File configurationFile : getMutableModelFiles() )
			if( configurationFile.lastModified() > latestAllowedModification ) {
				log.debug(configurationFile + " has been modified.");
				return true;
			}

		return false;
	}

	private Collection<File> getMutableModelFiles() {
		final Collection<File> configurationFiles = new ArrayList<File>();

		configurationFiles.add(project.getFile());

		addMutableAncestorModelFiles(configurationFiles, project.getParent());

		return configurationFiles;
	}

	private void addMutableAncestorModelFiles(final Collection<File> collection, final MavenProject parent) {
		if( parent == null )
			return;

		final Artifact parentPomArtifact = artifactManager.createProjectArtifact( parent );

		if(!parentPomArtifact.isSnapshot()) {
			log.debug("Parent " + parent + " isn't a SNAPSHOT. Ancenstry will not be searched for further mutable configurations.");
			return;
		}

		final File parentPomFile = artifactManager.getPomOfArtifact(parentPomArtifact);

		log.debug("Found mutable parent file " + parentPomFile);
		collection.add(parentPomFile);
		addMutableAncestorModelFiles(collection, parent.getParent());
	}
}
