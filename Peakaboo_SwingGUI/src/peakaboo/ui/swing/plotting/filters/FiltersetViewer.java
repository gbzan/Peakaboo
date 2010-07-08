package peakaboo.ui.swing.plotting.filters;

import java.awt.CardLayout;
import java.awt.Dimension;

import peakaboo.controller.plotter.FilterController;
import swidget.containers.SwidgetContainer;
import swidget.widgets.ClearPanel;

public class FiltersetViewer extends ClearPanel {

	
	private FilterController controller;
	private CardLayout layout;
	
	private String EDIT = "EDIT";
	private String SELECT = "SELECT";
	
	@Override
	public String getName()
	{
		return "Filters";
	}
	
	public FiltersetViewer(FilterController _controller, SwidgetContainer owner){
		
		super();
		
		setPreferredSize(new Dimension(200, getPreferredSize().height));
		
		this.controller = _controller;
		
		layout = new CardLayout();
		this.setLayout(layout);
		
		this.add(new FilterEditViewer(controller, owner, this), EDIT);
		this.add(new FilterSelectionViewer(controller, this), SELECT);
		
	}
	
	public void showEditPane(){
		layout.show(this, EDIT);
	}
	public void showSelectPane(){
		layout.show(this, SELECT);
	}
	
}