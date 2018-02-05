package edu.sla.picturechat;

import java.io.OutputStream;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.ArrayList;

public class ImageSender implements Runnable {
    private SynchronizedQueue outputQueue;
    private ArrayList<OutputStream> clientOutputStreams;

    // This is how a server creates an ImageSender (can have many OutputStreams);
    ImageSender(SynchronizedQueue queue, ArrayList<OutputStream> streams) {
        outputQueue = queue;
        clientOutputStreams = streams;
    }

    public void run() {
        while (true) {
            // Ask queue for an image to send
            Image imageToSend = outputQueue.get();
            while (imageToSend == null) {
                Thread.currentThread().yield();
                imageToSend = outputQueue.get();
            }
            // Finally have an image, let's try to send if over the socket
            try {
                // HOW TO WRITE SIMPLE TEXT TO SOCKET:
                //writer.println("SAYING HI");
                //writer.flush();

                // Turn the image into an array of bytes
                ByteArrayOutputStream imageAsBytes = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(imageToSend, null), "jpg", imageAsBytes);
                // Calculate number of bytes (size) needed for image (and put tha size into a byte array
                byte[] size = ByteBuffer.allocate(4).putInt(imageAsBytes.size()).array();
                // Send image to all output streams (clients will have 1: the server)
                //                                  (server will have many: all the clients)
                Iterator allClients = clientOutputStreams.iterator();
                while (allClients.hasNext()) {
                    try {
                        OutputStream nextOut = (OutputStream) allClients.next();
                        // Send the byte size of the image over the socket
                        nextOut.write(size);
                        // Send the image's bytes over the socket
                        nextOut.write(imageAsBytes.toByteArray());
                        // Tell the socket's listener to start reading
                        nextOut.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("PictureChat CommunicationHandler: telling all clients failed");
                    }
                }
                System.out.println("PictureChat ImageSender: sent image");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("PictureChat ImageSender: unable to send image");
            }
        }
    }

}
