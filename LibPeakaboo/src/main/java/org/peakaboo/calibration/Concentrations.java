package org.peakaboo.calibration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.peakaboo.curvefit.peak.table.Element;
import org.peakaboo.curvefit.peak.transition.ITransitionSeries;
import org.peakaboo.curvefit.peak.transition.TransitionShell;

public class Concentrations {

	private Map<Element, Float> concentrations;
	private Map<Element, ITransitionSeries> sources;
	private NumberFormat format = new DecimalFormat("0.000000");
	private CalibrationProfile profile;
	
	public Concentrations(Map<Element, Float> concentrations, Map<Element, ITransitionSeries> sources, CalibrationProfile profile) {
		this.concentrations = concentrations;
		this.profile = profile;
		this.sources = sources;
	}
	
	public List<Element> elementsByZ() {
		List<Element> sorted = new ArrayList<>(concentrations.keySet());
		sorted.sort(Element::compare);
		return sorted;
	}
	
	public List<Element> elementsByConcentration() {
		List<Element> sorted = new ArrayList<>(concentrations.keySet());
		sorted.sort((e1, e2) -> -Float.compare(getPercent(e1), getPercent(e2)));
		return sorted;
	}
	
	public float getPercent(Element e) {
		if (concentrations.containsKey(e)) {
			return concentrations.get(e);
		}
		return 0f;
	}
	
	public String getPercentFormatted(Element e) {
		float ppm = getPercent(e);
		return format.format(ppm/10000) + "%";
	}
	
	public float getRatio(Element e) {
		if (!concentrations.containsKey(e)) {
			return 0;
		}
		float percent = getPercent(e);
		float anchor = getPercent(profile.getReference().getAnchor().getElement());
		float ratio = percent / anchor;
		return ratio;
	}
	
	public String getRatioFormatted(Element e) {
		float ratio = getRatio(e);
		return format.format(ratio);
	}
	

	
	public boolean contains(Element e) {
		return concentrations.containsKey(e);
	}
	
	public boolean isCalibrated(Element e) {
		if (!sources.containsKey(e)) {
			return false;
		}
		ITransitionSeries source = getConcentrationSource(e);
		return profile.contains(source);
	}
	
	public ITransitionSeries getConcentrationSource(Element e) {
		return sources.getOrDefault(e, null);
	}
	
	public CalibrationProfile getProfile() {
		return profile;
	}

	public static Concentrations calculate(List<ITransitionSeries> tss, CalibrationProfile profile, Function<ITransitionSeries, Float> intensityFunction) {

		//find best TransitionSeries per element to measure
		Map<Element, ITransitionSeries> elements = new LinkedHashMap<>();
		for (TransitionShell shell : new TransitionShell[] {TransitionShell.M, TransitionShell.L, TransitionShell.K}) {
			for (ITransitionSeries ts : tss) {
				Element e = ts.getElement();
				if (ts.getShell() != shell) { continue; }
				
				/*
				 * if there is already an entry, we generally want to replace it with a lower
				 * shell, but we also don't want to replace a calibrated value with an
				 * uncalibrated one.
				 */				
				if (elements.containsKey(e)) {
					ITransitionSeries prev = elements.get(e);
					//if the previous entry is calibrated, but the new one isn't, skip it
					if (profile.contains(prev) && !profile.contains(ts)) {
						continue;
					}
				}
				elements.put(e, ts);
			}
		}
		
		//calculate calibrated intensities per element and sum total intensity
		float sum = 0;
		Map<Element, Float> intensities = new LinkedHashMap<>();
		for (Element element : elements.keySet()) {
			ITransitionSeries ts = elements.get(element);
			float intensity = intensityFunction.apply(ts);
			
			intensities.put(ts.getElement(), intensity);
			sum += intensity;
		}
		
		//TODO: How to handle uncalibrated elements?
		Map<Element, Float> ppm = new LinkedHashMap<>();
		List<Element> sorted = new ArrayList<>(intensities.keySet());
		sorted.sort(Element::compare);
		
		for (Element element : sorted) {
			ppm.put(element, intensities.get(element) / sum * 1e6f);
		}
		return new Concentrations(ppm, elements, profile);
	}
	
}
