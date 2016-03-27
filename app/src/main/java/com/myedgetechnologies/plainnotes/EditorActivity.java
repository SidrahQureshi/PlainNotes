package com.myedgetechnologies.plainnotes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editor;
    private String noteFilter;
    private String oldText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor=(EditText)findViewById(R.id.editText);
        //Eventually, when I add code to update an existing note, I'll be passing in a URI as an intent extra.
        // When the user presses the "+" button, I won't pass in that URI so I need to detect that state.
        // I'll get a reference to the current intent object. This is the intent that launched this activity.
        // Then, I'll declare a reference to an URI object.
        // I'll name it "uri," all lowercase, and get it from this expression intent.getParcelableExtra.

        Intent intent=getIntent();
        Uri uri =intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if(uri == null)
        {
            //insert a new note
            action=Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));//the default title is "edit note"
        }
        else
        {
            action=Intent.ACTION_EDIT;
            noteFilter=DBOpenHelper.NOTE_ID+"="+uri.getLastPathSegment();//where clause
            Cursor cursor=getContentResolver().query(uri,DBOpenHelper.ALL_COLUMNS,noteFilter,null,null);
            cursor.moveToFirst();
            oldText=cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            editor.setText(oldText);
            editor.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       switch (item.getItemId())
       {
           case android.R.id.home:
               finishEditing();
               break;
           case R.id.action_delete:
               deleteNote();
               break;
       }

        return true;
    }

    private void finishEditing() {
    String newText=editor.getText().toString().trim();//trim leadng or following white space
        switch  (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0)
                    setResult(RESULT_CANCELED);//sending msg back to main activity to cancel whatever operation was requested
                else
                    insertNote(newText);
                break;
            case Intent.ACTION_EDIT:
                if(newText.length()==0)
                {
                    deleteNote();
                }
                else if(oldText.equals(newText))
                {
                    setResult(RESULT_CANCELED);
                                    }
                else
                    updateNote(newText);

        }
        finish(); //finished with this activity so go back to parent activity
        }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this,getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()   ;
        setResult(RESULT_OK);
        finish();
    }

    private void updateNote(String noteText) {
        ContentValues values=new ContentValues();

        values.put(DBOpenHelper.NOTE_TEXT, noteText);

        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show()   ;
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) {
        ContentValues values=new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);

        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);//requested operation has been completed
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}

