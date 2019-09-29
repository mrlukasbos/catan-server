public class Response {
    private int code = 0;
    private String jsonData;
    private String additionalInfo;

    Response(int code) {
        this.code = code;
        this.jsonData = "{}";
        this.additionalInfo = "";
    }

    Response(int code, String jsonData) {
        this.code = code;
        this.jsonData = jsonData;
        this.additionalInfo = "";
    }

    private Response(int code, String jsonData, String additionalInfo) {
        this.code = code;
        this.jsonData = jsonData;
        this.additionalInfo = additionalInfo;
    }

    Response withData(String data) {
        return new Response(this.code, data, this.additionalInfo);
    }

    Response withAdditionalInfo(String info) {
        return new Response(code, jsonData, info);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        if (jsonData.isEmpty()) System.out.println("[response] Warning: no data is sent with the response!");
        return  "{" +
                "\"code\": " + code + ", " +
                "\"data\": " + jsonData +
                "}\r\n";
    }
}
