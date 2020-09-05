import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonValidatorTest {
    Player player = new Player(null,0, "tester");
    jsonValidator validator = new jsonValidator();

    @Test
    public void testInValidJsons() {
        assertNull(validator.getAsJsonObject("["));
        assertNull(validator.getAsJsonObject("{}"));
        assertNull(validator.getAsJsonObject("{ \"from\": \"stone\", \"to\": \"wood\" }, { \"from\": \"grain\", \"to\": \"stone\" }"));
        assertNull(validator.getAsJsonObject("{ \"from\": \"stone\", \"to\": \"wood\" }, "));
        assertNull(validator.getAsJsonObject("[ \"from\": \"stone\", \"to\": \"wood\" ] "));
        assertNull(validator.getAsJsonObject(null));
    }

    @Test
    public void testValidJsons() {
        assertEquals(validator.getAsJsonObject("[]"), new JsonArray());
        assertEquals(validator.getAsJsonObject("    []      \n "), new JsonArray());
        assertEquals(validator.getAsJsonObject("[{ \"from\": \"ore\", \"to\": \"wood\" }]").size(), 1);
    }

    @Test
    public void testValidOutput() {
        String message = "[{ \"structure\": \"city\", \"location\": \"([1,2],[2,1],[2,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,1])\" }]\n";
        JsonArray array = validator.getAsJsonObject(message);
        assertEquals(array.get(0).getAsJsonObject().get("structure").getAsString(), "city");
        assertEquals(array.get(1).getAsJsonObject().get("structure").getAsString(), "street");
    }
}
