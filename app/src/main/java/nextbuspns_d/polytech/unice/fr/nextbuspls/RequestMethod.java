package nextbuspns_d.polytech.unice.fr.nextbuspls;

/**
 * Created by Dorian on 12/01/2016.
 */
public enum RequestMethod {
    GET("GET"), POST("POST"), PUT("PUT");

    private String requestMethod;

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    private RequestMethod(String requestMethod){
        this.requestMethod = requestMethod;
    }


    @Override
    public String toString() {
        return requestMethod;
    }
}
