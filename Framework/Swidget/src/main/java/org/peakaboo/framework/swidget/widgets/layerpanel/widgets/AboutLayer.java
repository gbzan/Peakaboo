package org.peakaboo.framework.swidget.widgets.layerpanel.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.peakaboo.framework.swidget.dialogues.AboutDialogue;
import org.peakaboo.framework.swidget.dialogues.AboutDialogue.Contents;
import org.peakaboo.framework.swidget.widgets.Spacing;
import org.peakaboo.framework.swidget.widgets.layerpanel.HeaderLayer;
import org.peakaboo.framework.swidget.widgets.layerpanel.LayerPanel;
import org.peakaboo.framework.swidget.widgets.layout.CenteringLayout;
import org.peakaboo.framework.swidget.widgets.layout.HeaderTabBuilder;
import org.peakaboo.framework.swidget.widgets.layout.PropertyPanel;

public class AboutLayer extends HeaderLayer {

	public AboutLayer(LayerPanel owner, AboutDialogue.Contents contents) {
		super(owner, true);
		
		
		
		HeaderTabBuilder tb = new HeaderTabBuilder();
		tb.addTab("About", AboutDialogue.aboutPanel(contents));
		tb.addTab("Credits", creditsPanel(contents));
		tb.addTab("Licence", licencePanel(contents));
		
		getHeader().setCentre(tb.getTabStrip());
		setBody(tb.getBody());
		getBody().setPreferredSize(new Dimension(610, 280));
		
	}

	private Component creditsPanel(Contents contents) {
		
		Map<String, String> credits = new LinkedHashMap<>();
		for (String credit : contents.credits.split("\n")) {
			if (credit.trim().length() == 0) {
				continue;
			}
			String[] creditParts = credit.split(": ");
			credits.put(creditParts[0], creditParts[1]);
		}
		PropertyPanel creditsPanel = new PropertyPanel(credits, false, 0);
		JPanel container = new JPanel(new CenteringLayout());
		container.add(creditsPanel);
		return container;
	}
	
	private static Component licencePanel(AboutDialogue.Contents contents) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(Spacing.bHuge());
		JTextArea text = new JTextArea();
		text.setEditable(false);
		text.setBackground(Color.WHITE);
		text.setText(contents.licence);
		text.setFont(Font.decode(Font.MONOSPACED));
		
		JScrollPane scroller = new JScrollPane(text);
		panel.add(scroller, BorderLayout.CENTER);
		return panel;
	}



}
