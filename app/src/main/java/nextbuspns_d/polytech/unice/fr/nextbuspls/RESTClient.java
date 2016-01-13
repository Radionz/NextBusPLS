package nextbuspns_d.polytech.unice.fr.nextbuspls;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Base class for REST Clients
 */
public class RESTClient extends AsyncTask<String, Void, JSONObject> {

    private static final String LOGGER_TAG = "RESTClient";
    public AsyncResponse delegate = null;
    private JSONObject location;
    public RESTClient(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    private static String convertStreamToString(InputStream inputStream) {
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

    public String requestUrl(String requestMethod, String url, String json) {
        if (Log.isLoggable(LOGGER_TAG, Log.INFO)) {
            Log.i(LOGGER_TAG, "Requesting service: " + url + "\nmethod: " + requestMethod);
        }
        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(url);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            //urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            //urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);


            if (Log.isLoggable(LOGGER_TAG, Log.INFO) && !json.isEmpty()) {
                Log.i(LOGGER_TAG, requestMethod + " json: " + json);
            }

            urlConnection.setRequestMethod(requestMethod);

            // handle request parameters
            if (!json.isEmpty()) {
                if (!requestMethod.equals(RequestMethod.GET.toString())) {
                    urlConnection.setDoOutput(true);
                    urlConnection.setFixedLengthStreamingMode(json.getBytes().length);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    //send the request out
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(json);
                    out.close();
                }
            }

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // throw some exception
                Log.e(LOGGER_TAG, "statusCode: " + statusCode);
                return null;
            } else {
                // read output (only for GET)

                if (json != null) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    return convertStreamToString(inputStream);
                } else {
                    return null;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    protected void execute(RequestMethod requestMethod, String url, JSONObject jsonObject) {
        this.execute(requestMethod.toString(), url, jsonObject.toString());
    }

    protected void execute(RequestMethod requestMethod, String url) {
        this.execute(requestMethod.toString(), url, "");
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject jsonObjectReturn = null;
        try {
            String response = requestUrl(params[0], params[1], params[2]);
            if (response != null) {
                if (Log.isLoggable(LOGGER_TAG, Log.INFO) && !response.isEmpty()) {
                    Log.i(LOGGER_TAG, "response: " + response);
                }
                jsonObjectReturn = new JSONObject(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectReturn;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        delegate.processFinish(jsonObject);
    }

    public interface AsyncResponse {
        void processFinish(JSONObject json);
    }
}