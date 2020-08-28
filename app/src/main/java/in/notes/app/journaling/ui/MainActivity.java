package in.notes.app.journaling.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import in.notes.app.journaling.R;
import in.notes.app.journaling.adapters.NotesAdapter;
import in.notes.app.journaling.database.Note;
import in.notes.app.journaling.database.NoteDatabase;
import in.notes.app.journaling.listeners.NotesListener;

import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_NOTE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_SHOW_NOTE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_UPDATE_NOTE;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private RecyclerView notesRecyclerView;
    private NotesAdapter adapter;
    private List<Note> notesList;
    private int noteClickedPosition = -1;

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
        adapter = new NotesAdapter(notesList,this);
        notesRecyclerView.setAdapter(adapter);

        getNotes(REQUEST_CODE_SHOW_NOTE,false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(notesList.size()!= 0){
                    adapter.searchNotes(s.toString());
                }
            }
        });
    }

    private void getNotes(final int requestcode,final boolean isNoteDeleted){
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
                if(requestcode == REQUEST_CODE_SHOW_NOTE){
                    notesList.addAll(notes);
                    adapter.notifyDataSetChanged();
                }else if(requestcode == REQUEST_CODE_ADD_NOTE){
                    notesList.add(0,notes.get(0));
                    adapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if(requestcode == REQUEST_CODE_UPDATE_NOTE){
                    notesList.remove(noteClickedPosition);
                    if (isNoteDeleted)
                        adapter.notifyItemRemoved(noteClickedPosition);
                    else {
                        notesList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        adapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null)
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
        }
    }

    @Override
    public void onNoteClicked(Note note, int pos) {
        noteClickedPosition = pos;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }
}