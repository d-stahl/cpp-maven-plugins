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

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class ArtifactManager {
	@SuppressWarnings("unchecked")
	private final List remoteRepositories;
	private final ArtifactFactory factory;
	private final ArtifactResolver resolver;
	private final ArtifactRepository localRepository;
	private final Collection<Artifact> carDependencies;
	boolean carDependenciesResolved = false;


	@SuppressWarnings("unchecked")
	public ArtifactManager(final Log log, final MavenProject project, final ArtifactFactory factory, final ArtifactResolver resolver, final ArtifactRepository localRepository, final List remoteRepositories) {
		this.factory = factory;
		this.resolver = resolver;
		this.localRepository = localRepository;
		this.remoteRepositories = remoteRepositories;

		this.carDependencies = new ArtifactFilter(log, "car").filter(project.getArtifacts());
	}

	public Artifact createProjectArtifact(final MavenProject project) {
		return factory.createProjectArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion() );
	}

	public File getPomOfArtifact(final Artifact artifact) {
		return new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
	}

	public Collection<Artifact> getDependencyArtifacts() throws MojoExecutionException {
		if(!carDependenciesResolved) {
			resolve(carDependencies);
			carDependenciesResolved = true;
		}
		
		return carDependencies;
	}

	private void resolve(final Collection<Artifact> artifacts) throws MojoExecutionException {
		for(Artifact artifact : artifacts)
			resolve(artifact);
	}

	private void resolve(final Artifact artifact) throws MojoExecutionException {
		try {
			resolver.resolve(artifact, remoteRepositories, localRepository);
		} 
		catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException("Failed to resolve artifact: " + artifact);
		}
	}
}
