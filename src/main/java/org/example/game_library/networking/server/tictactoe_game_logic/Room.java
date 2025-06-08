package org.example.game_library.networking.server.tictactoe_game_logic;

import lombok.Getter;
import lombok.Setter;
import org.example.game_library.networking.server.ThreadCreator;

import java.io.ObjectOutputStream;

@Getter
@Setter
public class Room {
    private final String roomId;
    private final ThreadCreator host;
    private ThreadCreator guest;
    private final TicTacToeGame game;

    public Room(final String roomId, final ThreadCreator host){
    this.roomId = roomId;
    this.host = host;
    this.game = new TicTacToeGame();
    this.game.setMode("network");
    }

    public boolean isFull(){
        return guest != null;
    }

    public boolean isPlayerTurn(ThreadCreator player) {
        return getPlayerSymbol(player).equals(game.getCurrentSymbol());
    }

    public String getPlayerSymbol(ThreadCreator player) {
        if (player == host) return "X";
        if (player == guest) return "O";
        return null;
    }

    public boolean containsPlayer(ThreadCreator player) {
        return player == host || player == guest;
    }

    public ObjectOutputStream getOtherPlayerOutput(ThreadCreator currentPlayer) {
        try {
            if (currentPlayer == host && guest != null) {
                return guest.getOutputStream();
            } else if (currentPlayer == guest && host != null) {
                return host.getOutputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
