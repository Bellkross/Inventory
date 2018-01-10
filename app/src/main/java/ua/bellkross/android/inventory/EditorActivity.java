package ua.bellkross.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ua.bellkross.android.inventory.data.ProductContract;

import static ua.bellkross.android.inventory.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;
    private boolean mEdit;

    private boolean mProductHasChanged = false;

    private static final int PRODUCT_LOADER = 0;

    private EditText mEditTextName;
    private EditText mEditTextPrice;
    private EditText mEditTextDescription;
    private EditText mEditTextCount;
    private Button mButtonPlus;
    private Button mButtonMinus;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mUri = getIntent().getData();
        mEdit = (mUri != null);

        if (mEdit) {
            getSupportActionBar().setTitle(R.string.edit_product);
        } else {
            getSupportActionBar().setTitle(R.string.new_product);
            mUri = ProductContract.ProductEntry.CONTENT_URI;
        }

        mEditTextName = findViewById(R.id.et_name);
        mEditTextPrice = findViewById(R.id.et_price);
        mEditTextDescription = findViewById(R.id.et_description);
        mEditTextCount = findViewById(R.id.tv_count);
        mButtonPlus = findViewById(R.id.plus);
        mButtonMinus = findViewById(R.id.minus);


        mEditTextName.setOnTouchListener(mTouchListener);
        mEditTextPrice.setOnTouchListener(mTouchListener);
        mEditTextDescription.setOnTouchListener(mTouchListener);
        mButtonPlus.setOnTouchListener(mTouchListener);
        mButtonMinus.setOnTouchListener(mTouchListener);

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countValue = mEditTextCount.getText().toString().trim();
                int count = Integer.parseInt(countValue);
                mEditTextCount.setText(String.valueOf(++count));

            }
        });

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countValue = mEditTextCount.getText().toString().trim();
                int count = Integer.parseInt(countValue);
                mEditTextCount.setText((--count) < 0 ? "0" : String.valueOf(count));

            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void insertProduct() {
        Integer count = Integer.parseInt(mEditTextCount.getText().toString().trim());
        String name = mEditTextName.getText().toString().trim();
        Integer price = Integer.parseInt(mEditTextPrice.getText().toString().trim());
        String description = mEditTextDescription.getText().toString().trim();

        if((float)(count/10000)>1){
            count = 9999;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.NAME, name.isEmpty() ? "unnamed" : name);
        values.put(ProductEntry.COUNT, String.valueOf(count < 0 ? 0 : count));
        values.put(ProductEntry.PRICE, String.valueOf(price < 0 ? 0 : price));
        values.put(ProductEntry.DESCRIPTION, description.isEmpty() ? "-" : description);

        if (mEdit) {
            String id = String.valueOf(ContentUris.parseId(mUri));
            String selectionClause = ProductEntry._ID + " = ?";
            String[] selectionArgs = {id};
            int rowsUpdated = 0;

            rowsUpdated = getContentResolver().update(
                    Uri.withAppendedPath(ProductEntry.CONTENT_URI, "/" + id),
                    values,
                    selectionClause,
                    selectionArgs);

            if (rowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful) +
                                " with id " + id,
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            Uri newUri;
            newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful) +
                                "with id " + ContentUris.parseId(newUri),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                insertProduct();
                finish();
                return true;
            case R.id.action_delete:
                if (mEdit) {
                    String id = "" + ContentUris.parseId(mUri);
                    String selectionClause = ProductEntry._ID + " = ?";
                    String[] selectionArgs = {id};
                    getContentResolver().delete(mUri, selectionClause, selectionArgs);
                    finish();
                }
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
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
                mUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Integer count = 0;
        String name = "";
        Integer price = 0;
        String description = "";
        if (cursor.moveToFirst() && mEdit) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COUNT));
            price = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.PRICE));
            name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.NAME));
            description = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.DESCRIPTION));
        }
        mEditTextName.setText(name);
        mEditTextPrice.setText(String.valueOf(price));
        mEditTextDescription.setText(description);
        mEditTextCount.setText(String.valueOf(count));
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!mEdit) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditTextName.setText("");
        mEditTextPrice.setText("");
        mEditTextDescription.setText("");
        mEditTextCount.setText("");
    }
}
