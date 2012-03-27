package com.ericsson.tools.cpp.car;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.ericsson.tools.cpp.car.settings.ArchivingSettings;
import com.ericsson.tools.cpp.car.settings.ArchivingSettingsImpl;

/**
 * Packages the contents of the output directory into a C/C++ archive.
 * The main artifact will contain headers and any resources.
 * Attached artifacts are created for each type of binary created by the compiler.
 *  
 * @goal car
 * @phase package
 * @threadSafe
 * @since 0.0.1
 */
public class CarMojo extends AbstractCarMojo {
	/**
	 * Any classifier to attach to the artifact
	 * 
	 * @parameter 
	 * @since 0.1.1
	 */
	private String classifier;

	/**
	 * The directory to archive, if other than the standard output directory
	 * 
	 * @parameter 
	 * @since 0.1.1
	 */
	private File directory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ArchivingSettings settings = new ArchivingSettingsImpl(project, outputDirectory, testOutputDirectory);

		if( directory == null || classifier == null )
			createDefaultArtifacts(settings);
		else
			createCustomArtifacts(directory, classifier);
	}
}
