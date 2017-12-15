package meegoo.os.multithreading;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.concurrent.atomic.AtomicReference;

public class Item {
	private final long operations;
	private final AtomicDouble remaining = new AtomicDouble();
	private final AtomicReference<Double> remainingReference = new AtomicReference<>();

	private final SimpleDoubleProperty remainingOperations = new SimpleDoubleProperty();
	private final String value;

	public Item(long operations, String value) {
		this.operations = operations;
		remaining.set(operations);
		remainingOperations.setValue(operations);
		this.value = value;
	}

	public boolean tick(double value) {
		remaining.addAndGet(-value);
		if (remainingReference.getAndSet(remaining.get()) == null) {
			Platform.runLater(() -> remainingOperations.set(remainingReference.getAndSet(null)));
		}
		return remaining.get() <= 0;
	}
	public long getOperations() {
		return operations;
	}

	public double getRemainingOperations() {
		return remainingOperations.get();
	}

	public SimpleDoubleProperty remainingOperationsProperty() {
		return remainingOperations;
	}

	public String getValue() {
		return value;
	}

}
