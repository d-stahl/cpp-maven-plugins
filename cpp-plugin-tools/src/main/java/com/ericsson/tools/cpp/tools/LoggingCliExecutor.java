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

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class LoggingCliExecutor extends CliExecutor {
	public LoggingCliExecutor(final Log log) {
		super(log);
		
		setStdOutConsumer(new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				log.info(line);
			}
		});
		
		setStdErrConsumer(new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				log.warn(line);
			}
		});
	}
}
