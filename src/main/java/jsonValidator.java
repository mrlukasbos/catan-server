/*
Helpers and utilities for deserializing JSON messages.

 */

import com.google.gson.*;
import java.util.Map;

enum ValidationType {
    NUMBER,
    STRING,
    BOOLEAN,
    OBJECT,
    NULL,
    ARRAY,
    RESOURCE,
    STRUCTURE,
    ALL_KEYS,
    EDGE_KEYS,
    NODE_KEYS,
    TILE_KEYS,
    EDGE_OR_NODE_KEYS,
}

public class jsonValidator {

    static JsonArray getAsJsonArray(String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonArray();
        } catch (Exception e) {
            return null;
        }
    }

    static JsonArray getJsonObjectIfCorrect(String message, Map<String, ValidationType> props, Board board) {
        JsonArray jsonArray = getAsJsonArray(message);
        return childrenHaveProperties(jsonArray, props, board) ? jsonArray : null;
    }

    // determine whether the key and types are as expected
    static boolean objectHasProperties(JsonObject object, Map<String, ValidationType> props, Board board) {
        for (Map.Entry<String, ValidationType> prop : props.entrySet()) {
            if (!object.has(prop.getKey())
                    || !object.get(prop.getKey()).isJsonPrimitive()
                    || !typesMatch(prop.getValue(), object.get(prop.getKey()).getAsJsonPrimitive(), board)) return false;
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
    static boolean typesMatch(ValidationType validationType, JsonPrimitive primitive, Board board) {

        // check the regular types
        switch (validationType) {
            case NUMBER:
                return primitive.isNumber();
            case BOOLEAN:
                return primitive.isBoolean();
            case STRING:
                return primitive.isString();
            case OBJECT:
                return primitive.isJsonObject();
            case NULL:
                return primitive.isJsonNull();
            case ARRAY:
                return primitive.isJsonArray();
            default:
                // if we expect a custom type (resources, structure, key, etc) then it must for now be seen as String
                if (!primitive.isString()) return false;
        }

        String value = primitive.getAsString();
        switch (validationType) {
            case RESOURCE: {
                return Helpers.getResourceByName(value) != Resource.NONE;
            }
            case STRUCTURE: {
                return Helpers.getStructureByName(value) != Structure.NONE;
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