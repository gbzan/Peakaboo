package org.peakaboo.ui.swing.plotting.filters;



import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.peakaboo.controller.plotter.filtering.FilteringController;
import org.peakaboo.filter.model.Filter;
import org.peakaboo.framework.autodialog.view.editors.AutoDialogButtons;
import org.peakaboo.framework.autodialog.view.swing.SwingAutoDialog;
import org.peakaboo.framework.swidget.icons.IconSize;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.framework.swidget.widgets.Spacing;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButton;
import org.peakaboo.framework.swidget.widgets.fluent.button.FluentButtonLayout;



class EditButtonEditor extends DefaultCellEditor {

	private FluentButton				button;
	private JPanel 					container;
	
	private Window					owner;

	private Filter					filter;
	private FilteringController		controller;

	private String					label;
	private boolean					isPushed;
	
	private Map<Filter, SwingAutoDialog> settingsDialogs;


	public EditButtonEditor(FilteringController controller, Window owner) {
		super(new JCheckBox());

		this.controller = controller;
		this.owner = owner;
		this.settingsDialogs = new HashMap<>();

		button = new FluentButton(StockIcon.MISC_PREFERENCES, IconSize.TOOLBAR_SMALL)
				.withTooltip("Edit Filter")
				.withLayout(FluentButtonLayout.IMAGE)
				.withBordered(false)
				.withAction(this::fireEditingStopped);
		container = new JPanel();
		container.setBorder(Spacing.bNone());
	}


	@Override
	public Component getTableCellEditorComponent(JTable table, Object filterObject, boolean isSelected, int row, int column) {

		filter = (Filter) filterObject;
		int numParameters = (filter == null) ? 0 : filter.getParameters().size();
		
		label = (filter == null) ? "" : filter.toString();
		isPushed = true;
		
		
		container.setBackground(table.getSelectionBackground());
		container.setOpaque(true);

		
		container.remove(button);
		if (numParameters == 0)
		{		
			return container;
		}
		container.add(button);
		return container;
	}


	@Override
	public Object getCellEditorValue() {
		
		if (isPushed) {
			
			FilterDialog dialog;

			if (!settingsDialogs.containsKey(filter)) {
				
				dialog = new FilterDialog(controller, filter, AutoDialogButtons.CLOSE, owner);	
				dialog.setHelpMessage(filter.getFilterDescription());
				dialog.setHelpTitle(filter.getFilterName());
				settingsDialogs.put(filter, dialog);
				dialog.initialize();
			} else {
				settingsDialogs.get(filter).setVisible(true);
			}

		}
		isPushed = false;
		return label;
	}


	@Override
	public boolean stopCellEditing() {
		isPushed = false;
		return super.stopCellEditing();
	}


	@Override
	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}