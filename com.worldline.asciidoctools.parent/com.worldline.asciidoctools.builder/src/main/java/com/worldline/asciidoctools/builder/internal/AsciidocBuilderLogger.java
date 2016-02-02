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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 * Implementation of logger class that wraps the Eclipse Error Log logging mechanism
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocBuilderLogger {

	private static int loggerLevel = getLoggerLevel();

	public static void setLevel(int level) {
		if (isValidLevel(level)) {
			Activator.getDefault().getPreferenceStore().setValue(LOGGER_LEVEL, level);
			AsciidocBuilderLogger.loggerLevel = getLoggerLevel();
		}
	}

	public static final String LOGGER_LEVEL = Activator.PLUGIN_ID + ".loggerlevel";

	private static int getLoggerLevel() {
		boolean contains = Activator.getDefault().getPreferenceStore().contains(LOGGER_LEVEL);
		if (!contains) {
			Activator.getDefault().getPreferenceStore().setValue(LOGGER_LEVEL, IStatus.WARNING);
		}
		return Activator.getDefault().getPreferenceStore().getInt(LOGGER_LEVEL);
	}

	public static void info(String message, Object... parameters) {
		log(IStatus.INFO, message, null, parameters);
	}

	public static void warn(String message, Object... parameters) {
		log(IStatus.WARNING, message, null, parameters);
	}

	public static void error(String message, Object... parameters) {
		log(IStatus.ERROR, message, null, parameters);
	}

	public static void cancel(String message, Object... parameters) {
		log(IStatus.CANCEL, message, null, parameters);
	}

	public static void info(String message, Throwable t, Object... parameters) {
		log(IStatus.INFO, message, t, parameters);
	}

	public static void warn(String message, Throwable t, Object... parameters) {
		log(IStatus.WARNING, message, t, parameters);
	}

	public static void error(String message, Throwable t, Object... parameters) {
		log(IStatus.ERROR, message, t, parameters);
	}

	public static void cancel(String message, Throwable t, Object... parameters) {
		log(IStatus.CANCEL, message, t, parameters);
	}

	private static void log(int level, String message, Throwable t, Object... parameters) {
		if (loggerLevel >= level && isValidLevel(level)) {
			String _message = parameters.length > 0 ? String.format(message, parameters) : message;
			Status status = t != null ? new Status(level, Activator.PLUGIN_ID, _message, t) : new Status(level, Activator.PLUGIN_ID, _message);
			Activator.getDefault().getLog().log(status);
		}

	}

	private static boolean isValidLevel(int level) {
		return (level == IStatus.CANCEL || level == IStatus.INFO || level == IStatus.WARNING || level == IStatus.ERROR);
	}

}
