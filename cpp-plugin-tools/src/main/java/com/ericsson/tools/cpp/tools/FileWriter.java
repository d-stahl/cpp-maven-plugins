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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.maven.plugin.MojoExecutionException;

public class FileWriter {
	public void extractResource(final String resourcePath, final File destination) throws MojoExecutionException {
		try {
			final InputStream in = this.getClass().getResourceAsStream(resourcePath);
			writeFromStream(in, destination);
		}
		catch(MojoExecutionException e) {
			throw new MojoExecutionException("Failed to extract resource " + resourcePath + " to " + destination + ".", e);
		}
	}
	
	public void writeFromStream(final InputStream in, final File destination) throws MojoExecutionException {
		if( in == null )
			throw new MojoExecutionException("Failed to open input stream.");

		OutputStream out = null; 

		try {
			out = new FileOutputStream(destination, true);


			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0)
				out.write(buffer, 0, length);
		} 
		catch (IOException e) {
			throw new MojoExecutionException("Failed to write to " + destination + ".", e);
		}
		finally {
			try {
				if( in != null)
					in.close();

				if( out != null)
					out.close();
			} 
			catch (IOException e) {
				throw new MojoExecutionException("Failed to close file stream after attempting to write to " + destination + ".", e);
			}
		}
	}
}
