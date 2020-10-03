package game;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Response {
    private int code;
    private String title;
    private String description;
    private String additionalInformation;

    public Response(int code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }

    private Response(int code, String title, String description, String additionalInformation) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.additionalInformation = additionalInformation;
    }

    public Response withAdditionalInfo(String additionalInformation) {
        return new Response(this.code, this.title, this.description, additionalInformation);
    }

    public int getCode() {
        return code;
    }


    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        Gson g = new Gson();
        String additionalInformationEscaped = g.toJson(additionalInformation);

        if (additionalInformationEscaped.equals("null")) {
            additionalInformationEscaped = "\"\"";
        }

        return  "{" +
                "\"model\": \"response\", " +
                "\"attributes\": {" +
                "\"title\": \"" + title + "\", " +
                "\"is_error\": " + (code > 200) + ", " +
                "\"code\": " + code + ", " +
                "\"additional_info\": " + additionalInformationEscaped + ", " +
                "\"description\": \"" + description + "\"" +
                "}" +
                '}';
    }
}
