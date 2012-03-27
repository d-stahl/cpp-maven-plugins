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

package com.ericsson.tools.cpp.tools.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.ericsson.tools.cpp.tools.environment.EnvironmentManager;


public class BundleLoader {
	private final List<Bundle> bundles = new ArrayList<Bundle>();
	private final ClassLoader loader;
	private final Log log;
	private final EnvironmentManager em;

	private static class Bundle {
		private final Properties properties;

		public Bundle(Properties properties) {
			this.properties = properties;
		}

		public Class<?> loadClass(String name, ClassLoader loader) {
			String className = (String) properties.get(name);
			if (className == null)
				return null;
			try {
				return loader.loadClass(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public <T> Class<T> loadClass(String name, Class<T> iface, ClassLoader loader) {
			Class<?> cls = loadClass(name, loader);
			if (cls == null)
				return null;
			if (!iface.isAssignableFrom(cls))
				return null;
			return (Class<T>) cls;
		}
	}

	public BundleLoader(ClassLoader loader, EnvironmentManager environment, Log log) throws IOException {
		this.loader = loader;
		this.log = log;
		this.em = environment;
		Enumeration<URL> urls = loader.getResources("META-INF/cpp/bundle.properties");
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			InputStream is = url.openStream();
			try {
				Properties p = new Properties();
				p.load(is);
				bundles.add(new Bundle(p));
			} 
			finally {
				is.close();
			}
		}
		log.debug("Loaded "+bundles.size()+" bundles");
	}

	public <T> Collection<T> activate(String name, Class<T> iface) throws MojoExecutionException {
		log.debug("Activating bundles for: " + name);
		Collection<T> objects = new ArrayList<T>(bundles.size());
		for (Bundle bundle : bundles) {
			Class<T> cls = bundle.loadClass(name, iface, loader);
			if (cls != null) {
				try {
					Constructor<T> construct = cls.getConstructor(EnvironmentManager.class, Log.class);
					objects.add(construct.newInstance(em, log));
				} 
				catch (NoSuchMethodException e) {
					throw new MojoExecutionException("Incompatible constructor for: " + name, e);
				} 
				catch (Exception e) {
					throw new MojoExecutionException("Unable to instantiate: " + name, e);
				}
			}
		}
		log.debug("Activated " + objects.size() + " of " + bundles.size());
		return objects;
	}
}
