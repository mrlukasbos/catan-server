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

    static JsonArray getJsonObjectIfCorrect(String message, Map<String, ValidationType> props) {
        JsonArray jsonArray = getAsJsonArray(message);
        return childrenHaveProperties(jsonArray, props) ? jsonArray : null;
    }

    // determine whether the key and types are as expected
    static boolean objectHasProperties(JsonObject object, Map<String, ValidationType> props) {
        for (Map.Entry<String, ValidationType> prop : props.entrySet()) {
            if (!object.has(prop.getKey())
                    || !object.get(prop.getKey()).isJsonPrimitive()
                    || !typesMatch(prop.getValue(), object.get(prop.getKey()).getAsJsonPrimitive())) return false;
        }
        return true;
    }

    static boolean childrenHaveProperties(JsonArray jsonArray, Map<String, ValidationType> props) {
        if (jsonArray == null) return false;
        for (JsonElement elem : jsonArray) {
            if (!objectHasProperties(elem.getAsJsonObject(), props)) return false;
        }
        return true;
    }

    // check if the validationtype matches the primitive type
    static boolean typesMatch(ValidationType validationType, JsonPrimitive primitive) {
        switch (validationType) {
            case NUMBER:  return primitive.isNumber();
            case BOOLEAN: return primitive.isBoolean();
            case STRING: return primitive.isString();
            case OBJECT: return primitive.isJsonObject();
            case NULL: return primitive.isJsonNull();
            case ARRAY: return primitive.isJsonArray();
            case RESOURCE: {
                if (!primitive.isString()) return false;
                return Helpers.getResourceByName(primitive.getAsString()) != Resource.NONE;
            }
            case STRUCTURE: {
                if (!primitive.isString()) return false;
                return Helpers.getStructureByName(primitive.getAsString()) != Structure.NONE;
            }
        }
        return false;
    }
}