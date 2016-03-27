package com.myedgetechnologies.plainnotes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final int EDITOR_REQUEST_CODE =3002 ;
    private CursorAdapter cursorAdapter;
   // @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //insertNote("new note");


        //Cursor cursor=getContentResolver().query(NotesProvider.CONTENT_URI,DBOpenHelper.ALL_COLUMNS,null,null,null,null);

        cursorAdapter = new NotesCursorAdapter(this,null,0);

        ListView list=(ListView)findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // First, I'll create an intent object that says I want to go to the editor activity.
                // I'll construct the intent object, and pass in MainActivity.this.
                // I can't just pass in this here because this would refer to the ClickListener object, so I prefix it with the parent class.
                // And then, I'll pass in the class for EditorActivity with EditorActivity.class.
                Intent intent=new Intent(MainActivity.this,EditorActivity.class);
                //create a uri object that represents the primary key value of the currently selected item from the list.
                Uri uri=Uri.parse(NotesProvider.CONTENT_URI+"/"+id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE,uri);
                startActivityForResult(intent,EDITOR_REQUEST_CODE);


            }
        });

        getLoaderManager().initLoader(0, null, this);//initialize loader and use 'this' class to manage the loader
    }

    private void insertNote(String noteText) {
        ContentValues values=new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);

        Uri noteUri=getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        Log.d("MainActivity", "Inserted note" + noteUri.getLastPathSegment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_create_sample:
                insertSampleData();
                break;

            case R.id.action_delete_all:
                deleteAllNotes();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(NotesProvider.CONTENT_URI,null,null);
                            restartLoader();

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();


    }

    private void insertSampleData() {
        insertNote("simple note");
        insertNote("multi-line\n text");
        insertNote("veryyyy long note with a lotttttttttt of textttttttt that exxceeds the width of screen");
        //re-read data from DB
        restartLoader();

    }

    private void restartLoader() {
        //re-read data from DB
        getLoaderManager().restartLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //When you create the cursor loader object it executes the Query method on the background thread.
        return new CursorLoader(this,NotesProvider.CONTENT_URI,null,null,null,null);//Projection=list of columns-already coded in provider, selection=null means get all data
    }

    @Override
    //When you create the cursor loader object it executes the Query method on the background thread.
    //And when the data comes back, Onload Finished is called for you.
    //Your job is to take the data represented by the cursor object, named data, and pass it to the cursor adaptor
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    // The Onloader Reset method is called whenever the data needs to be wiped out.
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void openEditorForNewNote(View view) {
        Intent intent=new Intent(this,EditorActivity.class);
        startActivityForResult(intent,EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==EDITOR_REQUEST_CODE&& resultCode==RESULT_OK)
        {
restartLoader();
        }
    }
}
