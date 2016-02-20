package pakett.tempname;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Karl on 20.02.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context){
        super(context,"expenseDB.db",null,1);
    }
    public void onCreate(SQLiteDatabase db) {}
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {}
}
