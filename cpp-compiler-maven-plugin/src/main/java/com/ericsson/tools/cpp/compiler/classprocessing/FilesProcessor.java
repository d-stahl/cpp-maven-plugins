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

package com.ericsson.tools.cpp.compiler.classprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.compiler.files.NativeCodeFile;



public abstract class FilesProcessor implements Runnable {
	protected final Log log;
	protected final BlockingQueue<NativeCodeFile> classesToProcess;
	protected final int numberOfProcessorThreads;
	protected final ConcurrentLinkedQueue<NativeCodeFile> processedClasses;
	protected final Collection<NativeCodeFile> batch = new ArrayList<NativeCodeFile>();

	protected boolean abortCalled = false;
	protected boolean done = false;

	private boolean classListingIsComplete = false;
	private AbstractMojoExecutionException caughtException = null;
	private final String name;
	private final Object monitor;

	public FilesProcessor(final String name, final Log log, final BlockingQueue<NativeCodeFile> classesToProcess, final ConcurrentLinkedQueue<NativeCodeFile> processedClasses, final int numberOfProcessorThreads, final Object monitor) {
		this.name = name;
		this.log = log;
		this.classesToProcess = classesToProcess;
		this.processedClasses = processedClasses;
		this.numberOfProcessorThreads = numberOfProcessorThreads;
		this.monitor = monitor;
	}

	public void run() {
		debug("Starting...");
		try {
			while (!isAbortCalled() && thereMightBeMoreClassesToProcess()) {
				final int batchSize = calculateBatchSize(classesToProcess
						.size());
				classesToProcess.drainTo(batch, batchSize);

				if (batch.isEmpty())
					debug("Batch is empty, will wait for new cycle.");
				else
					processBatch();

				batch.clear();
				sleep(2);
			}

			if (isAbortCalled())
				debug("Aborted!");
			else
				debug("Done!");
		} finally {
			synchronized (monitor) {
				setDone(true);
				monitor.notifyAll();
			}
		}
	}

	private synchronized boolean isAbortCalled() {
		return abortCalled;
	}

	protected abstract void processBatch();

	protected synchronized boolean thereMightBeMoreClassesToProcess() {
		return !classListingIsComplete || !classesToProcess.isEmpty(); 
	}

	protected void setCaughtException(AbstractMojoExecutionException e) {
		caughtException = e;
	}

	protected void setDone(final boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public synchronized void abort() {
		abortCalled = true;
	}

	public synchronized void setClassListingIsComplete(final boolean complete) {
		classListingIsComplete = complete;
	}

	public AbstractMojoExecutionException getCaughtException() {
		return caughtException;
	}

	protected void sleep(final int ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
		}
	}

	protected void debug(final String message) {
		log.debug("[" + name + "] " + message);
	}

	protected void info(final String message) {
		log.info("[" + name + "] " + message);
	}

	protected String getRemainingClassesRepresentation() {
		if( !classListingIsComplete )
			return "?";

		return "" + classesToProcess.size();
	}

	private int calculateBatchSize(int currentPoolSize) {
		final int batchSize = (int)Math.ceil((double)currentPoolSize / (numberOfProcessorThreads * 1.3)); 
		return batchSize;
	}
}
