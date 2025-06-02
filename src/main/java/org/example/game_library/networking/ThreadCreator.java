package org.example.game_library.networking;

import org.example.game_library.utils.loggers.AppLogger;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.logging.*;

public class ThreadCreator extends Thread {
    private final Socket clientSocket;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private static final Logger logger = AppLogger.getLogger();
    private final long threadId;

    public ThreadCreator(Socket socket) {
        this.clientSocket = socket;
        this.threadId = this.threadId();

        try {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
            logger.log(Level.INFO, "Streams created for thread {0}", threadId);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up input stream: {0}",  e.getMessage());
        }
    }

    @Override
    public void run() {
        boolean logged = false;
        String currentUsername = null;

        logger.log(Level.INFO, "Thread {0} started successfully!", threadId);

        try {
            Object obj;

            while ((obj = input.readObject()) != null) {
                if(!(obj instanceof List<?> list)){
                    output.writeObject("Invalid message format!");
                    continue;
                }

                //Safe cast
                @SuppressWarnings("unchecked")
                List<String> request = (List<String>) list;

                logger.log(Level.INFO, "Thread {0} received {1}", new Object[]{threadId, request});

                if(request.isEmpty()){
                    output.writeObject("Empty request!");
                    continue;
                }

                String command = request.get(0).toLowerCase();
                Command commandEnum = Command.fromString(command);
                if(commandEnum == null){
                    output.writeObject("Invalid command!");
                    continue;
                }

                if(!logged){
                    switch(commandEnum){
                        case LOGIN -> {
                            // HANDLE LOGIN
                            break;
                        }
                        case REGISTER -> {
                            // HANDLE REGISTER
                            break;
                        }
                        case EXIT -> {
                            output.writeObject("User pressed exit!");
                            return;
                        }
                        default -> {
                            output.writeObject("Command not yet implemented!");
                        }
                    }
                } else {
                    switch(commandEnum){
                        case LOGOUT -> {
                            //HANDLE LOGOUT
                            break;
                        }
                        case DELETE -> {
                            // HANDLE DELETE
                            break;
                        }
                        case EXIT -> {
                            output.writeObject("User pressed exit!");
                            return;
                        }
                        case TICTACTOE -> {
                            break;
                        }
                        case MINESWEEPER -> {
                            break;
                        }
                        default -> {
                            output.writeObject("Command not yet implemented!");
                        }
                    }
                }

            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Thread {0} connection error: {1}", new Object[]{threadId, e.getMessage()});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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