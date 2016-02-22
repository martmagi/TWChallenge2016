package pakett.tempname;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

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
            + " INTEGER, " + KEY_COMP + " VARCHAR(50), " + KEY_SORTED
            + " BOOLEAN, " + KEY_SUM + " INT" + ")";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECEIPT);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPT);
    }
    public boolean insertIntoDB(Receipt receipt){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMP, receipt.getCompanyName());
        values.put(KEY_SUM, receipt.getPrice());
        values.put(KEY_SORTED, receipt.isUseful());
        values.put(KEY_DATE, receipt.getDate());

        db.insert(TABLE_RECEIPT, null, values);
        return true;
    }
    public ArrayList readFromDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPT;
        ArrayList<Receipt> list = new ArrayList<>();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            if (c.moveToFirst()){
                do{
                    Receipt r = new Receipt();
                    r.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                    r.setPrice(c.getInt(c.getColumnIndex(KEY_SUM)));
                    r.setCompanyName((c.getString(c.getColumnIndex(KEY_COMP))));
                    r.setDate(c.getColumnIndex(KEY_DATE));
                    if (c.getString(c.getColumnIndex(KEY_SORTED)) == "FALSE"){
                        r.setUseful(false);
                    }
                    else{
                        r.setUseful(true);
                    }
                    list.add(r);
                }while (c.moveToNext());
            }
        System.out.println(list.size());
        return list;
    }
    public ArrayList readSpecificFromDB(int a){
        SQLiteDatabase db = this.getReadableDatabase();
        int START_DATE;
        int END_DATE;

        START_DATE = calcDate(a);
        END_DATE = calcDate(0);
        if (a != 1){
            END_DATE -= 1;
        }

        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPT + " WHERE " + KEY_DATE + " BETWEEN " + START_DATE + " AND " + END_DATE;
        ArrayList<Receipt> list = new ArrayList<>();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()){
            do{
                Receipt r = new Receipt();
                r.setPrice(c.getInt(c.getColumnIndex(KEY_SUM)));
                r.setCompanyName((c.getString(c.getColumnIndex(KEY_COMP))));
                r.setDate(c.getColumnIndex(KEY_DATE));
                if (c.getString(c.getColumnIndex(KEY_SORTED)) == "FALSE"){
                    r.setUseful(false);
                }
                else{
                    r.setUseful(true);
                }
                list.add(r);
            }while (c.moveToNext());
        }
        System.out.println("PEETER " + list.size() + " " + a);
        return list;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public int calcDate(int margin){
        Calendar cal0 = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();

        cal0.set(2015, 01, 01);
        if (margin == 1){
            cal1.add(Calendar.DAY_OF_MONTH, -1);
        }
        else if (margin == 2){
            cal1.add(Calendar.WEEK_OF_MONTH, -1);
        }
        else if (margin == 3){
            cal1.add(Calendar.MONTH, -1);
        }

        return (int)((cal1.getTime().getTime() - cal0.getTime().getTime())/(1000 * 60 * 60 * 24));
    }

    public boolean assignSorted(int id, Receipt receipt){
        ContentValues values = new ContentValues();
        values.put(KEY_ID, receipt.getCompanyName());
        values.put(KEY_COMP, receipt.getCompanyName());
        values.put(KEY_SUM, receipt.getPrice());
        values.put(KEY_SORTED, true);
        values.put(KEY_DATE, receipt.getDate());

        this.getReadableDatabase().update(TABLE_RECEIPT, values, KEY_ID, null);
        return true;
    }
}