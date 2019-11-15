package org.peakaboo.framework.stratus.painters.tabs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import org.peakaboo.framework.stratus.Stratus;
import org.peakaboo.framework.stratus.Stratus.ButtonState;
import org.peakaboo.framework.stratus.painters.StatefulPainter;
import org.peakaboo.framework.stratus.theme.Theme;

public class TabPainter extends StatefulPainter{

	Color fillNL, bottomNL;
	Color fillTL, bottomTL;
	
	Stroke bottomStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	
	public TabPainter(Theme theme, ButtonState... buttonState) {
		super(theme, buttonState);
		
		if (isSelected()) {
			fillNL = getTheme().getNegative();
			bottomNL = getTheme().getHighlight();
			fillTL = getTheme().getNegative();
			bottomTL = getTheme().getHighlight();
		} else {
			fillNL = getTheme().getNegative();
			bottomNL = Stratus.darken(getTheme().getWidgetBorder());
			fillTL = getTheme().getNegative();
			bottomTL = Stratus.darken(getTheme().getWidgetBorder());
		}

		
	}

	
	@Override
	public void paint(Graphics2D g, JComponent object, int width, int height) {
		
		
		Color fill, bottom;
		boolean isTopLevel = (object.getParent().getParent().getParent().getParent() instanceof Window);
		if (isTopLevel) {
			fill = fillTL;
			bottom = bottomTL;
		} else {
			fill = fillNL;
			bottom = bottomNL;
		}
		
		if (isFocused() || isSelected() || isMouseOver()) {
		
			g.setColor(fill);
			g.fillRect(0, 0, width, height);

			//bottom stroke
			Stroke old = g.getStroke();
			g.setStroke(bottomStroke);
			g.setColor(bottom);
			g.drawLine(0, height-2, width, height-2);
			g.setStroke(old);
			
			//border
			g.setColor(getTheme().getWidgetBorder());
			GeneralPath border = new GeneralPath();
			border.moveTo(0, height-1);
			border.lineTo(0, 0);
			border.lineTo(width, 0);
			border.lineTo(width, height-1);
			g.draw(border);
			

		}
		
    	//Focus dash if focused but not pressed
		int pad = 4;
    	if (isFocused() && !isPressed()) {
        	g.setPaint(new Color(0, 0, 0, 0.15f));
        	Shape focus = new RoundRectangle2D.Float(pad, pad, width-pad*2, height-pad*2 - 3, 0, 0);
        	Stroke old = g.getStroke();
        	g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {2, 2}, 0f));
        	g.draw(focus);
        	g.setStroke(old);
    	}
	}

}
