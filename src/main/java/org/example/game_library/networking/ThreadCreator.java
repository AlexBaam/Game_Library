package org.example.game_library.networking;

import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.utils.jpa.JPAUtils;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.logging.*;


import jakarta.persistence.PersistenceException;
import javax.security.auth.login.LoginException;

public class ThreadCreator extends Thread {
    private final Socket clientSocket;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private static final Logger logger = AppLogger.getLogger();
    private final long threadId;

    private boolean logged = false;
    private int currentUserId = -1; // Ptr utilizator mai tarziu

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
        logger.log(Level.INFO, "Thread {0} started successfully!", threadId);

        try {

            while (true) {

                Object obj;
                try{
                    obj = input.readObject();
                } catch (EOFException e) {
                    logger.log(Level.INFO, "Thread {0} received EOF – closing connection.", threadId);
                    break;
                }

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

                if (commandEnum == null) {
                    output.writeObject("Invalid command!");
                    continue;
                }

                if(!logged){
                    handleUnauthenticatedCommand(commandEnum, request);
                } else {
                    handleAuthenticatedCommand(commandEnum, request);
                }

            }
        } catch (IOException | ClassNotFoundException e){
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

    private void handleUnauthenticatedCommand(Command commandEnum, List<String> request) throws IOException {
        switch(commandEnum){
            case LOGIN -> handleLogin(request);
            case REGISTER -> handleRegister(request);
            case EXIT -> handleExit(request);
            default -> output.writeObject("Command " + request.get(0) + " not yet implemented!");
        }
    }

    private void handleAuthenticatedCommand(Command commandEnum, List<String> request) throws IOException {
        switch(commandEnum){
            case LOGOUT -> handleLogout(request);
            case DELETE -> handleDelete(request);
            case EXIT -> handleExit(request);
            case TICTACTOE -> handleTicTacToe(request);
            case MINESWEEPER -> handleMinesweeper(request);
            default -> output.writeObject("Command " + request.get(0) + " not yet implemented!");
        }
    }

    private void handleRegister(List<String> request) throws IOException {
        if (request.size() < 4) {
            output.writeObject("Not enough arguments for REGISTER");
            return;
        }

        String email = request.get(1);
        String username = request.get(2);
        String password = request.get(3);

        UserRepository repo = new UserRepository(JPAUtils.getEntityManager());
        User user = repo.registration(email, username, password);

        if (user != null) {
            output.writeObject("SUCCESS");
        } else {
            output.writeObject("FAILURE");
        }
    }

    private void handleLogin(List<String> request) throws IOException {
        if(request.size() < 3){
            output.writeObject("Not enough arguments for LOGIN");
            return;
        }

        String username = request.get(1);
        String password = request.get(2);

        UserRepository userRepo = new UserRepository(JPAUtils.getEntityManager());
        try {
            User user = userRepo.authenticate(username, password);
            logged = true; // Marchează sesiunea ca autentificată
            currentUserId = user.getUser_id(); // Stochează ID-ul utilizatorului
            output.writeObject("SUCCESS");
        } catch (org.example.game_library.utils.exceptions.LoginException e) {
            // Prindem excepția specifică de login (inclusiv "deja conectat")
            logger.log(Level.INFO, "Login failed for user {0}: {1}", new Object[]{username, e.getMessage()});
            output.writeObject(e.getMessage()); // Trimite mesajul de eroare clientului
        } catch (PersistenceException e) {
            // Prindem alte erori de persistență care nu sunt direct legate de login logică (e.g., probleme cu DB)
            logger.log(Level.SEVERE, "Database error during login for user {0}: {1}", new Object[]{username, e.getMessage()});
            output.writeObject("Eroare de bază de date la autentificare.");
        }
    }

    private void handleDelete(List<String> request) throws IOException {
        //TODO
    }

    private void handleLogout(List<String> request) throws IOException {
        //TODO
    }

    private void handleExit(List<String> request) throws IOException {
        output.writeObject("User pressed exit!");
        throw new EOFException();
    }

    private void handleMinesweeper(List<String> request) throws IOException {
        //TODO
    }

    private void handleTicTacToe(List<String> request) throws IOException {
        //TODO
    }
}
