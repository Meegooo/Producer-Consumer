package meegoo.os.multithreading;

import java.util.Random;
import java.util.concurrent.*;

public class Producer {

	private static Producer instance;
	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private Random rand = new Random();

	private Thread ticker;
	private int lastVal = 0;

	private boolean autoProducerRunning = false;
	private boolean run = true;

	private Semaphore tickSemaphore = new Semaphore(1);
	private Semaphore produceSemaphore = new Semaphore(0);

	private String operations;
	private String value;

	public Producer() {
		instance = this;
		ticker = new Thread(() -> {
			while (run)
				tick();
		});
		ticker.setName("Producer:0");
		ticker.start();
	}

	public static Producer getInstance() {
		return instance;
	}

	public void tick() {
		try {
			produceSemaphore.acquire();
		} catch (InterruptedException e) {
			//System.out.println(ticker.getName() + " interrupted");
			return;
		}
		long ops;
		String val;
		try {
			if (operations.startsWith("rand(") && operations.endsWith(")")) {
				String randomParams = operations.substring(5, operations.length() - 1);
				String[] split = randomParams.split(",");
				if (split.length == 2) {
					long lower = Long.parseLong(split[0].trim());
					long upper = Long.parseLong(split[1].trim());
					ops = Math.max(Math.abs(rand.nextLong()) % (upper - lower + 1) + lower, 0);
				} else if (randomParams.equals("")) {
					ops = Math.max(Math.abs(rand.nextLong()) % (2000 - 1000 + 1) + 1000, 0);
				} else return;
			} else {
				ops = Math.max(Long.parseLong(operations), 0);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		if (value.equals("seq()")) {
			val = "Value " + ++lastVal;
		} else val = value;

		Item item = new Item(ops, val);
		Buffer.produce(item);
		operations = null;
		value = null;
		tickSemaphore.release();
	}

	public void startAutoProducer(String value, String operations, long period) {
		scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
			if (tickSemaphore.availablePermits()>0)
				tickSemaphore.acquireUninterruptibly();
			else
				return;
			this.value = value;
			this.operations = operations;
			produceSemaphore.release();
		}, 0, period, TimeUnit.MILLISECONDS);
		autoProducerRunning = true;
	}

	public void stopAutoProducer() {
		if (scheduledFuture != null) try {
			scheduledFuture.cancel(false);
		} finally {
			autoProducerRunning = false;
		}
	}

	public void stop() {
		if (scheduledFuture != null)
			scheduledFuture.cancel(true);
		run = false;
		ticker.interrupt();
		executor.shutdownNow();
		scheduledExecutor.shutdownNow();
	}

	public void produce(String value, String operations) {
		executor.schedule(() -> {
			if (tickSemaphore.availablePermits()>0) {
				try {
					tickSemaphore.acquire();
				} catch (InterruptedException e) {
				}
			}
			else
				return;
			this.value = value;
			this.operations = operations;
			produceSemaphore.release();
		}, 0, TimeUnit.SECONDS);
	}

	public boolean isAutoProducerRunning() {
		return autoProducerRunning;
	}

	public Thread getTicker() {
		return ticker;
	}
}
