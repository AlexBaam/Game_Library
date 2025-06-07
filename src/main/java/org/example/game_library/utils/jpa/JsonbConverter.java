package org.example.game_library.utils.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;

@Converter(autoApply = false)
public class JsonbConverter implements AttributeConverter<Object, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error serializing object to JSON", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, TicTacToeGame.class); // IMPORTANT!
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deserializing JSON to TicTacToeGame", e);
        }
    }
}