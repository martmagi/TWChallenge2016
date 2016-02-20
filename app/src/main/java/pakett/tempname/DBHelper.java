package pakett.tempname;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

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
            + " VARCHAR(50), " + KEY_COMP + " VARCHAR(50), " + KEY_SORTED
            + " BOOLEAN, " + KEY_SUM + " INT" + ")";

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
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
        values.put(KEY_DATE, String.valueOf(receipt.getDate()));

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
                    r.setPrice(c.getInt(c.getColumnIndex(KEY_SUM)));
                    r.setCompanyName((c.getString(c.getColumnIndex(KEY_COMP))));
                    r.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                    if (c.getString(c.getColumnIndex(KEY_SORTED)) == "FALSE"){
                        r.setUseful(false);
                    }
                    else{
                        r.setUseful(true);
                    }
                    list.add(r);
                }while (c.moveToNext());
            }
        return list;
    }
}
