package org.example.game_library.Server.Connection;

import java.io.*;
import java.net.Socket;

public class ThreadCreator extends Thread {
    private Socket clientSocket;
    private DataInputStream input;

    public ThreadCreator(Socket socket) {
        this.clientSocket = socket;
        try {
            input = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error setting up input stream: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String line = "";

        try {
            while (!line.equals("End")) {
                line = input.readUTF();
                System.out.println("Client [" + clientSocket.getInetAddress() + "] says: " + line);
            }

            System.out.println("Client [" + clientSocket.getInetAddress() + "] disconnected.");
            clientSocket.close();
            input.close();

        } catch (IOException e) {
            System.out.println("Connection error with client: " + e.getMessage());
        }
    }
}