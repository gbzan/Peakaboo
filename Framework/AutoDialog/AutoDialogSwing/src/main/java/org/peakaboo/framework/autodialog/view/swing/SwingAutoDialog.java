package org.peakaboo.framework.autodialog.view.swing;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.peakaboo.framework.autodialog.view.swing.layouts.SwingLayoutFactory;

import org.peakaboo.framework.autodialog.model.Group;
import org.peakaboo.framework.autodialog.view.editors.AutoDialogButtons;
import org.peakaboo.framework.swidget.Swidget;
import org.peakaboo.framework.swidget.icons.IconSize;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.framework.swidget.live.LiveDialog;
import org.peakaboo.framework.swidget.widgets.Spacing;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButton;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButtonLayout;
import org.peakaboo.framework.swidget.widgets.layout.ButtonBox;


public class SwingAutoDialog extends LiveDialog
{

	//private IADController controller;
	private Container parent;

	private String helpTitle;
	private String helpMessage;
	
	private Group group;
	private AutoDialogButtons buttons;
	private boolean selected_ok = false;
	
	
	private ImageButton info;

	
	public SwingAutoDialog(Window owner, Group group) {
		this(owner, group, AutoDialogButtons.OK_CANCEL);
	}

	public SwingAutoDialog(Window owner, Group group, AutoDialogButtons buttons) {
		super(owner);
		this.buttons = buttons;
		this.group = group;
	}

	public SwingAutoDialog(Group group) {
		this(group, AutoDialogButtons.OK_CANCEL);
	}

	public SwingAutoDialog(Group group, AutoDialogButtons buttons) {
		this.buttons = buttons;
		this.group = group;
	}


	public void initialize(){
		
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());

			
		JScrollPane scroller = new JScrollPane(SwingLayoutFactory.forGroup(group).getComponent());
		scroller.setBorder(Spacing.bNone());
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		scroller.setBorder(Spacing.bMedium());
		c.add(scroller, BorderLayout.CENTER);


		c.add(createButtonBox(), BorderLayout.SOUTH);
		
				
		pack();
		setLocationRelativeTo(parent);
		setTitle(group.getName());
		setVisible(true);
	}
	
	
	
	
	
	
	private JPanel createButtonBox()
	{
		
		ButtonBox bbox = new ButtonBox();
		
		if (buttons == AutoDialogButtons.OK_CANCEL) {
			
			ImageButton ok = new ImageButton("OK", StockIcon.CHOOSE_OK);
			ok.addActionListener(e -> {
				this.selected_ok = true;
				System.err.println("Set OK");
				SwingAutoDialog.this.setVisible(false);
			});
			
			ImageButton cancel = new ImageButton("Cancel", StockIcon.CHOOSE_CANCEL);
			cancel.addActionListener(e -> {
				SwingAutoDialog.this.setVisible(false);
			});
			
			bbox.addRight(0, cancel);
			bbox.addRight(0, ok);
			
		} else if (buttons == AutoDialogButtons.CLOSE) {
			
			ImageButton close = new ImageButton("Close", StockIcon.WINDOW_CLOSE);
			close.addActionListener(e -> {
				SwingAutoDialog.this.setVisible(false);
			});
			
			bbox.addRight(0, close);
			
		}
		
		
		info = new ImageButton(StockIcon.BADGE_HELP).withTooltip("More Information").withLayout(ImageButtonLayout.IMAGE).withBordered(true);
		info.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{	
				JOptionPane.showMessageDialog(
						SwingAutoDialog.this, 
						Swidget.lineWrapHTML(SwingAutoDialog.this, helpMessage),
						helpTitle, 
						JOptionPane.INFORMATION_MESSAGE, 
						StockIcon.BADGE_HELP.toImageIcon(IconSize.ICON)
					);

			}
		});
		info.setFocusable(false);
		if (helpMessage == null) info.setVisible(false);
		
		
		bbox.addLeft(0, info);

		
				
		return bbox;
		
	}

	
	
	
	
	public String getHelpTitle() {
		return helpTitle;
	}

	public void setHelpTitle(String helpTitle) {
		this.helpTitle = helpTitle;
		
	}

	public String getHelpMessage() {
		return helpMessage;
	}

	public void setHelpMessage(String helpMessage) {
		this.helpMessage = helpMessage;
		if (info != null) info.setVisible((helpMessage != null && helpMessage.length() > 0));
	}
	
	
	
	public Container getParent() {
		return parent;
	}

	public void setParent(Container parent) {
		this.parent = parent;
	}

	public boolean okSelected() {
		return selected_ok;
	}

	public Group getGroup() {
		return group;
	}
	
	
	

}
