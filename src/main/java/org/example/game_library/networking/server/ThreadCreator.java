    package org.example.game_library.networking.server;

    import lombok.Getter;
    import lombok.Setter;
    import org.example.game_library.database.model.User;
    import org.example.game_library.database.repository.UserRepository;
    import org.example.game_library.networking.enums.Command;
    import org.example.game_library.networking.enums.CommandMinesweeper;
    import org.example.game_library.networking.enums.CommandTicTacToe;
    import org.example.game_library.networking.server.minesweeper_game_logic.MinesweeperGameState;
    import org.example.game_library.networking.server.minesweeper_game_logic.MinesweeperRequests;
    import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
    import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeRequests;
    import org.example.game_library.utils.loggers.AppLogger;

    import java.io.*;
    import java.net.*;
    import java.util.List;
    import java.util.logging.*;

    import jakarta.persistence.PersistenceException;

    import static org.example.game_library.networking.enums.CommandMinesweeper.*;
    import static org.example.game_library.networking.enums.CommandTicTacToe.FORFEIT;
    import static org.example.game_library.networking.enums.CommandTicTacToe.SHOVEL;

    public class ThreadCreator extends Thread {
        private final Socket clientSocket;

        private ObjectInputStream input;
        private ObjectOutputStream output;

        private static final Logger logger = AppLogger.getLogger();
        private final long threadId;

        private boolean logged = false;
        @Getter
        private User currentUser;

        @Getter
        @Setter
        private TicTacToeGame ticTacToeGame;

        private final UserRepository userRepository;

        private MinesweeperGameState minesweeperGameState;


        public ThreadCreator(Socket socket) {
            this.clientSocket = socket;
            this.threadId = this.threadId();

            this.userRepository = new UserRepository();

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
                        logger.log(Level.WARNING, "Thread {0} received EOF – closing connection.", threadId);
                        logger.log(Level.SEVERE, "Message for thread {0}: {1}", new Object[]{threadId, e.getMessage()});
                        break;
                    }

                    if(!(obj instanceof List<?> list)){
                        output.writeObject("Invalid message format!");
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    List<String> request = (List<String>) list;

                    logger.log(Level.INFO, "Thread {0} received {1}", new Object[]{threadId, request});

                    if(request.isEmpty()){
                        output.writeObject("Empty request!");
                        continue;
                    }

                    String command = request.getFirst().toLowerCase();
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
                default -> output.writeObject("Command " + request.getFirst() + " not yet implemented!");
            }
        }

        private void handleAuthenticatedCommand(Command commandEnum, List<String> request) throws IOException {
            switch(commandEnum){
                case LOGOUT -> handleLogout(request);
                case DELETE -> handleDelete();
                case EXIT -> handleExit(request);
                case TICTACTOE -> handleTicTacToe(request);
                case MINESWEEPER -> handleMinesweeper(request);
                default -> output.writeObject("Command " + request.getFirst() + " not yet implemented!");
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

            User user = userRepository.registration(email, username, password);

            if (user != null) {
                logged = true;
                currentUser = user;
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

            try {
                User user = userRepository.authenticate(username, password);
                logged = true;
                currentUser = user;
                output.writeObject("SUCCESS");
            } catch (org.example.game_library.utils.exceptions.LoginException e) {

                logger.log(Level.INFO, "Login failed for user {0}: {1}", new Object[]{username, e.getMessage()});
                output.writeObject(e.getMessage());
            }
        }

        private void handleDelete() throws IOException {
            if (!logged || currentUser == null) {
                output.writeObject("You must login first!");
                logger.log(Level.WARNING, "Attempted delete by unauthenticated thread {0}.", threadId);
                return;
            }

            try {
                boolean success = userRepository.deleteUserByUsername(currentUser.getUsername());
                if (success) {
                    output.writeObject("SUCCESS");
                    logger.log(Level.INFO, "User {0} successfully deleted account.", currentUser.getUsername());
                    logged = false;
                    currentUser = null;
                } else {
                    output.writeObject("Error deleting account! User could not be found or deleted!");
                    logger.log(Level.WARNING, "Failed to delete user {0}.", currentUser.getUsername());
                }
            } catch (PersistenceException e) {
                logger.log(Level.SEVERE, "Database error during user deletion for {0}: {1}", new Object[]{currentUser.getUsername(), e.getMessage()});
                output.writeObject("Error deleting account: " + e.getMessage());
            }
        }

        private void handleLogout(List<String> request) throws IOException {

            if (!logged || currentUser == null) {
                output.writeObject("You must login first!.");
                logger.log(Level.WARNING, "Attempted logout by unauthenticated thread {0}.", threadId);
                return;
            }

            try {
                boolean success = userRepository.updateUserLoggedInStatus(currentUser.getUsername(), false);
                if (success) {
                    output.writeObject("SUCCESS");
                    logger.log(Level.INFO, "User {0} successfully logged out.", currentUser.getUsername());
                    logged = false;
                    currentUser = null;
                } else {
                    output.writeObject("Eroare la deconectare. Va rugam sa incercati din nou.");
                    logger.log(Level.WARNING, "Failed to update logged_in status to FALSE for user {0}.", currentUser.getUsername());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Database error during logout for user {0}: {1}    ", new Object[]{currentUser.getUsername(), e.getMessage()});
                output.writeObject("Eroare de baza de date la deconectare: " + e.getMessage());
            }
        }

        private void handleExit(List<String> request) throws IOException {
            handleLogout(request);
            output.writeObject("User pressed exit!");
            throw new EOFException();
        }

        private void handleMinesweeper(List<String> request) throws IOException {
            if (request.size() == 1) {
                output.writeObject("SUCCESS");
                logger.log(Level.INFO, "Thread {0} successfully entered Minesweeper menu.", threadId);
            } else if(request.size() >= 2) {
                String subCommandStr = request.get(1);
                CommandMinesweeper subCommand = CommandMinesweeper.fromString(subCommandStr);

                if (subCommand == null) {
                    output.writeObject("Eroare: Subcomandă Minesweeper invalidă: " + subCommandStr);
                    return;
                }

                switch (subCommand) {
                    case NEWGAME -> MinesweeperRequests.handleNewGame(request, this, output, input);
                    //case LOADGAME -> MinesweeperRequests.handleLoadGame(request, this, output, input);
                    //case SAVEGAME -> MinesweeperRequests.handleSaveGame(request, this, output, input);
                    case FLAG -> MinesweeperRequests.handleFlag(request, this, output, input);
                    case EXIT -> handleExit(request);
                    case SCORE -> MinesweeperRequests.handleScore(request, this, output, input, userRepository);
                    case FORFEIT -> MinesweeperRequests.handleForfeit(request, this, output, input);
                    case SHOVEL -> MinesweeperRequests.handleShovel(request, this, output, input);
                    default -> output.writeObject("Comanda Minesweeper " + subCommandStr + " nu este implementată.");
                }
            } else {
                output.writeObject("Eșec la procesarea comenzii TicTacToe.");
            }
        }

        private void handleTicTacToe(List<String> request) throws IOException {
            if (request.size() == 1) {
                output.writeObject("SUCCESS");
            } else if (request.size() >= 2) {
                String commandTicTacToe = request.get(1);
                CommandTicTacToe cTTT = CommandTicTacToe.fromString(commandTicTacToe);

                if (cTTT == null) {
                    output.writeObject("TicTacToe command is null! Command: " + commandTicTacToe);
                    return;
                }

                switch (cTTT) {
                    case NEWGAME -> TicTacToeRequests.handleNewGame(request, this, output, input);
                    case LOADGAME -> TicTacToeRequests.handleLoadGame(request, this, output, input);
                    case SAVEGAME -> TicTacToeRequests.handleSaveGame(request, this, output, input);
                    case FORFEIT -> TicTacToeRequests.handleForfeit(request, this, output, input);
                    case MOVE -> TicTacToeRequests.handleMove(request, this, output, input);
                    case EXIT -> handleExit(request);
                    case SCORE -> {
                        if (request.size() >= 3) {
                            String scoreType = request.get(2);
                            TicTacToeRequests.handleScore(request, this, output, input, scoreType, userRepository);
                        } else {
                            output.writeObject("Error: Score type could not be retrieved SCORE.");
                            logger.log(Level.WARNING, "Score request is missing the 'SCORE' parameter!");
                        }
                    }
                    default -> output.writeObject("Command " + request.get(1) + " not implemented yet!");
                }
            } else {
                output.writeObject("Failed to run the command TicTacToe.");
            }
        }

        public ObjectOutputStream getOutputStream() {
            return output;
        }

        public MinesweeperGameState getMinesweeperGameState() {
            return this.minesweeperGameState;
        }

        public void setMinesweeperGameState(MinesweeperGameState gameState) {
            this.minesweeperGameState = gameState;
        }

    }

