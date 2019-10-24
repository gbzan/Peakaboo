package org.peakaboo.ui.swing.plotting.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.peakaboo.datasink.plugin.DataSinkPlugin;
import org.peakaboo.datasink.plugin.DataSinkPluginManager;
import org.peakaboo.framework.bolt.plugin.core.BoltPluginPrototype;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.ui.swing.plotting.PlotPanel;

public class PlotMenuExport extends JPopupMenu {


	private JMenuItem exportFittingsMenuItem;
	private JMenuItem exportFilteredDataMenuItem;
	private JMenuItem exportFilteredSpectrumMenuItem;
	private JMenuItem exportArchive;
	private JMenu exportSinks;
	private JMenuItem snapshotMenuItem;
	
	public PlotMenuExport(PlotPanel plot) {
				
		exportSinks = new JMenu("Raw Data");
		
		for (BoltPluginPrototype<? extends DataSinkPlugin> plugin : DataSinkPluginManager.SYSTEM.getPlugins()) {
			exportSinks.add(PlotMenuUtils.createMenuItem(plot,
					plugin.getName(), null, null,
					e -> plot.actionExportData(plugin.create()),
					null, null
			));
		}
		
		this.add(exportSinks);
		

		
		snapshotMenuItem = PlotMenuUtils.createMenuItem(plot,
				"Plot as Image\u2026", StockIcon.DEVICE_CAMERA.toMenuIcon(), "Saves the current plot as an image",
				e -> plot.actionSavePicture(),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK), KeyEvent.VK_P
		);
		this.add(snapshotMenuItem);

		exportFilteredSpectrumMenuItem = PlotMenuUtils.createMenuItem(plot,
				"Filtered Spectrum as CSV", StockIcon.DOCUMENT_EXPORT.toMenuIcon(), "Saves the filtered spectrum to a CSV file",
				e -> plot.actionSaveFilteredSpectrum(),
				null, null
		);
		this.add(exportFilteredSpectrumMenuItem);
		
		exportFilteredDataMenuItem = PlotMenuUtils.createMenuItem(plot,
				"Filtered Data Set as CSV", StockIcon.DOCUMENT_EXPORT.toMenuIcon(), "Saves the filtered dataset to a CSV file",
				e -> plot.actionSaveFilteredDataSet(),
				null, null
		);
		this.add(exportFilteredDataMenuItem);
		
		exportFittingsMenuItem = PlotMenuUtils.createMenuItem(plot,
				"Fittings as Text", null, "Saves the current fitting data to a text file",
				e -> plot.actionSaveFittingInformation(),
				null, null
		);
		this.add(exportFittingsMenuItem);

		exportArchive = PlotMenuUtils.createMenuItem(plot,
				"All-In-One Zip Archive", null, "Saves the plot, session file, z-calibration and fittings",
				e -> plot.actionExportArchive(),
				null, null
		);
		this.add(exportArchive);
		

		
	}
	
	
	public void setWidgetState(boolean hasData) {
		snapshotMenuItem.setEnabled(hasData);
		exportFittingsMenuItem.setEnabled(hasData);
		exportFilteredDataMenuItem.setEnabled(hasData);
		exportArchive.setEnabled(hasData);
		exportSinks.setEnabled(hasData);
	}
	
}
