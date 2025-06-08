package org.example.game_library.networking.server.tictactoe_game_logic;

import org.example.game_library.networking.server.ThreadCreator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private static final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

    public static String createRoom(ThreadCreator host) {
        String roomId = generateRoomId();
        Room room = new Room(roomId, host);
        activeRooms.put(roomId, room);
        return roomId;
    }

    public static Room getRoom(String roomId) {
        return activeRooms.get(roomId);
    }

    public static boolean joinRoom(String roomId, ThreadCreator guest) {
        Room room = activeRooms.get(roomId);
        if (room == null || room.isFull()) {
            return false;
        }
        room.setGuest(guest);
        return true;
    }

    public static void removeRoom(String roomId) {
        activeRooms.remove(roomId);
    }

    private static String generateRoomId() {
        // 6 caractere, ex: "A2F9QK"
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public static void cleanupEmptyRooms() {
        activeRooms.entrySet().removeIf(entry -> !entry.getValue().isFull());
    }

    public static Map<String, Room> getAllRooms() {
        return activeRooms;
    }

    public static Room getRoomByPlayer(ThreadCreator threadCreator) {
        for (Room room : activeRooms.values()) {
            if (room.getHost() == threadCreator || room.getGuest() == threadCreator) {
                return room;
            }
        }
        return null;
    }
}
