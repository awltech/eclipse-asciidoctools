/**
 * AsciidocTools by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.worldline.asciidoctools.builder.internal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 * Singleton containing the effective underlying Asciidoc instance.
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public enum AsciidocInstance {
	INSTANCE;

	private Asciidoctor asciidoctor;

	private AsciidocInstance() {
		try {
			Enumeration<URL> resources = Activator.getDefault().getBundle().getResources("/META-INF/jruby.home");
			while (resources.hasMoreElements() && this.asciidoctor == null) {
				URL nextElement = resources.nextElement();
				String path = getPath(nextElement);
				if (path.endsWith("/"))
					path = path.substring(0, path.length() - 1);
				System.setProperty("jruby.home", path);
				Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Set JRuby Home path to : " + path));
				try {
					long begin = System.nanoTime();
					this.asciidoctor = JRubyAsciidoctor.create(Collections.singletonList("gems/asciidoctor-1.5.3/lib"));
					AsciidocBuilderLogger.info("Asciidoctor loaded successfully with version " + this.asciidoctor.asciidoctorVersion() + " in "
							+ (System.nanoTime() - begin) / 1000000 + " ms.");
				} catch (Exception e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			AsciidocBuilderLogger.warn(e.getMessage(), e);
		}

		if (asciidoctor == null) {
			AsciidocBuilderLogger.error(
					" Failed to load Asciidoctor instance. Embedded building process will fail. Maybe you should try to start Eclipse with -clean option ?");
		}

	}

	public Asciidoctor getAsciidoctor() {
		return asciidoctor;
	}

	private static String getPath(URL url) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		URLConnection conn = url.openConnection();
		Method method = conn.getClass().getMethod("getFileURL");
		method.setAccessible(true);
		URL url2 = (URL) method.invoke(conn);
		return url2.getPath();

	}

}
