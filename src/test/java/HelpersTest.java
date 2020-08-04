import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HelpersTest {

    @Test
    void toJSONArrayEmptyTest() {
        ArrayList<String> list = new ArrayList<String>();
        assertEquals("[]", Helpers.toJSONArray(list, false));
        assertEquals("[]", Helpers.toJSONArray(list, true));
    }

    @Test
    void toJSONArrayEscapeStringTest() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("test");
        list.add("test2");
        assertEquals("[test, test2]", Helpers.toJSONArray(list, false));
        assertEquals("[\"test\", \"test2\"]", Helpers.toJSONArray(list, true));
    }

    @Test
    void toJSONArrayEscapeIntTest() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        assertEquals("[1, 2]", Helpers.toJSONArray(list, false));
        assertEquals("[\"1\", \"2\"]", Helpers.toJSONArray(list, true));
    }

    @Test
    void getJSONArrayFromHashMapTest() throws JSONException {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("test", 1);
        map.put("test2", 2);
        String expected = "[ {\"testKey\": \"test\", \"testValue\": 1 },{\"testKey\":\"test2\", \"testValue\":2}]";
        String real = Helpers.getJSONArrayFromHashMap(map, "testKey", "testValue");
        JSONAssert.assertEquals(expected, real, false);
    }
}
