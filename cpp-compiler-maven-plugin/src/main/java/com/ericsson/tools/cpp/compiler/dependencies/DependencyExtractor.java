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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import com.ericsson.tools.cpp.compiler.artifacts.ArtifactManager;
import com.ericsson.tools.cpp.compiler.settings.DependencyExtractionSettings;
import com.ericsson.tools.cpp.tools.environment.Environment;
import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;


public class DependencyExtractor {
	private static final String VERSION_FILENAME = "version";

	private final Log log;
	private final DependencyExtractionSettings settings;
	private final ArtifactManager artifactManager;
	private final List<DependencyIdentifier> securedDependencies = new ArrayList<DependencyIdentifier>();

	public DependencyExtractor(final Log log, final DependencyExtractionSettings settings, final ArtifactManager artifactManager) {
		this.log = log;
		this.settings = settings;
		this.artifactManager = artifactManager;
	}

	public void secureAvailabilityOfExtractedDependencies(final Environment targetEnvironment) throws MojoExecutionException {
		for(Artifact artifact : artifactManager.getDependencyArtifacts()) {
			secureAvailabilityOfExtractedDependency(new DependencyIdentifier(artifact, EnvironmentManager.NOARCH_NAME, settings));
			secureAvailabilityOfExtractedDependency(new DependencyIdentifier(artifact, targetEnvironment.getCanonicalName(), settings));
		}
	}

	boolean isUpdatedSnapshot(final Artifact artifact, final File destination) {
		if( !artifact.isSnapshot() )
			return false;
		
		return artifact.getFile().lastModified() > destination.lastModified();
	}
	
	private void secureAvailabilityOfExtractedDependency(final DependencyIdentifier dep) throws MojoExecutionException {
		if ( securedDependencies.contains(dep) )
			return;

		log.debug("Dependency is not yet secured: " + dep + ". It should go into destination: " + dep.getDestination());

		if( dep.getDestination().exists() ) {
			log.debug("Destination " + dep.getDestination() + " already exists. It will be checked for validity.");
			deleteDestinationIfInvalid(dep.getArtifact(), dep.getDestination());
		}

		if( !dep.getDestination().exists() ) {
			log.debug("Destination " + dep.getDestination() + " does not exist. It will be created");
			setupDestination(dep.getArtifact(), dep.getDestination());
		}

		if(!dependencyIsAlreadyExtracted(dep)) 
			extractDependency(dep);

		securedDependencies.add(dep);
	}

	private boolean dependencyIsAlreadyExtracted(final DependencyIdentifier dep) {
		final File targetSubDirectory = new File(dep.getDestination(), dep.getTarget());
		return targetSubDirectory.exists();
	}

	private void extractDependency(final DependencyIdentifier dep) throws MojoExecutionException {
		int extractedFiles = 0;
		try {
			final ZipFile zipFile = new ZipFile(dep.getArtifact().getFile());
			final Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
			while (entriesEnum.hasMoreElements()) {
				final ZipEntry entry = entriesEnum.nextElement();
				if( entryMatchesDependency(entry, dep) ) {
					extractEntry(entry, zipFile, dep.getDestination());
					extractedFiles++;
				}
			}

			zipFile.close();
		}
		catch(IOException e) {
			throw new MojoExecutionException("Failed to extract " + dep.getArtifact() + ".", e);
		} 

		if(extractedFiles > 0)
			log.debug("Extracted " + extractedFiles + " files from \"" + dep + "\" to " + dep.getDestination());
	}

	private void extractEntry(final ZipEntry entry, final ZipFile zipFile, final File destination) throws MojoExecutionException {
		final File targetFile = new File(destination, entry.getName()); 

		if( entry.isDirectory() )
			targetFile.mkdirs();
		else
			writeFile(zipFile, entry, targetFile);
	}

	private boolean entryMatchesDependency(final ZipEntry entry, final DependencyIdentifier dep) {
		return entry.getName().startsWith(dep.getTarget());
	}

	private void deleteDestinationIfInvalid(final Artifact artifact, final File destination) throws MojoExecutionException {
		final File versionFile = new File(destination, VERSION_FILENAME);

		try {
			if(isUpdatedSnapshot(artifact, destination)) {
				log.info(destination + " will be cleaned. There is a newer SNAPSHOT version in local repository.");
				destination.delete();
			}

			if( !versionFile.exists() ) {
				log.warn(destination + " will be cleaned. It contains no version file, which might indicate a previous failed extraction attempt.");
				destination.delete();
			}

			final String previouslyExtractedVersion = FileUtils.fileRead(versionFile);
			if(!previouslyExtractedVersion.equals(artifact.getVersion())) {
				log.warn(destination + " will be cleaned. It contains version " + previouslyExtractedVersion + ", but the current dependency is to " + artifact.getVersion() + ".");
				destination.delete();
			}
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Inspection and/or cleaning of " + destination + " failed.", e);
		}
	}

	private void setupDestination(final Artifact artifact, final File destination) throws MojoExecutionException {
		destination.mkdirs();

		try {
			FileUtils.fileWrite(destination.getPath() + "/" + VERSION_FILENAME, artifact.getVersion());
		} 
		catch (IOException e){
			throw new MojoExecutionException("Failed to write version file.", e);
		}
	}

	private void writeFile(final ZipFile zipFile, final ZipEntry entry, final File targetFile) throws MojoExecutionException {
		InputStream in;
		try {
			in = zipFile.getInputStream(entry);
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));

			byte[] buffer = new byte[1024];
			int bufferLength;

			while ((bufferLength = in.read(buffer)) >= 0)
				out.write(buffer, 0, bufferLength);

			in.close();
			out.close();
			targetFile.setReadOnly();
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to extract " + entry.getName() + " from " + zipFile.getName() + " to " + targetFile.getPath(), e);
		}
	}
}
