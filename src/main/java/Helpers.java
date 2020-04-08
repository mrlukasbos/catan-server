import java.util.HashMap;
import java.util.List;

public class Helpers {

    // TODO needs a test
    static <T> String toJSONArray(List<T> list, boolean escape) {
        if (list.size() == 0) {
            return  "[]";
        }

        String output = "[";
        for (T obj : list) {
            if (escape) {
                output = output.concat("\"" + obj.toString() + "\"" + ",");
            } else {
                output = output.concat(obj.toString() + ",");
            }
        }
        output = output.substring(0, output.length() - 1);
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

    static Resource getResourceByName(String name) {
        for (Resource resource : Constants.ALL_RESOURCES) {
            if (name.toLowerCase().equals(resource.name().toLowerCase())) {
                return resource;
            }
        }
        return Resource.NONE;
    }
}
