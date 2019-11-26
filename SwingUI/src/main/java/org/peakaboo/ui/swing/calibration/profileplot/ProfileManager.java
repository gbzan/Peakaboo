package org.peakaboo.ui.swing.calibration.profileplot;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.peakaboo.calibration.CalibrationProfile;
import org.peakaboo.common.ConfigurationLoadException;
import org.peakaboo.common.PeakabooLog;
import org.peakaboo.controller.plotter.PlotController;
import org.peakaboo.controller.plotter.PlotUpdateType;
import org.peakaboo.curvefit.peak.transition.TransitionShell;
import org.peakaboo.framework.eventful.EventfulTypeListener;
import org.peakaboo.framework.stratus.controls.ButtonLinker;
import org.peakaboo.framework.swidget.Swidget;
import org.peakaboo.framework.swidget.dialogues.fileio.SimpleFileExtension;
import org.peakaboo.framework.swidget.dialogues.fileio.SwidgetFilePanels;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.framework.swidget.widgets.Spacing;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButton;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButtonSize;
import org.peakaboo.framework.swidget.widgets.layerpanel.HeaderLayer;
import org.peakaboo.framework.swidget.widgets.layerpanel.LayerDialog;
import org.peakaboo.framework.swidget.widgets.layerpanel.ToastLayer;
import org.peakaboo.framework.swidget.widgets.layerpanel.LayerDialog.MessageType;
import org.peakaboo.framework.swidget.widgets.layout.HeaderTabBuilder;
import org.peakaboo.ui.swing.calibration.picker.ReferencePicker;
import org.peakaboo.ui.swing.plotting.PlotPanel;

public class ProfileManager extends HeaderLayer {

	private PlotController controller;
	private PlotPanel parent;
	
	protected JPanel body;
	protected List<ProfilePlot> profileplots = new ArrayList<>();

	
	private JPanel namePanel;
	private JTextField nameField;
	private boolean nameShown = false;

	
	private FluentButton create, open, clear, save;
	
	public ProfileManager(PlotPanel parent, PlotController controller) {
		super(parent, true);
		this.controller = controller;
		this.parent = parent;
		
		
		EventfulTypeListener<PlotUpdateType> listener = t -> {
			CalibrationProfile profile = controller.calibration().getCalibrationProfile();
			File file = controller.calibration().getCalibrationProfileFile();
			if (profile == null) {
				return;
			}
			for (ProfilePlot plot : profileplots) {
				plot.setCalibrationProfile(profile, file);
			}
			
			updateNamePanel();
			
			save.setEnabled(controller.calibration().hasCalibrationReference());
			
		};
		controller.addListener(listener);
		
		getHeader().setShowClose(true);
		getHeader().setOnClose(() -> {
			controller.removeListener(listener);
			remove();
		});	

		
		
		create = new FluentButton(StockIcon.DOCUMENT_NEW).withTooltip("Create Z-Calibration Profile").withButtonSize(FluentButtonSize.LARGE).withAction(() -> {
			promptCreateProfile(this::actionLoadCalibrationReference);
		});
		
		open = new FluentButton(StockIcon.DOCUMENT_OPEN).withTooltip("Load Z-Calibration Profile").withButtonSize(FluentButtonSize.LARGE).withAction(() -> {
			actionLoadCalibrationProfile();
		});
		
		clear = new FluentButton(StockIcon.EDIT_CLEAR).withTooltip("Clear Z-Calibration Profile").withButtonSize(FluentButtonSize.LARGE).withAction(() -> {
			controller.calibration().setCalibrationProfile(new CalibrationProfile(), null);
			
		});
		
		save = new FluentButton(StockIcon.DOCUMENT_SAVE_AS).withTooltip("Save New Z-Calibration Profile").withButtonSize(FluentButtonSize.LARGE).withAction(() -> {
			actionSaveCalibrationProfile();
		});
		save.setEnabled(controller.calibration().hasCalibrationReference());
		
		ButtonLinker buttons = new ButtonLinker(create, open, save, clear);
		
		makeNamePanel();
		

		
		init(controller.calibration().getCalibrationProfile(), controller.calibration().getCalibrationProfileFile(), buttons, null);
		
	}
	


	private void promptCreateProfile(Runnable onAccept) {
		String text = Swidget.lineWrap(parent, "Z-Calibration Profiles describe and correct the variable elemental sensitivity of an experimental setup. They are a necessary part of determining sample concentrations.\n\nThis will replace any existing work with the settings and fittings needed to create a new Z-Calibration Profile.\n\nYou should have a reference data set open before proceeding.");
		LayerDialog dialog = new LayerDialog("Create Z-Calibration Profile?", text, MessageType.QUESTION);
		FluentButton ok = new FluentButton("OK").withAction(onAccept).withStateDefault();
		FluentButton cancel = new FluentButton("Cancel");
		dialog.addLeft(cancel);
		dialog.addRight(ok);
		dialog.showIn(parent);
	}
	

