package meegoo.os.multithreading.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import meegoo.os.multithreading.Item;
import meegoo.os.multithreading.Main;
import meegoo.os.multithreading.Producer;

public class Controller {

	private static Controller instance;
	@FXML
	public VBox vbox_consumers;
	@FXML
	public TableView<Item> table_buffer;
	@FXML
	public TextField textField_operations;
	@FXML
	public TextField textField_value;
	@FXML
	public TextField textField_period;
	@FXML
	public TableColumn<Item, String> tableColumn_value;
	@FXML
	public TableColumn<Item, String> tableColumn_operations;
	@FXML
	public CheckBox checkBox_repeat;

	public Controller() {
		instance = this;
	}

	public static Controller getInstance() {
		return instance;
	}

	public void init() {
		tableColumn_value.setCellValueFactory(new PropertyValueFactory<>("value"));
		tableColumn_operations.setCellValueFactory(param -> {
			StringBinding binding = Bindings.createStringBinding(() ->
					Long.toString(Math.round(param.getValue().getRemainingOperations())), param.getValue().remainingOperationsProperty());
			SimpleStringProperty property = new SimpleStringProperty();
			property.bind(binding);
			return property;
		});
		textField_period.setTextFormatter(GUI.createIntegerFormatter(18));
	}

	@FXML
	public void onButtonAdd(ActionEvent actionEvent) {
		Main.createConsumer();
	}

	@FXML
	public void onCheckboxRepeat(ActionEvent actionEvent) {
		CheckBox checkbox = (CheckBox) actionEvent.getTarget();
		if (checkbox.isSelected()) {
			if (!textField_operations.getText().equals("") && !Producer.getInstance().isAutoProducerRunning())
				Producer.getInstance().startAutoProducer(textField_value.getText(), textField_operations.getText(), Long.parseLong(textField_period.getText()));
		} else {
			Producer.getInstance().stopAutoProducer();
		}
	}

	@FXML
	public void onButtonProduce(ActionEvent actionEvent) {
		try {
			if (!textField_operations.getText().equals(""))
				Producer.getInstance().produce(textField_value.getText(), textField_operations.getText());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void onTextFieldPeriod() {
		if (!textField_operations.getText().equals("") && checkBox_repeat.isSelected())  {
			Producer.getInstance().stopAutoProducer();
			Producer.getInstance().startAutoProducer(textField_value.getText(), textField_operations.getText(), Long.parseLong(textField_period.getText()));
		}
	}

	@FXML
	public void onTextFieldOperations() {
		if (!textField_operations.getText().equals("") && checkBox_repeat.isSelected())  {
			Producer.getInstance().stopAutoProducer();
			Producer.getInstance().startAutoProducer(textField_value.getText(), textField_operations.getText(), Long.parseLong(textField_period.getText()));
		}

	}

	@FXML
	public void onTextFieldValue() {
		if (!textField_operations.getText().equals("") && checkBox_repeat.isSelected())  {
			Producer.getInstance().stopAutoProducer();
			Producer.getInstance().startAutoProducer(textField_value.getText(), textField_operations.getText(), Long.parseLong(textField_period.getText()));
		}
	}
}
