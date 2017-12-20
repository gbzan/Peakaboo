package peakaboo.filter.plugins.noise;


import autodialog.model.Parameter;
import autodialog.model.style.editors.IntegerStyle;
import autodialog.model.style.editors.RealStyle;
import peakaboo.calculations.Noise;
import peakaboo.filter.model.AbstractSimpleFilter;
import peakaboo.filter.model.FilterType;
import scitypes.ReadOnlySpectrum;

/**
 * 
 * This class is a filter exposing the Moving Average functionality elsewhere in this programme.
 * 
 * @author Nathaniel Sherry, 2009
 */


public final class SpringSmoothing extends AbstractSimpleFilter
{

	private Parameter<Integer> iterations;
	private Parameter<Float> multiplier;
	private Parameter<Float> falloff;

	public SpringSmoothing()
	{
		super();
	}
	
	@Override
	public String pluginVersion() {
		return "1.0";
	}
	
	@Override
	public void initialize()
	{
		iterations = new Parameter<>("Iterations", new IntegerStyle(), 20, this::validate);
		multiplier = new Parameter<>("Linear Force Multiplier", new RealStyle(), 20.0f, this::validate);
		falloff = new Parameter<>("Exponential Force Falloff Rate", new RealStyle(), 2.0f, this::validate);
		
		addParameter(iterations, multiplier, falloff);
		
	}


	@Override
	public String getFilterName()
	{
		return "Spring Smoothing";
	}



	@Override
	public FilterType getFilterType()
	{

		return FilterType.NOISE;
	}


	private boolean validate(Parameter<?> p)
	{

		double mult, power;
		int iters;

		
		mult = multiplier.getValue();
		if (mult > 100 || mult < 0.1) return false;
		
		power = falloff.getValue();
		if (power > 10 || power <= 0.0) return false;
		
		iters = iterations.getValue();
		if (iters > 50 || iters < 1) return false;
		

		return true;
	}


	@Override
	public String getFilterDescription()
	{
		// TODO Auto-generated method stub
		return "The "
				+ getFilterName()
				+ " filter operates on the assumption that weak signal should be smoothed more than strong signal. It treats each adjacent pair of points as if they were connected by a spring. With each iteration, a tension force draws neighbouring points closer together. The Force Multiplier controls how strongly a pair of elements are pulled together, and the Force Falloff Rate controls how aggressively stronger signal is anchored in place, unmoved by spring forces. This prevents peak shapes from being distorted by the smoothing algorithm.";
	}


	@Override
	public ReadOnlySpectrum filterApplyTo(ReadOnlySpectrum data)
	{
		data = Noise.SpringFilter(
				data, 
				multiplier.getValue().floatValue(), 
				falloff.getValue().floatValue(), 
				iterations.getValue()
			);
		return data;
	}

	@Override
	public boolean pluginEnabled()
	{
		return true;
	}

	@Override
	public boolean canFilterSubset()
	{
		return true;
	}

	
}
