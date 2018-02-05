package edu.sla.picturechat;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GUIUpdater implements Runnable {
    private SynchronizedQueue _inputQueue;
    private ImageView _imageview;

    GUIUpdater(SynchronizedQueue queue, ImageView imageview) {
        _inputQueue = queue;
        _imageview = imageview;
    }

    public void run() {
        while (true) {
            // Ask queue for a file to open
            Image next = _inputQueue.get();
            while (next == null) {
                Thread.currentThread().yield();
                next = _inputQueue.get();
            }
            // FINALLY I have an Image to do something with
            _imageview.setImage(next);
        }
    }

}
