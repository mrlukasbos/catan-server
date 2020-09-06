import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class JsonValidatorTest {
    @Test
    public void testInValidJsons() {
        assertNull(jsonValidator.getAsJsonArray("["));
        assertNull(jsonValidator.getAsJsonArray("{}"));
        assertNull(jsonValidator.getAsJsonArray("{ \"from\": \"stone\", \"to\": \"wood\" }, { \"from\": \"grain\", \"to\": \"stone\" }"));
        assertNull(jsonValidator.getAsJsonArray("{ \"from\": \"stone\", \"to\": \"wood\" }, "));
        assertNull(jsonValidator.getAsJsonArray("[ \"from\": \"stone\", \"to\": \"wood\" ] "));
        assertNull(jsonValidator.getAsJsonArray(null));
    }

    @Test
    public void testValidJsons() {
        assertEquals(jsonValidator.getAsJsonArray("[]"), new JsonArray());
        assertEquals(jsonValidator.getAsJsonArray("    []      \n "), new JsonArray());
        assertEquals(jsonValidator.getAsJsonArray("[{ \"from\": \"ore\", \"to\": \"wood\" }]").size(), 1);
    }

    @Test
    public void testValidOutput() {
        String message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,1])\" }]\n";
        JsonArray array = jsonValidator.getAsJsonArray(message);
        assertEquals(array.get(0).getAsJsonObject().get("structure").getAsString(), "city");
        assertEquals(array.get(1).getAsJsonObject().get("structure").getAsString(), "street");
    }

    @Test
    public void testValidation() {
        Board board = new Board();
        String message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,1])\" }]\n";
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("structure", ValidationType.STRUCTURE);
            put("location", ValidationType.STRING);
        }};
        assertNotNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        // incorrect message (street is misspelled and is thus not a structure)
        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"stret\", \"location\": \"([2,2],[3,1])\" }]\n";
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        // incorrect message (structure key is misspelled while it is expected)
        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structre\": \"street\", \"location\": \"([2,2],[3,1])\" }]\n";
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        // incorrect message (child misses a field)
        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"street\"}]\n";
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        // incorrect message (child has null field)
        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": }]\n";
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));
        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": null}]\n";
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));
    }

    public void testEdgeNodeTileKeyValidations() {
        Board board = new Board();
        String message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,1])\" }]\n";
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("structure", ValidationType.STRUCTURE);
            put("location", ValidationType.ALL_KEYS);
        }};
        assertNotNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"stret\", \"location\": \"([2,2],[3,1])\" }]\n";
        props = new HashMap<>() {{
            put("structure", ValidationType.STRUCTURE);
            put("location", ValidationType.EDGE_OR_NODE_KEYS);
        }};
        assertNotNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }]\n";
        props = new HashMap<>() {{
            put("structure", ValidationType.STRUCTURE);
            put("location", ValidationType.EDGE_KEYS);
        }};
        assertNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));

        props = new HashMap<>() {{
            put("structure", ValidationType.STRUCTURE);
            put("location", ValidationType.NODE_KEYS);
        }};
        assertNotNull(jsonValidator.getJsonObjectIfCorrect(message, props, board));
    }
}
