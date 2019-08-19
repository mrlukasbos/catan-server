public class Response {
    private int code = 0;
    private String title;
    private String description;
    private String additionalInformation;

    Response(int code, String title, String description) {
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

    Response withAdditionalInfo(String additionalInformation) {
        return new Response(this.code, this.title, this.description, additionalInformation);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return  "{" +
                "\"model\": \"response\", " +
                "\"attributes\": {" +
                "\"isError\": " + (code != 0) + ", " +
                "\"errorCode\": " + code + ", " +
                "\"additionalInfo\": \"" + additionalInformation + "\", " +
                "\"description\": " + description +
                "}" +
                '}';
    }
}
