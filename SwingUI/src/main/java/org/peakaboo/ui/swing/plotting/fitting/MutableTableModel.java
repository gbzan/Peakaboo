package org.peakaboo.ui.swing.plotting.fitting;

import javax.swing.table.TableModel;


public interface MutableTableModel extends TableModel {

	void fireChangeEvent();
	
}
