package com.budgetmaster;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BudgetMasterSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Create login session
    public void createLoginSession(int userId, String email, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Get email
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Get username
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    // Logout user
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}