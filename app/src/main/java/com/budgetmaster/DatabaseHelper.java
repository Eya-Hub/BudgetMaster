package com.budgetmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "BudgetMaster.db";
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_INVESTMENTS = "investments";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_GOALS = "goals";

    // Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Users Table Columns
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // Transactions Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DATE = "date";
    private static final String KEY_LABEL = "label";
    private static final String KEY_DESCRIPTION = "description";

    // Goals Table Columns
    private static final String KEY_GOAL_NAME = "goal_name";
    private static final String KEY_TARGET_AMOUNT = "target_amount";
    private static final String KEY_CURRENT_AMOUNT = "current_amount";

    // Table Create Statement - Users
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_EMAIL + " TEXT UNIQUE,"
            + KEY_USERNAME + " TEXT,"
            + KEY_PASSWORD + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    // Table Create Statement - Transactions
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_AMOUNT + " REAL NOT NULL,"
            + KEY_CATEGORY + " TEXT NOT NULL,"
            + KEY_DATE + " TEXT,"
            + KEY_LABEL + " TEXT,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    // Table Create Statement - Investments
    private static final String CREATE_TABLE_INVESTMENTS = "CREATE TABLE " + TABLE_INVESTMENTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_AMOUNT + " REAL NOT NULL,"
            + KEY_DATE + " TEXT,"
            + KEY_LABEL + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    // Table Create Statement - Goals
    private static final String CREATE_TABLE_GOALS = "CREATE TABLE " + TABLE_GOALS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_GOAL_NAME + " TEXT NOT NULL,"
            + KEY_TARGET_AMOUNT + " REAL NOT NULL,"
            + KEY_CURRENT_AMOUNT + " REAL DEFAULT 0,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_INVESTMENTS);
        db.execSQL(CREATE_TABLE_GOALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVESTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);

        // Create tables again
        onCreate(db);
    }
    // ============= USER METHODS =============

    // Add a new user
    public long addUser(String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email);
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password); // In production, hash this password!

        // Insert row
        long userId = db.insert(TABLE_USERS, null, values);
        //db.close();

        return userId;
    }

    // Check if email already exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { KEY_ID };
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        //db.close();

        return exists;
    }

    // Check if username already exists
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { KEY_ID };
        String selection = KEY_USERNAME + " = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        //db.close();

        return exists;
    }

    // Authenticate user (for login)
    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { KEY_ID };
        String selection = KEY_EMAIL + " = ? AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = { email, password };

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        boolean authenticated = cursor.getCount() > 0;
        cursor.close();
        //db.close();

        return authenticated;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { KEY_ID, KEY_EMAIL, KEY_USERNAME, KEY_CREATED_AT };
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
        }

        if (cursor != null) {
            cursor.close();
        }
        //db.close();

        return user;
    }

    // Add user with Google data (no password)
    public long addGoogleUser(String email, String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email);
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, "google_oauth"); // Special marker for Google users

        long userId = db.insert(TABLE_USERS, null, values);
        return userId;
    }

    // Get or create user for Google sign-in
    public User getOrCreateGoogleUser(String email, String displayName) {
        // Check if user already exists
        User existingUser = getUserByEmail(email);
        if (existingUser != null) {
            return existingUser;
        }

        // Create new user with Google data
        String username = generateUsernameFromEmail(email, displayName);
        long userId = addGoogleUser(email, username);

        if (userId != -1) {
            return getUserByEmail(email);
        }
        return null;
    }

    // Generate username from email or display name
    private String generateUsernameFromEmail(String email, String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            // Use display name and remove spaces
            return displayName.replaceAll("\\s+", "").toLowerCase();
        } else {
            // Use email username part
            return email.split("@")[0];
        }
    }

    // ============= TRANSACTION METHODS =============

    public boolean insertTransaction(int userId, double amount, String category, String date, String label, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_AMOUNT, amount);
        cv.put(KEY_CATEGORY, category);
        cv.put(KEY_DATE, date);
        cv.put(KEY_LABEL, label);
        cv.put(KEY_DESCRIPTION, description);

        long result = db.insert(TABLE_TRANSACTIONS, null, cv);
        return result != -1;
    }

    public double getTotalByCategory(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + KEY_USER_ID + " = ? AND " + KEY_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), category}
        );

        double total = 0;
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    public Cursor getLabelsByCategory(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT " + KEY_LABEL + " FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + KEY_USER_ID + " = ? AND " + KEY_CATEGORY + " = ? ORDER BY " + KEY_LABEL,
                new String[]{String.valueOf(userId), category}
        );
    }

    public Cursor getAllTransactions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_CREATED_AT + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    // ============= INVESTMENT METHODS =============

