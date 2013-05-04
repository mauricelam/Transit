package include;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;
import com.mauricelam.transit.Connector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class contains methods that are used commonly by multiple packages
 *
 * @author Maurice Lam
 * 
 */
public class Helper {
	private static final String TAG = "Transit Helper";
//	private static final int TIMEOUT = 100000; // timeout for HTTP connections to server

	/**
	 * A simple data class (i.e. struct) of information about an HTTP connection
	 */
	private static class HttpData {
		public String content;
		public int status;
	}

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
		Log.v("Transit Helper", server[0] + query);
		HttpData postObj = httpGet(server[0] + query);

		// fallback if server failed
//		if (postObj.status != 200) {
//			Log.w("Transit Helper", "Using fallback");
//			postObj = httpGet(server[1] + query);
//			Log.v("Transit Helper", server[1] + query);
//			if (postObj.status == 200) {
//				Log.d("Transit Helper", "alternate successful");
//				Connector.alternateSuccess();
//			}
//		}

		String returnString = postObj.content;

		if (returnString == null)
			return "";
//		int index = returnString.indexOf("<!--");
//		if (index != -1)
//			returnString = returnString.substring(0, index);
		Log.v("Transit Helper", postObj.status + " String: " + returnString);
		return returnString;
	}

    public static String restRawString(String url) {
        String[] server = Connector.getServerAddresses();
        Log.v("Transit Helper", server[0] + url);
        HttpData postObj = httpGet(url);

        String returnString = postObj.content;

        if (returnString == null)
            return "";
        int index = returnString.indexOf("<!--");
        if (index != -1)
            returnString = returnString.substring(0, index);
        Log.v("Transit Helper", postObj.status + " String: " + returnString);
        return returnString;
    }

	private static HttpData httpGet(String address) {
		HttpData ret = new HttpData();
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), TIMEOUT);
//		HttpConnectionParams.setSoTimeout(httpclient.getParams(), TIMEOUT * 3);
//		HttpGet httpget = new HttpGet(address);
		StringBuilder stringBuffer = new StringBuilder();

        try {
            URL url = new URL(address);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
        } catch (IOException e) {
            ret.status = 400;
        }

//		try {
//			HttpResponse response = httpclient.execute(httpget);
//			HttpEntity entity = response.getEntity();
//
//			InputStream instream = response.getEntity().getContent();
//			Header encoding = response.getLastHeader("Content-Encoding");
//			if (encoding != null && encoding.getValue().equalsIgnoreCase("gzip")) {
//				instream = new GZIPInputStream(instream);
//			}
//			BufferedReader br = new BufferedReader(new InputStreamReader(instream));
//
//			int c;
//			while ((c = br.read()) != -1) {
//				stringBuffer.append((char) c);
//			}
//			if (entity != null) {
//				EntityUtils.consume(entity);
//			}
//			instream.close();
//			ret.content = stringBuffer.toString();
//			ret.status = response.getStatusLine().getStatusCode();
//		} catch (SocketTimeoutException e) {
//			Log.e(TAG, "408: " + e.getMessage());
//			ret.status = 408;
//		} catch (IOException e) {
//			Log.e(TAG, "400: " + e.getMessage());
//			ret.status = 400;
//		}

//		httpclient.getConnectionManager().shutdown();
        return ret;
	}

	/**
	 * Displays a message toast
	 * 
	 * @param context
	 *            The context the toast is displayed
	 * @param text
	 *            The text contained in the toast
	 */
	public static void createToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context The context the toast is displayed. Can be application context.
	 * @param text The text contained in the toast
	 * @param shortTime Whether to display toast with shorter duration.
	 */
	public static void createToast(Context context, String text, boolean shortTime) {
		int duration = (shortTime) ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
		Toast.makeText(context, text, duration).show();
	}

	/**
	 * Creates a popup dialog that does nothing if canceled.
	 * 
	 * @see <a href="http://developer.android.com/guide/appendix/faq/commontasks.html#alerts">Android dialog</a>
	 * @param context The context to create the dialog.
	 * @param text The text to be displayed in the dialog.
	 * @param okAction
	 *            the click listener when the OK button of the dialog is clicked
	 */
	public static void createDialog(Context context, String text, OnClickListener okAction) {
		new AlertDialog.Builder(context).setMessage(text).setPositiveButton("OK", okAction)
				.setNegativeButton("Cancel", null).show();
		;
	}

	/**
	 * Parse the Date String in mySQL format into date object
	 * 
	 * @param SQLDate the date String in mySQL format ("yyyy-MM-dd HH:mm:ss")
	 * @return The date object corresponding to SQLDate
	 */
	public static Date parseSQLDate(String SQLDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.parse(SQLDate, new ParsePosition(0));
	}

	/**
	 * Get the current date and time in mySQL date format ("yyyy-MM-dd HH:mm:ss")
	 * 
	 * @return String representing current date and time
	 */
	public static String timeNow() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Gives a number only string representing current time ("yyyyMMddHHmmss")
	 * 
	 * @return The string representation of current time.
	 */
	public static String timeNumbers() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Get the current time in a custom format, with seconds truncated
	 * 
	 * @return String of current time in custom format
	 */
	public static String getCurrentMinute() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Turns a Java date object into a mySQL acceptable date string
	 * 
	 * @param date
	 *            Date object
	 * @return mySQL acceptable date string
	 */
	public static String toSQLDateTimeString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

    /**
     * Turns a Java date object into a mySQL acceptable date string
     *
     * @param date
     *            Date object
     * @return mySQL acceptable date string
     */
    public static String toSQLDateString(Date date, String timezone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return dateFormat.format(date);
    }

	public static String toNumberDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

	public static boolean hasString(String[] arr, String needle) {
        for (String item : arr) {
            if (item.equals(needle))
                return true;
        }
		return false;
	}

	public static int convertDip2Pixels(Context context, int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources()
				.getDisplayMetrics());
	}
	
	public static String md5(final String string) {
		try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(string.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuilder hexString = new StringBuilder();
            for (byte currentByte : messageDigest) {
                String h = Integer.toHexString(0xFF & currentByte);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

    public static int clamp(int value, int lo, int hi) {
        return Math.min(Math.max(value, lo), hi);
    }

    /**
     * Encode a String of URL into UTF-8, and handles exception by returning a default value.
     * @param url The URL to encode
     */
    public static String urlEncode(String url, String def) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.w("Transit URL encode", "Unsupported encoding: " + e.getMessage());
            return def;
        }
    }
}
