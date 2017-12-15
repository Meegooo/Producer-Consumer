package meegoo.os.multithreading;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import meegoo.os.multithreading.gui.ConsumerGUI;

import java.util.concurrent.*;

public class Consumer {
	private SimpleIntegerProperty frequency = new SimpleIntegerProperty(100);
	private SimpleObjectProperty<State> state = new SimpleObjectProperty<>(State.CREATED);
	private Item currentItem;
	private int id;
	private static int lastId = -1;
	private Semaphore lock = new Semaphore(1);
	private ConsumerGUI gui = null;
	private Thread ticker;

	private boolean run = true;

	private ScheduledExecutorService executorService;
	private static int lastThread = 0;

	private long previousTickAtMcs = -1;

	public Consumer() {
		id = ++lastId;
		ticker = new Thread(() -> {
			while(run)
				tick();
			System.out.println("Thread " + ticker.getName() + " stopped");
		});
		ticker.setName("Consumer:" + id);
		executorService = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setName(ticker.getName() + "-pool-1"+ "-thread-" + ++lastThread);
			return thread;
		});
	}

	public ConsumerGUI getGui() {
		if (gui == null) {
			gui = new ConsumerGUI(this);
		}
		return gui;
	}

	public void tick() {
		ScheduledFuture<?> future = executorService.schedule(() -> Platform.runLater(() -> state.setValue(State.PAUSED)), 2, TimeUnit.MILLISECONDS);
		try {
			lock.acquire();
		} catch (InterruptedException e) {
		}
		future.cancel(true);
		Platform.runLater(() -> state.setValue(State.RUNNING));

		if (currentItem == null) {
			gui.clearItem();
			Platform.runLater(() ->	state.setValue(State.WAITING));
			currentItem = Buffer.consume();
			if (currentItem == null) {
				lock.release();
				return;
			}
			Platform.runLater(() ->	state.setValue(State.RUNNING));
			gui.processItem(currentItem);
			System.out.println("Processing " + currentItem.getValue() + " at " + ticker.getName() + " (" + currentItem.getOperations() + "ms)");
			previousTickAtMcs = -1;
		}

		if (previousTickAtMcs < 0) {
			previousTickAtMcs = System.nanoTime()/1000;
		}
		long currTimeMcs = System.nanoTime()/1000;
		if (currentItem.tick(frequency.getValue()/1000000.0 * (currTimeMcs - previousTickAtMcs))) {
			currentItem = null;
		}
		previousTickAtMcs = currTimeMcs;
		lock.release();
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}

	public void pause() {
		new Thread(() -> {
			try {
				lock.acquire();
			} catch (InterruptedException e) {
			}
		}, "Pause lock at " + ticker.getName()).start();
	}

	public void start() {
		if (lock.availablePermits()==0)
			lock.release();
		else {
			ticker.start();
		}
	}

	public void stop() {
		Platform.runLater(() ->	state.setValue(State.INTERRUPTED));
		run = false;
		ticker.interrupt();
		executorService.shutdownNow();
	}

	public int getId() {
		return id;
	}

	public Thread getTicker() {
		return ticker;
	}

	public void setTicker(Thread ticker) {
		this.ticker = ticker;
	}

	public int getFrequency() {
		return frequency.get();
	}

	public SimpleIntegerProperty frequencyProperty() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency.set(frequency);
	}

	public State getState() {
		return state.get();
	}

	public SimpleObjectProperty<State> stateProperty() {
		return state;
	}

	public void setState(State state) {
		this.state.set(state);
	}

	public enum State {
		CREATED, RUNNING, PAUSED, INTERRUPTED, WAITING
	}
}