//    public boolean insertInvestment(double amount, String date, String label) {
//        return insertInvestment(1, amount, date, label);
//    }

    public boolean insertInvestment(int userId, double amount, String date, String label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_AMOUNT, amount);
        cv.put(KEY_DATE, date);
        cv.put(KEY_LABEL, label);

        long result = db.insert(TABLE_INVESTMENTS, null, cv);
        return result != -1;
    }

    public double getTotalInvestments(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_INVESTMENTS +
                        " WHERE " + KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        double total = 0;
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    public Cursor getInvestmentSumByLabel(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + KEY_LABEL + ", SUM(" + KEY_AMOUNT + ") as total FROM " + TABLE_INVESTMENTS +
                        " WHERE " + KEY_USER_ID + " = ? GROUP BY " + KEY_LABEL + " ORDER BY " + KEY_LABEL,
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getAllInvestmentLabelsCursor(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT " + KEY_LABEL + " FROM " + TABLE_INVESTMENTS +
                        " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_LABEL,
                new String[]{String.valueOf(userId)}
        );
    }


    // ============= GOAL METHODS =============

    public long createGoal(int userId, String goalName, double targetAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, userId);
        values.put(KEY_GOAL_NAME, goalName);
        values.put(KEY_TARGET_AMOUNT, targetAmount);
        values.put(KEY_CURRENT_AMOUNT, 0.0);

        long goalId = db.insert(TABLE_GOALS, null, values);
        return goalId;
    }

    public Cursor getAllGoals(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_GOALS +
                        " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_CREATED_AT + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getGoalNames(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT " + KEY_GOAL_NAME + " FROM " + TABLE_GOALS +
                        " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_GOAL_NAME,
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean addAmountToGoal(int userId, String goalName, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Update the current_amount by adding the new amount
        db.execSQL(
                "UPDATE " + TABLE_GOALS +
                        " SET " + KEY_CURRENT_AMOUNT + " = " + KEY_CURRENT_AMOUNT + " + ?" +
                        " WHERE " + KEY_USER_ID + " = ? AND " + KEY_GOAL_NAME + " = ?",
                new Object[]{amount, userId, goalName}
        );

        return true;
    }

    public boolean deleteGoal(int userId, int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_GOALS,
                KEY_ID + " = ? AND " + KEY_USER_ID + " = ?",
                new String[]{String.valueOf(goalId), String.valueOf(userId)});
        return result > 0;
    }

    public Cursor getGoalByName(int userId, String goalName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_GOALS +
                        " WHERE " + KEY_USER_ID + " = ? AND " + KEY_GOAL_NAME + " = ?",
                new String[]{String.valueOf(userId), goalName}
        );
    }

    public boolean checkGoalNameExists(int userId, String goalName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + KEY_ID + " FROM " + TABLE_GOALS +
                        " WHERE " + KEY_USER_ID + " = ? AND " + KEY_GOAL_NAME + " = ?",
                new String[]{String.valueOf(userId), goalName}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public double getTotalGoalsAmount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + KEY_CURRENT_AMOUNT + ") FROM " + TABLE_GOALS +
                        " WHERE " + KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

}