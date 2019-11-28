package org.peakaboo.framework.autodialog.view.javafx.editors;


import org.peakaboo.framework.autodialog.model.Parameter;
import org.peakaboo.framework.autodialog.model.SelectionParameter;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

public class ListEditor<T> extends AbstractEditor<T>{

	private ChoiceBox<T> node = new ChoiceBox<>();
	
	public ListEditor() {}
	
	@Override
	public void init(Parameter<T> parameter) {
		this.parameter = (SelectionParameter<T>) parameter;
		SelectionParameter<T> selectionParameter = (SelectionParameter<T>) parameter;
		node.getItems().addAll(selectionParameter.getPossibleValues());
		
		node.getSelectionModel().selectedItemProperty().addListener(change -> {
			getEditorValueHook().updateListeners(getEditorValue());
			if (!parameter.setValue(getEditorValue())) {
				validateFailed();
			}
		});
	}
	

	@Override
	public Node getComponent() {
		return node;
	}

	@Override
	public void setEditorValue(T value) {
		node.getSelectionModel().select(value);
	}

	@Override
	public T getEditorValue() {
		return node.getSelectionModel().getSelectedItem();
	}

	@Override
	public boolean expandVertical() {
		return false;
	}

	@Override
	public boolean expandHorizontal() {
		return true;
	}

	@Override
	public LabelStyle getLabelStyle() {
		return LabelStyle.LABEL_ON_SIDE;
	}

	public void validateFailed() {
		setFromParameter();
	}

}
