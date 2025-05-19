package org.example.game_library.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private Scanner scanner;

    public ClientMain(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected to server: " + address + ":" + port);

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            scanner = new Scanner(System.in);

            String message = "";

            // Keep sending until "End" is typed
            while (!message.equals("End")) {
                System.out.print("You: ");
                message = scanner.nextLine();
                output.writeUTF(message);
            }

            System.out.println("Closing connection...");
            socket.close();
            input.close();
            output.close();

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ClientMain("127.0.0.1", 5000);
    }
}