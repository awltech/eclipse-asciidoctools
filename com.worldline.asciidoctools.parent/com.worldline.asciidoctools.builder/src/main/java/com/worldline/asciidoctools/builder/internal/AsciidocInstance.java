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

public enum AsciidocInstance {
	INSTANCE;

	private Asciidoctor asciidoctor;

	private AsciidocInstance() {
		try {
			Enumeration<URL> resources = Activator.getDefault().getBundle().getResources("/META-INF/jruby.home");
			while (resources.hasMoreElements() && this.asciidoctor == null) {
				URL nextElement = resources.nextElement();

				// Next try : https://github.com/ajuckel/jbosstools-asciidoctor/commit/ae76a4ea572c3ab49daae3ce975c7994e337e585
				String path = getPath(nextElement);
				if (path.endsWith("/"))
					path = path.substring(0, path.length() - 1); // remove trailing slash
				System.setProperty("jruby.home", path);
				try {
					this.asciidoctor = JRubyAsciidoctor.create(Collections.singletonList("gems/asciidoctor-1.5.3/lib"));
				} catch (Exception e) {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
		}

		if (asciidoctor == null) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, " Failed to load Asciidoctor instance. Embedded building process will fail. Maybe you should try to start Eclipse with -clean option ?"));
		}

	}

	public Asciidoctor getAsciidoctor() {
		return asciidoctor;
	}

	private static String getPath(URL url) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		URLConnection conn = url.openConnection();
		Method method = conn.getClass().getMethod("getFileURL");
		method.setAccessible(true);
		URL url2 = (URL) method.invoke(conn);
		return url2.getPath();

	}

}
