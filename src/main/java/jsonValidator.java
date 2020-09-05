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
}

public class jsonValidator {

    JsonArray getJsonIfValid(Player player, String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonArray();
        } catch (Exception e) {
            return null;
        }
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
        for (JsonElement elem: jsonArray) {
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
        }
        return false;
    }
}