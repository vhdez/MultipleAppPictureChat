package edu.sla.picturechat;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerNetworking implements Runnable {
    private ArrayList<OutputStream> clientOutputStreams;
    private int numOfClients = 1;
    private SynchronizedQueue inputQueue;
    private SynchronizedQueue outputQueue;

    ServerNetworking(SynchronizedQueue inQueue, SynchronizedQueue outQueue, ArrayList<OutputStream> clientStreams) {
        clientOutputStreams = clientStreams;
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
                // keep track of each client's socket stream, so that server can broadcast to all of them
                clientOutputStreams.add(clientSocket.getOutputStream());

                // for every new client, run an IncomingDataReceiver on a new thread to receive data from it
                CommunicationHandler handler = new CommunicationHandler(clientSocket.getInputStream(), inputQueue, outputQueue);
                Thread handlerThread = new Thread(handler);
                handlerThread.setName("ServerNetworking CommunicationHandler thread " + numOfClients);
                numOfClients++;
                handlerThread.start();
                System.out.println("ServerNetworking: accepted client connection");
            }
        } catch(Exception ex){
            ex.printStackTrace();
            System.out.println("ServerNetworking: networking failed.  Exiting...");
        }

    }
}