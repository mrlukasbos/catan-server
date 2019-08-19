import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
}