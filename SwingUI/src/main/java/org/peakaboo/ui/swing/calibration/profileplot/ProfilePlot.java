package org.peakaboo.ui.swing.calibration.profileplot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;

import org.peakaboo.calibration.CalibrationProfile;
import org.peakaboo.curvefit.peak.table.Element;
import org.peakaboo.curvefit.peak.transition.TransitionShell;
import org.peakaboo.display.calibration.CalibrationProfilePlot;
import org.peakaboo.framework.cyclops.Coord;
import org.peakaboo.framework.cyclops.visualization.Surface;
import org.peakaboo.framework.cyclops.visualization.backend.awt.GraphicsPanel;

public class ProfilePlot extends GraphicsPanel {

	private CalibrationProfilePlot plot;

	public ProfilePlot(CalibrationProfile profile, File source, TransitionShell type) {
		plot = new CalibrationProfilePlot(profile, type, source);
		
		addMouseMotionListener(new MouseMotionAdapter() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				Element element = plot.getElement(plot.getIndex(e.getX()));
								
				boolean changed = plot.setHighlighted(element);
				if (changed) {
					ProfilePlot.this.repaint();
				}
			}

		});
		
	}
	
	public void setCalibrationProfile(CalibrationProfile profile, File source) {
		plot.setData(profile, source);
	}
	
	
	
	public boolean isLogView() {
		return plot.isLogView();
	}

	public void setLogView(boolean logView) {
		boolean needsRepaint = logView != plot.isLogView();
		plot.setLogView(logView);
		if (needsRepaint) { repaint(); }
	}

	@Override
	protected void drawGraphics(Surface backend, Coord<Integer> size) {
		plot.draw(backend, size);		
	}

	@Override
	public float getUsedWidth() {
		return getWidth();
	}

	@Override
	public float getUsedWidth(float zoom) {
		return getWidth();
	}

	@Override
	public float getUsedHeight() {
		return getHeight();
	}

	@Override
	public float getUsedHeight(float zoom) {
		return getHeight();
	}


	
}
