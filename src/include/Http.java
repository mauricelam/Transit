package include;

import android.util.Log;
import com.mauricelam.transit.Connector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Class to manage HTTP connections. Supports directly getting JSON objects, arrays for raw strings.
 *
 * User: mauricelam
 * Date: 7/5/13
 * Time: 6:32 PM
 */
public class Http {
    private static final int TIMEOUT = 5000; // timeout for HTTP connections to server

    /**
     * Request a JSON object from a query post to server
     *
     * @param query
     *            URL to post to
     * @param params
     *            The string to post to server (e.g. "word=abc&num=123")
     * @return a JSON object
     */
    public static JSONObject restJSONObject(String query, String params) {
        String returnString = restString(query, params);
        return toJSONObject(returnString);
    }

    /**
     * Convert a JSON compatible string into JSON object
     *
     * @param string A JSON string.
     * @return JSON object created from the string.
     */
    public static JSONObject toJSONObject(String string) {
        try {
            return new JSONObject(string);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Request a JSON array instead of JSON object
     *
     * @param query The REST endpoint, relative to the server.
     * @param params GET parameters
     * @return a JSON array
     */
    public static JSONArray restJSONArray(String query, String params) {
        String returnString = restString(query, params);
        return toJSONArray(returnString);
    }

    /**
     * Convert a JSON compatible string into JSON array
     *
     * @param string A JSON string.
     * @return JSON Array created from the string.
     */
    public static JSONArray toJSONArray(String string) {
        try {
            return new JSONArray(string);
        } catch (JSONException e) {
            Log.e("Transit Helper toJSONArray", e.getMessage());
            return null;
        }
    }

    public static String restString(String query, String params) {
        params = params.replace(" ", "%20");
        return restString(query + "?" + params);
    }

    /**
     * Request a String value from a query post to server
     *
     * @param query
     *            URL to post to
     * @return String value of whatever returned from server
     */
    public static String restString(String query) {
        if (query.startsWith("http")) {
            return restRawString(query);
        }
        String[] server = Connector.getServerAddresses();
        return restRawString(server[0] + query);
    }

    public static String restRawString(String url) {
        Log.v("Transit Helper", url);
        HttpData postObj = httpGet(url);

        String returnString = postObj.content;

        if (returnString == null)
            return "";
        // Filter out the HTML comment that might be added in some free hosts
        int index = returnString.indexOf("<!--");
        if (index != -1)
            returnString = returnString.substring(0, index);
        Log.v("Transit Helper", postObj.status + " String: " + returnString);
        return returnString;
    }

    private static HttpData httpGet(String address) {
        HttpData ret = new HttpData();
        StringBuilder stringBuffer = new StringBuilder();

        try {
            URL url = new URL(address);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data;
            while ((data = isw.read()) != -1) {
                char current = (char) data;
                stringBuffer.append(current);
            }
            ret.status = 200;
            ret.content = stringBuffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ret.status = 400;
        } catch (SocketTimeoutException e) {
            ret.status = 400;
        } catch (IOException e) {
            ret.status = 400;
        }

        return ret;
    }

    /**
     * A simple data class (i.e. struct) of information about an HTTP connection
     */
    private static class HttpData {
        public String content;
        public int status;
    }
}
