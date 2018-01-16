package edu.sla.picturechat;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStream;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GUI {
    // this queue is used for all communication between the GUI and the GUIUpdater
    private SynchronizedQueue inputQueue;
    private SynchronizedQueue outputQueue;
    private ImageView RECEIVER_imageView;
    private String name;

    // networking needed by GUI to respond to GUI usage
    private BufferedReader reader;
    private PrintWriter writer;
    private OutputStream out;

    GUI(String s, SynchronizedQueue in, SynchronizedQueue out) {
        name = s;
        inputQueue = in;
        outputQueue = out;
    }

    public void setClientNetworking(BufferedReader r, PrintWriter w, OutputStream stream) {
        reader = r;
        writer = w;
        out = stream;
    }

    public void run(final Stage stage) {
        RECEIVER_imageView = new ImageView();

        // Create and start the GUI updater thread
        GUIUpdater updater = new GUIUpdater(inputQueue, RECEIVER_imageView);
        Thread updaterThread = new Thread(updater);
        updaterThread.start();

        // Create and start the sender thread
        ImageSender sender = new ImageSender(outputQueue, writer, out);
        Thread senderThread = new Thread(sender);
        senderThread.start();

        stage.setTitle(name + " Picture Chat");

        final FileChooser fileChooser = new FileChooser();
        final ImageView SENDER_imageView = new ImageView();
        SENDER_imageView.setFitWidth(400);
        SENDER_imageView.setFitHeight(300);

        final Button SENDER_openButton = new Button("Open a Picture...");
        // This lambda is called whenever user presses the "Open a Picture..." button
        SENDER_openButton.setOnAction((event) -> {
            // Show a FileChooser
            File file = fileChooser.showOpenDialog(stage);

            // If user chose a file via FileChooser
            if (file != null) {
                Image newImage = new Image(file.toURI().toString());
                SENDER_imageView.setImage(newImage);
            }
        });

        final Button SENDER_sendButton = new Button("Send this picture");
        SENDER_sendButton.setOnAction((event) -> {
            Image imageToSend = SENDER_imageView.getImage();

            if (imageToSend != null) {
                while (!outputQueue.put(imageToSend)) {
                    Thread.currentThread().yield();
                }
            }
        });

        RECEIVER_imageView.setFitWidth(400);
        RECEIVER_imageView.setFitHeight(300);

        VBox vertical = new VBox(12);
        HBox horizontal = new HBox(12);

        horizontal.getChildren().addAll(SENDER_openButton, SENDER_sendButton);
        vertical.getChildren().addAll(SENDER_imageView, horizontal, RECEIVER_imageView);

        stage.setScene(new Scene(vertical));
        stage.show();
    }

}
