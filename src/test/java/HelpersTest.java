import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import utils.Helpers;

import java.util.ArrayList;
import java.util.HashMap;

public class HelpersTest {

    @Test
    void toJSONArrayEmptyTest() throws JSONException {
        ArrayList<String> list = new ArrayList<String>();
        JSONAssert.assertEquals("[]", Helpers.toJSONArray(list, false), true);
        JSONAssert.assertEquals("[]", Helpers.toJSONArray(list, true), true);
    }

    @Test
    void toJSONArrayEscapeStringTest() throws JSONException {
        ArrayList<String> list = new ArrayList<String>();
        list.add("test");
        list.add("test2");
        JSONAssert.assertEquals("[test, test2]", Helpers.toJSONArray(list, false), true);
        JSONAssert.assertEquals("[\"test\", \"test2\"]", Helpers.toJSONArray(list, true), true);
    }

    @Test
    void toJSONArrayEscapeIntTest() throws JSONException {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        JSONAssert.assertEquals("[1, 2]", Helpers.toJSONArray(list, false), true);
        JSONAssert.assertEquals("[\"1\", \"2\"]", Helpers.toJSONArray(list, true), true);
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
