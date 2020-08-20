package in.notes.app.journaling.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import in.notes.app.journaling.R;
import in.notes.app.journaling.database.Note;
import in.notes.app.journaling.database.NoteDatabase;

import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_NOTE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(),CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        getNotes();
    }

    private void getNotes(){
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void,Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
               return  NoteDatabase.getNoteDatabase(getApplicationContext())
                        .noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                Log.d("MY_NOTES",  notes.toString());
            }
        }
        new GetNoteTask().execute();
    }
}