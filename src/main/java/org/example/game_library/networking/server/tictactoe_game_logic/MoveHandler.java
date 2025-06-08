package org.example.game_library.networking.server.tictactoe_game_logic;

import org.example.game_library.networking.server.ThreadCreator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public interface MoveHandler {
    void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) throws IOException;
}
