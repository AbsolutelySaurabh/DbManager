package dexter.appsmoniac.androidsocket.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class BikesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Bike.db";
    public static final String BIKES_TABLE_NAME = "bikes";
    public static final String BIKES_COLUMN_ID = "id";
    public static final String BIKES_COLUMN_NAME = "name";
    public static final String BIKES_COLUMN_COLOR = "color";
    public static final String BIKES_COLUMN_MILEAGE = "mileage";

    public BikesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table bikes " +
                        "(id integer primary key, name text, color text, mileage real)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS bikes");
        onCreate(db);
    }

    public boolean insertBike(String name, String color, float mileage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("color", color);
        contentValues.put("mileage", mileage);
        db.insert("bikes", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from bikes where id=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, BIKES_TABLE_NAME);
        return numRows;
    }

    public boolean updateBike(Integer id, String name, String color, float mileage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("color", color);
        contentValues.put("mileage", mileage);
        db.update("bikes", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteBike(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("bikes",
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllBikes() {
        ArrayList<String> arrayList = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from bikes", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            arrayList.add(res.getString(res.getColumnIndex(BIKES_COLUMN_NAME)));
            res.moveToNext();
        }
        return arrayList;
    }

    public int count() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from bikes", null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } else {
            return 0;
        }
    }
}