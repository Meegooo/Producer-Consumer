package meegoo.os.multithreading.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import meegoo.os.multithreading.Consumer;
import meegoo.os.multithreading.Item;
import org.controlsfx.control.PopOver;

import static meegoo.os.multithreading.Consumer.State.*;


public class ConsumerGUI extends BorderPane {

	private Label label_id = new Label();
	private Label label_speed = new Label();
	private Label label_state = new Label();
	private Button button_pause = new Button();
	private Button button_start = new Button();
	private Button button_remove = new Button();
	private Button button_speed = new Button();

	private Consumer consumer;

	private TableView<Item> table = new TableView<>();
	public ConsumerGUI(Consumer consumer) {
		this.consumer = consumer;
		HBox status = new HBox();
		status.setSpacing(5);
		Consumer.State consumerState = consumer.getState();
		StringBinding stateBinding = Bindings.createStringBinding(() -> "State: " + consumer.stateProperty().getValue(), consumer.stateProperty());
		label_state.textProperty().bind(stateBinding);
		StringBinding speedBinding = Bindings.createStringBinding(() -> "Speed: " + consumer.frequencyProperty().getValue() + " ops", consumer.frequencyProperty());
		label_speed.textProperty().bind(speedBinding);
		label_id.setText("ID: " + consumer.getId());
		status.getChildren().addAll(label_id, new Separator(Orientation.VERTICAL), label_state, new Separator(Orientation.VERTICAL), label_speed);
		BorderPane.setMargin(status, new Insets(0, 4, 4, 4));

		VBox controls = new VBox();

		button_pause.setText("Pause");
		button_pause.setPrefWidth(USE_COMPUTED_SIZE);
		button_pause.setMaxWidth(Double.POSITIVE_INFINITY);
		VBox.setMargin(button_pause, new Insets(0, 4, 4, 4));
		button_start.setText("Start");
		button_start.setPrefWidth(USE_COMPUTED_SIZE);
		button_start.setMaxWidth(Double.POSITIVE_INFINITY);
		VBox.setMargin(button_start, new Insets(0, 4, 4 ,4));
		button_remove.setText("Remove");
		button_remove.setPrefWidth(USE_COMPUTED_SIZE);
		button_remove.setMaxWidth(Double.POSITIVE_INFINITY);
		VBox.setMargin(button_remove, new Insets(0, 4, 4, 4));
		button_speed.setText("Speed");
		button_speed.setPrefWidth(USE_COMPUTED_SIZE);
		button_speed.setMaxWidth(Double.POSITIVE_INFINITY);
		VBox.setMargin(button_speed, new Insets(0, 4, 0, 4));

		button_start.setPadding(new Insets(0, 2, 0, 2));
		button_pause.setPadding(new Insets(0, 2, 0, 2));
		button_remove.setPadding(new Insets(0, 2, 0, 2));
		button_speed.setPadding(new Insets(0, 2, 0, 2));
		button_start.setPrefHeight(20);
		button_pause.setPrefHeight(20);
		button_remove.setPrefHeight(20);
		button_speed.setPrefHeight(20);
		controls.getChildren().addAll(button_start, button_pause, button_remove, button_speed);
		if (consumerState == PAUSED || consumerState == CREATED || consumerState == WAITING) {
			button_start.setVisible(true);
			button_start.setManaged(true);
			button_pause.setVisible(false);
			button_pause.setManaged(false);
		}
		 else {
			button_start.setVisible(false);
			button_start.setManaged(false);
			button_pause.setVisible(true);
			button_pause.setManaged(true);
		}

		table.setPlaceholder(new Label("Waiting"));
		TableColumn<Item, String> tableColumn_value = new TableColumn<>();
		TableColumn<Item, String> tableColumn_remaining = new TableColumn<>();
		tableColumn_remaining.setPrefWidth(200);

		tableColumn_value.setCellValueFactory(new PropertyValueFactory<>("value"));
		tableColumn_remaining.setCellValueFactory(param -> {
			StringBinding binding = Bindings.createStringBinding(() ->
					Long.toString(Math.round(param.getValue().getRemainingOperations())), param.getValue().remainingOperationsProperty());
			SimpleStringProperty property = new SimpleStringProperty();
			property.bind(binding);
			return property;
		});

		tableColumn_value.setText("Value");
		tableColumn_remaining.setText("Remaining Operations");
		table.getColumns().addAll(tableColumn_value, tableColumn_remaining);
		BorderPane.setMargin(table, new Insets(0, 4, 4, 4));
		table.setPrefHeight(50);

		initHandlers();

		VBox tableAndStatus = new VBox(table, status);

		setRight(controls);
		setBottom(new Separator(Orientation.HORIZONTAL));
		setCenter(tableAndStatus);

	}

	public void initHandlers() {
		button_pause.setOnAction(event -> {
			consumer.pause();
			button_start.setVisible(true);
			button_start.setManaged(true);
			button_pause.setVisible(false);
			button_pause.setManaged(false);
		});

		button_start.setOnAction(event -> {
			consumer.start();
			button_start.setVisible(false);
			button_start.setManaged(false);
			button_pause.setVisible(true);
			button_pause.setManaged(true);
		});

		button_remove.setOnAction(event -> {
			consumer.stop();
			Controller.getInstance().vbox_consumers.getChildren().remove(this);
		});

		button_speed.setOnAction(event -> {
			PopOver popOver = new PopOver();
			popOver.setDetachable(false);
			popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
			HBox content = new HBox();
			TextField textField_speed = new TextField();
			textField_speed.setTextFormatter(GUI.createIntegerFormatter(8));
			textField_speed.setText(Integer.toString(consumer.getFrequency()));
			Label label_ops = new Label("ops");
			content.getChildren().addAll(textField_speed, label_ops);
			content.setSpacing(7);
			content.setPadding(new Insets(5));
			textField_speed.setPrefWidth(60);
			popOver.setContentNode(content);
			content.setAlignment(Pos.CENTER);
			popOver.setArrowIndent(0);
			popOver.show(button_speed);

			textField_speed.setOnAction(event1 -> popOver.hide());

			popOver.setOnHiding(event1 -> consumer.setFrequency(Integer.parseInt(textField_speed.getText())));
		});
	}

	public void processItem(Item item) {
		table.getItems().add(item);
	}

	public void clearItem() {
		table.getItems().clear();
	}

}
