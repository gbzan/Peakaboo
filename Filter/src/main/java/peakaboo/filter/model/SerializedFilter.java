package peakaboo.filter.model;

import java.util.List;

public class SerializedFilter {


	private Filter filter;
	
	//These values exist only to initialize the filter, not to be read from.
	private String clazz;
	private List<Object> settings;
	
	
	
	public SerializedFilter() {	}
	
	public SerializedFilter(Filter filter) {
		
		this.filter = filter;
		
	}



	public String getClazz() {
		return filter.getClass().getName();
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public List<Object> getSettings() {
		return filter.save();
	}

	public void setSettings(List<Object> settings) {
		this.settings = settings;
	}

	public Filter getFilter() {
		if (filter != null) { return filter; }
			
		FilterSet filterset = new FilterSet();
		for (Filter f : filterset.getAvailableFilters()) {
			if (f.getClass().getName().equals(clazz)) {
				filter = f;
				filter.initialize();
				filter.load(settings);
				return filter;
			}
		}
		throw new RuntimeException("Cannot find plugin " + clazz);
	}

	
	
	
	
	
	
	
}
