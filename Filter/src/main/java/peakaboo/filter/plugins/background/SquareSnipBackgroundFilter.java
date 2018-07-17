package peakaboo.filter.plugins.background;


import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.style.editors.IntegerStyle;
import peakaboo.filter.model.AbstractBackgroundFilter;
import scitypes.ISpectrum;
import scitypes.ReadOnlySpectrum;
import scitypes.Spectrum;

public class SquareSnipBackgroundFilter extends AbstractBackgroundFilter {

	Parameter<Integer> pHalfWindow;
	Parameter<Integer> pIterations;
	
	@Override
	public void initialize() {
		pHalfWindow = new Parameter<>("Half-Window Size", new IntegerStyle(), 150, this::validate);
		pIterations = new Parameter<>("Iterations", new IntegerStyle(), 10, this::validate);
		addParameter(pHalfWindow, pIterations);
	}
	
	private boolean validate(Object o) {
		if (pHalfWindow.getValue() < 50 || pHalfWindow.getValue() > 200) return false;
		if (pIterations.getValue() < 5 || pIterations.getValue() > 50) return false;
		return true;
	}
	
	@Override
	public boolean pluginEnabled() {
		return true;
	}

	@Override
	public String pluginVersion() {
		return "1.0";
	}

	@Override
	protected ReadOnlySpectrum getBackground(ReadOnlySpectrum data, int percent) {
		//TODO: parameterize
		int window = pHalfWindow.getValue();
		int iterations = pIterations.getValue();
		
		//create 2 buffers to bounce the data back and forth between
		Spectrum buffer1 = new ISpectrum(data);
		Spectrum buffer2 = new ISpectrum(data);
		
		for (int i = 0; i < buffer1.size(); i++) {
			buffer1.set(i, (float) Math.sqrt(Math.sqrt(buffer1.get(i))));
		}
		
		Spectrum source = null, target = null;
		for (int l = 0; l < iterations; l++) {
			
			if (l - iterations < 8) {
				window /= 1.41;
			}
			
			source = buffer1;
			target = buffer2;
			for (int i = 0; i < source.size(); i++) {
				int lChannel = Math.max(0, i - window);
				int rChannel = Math.min(source.size()-1, i + window);
				float mean = (source.get(lChannel) + source.get(rChannel)) / 2f;
				if (mean < source.get(i)) { 
					target.set(i, mean);
				} else {
					target.set(i, source.get(i));
				}
			}
			//flip buffers 1 and 2 around so that we're constantly copying the data back and forth
			buffer1 = target;
			buffer2 = source;
		}
		
		float fPercent = percent  /100f;
		//which ever buffer was the target last, that is where we get our data from
		//now we apply an inverse transform to the sqrt at the start
		for (int i = 0; i < target.size(); i++) {
			target.set(i, (float) Math.pow(Math.pow(target.get(i), 2), 2) * fPercent);
		}
		
		return target;
	}

	@Override
	public String getFilterName() {
		return "Square Snip";
	}

	@Override
	public String getFilterDescription() {
		return "Square Snip is a fast background removal method based on the Peak Stripping algorithm. It iteratively replaces signal with the average of the points -window and +window channels apart if that average is less than the existing signal. By taking a double square root of the signal and then reversing it afterwards, the number if iterations required is greatly reduced. Because noise-reduction filters are separate and composable, this version of the algorithm does not do any smoothing of it's own.";
	}



	@Override
	public boolean canFilterSubset() {
		return true;
	}

}
