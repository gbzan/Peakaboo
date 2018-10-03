package peakaboo.datasink.plugin;

import java.io.File;
import java.util.logging.Level;

import net.sciencestudio.bolt.plugin.core.BoltFilesytstemPluginLoader;
import net.sciencestudio.bolt.plugin.core.BoltClassloaderPluginLoader;
import net.sciencestudio.bolt.plugin.core.BoltPluginSet;
import net.sciencestudio.bolt.plugin.core.BoltPluginManager;
import net.sciencestudio.bolt.plugin.java.IBoltJavaPluginLoader;
import net.sciencestudio.bolt.plugin.java.ClassInheritanceException;
import net.sciencestudio.bolt.plugin.java.ClassInstantiationException;
import peakaboo.common.PeakabooLog;
import peakaboo.datasink.plugin.plugins.CSV;

public class DataSinkPluginManager extends BoltPluginManager<DataSinkPlugin>
{

	public static DataSinkPluginManager SYSTEM;
	
	public static void init(File dataSinkDir) {
		if (SYSTEM == null) {
			SYSTEM = new DataSinkPluginManager(dataSinkDir);
			SYSTEM.load();
		}
	}
	
	public DataSinkPluginManager(File dataSinkDir) {
		super(dataSinkDir);
	}
	
	@Override
	protected void loadCustomPlugins() {
		try {
			
			BoltClassloaderPluginLoader<JavaDataSinkPlugin> javaLoader = javaLoader(getPlugins());
			
			//register built-in plugins
			javaLoader.registerPlugin(CSV.class);

			
		} catch (ClassInheritanceException | ClassInstantiationException e) {
			PeakabooLog.get().log(Level.SEVERE, "Failed to load Data Sink plugins", e);
		}  
		

	}

	@Override
	protected BoltClassloaderPluginLoader<JavaDataSinkPlugin> javaLoader(BoltPluginSet<DataSinkPlugin> pluginset) throws ClassInheritanceException {
		return new IBoltJavaPluginLoader<JavaDataSinkPlugin>(pluginset, JavaDataSinkPlugin.class);
	}

	@Override
	protected BoltFilesytstemPluginLoader<? extends DataSinkPlugin> filesystemLoader(BoltPluginSet<DataSinkPlugin> pluginset) {
		return null;
	}
	


		
}
