package com.mauricelam.transit;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

public class Suggester extends SearchRecentSuggestionsProvider {
	private static final String AUTHORITY = "com.mauricelam.transit.Suggester";
	private static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	
	// Static reference to self...
	public static Suggester currentProvider;
	
	public Suggester() {
		setupSuggestions(AUTHORITY, MODE);
		currentProvider = this;
	}
	
	public static void saveRecentQuery(Context context, String line, String line2){
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				context, AUTHORITY, MODE);
		suggestions.saveRecentQuery(line, line2);
	}

}