package org.example.game_library.database.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.game_library.database.model.User;
import org.example.game_library.utils.loggers.AppLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepository {
    private static final Logger logger = AppLogger.getLogger();

    private final EntityManager em;

    public UserRepository(EntityManager em) {
        this.em = em;
    }

    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User authenticate(String username, String password) {
        User user = findByUsername(username);

        if(user != null){
            logger.log(Level.INFO, "There is a user with the username {0}", username);

            if(password.equals(user.getPassword())){
                logger.log(Level.INFO, "Password entered: {0}", password);
                logger.log(Level.INFO, "User logged in successfully!");

                return user;
            } else {
                logger.log(Level.INFO, "Password entered: {0}", password);
                logger.log(Level.WARNING, "Password {0} doesn't match with user's {1} password!", new Object[]{password, username});
                return null;
            }
        } else {
            logger.log(Level.WARNING, "No user with username {0} found in the database!", username);
            return null;
        }
    }
}
