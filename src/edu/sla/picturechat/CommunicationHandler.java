package edu.sla.picturechat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

// CommunicationHandler is a handler thread that reads input Images from a Socket and:
//   1. Updates put the Image on the Chat app's input queue
//   2. If it is the Chat server, send the Image to all Clients.

public class CommunicationHandler implements Runnable {
    private InputStream in;
    private ArrayList clientOutputStreams;
    private BufferedReader reader;
    private boolean isServer;
    private SynchronizedQueue inputQueue;

    public CommunicationHandler(Socket sock, SynchronizedQueue inQueue, ArrayList streams) {
        inputQueue = inQueue;
        isServer = true;
        try {
            in = sock.getInputStream();
            InputStreamReader incomingDataReader = new InputStreamReader(in);
            reader = new BufferedReader(incomingDataReader);
            clientOutputStreams = streams;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("PictureChat CommunicationHandler: Server creation failed");
        }
    }

    public CommunicationHandler(Socket sock, SynchronizedQueue inQueue, BufferedReader r) {
        isServer = false;
        reader = r;
        inputQueue = inQueue;
        try {
            in = sock.getInputStream();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("PictureChat CommunicationHandler: Client creation failed");
        }

    }

    public void run() {
        try {
// HOW TO READ SIMPLE TEXT FROM SOCKET:
//            String message;
//            while ((message = reader.readLine()) != null) {
//                System.out.println("PictureChat CommunicationHandler: read " + message);
//            }

            byte[] sizeArray = new byte[4];
            int r;
            // Try reading from Socket until a new image's size+bytes appears
            while ((r = in.read(sizeArray)) > 0) {
                System.out.println("PictureChat CommunicationHandler: reading an image");
                // Read from socket the size of image
                int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
                // Make a byte array for the image based on the size of the image
                byte[] imageArray = new byte[size];
                // Read from socket the image's bytes and turn them into an image
                r = in.read(imageArray);
                if (r > 0) {
                    Image newImage = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(imageArray)), null);
                    System.out.println("PictureChat CommunicationHandler: read a " + size + " bytes image");
                    // Put the new image on to the GUI's inputQueue
                    while (!inputQueue.put(newImage)) {
                        Thread.currentThread().yield();
                    }
                } else {
                    System.out.println("PictureChat CommunicationHandler: read empty image?!?!");
                }

                if (isServer) {
                    // TODO: relay image to all clients
                    //tellAllClients(message);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("PictureChat CommunicationHandler: reading failed");
        }

    }

    public void tellAllClients(String message) {
        Iterator allClients = clientOutputStreams.iterator();
        while (allClients.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) allClients.next();
                writer.println(message);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("PictureChat CommunicationHandler: telling all clients failed");
            }
        }
    }
}
