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

package com.ericsson.tools.cpp.car;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;

import com.ericsson.tools.cpp.car.settings.ArchivingSettings;

public abstract class AbstractCarMojo extends AbstractMojo {

	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 0.0.1
	 */
	protected MavenProject project;

	/**
	 * The output directory
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @since 0.0.2
	 */
	protected File outputDirectory;	

	/**
	 * The test output directory
	 *
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @since 0.0.2
	 */
	protected File testOutputDirectory;

	protected void createCustomArtifacts(final File directory, final String classifier) throws MojoExecutionException {
		if(!directory.exists()) {
			getLog().warn("Directory " + directory + " does not exist. Skipping creation of artifact classified \"" + classifier + "\".");
			return;
		}
		
		attach(createArchive(directory, getArchiveName(classifier)), classifier);
	}

	protected void createDefaultArtifacts(final ArchivingSettings settings) throws MojoExecutionException {
		createMainArtifact(settings);
		createAttachedArtifacts(settings, null, settings.getAttachedOutputDirectory(false));
	}

	protected void createAttachedArtifacts(final ArchivingSettings settings, final String classifierPrefix, final File attachedDir) throws MojoExecutionException {
		if(!attachedDir.exists())
			return;

		for(File file : attachedDir.listFiles())
			if(file.isDirectory()) {
				final String classifier = getClassifier(classifierPrefix, file.getName());

				attach(createArchive(file, getArchiveName(classifier)), classifier);
			}
	}

	private void createMainArtifact(final ArchivingSettings settings) throws MojoExecutionException {
		final File archive = createArchive(settings.getMainOutputDirectory(false), getArchiveName(null));
		project.getArtifact().setFile(archive);
	}

	private File createArchive(final File archiveDirectory, final String archiveName) throws MojoExecutionException {
		try {
			final CarArchiver archiver = new CarArchiver();
			final File archive = new File(project.getBuild().getDirectory(), archiveName);
			archiver.addDirectory(archiveDirectory);
			archiver.setDestFile(archive);
			archiver.createArchive();
			return archive;
		} 
		catch (ArchiverException e) {
			throw new MojoExecutionException("Could not create archive.", e );
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Could not create archive.", e );
		}
	}

	private String getArchiveName(final String classifier) {
		String archiveName = project.getArtifactId() + "-" + project.getVersion();
		if( classifier != null )
			archiveName += "-" + classifier;
		archiveName += ".car";
		return archiveName;
	}

	private void attach(final File archiveFile, final String classifier) {
		new DefaultMavenProjectHelper().attachArtifact(project, archiveFile, classifier);
	}

	private String getClassifier(final String classifierPrefix, final String classifier) {
		if( classifier != null ) {
			if( classifierPrefix != null )
				return classifierPrefix + "-" + classifier;
			else
				return classifier;
		}
		else {
			if( classifierPrefix != null )
				return classifierPrefix;
			else
				return "";
		}
	}
}
