package edu.sla.picturechat;

import javafx.application.Application;
import javafx.stage.Stage;

public class Server extends Application {
    private static SynchronizedQueue inputQueue;
    private static SynchronizedQueue outputQueue;

    @Override
    public void start(final Stage stage) {
        GUI gui = new GUI("Server", inputQueue, outputQueue);
        gui.run(stage);
    }

    public static void main(String[] args) {
        // inputQueue communicates images from Server to GUIUpdater
        inputQueue = new SynchronizedQueue();
        // outputQueue communicates images from GUI to Server
        outputQueue = new SynchronizedQueue();

        // Create a thread that creates a ServerSocket and handles incoming client Sockets
        ServerNetworking serverNetworking = new ServerNetworking(inputQueue, outputQueue);
        Thread serverNetworkingThread = new Thread(serverNetworking);
        serverNetworkingThread.setName("serverNetworkingThread");
        serverNetworkingThread.start();

        // Start the Server's GUI thread
        Application.launch(args);
    }
}