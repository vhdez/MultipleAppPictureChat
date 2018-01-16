package edu.sla.picturechat;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerNetworking implements Runnable {
    private ArrayList clientOutputStreams;
    private int numOfClients = 1;
    private SynchronizedQueue inputQueue;
    private SynchronizedQueue outputQueue;

    public ServerNetworking(SynchronizedQueue inQueue, SynchronizedQueue outQueue) {
        clientOutputStreams = new ArrayList();
        inputQueue = inQueue;
        outputQueue = outQueue;
    }

    public void run() {
        // set up server-side networking
        try {
            ServerSocket serverSock = new ServerSocket(5000);
            System.out.println("PictureChat Server: networking is ready");
            while (true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);

                // for every new client, run an IncomingDataReceiver on a new thread to receive data from it
                CommunicationHandler handler = new CommunicationHandler(clientSocket, inputQueue, clientOutputStreams);
                Thread handlerThread = new Thread(handler);
                handlerThread.setName("Server communication thread " + numOfClients);
                handlerThread.start();
                System.out.println("PictureChat server: accepted client connection");
            }
        } catch(Exception ex){
            ex.printStackTrace();
            System.out.println("PictureChat server: networking failed.  Exiting...");
        }

    }
}