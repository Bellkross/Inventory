package ua.bellkross.android.inventory.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ua.bellkross.android.inventory.data.ProductContract.ProductEntry;

public class ProductDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public ProductDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + " integer primary key autoincrement, " +
                ProductEntry.COUNT + " integer not null, " +
                ProductEntry.NAME + " text not null, " +
                ProductEntry.PRICE + " integer not null, " +
                ProductEntry.DESCRIPTION + " text not null);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
