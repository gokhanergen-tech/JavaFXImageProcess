package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.opencv.core.Core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    double xOffset =0;
    double yOffset=0;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View/sample.fxml"));



        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 0,0));


       primaryStage.resizableProperty().setValue(false);

         primaryStage.setAlwaysOnTop(true);

        primaryStage.show();

    }


    public static void main(String[] args) {



        launch(args);
    }
}
