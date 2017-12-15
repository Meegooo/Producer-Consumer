package meegoo.os.multithreading.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import meegoo.os.multithreading.Item;
import meegoo.os.multithreading.Main;
import meegoo.os.multithreading.Producer;

import java.text.DecimalFormat;
import java.text.ParsePosition;


public class GUI extends Application {

	private static GUI instance;
	private Stage stage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;
		stage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		primaryStage.setTitle("Producer/Consumer");
		primaryStage.setScene(new Scene(root, 1000, 600));
		primaryStage.show();
		stage.setOnCloseRequest(event -> {
			event.consume();
			Main.getConsumers().forEach((integer, consumer) -> consumer.stop());
			Producer.getInstance().stop();
			Platform.exit();
		});
		Controller.getInstance().init();
	}

	public void bufferPoll() {
		Controller.getInstance().table_buffer.getItems().remove(0);
	}

	public void bufferAdd(Item item) {
		Controller.getInstance().table_buffer.getItems().add(item);
	}

	public static TextFormatter<Object> createIntegerFormatter(int maxLength) {
		DecimalFormat format = new DecimalFormat("#,###");
		return new TextFormatter<>(c -> {
			if (c.getControlNewText().isEmpty()) return c;
			if (c.getControlNewText().length() > maxLength) return null;
			ParsePosition parsePosition = new ParsePosition(0);
			Object object = format.parse(c.getControlNewText(), parsePosition);
			if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) return null;
			else return c;
		});
	}

	public static GUI getInstance() {
		return instance;
	}

	public Stage getStage() {
		return stage;
	}
}
