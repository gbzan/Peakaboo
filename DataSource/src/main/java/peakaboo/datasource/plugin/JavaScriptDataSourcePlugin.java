package peakaboo.datasource.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import bolt.scripting.BoltInterface;
import bolt.scripting.languages.JavascriptLanguage;
import bolt.scripting.plugin.BoltScriptPlugin;
import peakaboo.datasource.model.components.datasize.DataSize;
import peakaboo.datasource.model.components.fileformat.FileFormat;
import peakaboo.datasource.model.components.fileformat.SimpleFileFormat;
import peakaboo.datasource.model.components.interaction.Interaction;
import peakaboo.datasource.model.components.interaction.SimpleInteraction;
import peakaboo.datasource.model.components.metadata.Metadata;
import peakaboo.datasource.model.components.physicalsize.PhysicalSize;
import peakaboo.datasource.model.components.scandata.ScanData;
import peakaboo.datasource.model.components.scandata.SimpleScanData;
import scitypes.ISpectrum;
import scitypes.util.StringInput;


public class JavaScriptDataSourcePlugin implements DataSourcePlugin, BoltScriptPlugin {

	private BoltInterface js;
	private SimpleScanData scanData;
	private File scriptFile;
	
	private Interaction interaction = new SimpleInteraction();
	
	public JavaScriptDataSourcePlugin() {		

	}
	
	@Override
	public void setScriptFile(File file) {
		try {
			js = new BoltInterface(new JavascriptLanguage(), "");
			this.scriptFile = file;
			try {
				js.setScript(StringInput.contents(this.scriptFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			js.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private <T> T lookup(String var, T fallback) {
		Object o = js.get(var);
		T val = (T)o;
		if (val != null) {
			return val;
		}
		return fallback;
	}

	
	@Override
	public boolean pluginEnabled() {
		return true;
	}

	@Override
	public FileFormat getFileFormat() {
		String ext = lookup("formatExtension", null);
		if (ext == null) {
			return new SimpleFileFormat(
					true, 
					lookup("formatName", "Unknown JavaScript Data Source"), 
					lookup("formatDesc", "Unknown JavaScript Data Source")
			);
		} else {
			return new SimpleFileFormat(
					true, 
					lookup("formatName", "Unknown JavaScript Data Source"), 
					lookup("formatDesc", "Unknown JavaScript Data Source"), 
					ext
			);
		}
	}


	@Override
	public ScanData getScanData() {
		return scanData;
	}

	@Override
	public void read(File file) throws Exception {
		scanData = new SimpleScanData(file.getName());
		
		List<List<Double>> result = (List<List<Double>>) js.call("read", StringInput.contents(file));
		
		for (List<Double> scan : result) {
			ISpectrum spectrum = new ISpectrum(scan.size());
			for (Double entry : scan) {
				spectrum.add(entry.floatValue());
			}
			scanData.add(spectrum);
		}
		
	}

	@Override
	public void read(List<File> files) throws Exception {
		read(files.get(0));
	}

	@Override
	public String pluginVersion() {
		return lookup("pluginVersion", "None");
	}

	
	@Override
	public String pluginName() {
		return getFileFormat().getFormatName();
	}

	@Override
	public String pluginDescription() {
		return getFileFormat().getFormatDescription();
	}
	
	
	
	
	
	
	@Override
	public void setInteraction(Interaction interaction) {
		this.interaction = interaction;
	}
	
	public Interaction getInteraction() {
		return interaction;
	}
	
	
	
	
	

	
	@Override
	public Metadata getMetadata() {
		//Not Implemented
		return null;
	}

	@Override
	public DataSize getDataSize() {
		//Not Implemented
		return null;
	}

	@Override
	public PhysicalSize getPhysicalSize() {
		//Not Implemented
		return null;
	}


}