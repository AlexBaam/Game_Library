package org.example.game_library.views;

import javafx.scene.control.Alert;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.utils.jpa.JPAUtils;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.exceptions.NullData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;

public class RegisterForm {
    private String username;
    private String password;
    private String email;
    private static final Logger logger = AppLogger.getLogger();
    List<String> parameters = new ArrayList<>();
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 5000;
    private Socket clientSocket;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private void onRegisterClick() {
        this.username = usernameField.getText();
        this.password = passwordField.getText();
        this.email = emailField.getText();

        logger.log(Level.INFO, "User pressed register");

        try {
            if (username.isBlank()) {
                throw new NullData("Username cannot be blank");
            }

            if (email.isBlank()) {
                throw new NullData("Email cannot be blank");
            }

            if (password.isBlank()) {
                throw new NullData("Password cannot be blank");
            }

            logger.log(Level.INFO, "Username entered: {0}", username);
            logger.log(Level.INFO, "Password entered: {0}", password);
            logger.log(Level.INFO, "Email entered: {0}", email);

            int dateVerificate = 1;
            if (username.length() < 4) {
                dateVerificate = 0;
                logger.log(Level.WARNING, "User must have at least 4 characters");
            } else if (password.length() < 4) {
                dateVerificate = 0;
                logger.log(Level.WARNING, "Password must have at least 4 characters");
            } else
            {
                int numberARond=0, numberDot = 0;
                for (int i = 0; i < email.length(); i++) {
                    if(email.charAt(i)=='.')
                        numberDot++;
                    else if(email.charAt(i)=='@')
                        numberARond++;
                }
                if(numberARond!=1 && numberDot<1)
                {
                    dateVerificate = 0;
                    logger.log(Level.WARNING, "User must have at least one dot and at one @");
                }
            }


            if(dateVerificate==1)
            {
                clientSocket = new Socket(SERVER_IP, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                parameters.add("register");
                parameters.add(email);
                parameters.add(username);
                parameters.add(password);

                //String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                logger.log(Level.INFO, "Preparing to send registration data to server.");
                out.writeObject(parameters);
                out.flush();

                Object response = in.readObject();

                if (response instanceof String respMsg) {
                    switch (respMsg) {
                        case "SUCCESS" -> {
                            logger.log(Level.INFO, "User registered successfully.");
                            //showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created successfully!");
                        }
                        case "FAILURE" -> {
                            logger.log(Level.WARNING, "Registration failed.");
                            //showAlert(Alert.AlertType.ERROR, "Registration Failed", "Could not register account. Try different credentials.");
                        }
                        default -> {
                            logger.log(Level.WARNING, "Unexpected response from server: {0}", respMsg);
                            //showAlert(Alert.AlertType.WARNING, "Server Error", "Unexpected response: " + respMsg);
                        }
                    }
                }

                // Close streams & socket
                out.close();
                in.close();
                clientSocket.close();

                /*try {
                    // luam fluxul de iesire de la socket
                    OutputStream os = clientSocket.getOutputStream();
                    // cream un obiect peste acel flux de iesire
                    oos = new ObjectOutputStream(os);

                    // serializarea
                    oos.writeObject(parameters);
                    oos.flush();

                    logger.log(Level.INFO, "Registration data sent to server: {0}", parameters);

                    // Aici poți adăuga logica pentru a aștepta un răspuns de la server
                    // de ex: un ObjectInputStream pentru a citi un mesaj de succes/eșec.
                    // Căci, serverul va trebui să-ți răspundă dacă înregistrarea a avut succes sau nu.
                    // De exemplu:
                    // ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    // String serverResponse = (String) ois.readObject();
                    // logger.log(Level.INFO, "Server response: " + serverResponse);
                    // if ("SUCCESS".equals(serverResponse)) {
                    //     showAlert(AlertType.INFORMATION, "Registration Successful", "Account created successfully!");
                    //     clearFields();
                    // } else {
                    //     showAlert(AlertType.ERROR, "Registration Failed", serverResponse); // Serverul ar trebui să trimită mesajul de eroare
                    // }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error sending data through socket: {0}", e.getMessage());
                    //showAlert(AlertType.ERROR, "Network Error", "Could not communicate with the server. Please check your connection.");
                }
                // Nu închide socket-ul aici dacă vrei să-l refolosești pentru alte comunicații.
                // Închiderea socket-ului ar trebui să se facă atunci când clientul se deconectează complet.
                // If you need to close the OOS if it's new for this operation, do it here:
                // finally {
                //     try {
                //         if (oos != null) oos.close();
                //     } catch (IOException e) {
                //         logger.log(Level.SEVERE, "Error closing ObjectOutputStream: " + e.getMessage());
                //     }
                // }*/

            }

        } catch (NullData e) {
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred during registration: {0}", e.getMessage());
        } finally {
            // ne asiguram ca inchidem entitatea
            if (JPAUtils.getEntityManager().isOpen()) {
                JPAUtils.getEntityManager().close();
            }
        }
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/mainMenuForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @FXML
    private void onExitClick() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}