package org.peakaboo.filter.plugins.mathematical;


import org.peakaboo.dataset.DataSet;
import org.peakaboo.filter.model.AbstractFilter;
import org.peakaboo.filter.model.FilterType;
import org.peakaboo.framework.cyclops.ReadOnlySpectrum;
import org.peakaboo.framework.cyclops.Spectrum;
import org.peakaboo.framework.cyclops.SpectrumCalculations;


public class DerivativeMathFilter extends AbstractFilter {

	
	@Override
	public String pluginVersion() {
		return "1.0";
	}
	
	@Override
	public void initialize() {
		//NOOP
	}
	
	@Override
	protected ReadOnlySpectrum filterApplyTo(ReadOnlySpectrum data, DataSet dataset) {
		return deriv(data);
	}


	@Override
	public String getFilterDescription() {
		return "The " + getFilterName() + " transforms the data such that each channel represents the difference between itself and the channel before it.";
	}


	@Override
	public String getFilterName() {
		return "Derivative";
	}


	@Override
	public FilterType getFilterType() {
		return FilterType.MATHEMATICAL;
	}

	@Override
	public boolean pluginEnabled() {
		return true;
	}
	
	
	@Override
	public boolean canFilterSubset() {
		return true;
	}
	

	/**
	 * Calculates the derivitive (deltas) for a spectrum
	 * @param list the data to find the deltas for
	 * @return a list of deltas
	 */
	public static Spectrum deriv(ReadOnlySpectrum list) {
		return SpectrumCalculations.derivative(list);
	}

	@Override
	public String pluginUUID() {
		return "779ca35d-0f68-4ea9-b3f4-4aef46977477";
	}
	
}
