package in.notes.app.journaling.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.notes.app.journaling.R;
import in.notes.app.journaling.database.Note;
import in.notes.app.journaling.database.NoteDatabase;

import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_NOTE;

public class CreateNoteActivity extends AppCompatActivity {

    EditText inputNoteTitle,inputNoteSubTitle,inputNoteText;
    TextView dateTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubTitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        dateTimeText = findViewById(R.id.textDateTime);

        dateTimeText.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );
        //save note image icon
        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote(){
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note Title can't be empty!",Toast.LENGTH_LONG).show();
            return;
        }else if(inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note can't be empty!",Toast.LENGTH_LONG).show();
            return;
        }
        if(inputNoteSubTitle.getText().toString().trim().isEmpty()){
            inputNoteSubTitle.setText(" ");
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteTitle.getText().toString());
        note.setNoteText(inputNoteTitle.getText().toString());
        note.setDateTime(dateTimeText.getText().toString());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().insert(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }
}