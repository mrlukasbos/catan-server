import java.util.HashMap;
import java.util.List;

public class Helpers {

    // TODO needs a test
    static <T> String toJSONArray(List<T> list, boolean escape, String joint) {
        if (list.size() == 0) {
            return  "[]";
        }

        String output = "[";
        for (T obj : list) {
            if (escape) {
                output = output.concat("\"" + obj.toString() + "\"" + joint);
            } else {
                output = output.concat(obj.toString() + joint);
            }
        }
        output = output.substring(0, output.length() - joint.length());
        output = output.concat("]");
        return output;
    }

    // TODO needs a test
    static <T> String getJSONArrayFromHashMap(HashMap<T, Integer> map, String keyName, String valueName) {
        String output = "[";
        if (map.size() == 0) {
            output = "[]";
        } else {
            for (HashMap.Entry<T, Integer> entry : map.entrySet()) {
                output = output.concat("{\"" + keyName + "\":\"" + entry.getKey().toString() + "\", \"" + valueName + "\":" + entry.getValue()) + "},";
            }
            output = output.substring(0, output.length() - 1);
            output = output.concat("]");
        }
        return output;
    }
}
