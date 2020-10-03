package utils;/*
utils.Helpers and utilities for deserializing JSON messages.

 */

import board.Board;
import board.Structure;
import com.google.gson.*;
import game.Resource;

import java.util.Map;

public class jsonValidator {

    public static JsonArray getAsJsonArray(String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonArray();
        } catch (Exception e) {
            return null;
        }
    }

    static JsonObject getAsJsonObject(String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }


    public static JsonObject getJsonObjectIfCorrect(String message, Map<String, ValidationType> props, Board board) {
        JsonObject jsonObject = getAsJsonObject(message);
        return objectHasProperties(jsonObject, props, board) ? jsonObject : null;
    }

    public static JsonArray getJsonArrayIfCorrect(String message, Map<String, ValidationType> props, Board board) {
        JsonArray jsonArray = getAsJsonArray(message);
        return childrenHaveProperties(jsonArray, props, board) ? jsonArray : null;
    }

    // determine whether the key and types are as expected
    public static boolean objectHasProperties(JsonObject object, Map<String, ValidationType> props, Board board) {
        if (object == null) return false;
        for (Map.Entry<String, ValidationType> prop : props.entrySet()) {
            if (!object.has(prop.getKey())
                    || !typesMatch(prop.getValue(), object.get(prop.getKey()), board)) return false;
        }
        return true;
    }

    static boolean childrenHaveProperties(JsonArray jsonArray, Map<String, ValidationType> props, Board board) {
        if (jsonArray == null) return false;
        for (JsonElement elem : jsonArray) {
            if (!objectHasProperties(elem.getAsJsonObject(), props, board)) return false;
        }
        return true;
    }

    // check if the validationtype matches the primitive type
    static boolean typesMatch(ValidationType validationType, JsonElement element, Board board) {

        // check the regular types
        switch (validationType) {
            case NUMBER:
                boolean isNumber = element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();

                // a number can be formatted as a string, but still represnt a number
                boolean isNumberRepresentedThroughString = element.getAsString().matches("\\d+");
                return isNumber || isNumberRepresentedThroughString;
            case BOOLEAN:
                return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
            case STRING:
                return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
            case OBJECT:
                return element.isJsonObject();
            case NULL:
                return element.isJsonNull();
            case ARRAY:
                return element.isJsonArray();
            default:
                // if we expect a custom type (resources, structure, key, etc) then it must for now be seen as String
                if (!(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())) return false;
        }

        String value = element.getAsJsonPrimitive().getAsString();
        switch (validationType) {
            case RESOURCE: {
                return Helpers.getResourceByName(value) != Resource.NONE;
            }
            case STRUCTURE: {
                return Helpers.getStructureByName(value) != Structure.NONE;
            }
            case DEVELOPMENT_CARD: {
                return  Helpers.getStructureByName(value) == Structure.DEVELOPMENT_CARD;
            }
            case ALL_KEYS: {
                return board.hasKey(value);
            }
            case EDGE_KEYS: {
                return board.hasEdgeKey(value);
            }
            case NODE_KEYS: {
                return board.hasNodeKey(value);
            }
            case TILE_KEYS: {
                return board.hasTileKey(value);
            }
            case EDGE_OR_NODE_KEYS: {
                return board.hasEdgeKey(value) || board.hasNodeKey(value);
            }

        }
        return false;
    }
}