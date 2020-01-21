package org.peakaboo.ui.swing.calibration.referenceplot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import org.peakaboo.calibration.CalibrationReference;
import org.peakaboo.curvefit.peak.table.Element;
import org.peakaboo.curvefit.peak.transition.TransitionShell;
import org.peakaboo.display.calibration.CalibrationReferencePlot;
import org.peakaboo.framework.cyclops.Coord;
import org.peakaboo.framework.cyclops.visualization.Surface;
import org.peakaboo.framework.cyclops.visualization.backend.awt.GraphicsPanel;

public class ReferencePlot extends GraphicsPanel {

	CalibrationReferencePlot plot;
	
	public ReferencePlot(CalibrationReference reference, TransitionShell type) {
		plot = new CalibrationReferencePlot(reference, type);
		
		addMouseMotionListener(new MouseMotionAdapter() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				Element element = plot.getElement(plot.getIndex(e.getX()));
								
				boolean changed = plot.setHighlighted(element);
				if (changed) {
					ReferencePlot.this.repaint();
				}
			}

		});
		
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
