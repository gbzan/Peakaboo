package org.peakaboo.datasink.model.components.interaction;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CallbackInteraction implements Interaction {

	//TODO: Peakaboo 6 - replace with IntConsumer, BooleanSupplier, etc
	Consumer<Integer> callbackScansWritten = i -> {};
	Supplier<Boolean> callbackAbortRequested = () -> false;


	public void setCallbackScansWritten(Consumer<Integer> callbackScansWritten) {
		this.callbackScansWritten = callbackScansWritten;
	}

	public void setCallbackAbortRequested(Supplier<Boolean> callbackAbortRequested) {
		this.callbackAbortRequested = callbackAbortRequested;
	}

	@Override
	public void notifyScanWritten(int count) {
		this.callbackScansWritten.accept(count);
	}

	@Override
	public boolean isAbortedRequested() {
		return this.callbackAbortRequested.get();
	}

}