	protected void init(CalibrationProfile profile, File source, JComponent left, JComponent right) {
		
		
		getContentRoot().setPreferredSize(new Dimension(700, 350));
		
		body = new JPanel(new BorderLayout());
		setBody(body);
		
		//plot views
		HeaderTabBuilder tabBuilder = new HeaderTabBuilder();
		
		ProfilePlot kplot = new ProfilePlot(profile, source, TransitionShell.K);
		ProfilePlot lplot = new ProfilePlot(profile, source, TransitionShell.L);
		ProfilePlot mplot = new ProfilePlot(profile, source, TransitionShell.M);
		profileplots.add(kplot);
		profileplots.add(lplot);
		profileplots.add(mplot);

		
		
		tabBuilder.addTab("K Series", kplot);
		tabBuilder.addTab("L Series", lplot);
		tabBuilder.addTab("M Series", mplot);
		
		body.add(tabBuilder.getBody(), BorderLayout.CENTER);
		getHeader().setComponents(left, tabBuilder.getTabStrip(), right);
		updateNamePanel();
		
	}

	
	
	public void actionSaveCalibrationProfile() {
		
		//generate profile
		CalibrationProfile profile = controller.calibration().generateCalibrationProfile();
		if (profile == null) {
			LayerDialog layer = new LayerDialog("Failed to Generate Profile", "Peakaboo could not generate a calibration profile", MessageType.ERROR);
			layer.showIn(parent);
			return;
		}

		//apply name field to new profile
		String name = nameField.getText();		
		if (name == null || name.trim().length() == 0) {
			ToastLayer toast = new ToastLayer(parent, "Z-Calibration Profile must have a name");
			parent.pushLayer(toast);
			return;
		}
		profile.setName(name);
		
		
		//save the profile
		saveCalibrationProfile(profile);
		
	
	}
	
	private void makeNamePanel() {
		namePanel = new JPanel(new BorderLayout(Spacing.small, Spacing.small));
		namePanel.setBorder(Spacing.bMedium());
		nameField = new JTextField();
		namePanel.add(nameField, BorderLayout.CENTER);
		namePanel.add(new JLabel("Name"), BorderLayout.WEST);

	}
	
	private void showNamePanel() {
		if (nameShown) { return; }
		nameShown = true;
		body.add(namePanel, BorderLayout.NORTH);
		if (controller.calibration().hasCalibrationReference()) {
			nameField.setText(controller.calibration().getCalibrationReference().getName());
		}
	}
	
	private void hideNamePanel() {
		if (!nameShown) { return; }
		nameShown = false;
		body.remove(namePanel);
	}

	private void updateNamePanel() {
		//show the name field if a ref is set, because it means we're creating
		if (controller.calibration().hasCalibrationReference()) {
			showNamePanel();				
		} else {
			hideNamePanel();
		}
	}

	private void saveCalibrationProfile(CalibrationProfile profile) {
		String yaml = CalibrationProfile.save(profile);
		
		SimpleFileExtension ext = new SimpleFileExtension("Peakaboo Calibration Profile", "pbcp");
		SwidgetFilePanels.saveFile(parent, "Save Calibration Profile", controller.io().getLastFolder(), ext, file -> {
			if (!file.isPresent()) { return; }
			File f = file.get();
			FileWriter writer;
			try {
				writer = new FileWriter(f);
				writer.write(yaml);
				writer.close();

				actionLoadCalibrationProfileFromFile(f);
				
			} catch (IOException e) {
				PeakabooLog.get().log(Level.SEVERE, "Failed to save calibration file", e);
			}

		});
	}
	
	
	public void actionLoadCalibrationProfile() {
		SwidgetFilePanels.openFile(parent, "Select Calibration Profile", null, new SimpleFileExtension("Peakaboo Calibration Profile", "pbcp"), result -> {
			if (!result.isPresent()) {
				return;
			}
			actionLoadCalibrationProfileFromFile(result.get());
		});
	}
	
	public void actionLoadCalibrationProfileFromFile(File file) {
		try {

			CalibrationProfile profile = CalibrationProfile.load(new String(Files.readAllBytes(file.toPath())));
			controller.calibration().clearCalibrationReference();
			controller.calibration().setCalibrationProfile(profile, file);
			
		} catch (IOException | ConfigurationLoadException e1) {
			PeakabooLog.get().log(Level.SEVERE, "Could not load calibration profile", e1);
		}
	}
		
	
	public void actionLoadCalibrationReference() {
		
		ReferencePicker picker = new ReferencePicker(parent, ref -> {
			parent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			controller.calibration().loadCalibrationReference(ref);
			
			/*
			 * force the results to be recalculated eagerly. This is the part of this
			 * process that introduces the most UI delay, but because the value is cached
			 * and recalculated lazily after an invalidation, it doesn't get done unless we
			 * do it explicitly
			 */
			controller.fitting().getFittingSelectionResults();
			
			parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		});
				
		parent.pushLayer(picker);
		
	}
	
	
}
