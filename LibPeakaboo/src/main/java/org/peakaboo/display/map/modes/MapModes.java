package org.peakaboo.display.map.modes;

import org.peakaboo.display.map.modes.composite.CompositeMapMode;
import org.peakaboo.display.map.modes.correlation.CorrelationMapMode;
import org.peakaboo.display.map.modes.overlay.OverlayMapMode;
import org.peakaboo.display.map.modes.ratio.RatioMapMode;

public enum MapModes {
	COMPOSITE
	{
		@Override
		public String toString() { return "Composite"; }
		
		@Override 
		public MapMode getMapper() { return new CompositeMapMode(); }
		
	},
	OVERLAY
	{
		@Override
		public String toString() { return "Overlay"; }
		
		@Override 
		public MapMode getMapper() { return new OverlayMapMode(); }
		
	},
	RATIO
	{
		@Override
		public String toString() { return "Ratio"; }
		
		@Override 
		public MapMode getMapper() { return new RatioMapMode(); }
	},
	CORRELATION
	{
		@Override
		public String toString() { return "Correlation"; }
		
		@Override 
		public MapMode getMapper() { return new CorrelationMapMode(); }
	}
	
	;
	
	public MapMode getMapper() {
		return null;
	}
}