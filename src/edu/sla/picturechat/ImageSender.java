package edu.sla.picturechat;

import java.io.PrintWriter;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageSender implements Runnable {
    private SynchronizedQueue outputQueue;
    private PrintWriter writer;
    private OutputStream out;

    ImageSender(SynchronizedQueue queue, PrintWriter w, OutputStream stream) {
        outputQueue = queue;
        writer = w;
        out = stream;
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
                // Send the byte size of the image over the socket
                out.write(size);
                // Send the image's bytes over the socket
                out.write(imageAsBytes.toByteArray());
                // Tell the socket's listener to start reading
                out.flush();
                System.out.println("PictureChat ImageSender: sent image");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("PictureChat ImageSender: unable to send image");
            }
        }
    }

}
