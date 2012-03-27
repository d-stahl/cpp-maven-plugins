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
import java.io.InputStream;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class CliExecutor {
	private final Commandline commandLine;
	private boolean initialized = false;
	private final Log log;
	private StreamConsumer stdOutConsumer = new StringStreamConsumer();
	private StreamConsumer stdErrConsumer = new StringStreamConsumer();
	private InputStream inStream = null;
	
	public CliExecutor(Log log) {
		this.log = log;
		this.commandLine = new Commandline();
		this.commandLine.getShell().setQuotedArgumentsEnabled(false);
	}
	
	public void initialize(final String executable) {
		initialize(new File("."), executable);
	}
	
	public void initialize(final File workingDirectory, final String executable) {
		commandLine.setWorkingDirectory(workingDirectory);
		commandLine.setExecutable(executable);
		initialized = true;
	}
	
	public int execute() throws MojoFailureException, MojoExecutionException {
		return execute(true);
	}
	
	public int execute(boolean failOnBadResult) throws MojoFailureException, MojoExecutionException {
		if( !initialized )
			throw new MojoExecutionException("Attempted to execute an uninitialized command line");
		
		try
		{
			log.debug( "About to execute \'" + commandLine.toString() + "\'" );

			int result = CommandLineUtils.executeCommandLine( commandLine, inStream, getStdOutConsumer(), getStdErrConsumer() );

			if ( failOnBadResult && result != 0 ) {
				String exceptionMessage = "Failed to execute command line: \'" + commandLine.toString() + "\'. Result: \'" + result + "\'.";
				if( stdErrConsumer instanceof StringStreamConsumer )
					exceptionMessage += " StdErr: \'" + ((StringStreamConsumer)stdErrConsumer).getOutput() + "\'.";
				
				throw new MojoFailureException( exceptionMessage );
				
			}

			log.debug("Successfully executed \'" + commandLine.toString() + "\'.");
			return result;
		}
		catch ( CommandLineException e )
		{
			throw new MojoExecutionException( "Command line execution failed.", e );
		}
	}
	
	public void appendFiles(Collection<File> files) {
		for(File file : files)
			commandLine.createArg().setValue(file.toString());
	}

	public Commandline getCommandline() {
		return commandLine;
	}

	public void setStdOutConsumer(StreamConsumer stdOutConsumer) {
		this.stdOutConsumer = stdOutConsumer;
	}

	public StreamConsumer getStdOutConsumer() {
		return stdOutConsumer;
	}

	public void setStdErrConsumer(StreamConsumer stdErrConsumer) {
		this.stdErrConsumer = stdErrConsumer;
	}

	public StreamConsumer getStdErrConsumer() {
		return stdErrConsumer;
	}

	public void setInStream(InputStream inStream) {
		this.inStream = inStream;
	}
}
