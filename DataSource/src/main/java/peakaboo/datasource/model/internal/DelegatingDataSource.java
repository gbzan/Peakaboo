package peakaboo.datasource.model.internal;

import java.io.File;
import java.util.List;

import peakaboo.datasource.model.DataSource;
import peakaboo.datasource.model.components.datasize.DataSize;
import peakaboo.datasource.model.components.fileformat.FileFormat;
import peakaboo.datasource.model.components.interaction.Interaction;
import peakaboo.datasource.model.components.metadata.Metadata;
import peakaboo.datasource.model.components.physicalsize.PhysicalSize;
import peakaboo.datasource.model.components.scandata.ScanData;

public class DelegatingDataSource implements DataSource {

	private DataSource backer;
	
	public DelegatingDataSource() {
		this(new EmptyDataSource());
	}
	
	public DelegatingDataSource(DataSource backer) {
		this.backer = backer;
	}
	
	public void setDataSource(DataSource backer) {
		this.backer = backer;
	}
	
	public DataSource getDataSource() {
		return backer;
	}
	
	@Override
	public void setInteraction(Interaction interaction) {
		this.backer.setInteraction(interaction);
	}
	@Override
	public Interaction getInteraction() {
		return this.backer.getInteraction();
	}

	
	public Metadata getMetadata() {
		return backer.getMetadata();
	}

	public boolean hasMetadata() {
		return backer.hasMetadata();
	}

	public DataSize getDataSize() {
		return backer.getDataSize();
	}

	public boolean hasDataSize() {
		return backer.hasDataSize();
	}

	public FileFormat getFileFormat() {
		return backer.getFileFormat();
	}

	public void read(File file) throws Exception {
		backer.read(file);
	}

	public void read(List<File> files) throws Exception {
		backer.read(files);
	}

	@Override
	public ScanData getScanData() {
		return backer.getScanData();
	}

	@Override
	public PhysicalSize getPhysicalSize() {
		return backer.getPhysicalSize();
	}
	
	
	
}