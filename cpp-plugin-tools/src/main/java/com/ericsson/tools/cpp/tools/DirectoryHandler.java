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

package com.ericsson.tools.cpp.tools;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

public class DirectoryHandler {
	public enum OverwriteStyle {
		ALWAYS_OVERWRITE,
		NEVER_OVERWRITE,
		OVERWRITE_IF_NEWER
	}
	
	private final Log log;

	public DirectoryHandler(final Log log) {
		this.log = log;
	}

	/**
	 * Create a directory
	 * 
	 * @param directory					The directory to create
	 * @param recreateIfExists			Whether to delete the directory first if it already exists. 
	 * @throws MojoExecutionException 	Thrown if the directory could not be created
	 */
	public void create(final File directory, final boolean recreateIfExists) throws MojoExecutionException {
		if( recreateIfExists )
			delete(directory);

		if( directory.exists() )
			return;

		final boolean successful = directory.mkdirs();

		if( !successful )
			throw new MojoExecutionException("Failed to create directory \"" + directory + "\".");
		log.debug("Creating directory " + directory + ".");

	}

	/**
	 * Creates a directory unless it already exists.
	 * 
	 * @param directory					The directory to create
	 * @throws MojoExecutionException 	Thrown if the directory could not be created
	 */
	public void create(final File directory) throws MojoExecutionException {
		create(directory, false);
	}

	/**
	 * Deletes a directory and all its children
	 * 
	 * @param directory		The directory to delete
	 */
	public void delete(final File directory) {
		if( !directory.exists() )
			return;

		for(File child : directory.listFiles())
			if(child.isDirectory())
				delete(child);
			else
				child.delete();

		directory.delete();

		log.debug("Deleted " + directory);
	}

	/**
	 * Copies one file or directory and all its contents, 
	 * ignoring any file or directory names beginning with "."
	 * Any existing files will be overwritten.
	 * 
	 * @param source				The directory or file to copy from
	 * @param destination			The directory or file to copy to
	 * @param overwriteStyle		Whether to overwrite any existing target file
	 * @throws MojoFailureException 
	 * @throws MojoExecutionException 
	 */
	public void copyRecursively(final File source, final File destination, final OverwriteStyle overwriteStyle) throws MojoFailureException, MojoExecutionException {
		if( !source.exists() || source.getName().startsWith(".") )
			return;

		try {
			if( source.isDirectory() )
				copyDirectoryRecursively(source, destination, overwriteStyle);
			else if( fileShallBeCopied(source, destination, overwriteStyle) )
				FileUtils.copyFile(source, destination);
		}
		catch(IOException e) {
			throw new MojoExecutionException("Failed to perform copy of " + source + " to " + destination + ".", e);
		}
	}


	private boolean fileShallBeCopied(final File source, final File destination, final OverwriteStyle overwriteStyle) {
		if( overwriteStyle == OverwriteStyle.ALWAYS_OVERWRITE )
			return true;
		
		if( !destination.exists() )
			return true;
		
		if( overwriteStyle == OverwriteStyle.OVERWRITE_IF_NEWER && source.lastModified() > destination.lastModified() )
			return true;
		
		return false;
	}

	private void copyDirectoryRecursively(final File source, final File destination, final OverwriteStyle overwriteStyle) throws MojoFailureException, MojoExecutionException {
		if( !destination.exists() ) { 
			final boolean creationSuccessful = destination.mkdirs();
			if( !creationSuccessful )
				throw new MojoFailureException("Attempted to copy " + source + " to " + destination + ", but the destination directory does not exist and could not be created.");

		}

		for(File file : source.listFiles())
			copyRecursively(file, new File(destination, file.getName()), overwriteStyle);
	}
}