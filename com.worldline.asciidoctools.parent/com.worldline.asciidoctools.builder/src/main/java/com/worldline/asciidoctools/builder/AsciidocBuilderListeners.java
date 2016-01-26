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
package com.worldline.asciidoctools.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;

/**
 * 
 * Singleton instance that contains the listeners to be called when project is being built.
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public enum AsciidocBuilderListeners {

	INSTANCE;

	private Collection<AsciidocBuilderListener> listeners = new ArrayList<AsciidocBuilderListener>();

	public void register(AsciidocBuilderListener listener) {
		this.listeners.add(listener);
	}

	public void unregister(AsciidocBuilderListener listener) {
		this.listeners.remove(listener);
	}

	public void notifyBuild(IFile file) {
		for (AsciidocBuilderListener listener : this.listeners) {
			listener.onBuild(file);
		}
	}

	public void notify(Iterable<IFile> files) {
		for (IFile file : files) {
			this.notifyBuild(file);
		}
	}

}
