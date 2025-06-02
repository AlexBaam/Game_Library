package org.example.game_library.database.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.game_library.database.model.User;
import org.example.game_library.utils.loggers.AppLogger;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;


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

            if (BCrypt.checkpw(password, user.getPassword())) {
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

    public User registration (String email, String username, String password) {

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, email, hashedPassword);

        em.getTransaction().begin(); // Start a new transaction
        try {
            em.persist(user);
            em.getTransaction().commit();
            logger.log(Level.INFO, "User {0} registered successfully.", username);
            return user;
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error during user registration for {0}: {1}", new Object[]{username, e.getMessage()});

            // Attempt to extract the specific message from the PostgreSQL trigger
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                SQLException sqlException = (SQLException) cause;
                logger.log(Level.SEVERE, "SQL Exception details: SQLState={0}, ErrorCode={1}, Message={2}",
                        new Object[]{sqlException.getSQLState(), sqlException.getErrorCode(), sqlException.getMessage()});
                // The message from your PL/pgSQL RAISE EXCEPTION will be in sqlException.getMessage()
                logger.log(Level.SEVERE, "Database trigger message: {0}", sqlException.getMessage());
            } else if (cause != null) {
                logger.log(Level.SEVERE, "Underlying cause of PersistenceException: {0}", cause.getMessage());
            }

            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "An unexpected error occurred during user registration for {0}: {1}", new Object[]{username, e.getMessage()});
            return null;
        }
    }
}
