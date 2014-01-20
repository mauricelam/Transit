package include;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;
import com.mauricelam.transit.TransitApplication;

import java.io.UnsupportedEncodingException;
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
	 * Parse the Date String in mySQL format into date object
	 * 
	 * @param SQLDate the date String in SQL format ("yyyy-MM-dd HH:mm:ss")
	 * @return The date object corresponding to SQLDate
	 */
	public static Date parseSQLDate(String SQLDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.parse(SQLDate, new ParsePosition(0));
	}

	/**
	 * Get the current date and time in SQL date format ("yyyy-MM-dd HH:mm:ss")
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

	public static float dp2Px(float dip) {
        DisplayMetrics metrics = TransitApplication.getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
	}

    public static int lighterColor(int color, int lighter) {
        int r = Math.max(Math.min(Color.red(color) + lighter, 255), 0);
        int g = Math.max(Math.min(Color.green(color) + lighter, 255), 0);
        int b = Math.max(Math.min(Color.blue(color) + lighter, 255), 0);
        return Color.rgb(r, g, b);
    }
	
	public static String md5(final String string) {
		try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
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

    public static Handler setTimeout(Runnable runnable, int timeout) {
        Handler handler = new Handler();
        handler.postDelayed(runnable, timeout);
        return handler;
    }
}
