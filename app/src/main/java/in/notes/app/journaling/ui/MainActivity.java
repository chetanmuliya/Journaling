package in.notes.app.journaling.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.notes.app.journaling.R;
import in.notes.app.journaling.adapters.NotesAdapter;
import in.notes.app.journaling.database.Note;
import in.notes.app.journaling.database.NoteDatabase;
import in.notes.app.journaling.listeners.NotesListener;

import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_NOTE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_ADD_SELECT_IMAGE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_SELECT_IMAGE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_SHOW_NOTE;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_STORAGE_PERMISSION;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_STORAGE_PERMISSION_MAIN;
import static in.notes.app.journaling.constant.Constants.REQUEST_CODE_UPDATE_NOTE;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private RecyclerView notesRecyclerView;
    private NotesAdapter adapter;
    private List<Note> notesList;
    private int noteClickedPosition = -1;

    private AlertDialog dialogAddUrl;

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

        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(),CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermission();
            }
        });

        findViewById(R.id.imageAddWeblink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
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
        }else if(requestCode == REQUEST_CODE_ADD_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        String selectedImagePath= getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","image");
                        intent.putExtra("imagePath",selectedImagePath);
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_ADD_SELECT_IMAGE);
        }
    }

    private void getPermission() {
        if(ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION_MAIN
            );
        }else{
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION_MAIN && grantResults.length > 0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    //loading image from external file
    private String getPathFromUri(Uri contentUri){
        String filepath;
        Cursor cursor = getContentResolver()
                .query(contentUri,null,null,null,null);
        if (cursor == null){
            filepath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filepath = cursor.getString(index);
            cursor.close();
        }
        return filepath;
    }

    private void showAddUrlDialog(){
        if(dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if(dialogAddUrl.getWindow() != null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputurl = view.findViewById(R.id.inputUrl);
            inputurl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputurl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this,"Enter URL",Toast.LENGTH_SHORT).show();
                    }else if(!Patterns.WEB_URL.matcher(inputurl.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this,"Enter valid URL",Toast.LENGTH_SHORT).show();
                    }else{
                        dialogAddUrl.dismiss();
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","weblink");
                        intent.putExtra("url",inputurl.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                    dialogAddUrl = null;
                }
            });

            dialogAddUrl.show();
        }
    }
}