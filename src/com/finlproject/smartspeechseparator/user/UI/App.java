package com.finlproject.smartspeechseparator.user.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("UI.fxml"));
		UIController eventHandler = new UIController();

		Parent root = loader.load();
		primaryStage.setTitle("SMART SOUND SEPARATION");
		Scene scene = new Scene(root, 800,600);
		scene.getStylesheets().add("com/finlproject/smartspeechseparator/user/UI/UI.css");
		primaryStage.setScene(scene);
		
		eventHandler.init(primaryStage);
		
		primaryStage.show();

	}

}
