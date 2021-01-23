package com.sum10.escape;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;
    private Cursor cursor;

    DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE THEME (_id INTEGER PRIMARY KEY AUTOINCREMENT, themename TEXT, hintcode INTEGER, hint TEXT, answer TEXT, hinturi TEXT, hinturi2 TEXT);");
        database.execSQL("CREATE TABLE THEMELIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, themename TEXT, themeuri TEXT, themetext TEXT, themetime TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    }

    public void insert(String themename, int hintcode, String hint, String answer, String uri, String uri2) {
        database = getWritableDatabase();
        database.execSQL("INSERT INTO THEME VALUES(null, '" + themename + "', '" + hintcode + "', '" + hint + "', '" + answer + "', '" + uri + "', '" + uri2 + "');");
        database.close();
    }

    public void insertTheme(String themename, String uri, String themetext, String themetime) {
        database = getWritableDatabase();
        database.execSQL("INSERT INTO THEMELIST VALUES(null, '" + themename + "', '" + uri + "', '" + themetext + "', '" + themetime + "');");
    }

    public void delete(String name, int hintcode) {
        database = getWritableDatabase();
        database.execSQL("DELETE FROM THEME WHERE hintcode=" + hintcode + " AND themename = '" + name + "';");
        database.close();
    }

    public void deleteTheme(String themename) {
        database = getWritableDatabase();
        database.execSQL("DELETE FROM THEMELIST WHERE themename = '" + themename + "';");
    }

    public ArrayList<String> getTheme() {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT themename FROM THEMELIST;", null);
        ArrayList<String> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }
        return result;
    }

    public String selectImg(String themename) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT themeuri FROM THEMELIST WHERE themename = '" + themename + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "null";
    }

    public String selectText(String themename) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT themetext FROM THEMELIST WHERE themename = '" + themename + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }

    public String selectTime(String themename) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT themetime FROM THEMELIST WHERE themename = '" + themename + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }

    public void clear() {
        database = getWritableDatabase();
        database.delete("THEME", null, null);
    }

    public String getManageList(String name) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT hintcode, answer FROM THEME WHERE themename = '" + name + "';", null);
        StringBuilder result = new StringBuilder();
        int i = 1;
        while (cursor.moveToNext()) {
            result.append(i++).append(". ").append(cursor.getString(0)).append(" -> ").append(cursor.getString(1)).append("\n");
        }
        return result.toString();
    }

    public String getHint(String name, int hintcode) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT hint FROM THEME WHERE hintcode = " + hintcode + " AND themename = '" + name + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }

    public String getImageUri(String name, int hintcode) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT hinturi FROM THEME WHERE hintcode = " + hintcode + " AND themename = '" + name + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }

    public String getImageUri2(String name, int hintcode) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT hinturi2 FROM THEME WHERE hintcode = " + hintcode + " AND themename = '" + name + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }

    public String getAnswer(String name, String hintcode) {
        database = getWritableDatabase();
        cursor = database.rawQuery("SELECT answer FROM THEME WHERE themename = '" + name + "' AND hintcode = '" + hintcode + "';", null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        } else
            return "-";
    }
}
