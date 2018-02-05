package edu.sla.picturechat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

// CommunicationHandler is a handler thread that reads input Images from a Socket and:
//   1. Puts the Image on the Chat app's input queue
//   2. If it is the Chat server, puts the Image on the Chat app's output queue.

public class CommunicationHandler implements Runnable {
    private InputStream in;
    private boolean isServer;
    private SynchronizedQueue inputQueue;
    private SynchronizedQueue outputQueue;

    // The way Server creates a CommunicationHandler
    CommunicationHandler(InputStream inStream, SynchronizedQueue inQueue, SynchronizedQueue outQueue) {
        isServer = true;
        in = inStream;
        inputQueue = inQueue;
        outputQueue = outQueue;
    }

    // The way Client creates a CommunicationHandler
    CommunicationHandler(InputStream inStream, SynchronizedQueue inQueue) {
        isServer = false;
        in = inStream;
        inputQueue = inQueue;
    }

    public void run() {
        try {
// HOW TO READ SIMPLE TEXT FROM SOCKET:
//            String message;
//            while ((message = reader.readLine()) != null) {
//                System.out.println("PictureChat CommunicationHandler: read " + message);
//                while (!inputQueue.put(message)) {
//                     Thread.currentThread().yield();
//                }
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
                    if (isServer) {
                        // Put the new image on to the server's outputqueue (so that it gets broadcast to all clients)
                        while (!outputQueue.put(newImage)) {
                            Thread.currentThread().yield();
                        }
                    }
                } else {
                    System.out.println("PictureChat CommunicationHandler: read empty image?!?!");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("PictureChat CommunicationHandler: reading failed");
        }
    }

}
