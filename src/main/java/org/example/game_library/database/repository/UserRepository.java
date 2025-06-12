package org.example.game_library.database.repository;

import jakarta.persistence.*;
import org.example.game_library.database.model.User;
import org.example.game_library.networking.server.tictactoe_game_logic.ScoreEntry;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntryM;
import org.example.game_library.utils.exceptions.LoginException;
import org.example.game_library.utils.jpa.JPAUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User findByUsername(String username) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User authenticate(String username, String password) throws LoginException {
        User user = findByUsername(username);
        if (user == null)
            throw new LoginException("Utilizator inexistent.");

        if (!BCrypt.checkpw(password, user.getPassword()))
            throw new LoginException("Parola incorecta.");

        EntityManager em = JPAUtils.getEntityManager();
        try {
            em.getTransaction().begin();
            user.setLoggedIn(true);
            em.merge(user);
            em.getTransaction().commit();
            return user;
        } catch (PersistenceException e) {
            throw new LoginException("Eroare la login: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean updateUserLoggedInStatus(String username, boolean status) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = findByUsername(username);
            if (user == null) return false;
            user.setLoggedIn(status);
            em.merge(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            em.close();
        }
    }

    public User registration(String email, String username, String password) {
        EntityManager em = JPAUtils.getEntityManager();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            User newUser = new User(username, email, hashedPassword);
            newUser.setLoggedIn(true);

            em.getTransaction().begin();
            em.persist(newUser);
            em.getTransaction().commit();
            return newUser;
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public boolean deleteUserByUsername(String username) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = findByUsername(username);
            if (user == null) return false;
            em.remove(em.merge(user));
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }
}
