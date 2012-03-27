package com.ericsson.tools.cpp.car;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.ericsson.tools.cpp.car.settings.ArchivingSettings;
import com.ericsson.tools.cpp.car.settings.ArchivingSettingsImpl;

/**
 * Packages the contents of the test output directories into attached archives.
 *  
 * @goal test-car
 * @phase package
 * @threadSafe
 * @since 1.0.0
 */
public class TestCarMojo extends AbstractCarMojo {
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ArchivingSettings settings = new ArchivingSettingsImpl(project, outputDirectory, testOutputDirectory);

		createCustomArtifacts(settings.getMainOutputDirectory(true), "test");
		createAttachedArtifacts(settings, "test", settings.getAttachedOutputDirectory(true));
	}
}
