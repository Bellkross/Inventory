package ua.bellkross.android.inventory.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ua.bellkross.android.inventory.R;

import static ua.bellkross.android.inventory.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    private Context mContext;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView count = view.findViewById(R.id.count);
        TextView name = view.findViewById(R.id.name);
        TextView price = view.findViewById(R.id.price);

        Button btnSell = view.findViewById(R.id.btn_sell);

        Integer countValue = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COUNT));
        String nameValue = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.NAME));
        Integer priceValue = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.PRICE));

        count.setText(String.valueOf(countValue));
        name.setText(nameValue);
        price.setText(String.valueOf(priceValue) + " $");

        final Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        final ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.NAME, nameValue);
        contentValues.put(ProductEntry.COUNT, (--countValue) < 0 ? 0 : countValue);
        contentValues.put(ProductEntry.PRICE, priceValue);
        contentValues.put(ProductEntry.DESCRIPTION,
                cursor.getColumnIndexOrThrow(ProductEntry.DESCRIPTION));

        final Uri uri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, String.valueOf(id));
        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getContentResolver().
                        update(uri, contentValues, null, null);
            }
        });


    }
}
