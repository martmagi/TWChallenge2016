package ee.doggify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

import ee.doggify.Models.Receipt;

/**
 * Created by Karl on 20.02.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    // Table
    private static final String TABLE_RECEIPT = "receipt";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_COMP = "company";
    private static final String KEY_SUM = "sum";
    private static final String KEY_SORTED = "sorted";

    private static final String CREATE_TABLE_RECEIPT = "CREATE TABLE "
            + TABLE_RECEIPT + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATE
            + " DATE, " + KEY_COMP + " VARCHAR(50), " + KEY_SORTED
            + " BOOLEAN, " + KEY_SUM + " REAL" + ")";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d("Database", "Creating table");
        db.execSQL(CREATE_TABLE_RECEIPT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPT);
    }

    public void truncateDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_RECEIPT);
        db.execSQL("VACUUM " + TABLE_RECEIPT);
    }

    public boolean insertIntoDB(Receipt receipt) {
        Log.d("Database", "Inserting into database. Receipt: " + receipt);
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_COMP, receipt.getCompanyName());
            values.put(KEY_SUM, receipt.getPrice());
            values.put(KEY_SORTED, receipt.isUseful() ? 1 : 0);
            values.put(KEY_DATE, String.valueOf(receipt.getDate()));
            db.insert(TABLE_RECEIPT, null, values);
            readFromDB();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public ArrayList<Receipt> readFromDB() {
        Log.d("Database", "Reading from database");
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPT;
        ArrayList<Receipt> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor == null) {
            Log.d("Database", "Database is empty");
            return new ArrayList();
        } else {
            if (cursor.moveToFirst()) {
                do {
                    Receipt newReceipt = new Receipt();
                    newReceipt.setPrice(cursor.getInt(cursor.getColumnIndex(KEY_SUM)));
                    newReceipt.setCompanyName((cursor.getString(cursor.getColumnIndex(KEY_COMP))));
                    DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

                    try {
                        Date date = formatter.parse(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                        newReceipt.setDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (cursor.getInt(cursor.getColumnIndex(KEY_SORTED)) == 0) {
                        newReceipt.setUseful(false);
                    } else {
                        newReceipt.setUseful(true);
                    }
                    list.add(newReceipt);
                    Log.d("Database", "query from database: " + newReceipt);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return list;
        }
    }

    public ArrayList<Receipt> readSpecificFromDB(int a) {
        SQLiteDatabase db = this.getReadableDatabase();
        int START_DATE;
        int END_DATE;

        START_DATE = calcDate(a);
        END_DATE = calcDate(0);
        if (a != 1) {
            END_DATE -= 1;
        }

        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPT + " WHERE " + KEY_DATE + " BETWEEN " + START_DATE + " AND " + END_DATE;
        ArrayList<Receipt> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Receipt newReceipt = new Receipt();
                newReceipt.setPrice(cursor.getInt(cursor.getColumnIndex(KEY_SUM)));
                newReceipt.setCompanyName((cursor.getString(cursor.getColumnIndex(KEY_COMP))));
                DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

                try {
                    Date date = formatter.parse(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                    newReceipt.setDate(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (cursor.getString(cursor.getColumnIndex(KEY_SORTED)).equals("False")) {
                    newReceipt.setUseful(false);
                } else {
                    newReceipt.setUseful(true);
                }
                list.add(newReceipt);
                Log.d("Database", "query from database: " + newReceipt);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public int calcDate(int margin) {
        Calendar cal0 = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();

        cal0.set(2015, 01, 01);
        if (margin == 1) {
            cal1.add(Calendar.DAY_OF_MONTH, -1);
        } else if (margin == 2) {
            cal1.add(Calendar.WEEK_OF_MONTH, -1);
        } else if (margin == 3) {
            cal1.add(Calendar.MONTH, -1);
        }

        return (int) ((cal1.getTime().getTime() - cal0.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

    public boolean assignSorted(int id, Receipt receipt) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, receipt.getCompanyName());
        values.put(KEY_COMP, receipt.getCompanyName());
        values.put(KEY_SUM, receipt.getPrice());
        values.put(KEY_SORTED, true);
        //values.put(KEY_DATE, receipt.getDate());

        this.getReadableDatabase().update(TABLE_RECEIPT, values, KEY_ID, null);
        return true;
    }
}