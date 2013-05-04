package include;

import android.content.ContentValues;

public class FlatJSON {
	private ContentValues cv = new ContentValues();

	public FlatJSON(String string) {
		string = string.substring(string.indexOf('{')+1);
		string = string.substring(0, string.indexOf('}'));
		String[] vals = string.split(",");
		for (int i = 0; i < vals.length; i++) {
			String[] pair = vals[i].split(":", 2);
			String key = pair[0].replaceAll("^\"|\"$", "");
			String val = pair[1].replaceAll("^\"|\"$", "");
			cv.put(key, val);
		}
	}
	
	public ContentValues getCV(){
		return cv;
	}

	public String getString(String key) {
		return cv.getAsString(key);
	}

	public int getInt(String key) {
		return Integer.parseInt(cv.getAsString(key));
	}
}
