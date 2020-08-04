import java.util.HashMap;
import java.util.List;

public class Helpers {

    // Convert a List to a readable string
    // If escape == true, then the objects in the list are put between quotation marks ("")
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

    // Convert a hashmap to a json object
    // The keyname variable is the name of the 'key' in the json (for example "type" or "key")
    // The valueName variable is the name of the 'variable' in the json (for example "value")
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
