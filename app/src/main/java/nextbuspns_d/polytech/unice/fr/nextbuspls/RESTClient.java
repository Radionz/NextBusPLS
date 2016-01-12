package nextbuspns_d.polytech.unice.fr.nextbuspls;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Base class for REST Clients
 */
public class RESTClient extends AsyncTask<String, Void, JSONObject> {

    public interface AsyncResponse {
        void processFinish(JSONObject location);
    }

    public AsyncResponse delegate = null;
    private JSONObject location;

    public RESTClient(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    private JSONObject requestContent(String url) {
        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(url);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
            }

            // create JSON object from content
            InputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertStreamToString(bufferedInputStream));

        } catch (MalformedURLException e) {
            // URL is invalid
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
        } catch (IOException e) {
            // could not read response body
            // (could not create input stream)
        } catch (JSONException e) {
            // response body is no valid JSON string
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        return requestContent(urls[0]);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        /*try {
            JSONArray results = (JSONArray) jsonObject.get("results");
            JSONObject geometry = (JSONObject) ((JSONObject) results.get(0)).get("geometry");
            location = (JSONObject) geometry.get("location");
            delegate.processFinish(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        delegate.processFinish(jsonObject);
    }
}