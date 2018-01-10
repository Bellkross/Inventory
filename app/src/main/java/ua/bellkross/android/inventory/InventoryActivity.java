package ua.bellkross.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import ua.bellkross.android.inventory.data.ProductContract;
import ua.bellkross.android.inventory.data.ProductCursorAdapter;
import static ua.bellkross.android.inventory.data.ProductContract.ProductEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    private ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                intent.setData(null);
                startActivity(intent);
            }
        });

        ListView displayView = findViewById(R.id.app_list_view);

        mCursorAdapter = new ProductCursorAdapter(this, null);

        displayView.setAdapter(mCursorAdapter);
        displayView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("logs","clicked");
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                intent.setData(Uri.withAppendedPath(ProductEntry.CONTENT_URI,""+id));
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(PET_LOADER, null, this);

    }

    private void insertProduct(){
        ContentValues v = new ContentValues();
        v.put(ProductEntry.COUNT,10);
        v.put(ProductEntry.NAME,"Apple iPhone X (10) 64Gb Space Gray");
        v.put(ProductEntry.PRICE,1220);
        v.put(ProductEntry.DESCRIPTION,"Компания Apple не перестает удивлять.");

        getContentResolver().insert(ProductEntry.CONTENT_URI, v);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_entries:
                int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                Log.v("InventoryActivity", rowsDeleted + " rows deleted from pet database");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COUNT,
                ProductEntry.NAME,
                ProductEntry.PRICE,
                ProductEntry.DESCRIPTION
        };
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
