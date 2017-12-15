package meegoo.os.multithreading;

import javafx.application.Platform;
import meegoo.os.multithreading.gui.GUI;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
	private static final Queue<Item> buffer = new LinkedList<>();

	private static ReentrantLock bufferLock = new ReentrantLock();
	private static Semaphore itemsSemaphore = new Semaphore(0, true);
	private static Semaphore remainingSemaphore = new Semaphore(10, true);
	public static void produce(Item item) {
		try {
			remainingSemaphore.acquire();
		} catch (InterruptedException e) {
			return;
		}

		try {
			bufferLock.lock();
			buffer.add(item);
			Platform.runLater(() -> GUI.getInstance().bufferAdd(item));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bufferLock.unlock();
		}
		itemsSemaphore.release();
	}

	public static Item consume() {
		Item item = null;
		try {
			itemsSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}

		try {
			bufferLock.lock();
			item = buffer.poll();
			Platform.runLater(() -> GUI.getInstance().bufferPoll());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bufferLock.unlock();
		}

		remainingSemaphore.release();
		if (item == null) throw new NullPointerException();
		return item;
	}


}
