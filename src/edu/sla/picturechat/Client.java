package edu.sla.picturechat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.stage.Stage;

public final class Client extends Application {
    private static SynchronizedQueue inputQueue;
    private static SynchronizedQueue outputQueue;
    private static InputStream socketIn;
    private static OutputStream socketOut;

    @Override
    public void start(final Stage stage) {
        ArrayList<OutputStream> listWithOneOutputStream = new ArrayList<>();
        listWithOneOutputStream.add(socketOut);
        GUI gui = new GUI(false, inputQueue, outputQueue, listWithOneOutputStream);
        gui.run(stage);
    }

    public static void main(String[] args) {
        try {
            // Set up client-side networking
            Socket sock = new Socket("127.0.0.1", 5000);
            // writer will send images to Server as soon as they are gotten from SynchronizedQueue
            socketIn = sock.getInputStream();
            socketOut = sock.getOutputStream();

            // Create the queues that will be used for communication
            // inputQueue communicates images from Server to GUIUpdater
            inputQueue = new SynchronizedQueue();
            // outputQueue communicates images from GUI to Server
            outputQueue = new SynchronizedQueue();

            // Run an CommunicationHandler on a new thread to receive data from the server
            CommunicationHandler handler = new CommunicationHandler(socketIn, inputQueue);
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            System.out.println("PictureChat client: created input communication handler");

            // Start the GUI thread
            System.out.println("PictureChat client: networking is ready... starting the GUI");
            Application.launch(args);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("PictureChat client: no PictureChat server available.  Exiting....");
        }

    }
}
