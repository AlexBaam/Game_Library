package org.example.game_library.networking;

import org.example.game_library.utils.loggers.AppLogger;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class ThreadCreator extends Thread {
    private final Socket clientSocket;

    private DataInputStream input;
    private DataOutputStream output;

    private static final Logger logger = AppLogger.getLogger();
    private final long threadId;

    public ThreadCreator(Socket socket) {
        this.clientSocket = socket;
        this.threadId = this.threadId();

        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            logger.log(Level.INFO, "Streams created for thread {0}", threadId);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up input stream: {0}",  e.getMessage());
        }
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Thread {0} started successfully!", threadId);

        try {
            String line;

            while ((line = input.readUTF()) != null) {
                logger.log(Level.INFO, "Thread {0} received {1}", new Object[]{threadId, line});

                if(line.trim().equalsIgnoreCase("end")){
                    logger.log(Level.INFO, "Thread {0} requested to disconnect!", threadId);
                    break;
                }

                //TODO: Trebuie sa adaugam raspunsuri ptr fiecare request de la client
                output.writeUTF("Server a primit: " + line);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Thread {0} connection error: {1}", new Object[]{threadId, e.getMessage()});
        } finally {
            try{
                if(output != null){
                    output.close();
                }
                if(input != null){
                    input.close();
                }
                clientSocket.close();
                logger.log(Level.INFO, "Thread {0} connection closed!", threadId);
            } catch(IOException e){
                logger.log(Level.SEVERE, "Thread {0} error closing streams: {1}", new Object[]{threadId, e.getMessage()});
            }
        }
    }
}