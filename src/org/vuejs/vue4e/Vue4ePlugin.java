package org.vuejs.vue4e;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Vue4ePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.vuejs.vue4e"; //$NON-NLS-1$

	// The shared instance
	private static Vue4ePlugin plugin;
	private static BundleContext bundleContext = null;
	
	/**
	 * The constructor
	 */
	public Vue4ePlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
		
		// TODO: initiate VLs installation if necessary (ConnectionProvider)
	  // TODO: check prerequisite TextMate plugins and enable either source.vue or lngpck.source.vue 
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		bundleContext = null;
	}

	
	public IPath getPluginDir() throws IOException {
		
		Bundle bundle = bundleContext.getBundle();
		IPath path = Platform.getStateLocation(bundle);
		Path dir = FileSystems.getDefault().getPath(path.toString());
		Files.createDirectories(dir);
		return path;
	}

	public IPath getPluginDir(String folder) throws IOException {
		
		Bundle bundle = bundleContext.getBundle();
		IPath path = Platform.getStateLocation(bundle).append(folder);
		Path dir = FileSystems.getDefault().getPath(path.toString());
		Files.createDirectories(dir);
		return path;
	}
	
	public static boolean isWindowsPlatform() {
		/*
		String osName = System.getProperty("os.name", "generic").toLowerCase();
	    return osName.contains("win");
	    */
		String osName = Platform.getOS();
		return (osName == Platform.OS_WIN32);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Vue4ePlugin getDefault() {
		return plugin;
	}

}
