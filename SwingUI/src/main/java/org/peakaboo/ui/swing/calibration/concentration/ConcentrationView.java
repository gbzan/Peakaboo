package org.peakaboo.ui.swing.calibration.concentration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JPanel;

import org.peakaboo.calibration.Concentrations;
import org.peakaboo.common.PeakabooLog;
import org.peakaboo.curvefit.peak.table.Element;
import org.peakaboo.curvefit.peak.transition.ITransitionSeries;
import org.peakaboo.framework.stratus.controls.ButtonLinker;
import org.peakaboo.framework.swidget.dialogues.fileio.SimpleFileExtension;
import org.peakaboo.framework.swidget.dialogues.fileio.SwidgetFilePanels;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButton;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButtonSize;
import org.peakaboo.framework.swidget.widgets.layerpanel.HeaderLayer;
import org.peakaboo.framework.swidget.widgets.layerpanel.LayerPanel;
import org.peakaboo.framework.swidget.widgets.layerpanel.ToastLayer;
import org.peakaboo.framework.swidget.widgets.layout.HeaderTabBuilder;

public class ConcentrationView extends HeaderLayer {

	private Concentrations conc;
	private LayerPanel parent;
	
	public ConcentrationView(Concentrations conc, LayerPanel parent) {
		super(parent, true);
		this.conc = conc;
		this.parent = parent;
		
		getContentRoot().setPreferredSize(new Dimension(700, 350));
		
		
		FluentButton save = new FluentButton(StockIcon.DOCUMENT_SAVE_AS)
				.withButtonSize(FluentButtonSize.LARGE)
				.withTooltip("Save as CSV")
				.withAction(this::saveData);
		
		FluentButton copy = new FluentButton(StockIcon.EDIT_COPY)
				.withButtonSize(FluentButtonSize.LARGE)
				.withTooltip("Copy to clipboard")
				.withAction(this::copyData);
		
		ButtonLinker linker = new ButtonLinker(save, copy);
		
		
		HeaderTabBuilder tabs = new HeaderTabBuilder();
		tabs.addTab("Chart", new ConcentrationPlotPanel(conc));
		tabs.addTab("Table", new ConcentrationTablePanel(conc));
		
		setBody(tabs.getBody());
		getHeader().setLeft(linker);
		getHeader().setCentre(tabs.getTabStrip());
		
	}

	private String textData(String delimiter) {
		List<Element> es = conc.elementsByConcentration();
		StringBuilder sb = new StringBuilder();
		
		sb.append("Element" + delimiter + "Ratio to ");
		sb.append(conc.getProfile().getReference().getAnchor().getElement().toString());
		sb.append("\n");
		
		for (Element e : es) {
			sb.append(e.name());
			sb.append(delimiter);
			sb.append(conc.getRatioFormatted(e));
			
			
			ITransitionSeries ts = conc.getConcentrationSource(e);
			if (ts == null) {
				sb.append(" (No Source!)");
			} else if (!conc.isCalibrated(e)) {
				sb.append(" (Not Calibrated!)");
			} else {
				/*
				 * We don't include this in the text output to avoid clutter and make the text
				 * easier to use in other documents. Missing calibration still requires a
				 * notice, though.
				 */
			}
			
			
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private void copyData() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection sel = new StringSelection(textData(": "));
		clipboard.setContents(sel, null);
		
		ToastLayer toast = new ToastLayer(parent, "Data copied to clipboard");
		parent.pushLayer(toast);
		
	}
	
	private void saveData() {
		
		//TODO: starting folder
		SwidgetFilePanels.saveFile(parent, "Save Concentration Data", null, new SimpleFileExtension("Comma Separated Value file", "csv"), result -> {
			if (!result.isPresent()) {
				return;
			}
			
			try {
				File f = result.get();
				FileWriter writer = new FileWriter(f);
				writer.write(textData(", "));
				writer.close();
			} catch (IOException e) {
				PeakabooLog.get().log(Level.SEVERE, "Failed to save concentration data", e);
			}
			
		});
		
	}
	
	
	
}
