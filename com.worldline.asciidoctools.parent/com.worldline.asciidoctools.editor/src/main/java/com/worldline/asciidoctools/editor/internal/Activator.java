package com.worldline.asciidoctools.editor.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.worldline.asciidoctools.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns image in plugin
	 * 
	 * @param pluginId
	 *            : Id of the plugin containing thie image
	 * @param imageFilePath
	 *            : image File Path in plugin
	 * @return Image if exists
	 */
	public Image getImage(String pluginId, String imageFilePath) {
		Image image = Activator.getDefault().getImageRegistry().get(pluginId + ":" + imageFilePath);
		if (image == null) {
			image = loadImage(pluginId, imageFilePath);
		}
		return image;
	}

	/**
	 * Loads image in Image Registry is not available in it
	 *
	 * @param pluginId
	 *            : Id of the plugin containing thie image
	 * @param imageFilePath
	 *            : image File Path in plugin
	 * @return Image if loaded
	 */
	private synchronized Image loadImage(String pluginId, String imageFilePath) {
		String id = pluginId + ":" + imageFilePath;
		Image image = Activator.getDefault().getImageRegistry().get(id);
		if (image != null)
			return image;
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
		if (imageDescriptor != null) {
			image = imageDescriptor.createImage();
			Activator.getDefault().getImageRegistry().put(pluginId + ":" + imageFilePath, image);
		}
		return image;
	}

	/**
	 * Returns image in this plugin
	 * 
	 * @param imageFilePath
	 *            : image File Path in this plugin
	 * @return Image if exists
	 */
	public Image getImage(String imageFilePath) {
		Image image = Activator.getDefault().getImageRegistry().get(Activator.PLUGIN_ID + ":" + imageFilePath);
		if (image == null)
			image = loadImage(Activator.PLUGIN_ID, imageFilePath);
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Activator.plugin.createImageRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.plugin.getImageRegistry().dispose();
		Activator.plugin = null;
		super.stop(context);
	}

}
