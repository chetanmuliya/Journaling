package in.notes.app.journaling.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import in.notes.app.journaling.R;
import in.notes.app.journaling.adapters.NotesAdapter;
import in.notes.app.journaling.database.Note;
import in.notes.app.journaling.database.NoteDatabase;

import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_NOTE;

public class MainActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter adapter;
    private List<Note> notesList;

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

        notesRecyclerView = findViewById(R.id.notesRecyclerview);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        notesList = new ArrayList<>();
        adapter = new NotesAdapter(notesList);
        notesRecyclerView.setAdapter(adapter);

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
                if(notesList.size() == 0){
                    notesList.addAll(notes);
                    adapter.notifyDataSetChanged();
                }else {
                    notesList.add(0,notes.get(0));
                    adapter.notifyItemInserted(0);
                }
                notesRecyclerView.smoothScrollToPosition(0);
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes();
        }
    }
}