package meegoo.os.multithreading;

import javafx.application.Application;
import javafx.application.Platform;
import meegoo.os.multithreading.gui.Controller;
import meegoo.os.multithreading.gui.GUI;

import java.util.HashMap;
import java.util.Map;

public class Main{
	private static Map<Integer, Consumer> consumers = new HashMap<>();
    public static void main(String[] args) {
	    new Producer();
	    Application.launch(GUI.class, args);
    }

    public static void createConsumer() {
    	Consumer consumer = new Consumer();
	    consumers.put(consumer.getId(), consumer);
	    Platform.runLater(() ->
			    Controller.getInstance().vbox_consumers.getChildren().add(consumer.getGui()));
    }

	public static Map<Integer, Consumer> getConsumers() {
		return consumers;
	}
}
