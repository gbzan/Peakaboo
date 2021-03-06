package org.peakaboo.filter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.peakaboo.common.PeakabooLog;
import org.peakaboo.dataset.DataSet;
import org.peakaboo.filter.plugins.JavaFilterPlugin;
import org.peakaboo.framework.autodialog.model.Parameter;
import org.peakaboo.framework.autodialog.model.Value;
import org.peakaboo.framework.cyclops.ReadOnlySpectrum;
import org.peakaboo.framework.cyclops.Spectrum;

/**
 * 
 * This abstract class defines a filter for a {@link Spectrum} of data. A large part of this abstract
 * class is for use in UIs, as it focuses on classifying the type and name of the filter, and the
 * types of parameters. This provides a way for UIs to display the filter and let the user change the
 * settings.
 * 
 * 
 * @author Nathaniel Sherry, 2009-2012
 * 
 */

public abstract class AbstractFilter implements Serializable, JavaFilterPlugin {
	
	private List<Value<?>>		parameters;
	public boolean				enabled;
		
	
	//==============================================
	// PLUGIN METHODS
	//==============================================	

	@Override
	public String pluginName() {
		return getFilterName();
	}

	@Override
	public String pluginDescription() {
		return getFilterDescription();
	}
	
	
	public AbstractFilter() {
		this.parameters = new ArrayList<>();
		this.enabled = true;
	}

	@Override
	public final List<Value<?>> getParameters() {
		return this.parameters;
	}
	
	@Override
	public final void setParameters(List<Value<?>> params) {
		parameters = params;
	}
	
	protected void addParameter(Parameter<?> param) {
		parameters.add(param);
	}
	
	protected void addParameter(Parameter<?>... params) {
		for (Parameter<?> param : params) { addParameter(param); }
	}
	
	
	/**
	 * Filter the given {@link Spectrum} and return the modified result
	 * @param data the Spectrum to filter
	 */
	protected abstract ReadOnlySpectrum filterApplyTo(ReadOnlySpectrum data, DataSet dataset);
		
	

	@Override
	public ReadOnlySpectrum filter(ReadOnlySpectrum data, DataSet dataset) {
		
		try{
			ReadOnlySpectrum newdata = filterApplyTo(data, dataset);
			if (newdata != null) return newdata;
			return data;
		}
		catch(Throwable e)
		{
			PeakabooLog.get().log(Level.SEVERE, "Error applying filter " + this.getClass().getSimpleName(), e);
			return data;
		}
		
	}

	public String toString() {
		return this.getFilterName();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}	
	
}
