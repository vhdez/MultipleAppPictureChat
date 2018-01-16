package edu.sla.picturechat;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GUIUpdater implements Runnable {
    private SynchronizedQueue inputQueue;
    private ImageView GUIimageview;

    GUIUpdater(SynchronizedQueue queue, ImageView imageview) {
        inputQueue = queue;
        GUIimageview = imageview;
    }

    public void run() {
        while (true) {
            // Ask queue for a file to open
            Image next = inputQueue.get();
            while (next == null) {
                Thread.currentThread().yield();
                next = inputQueue.get();
            }
            // FINALLY I have an Image to do something with
            GUIimageview.setImage(next);
        }
    }

}
