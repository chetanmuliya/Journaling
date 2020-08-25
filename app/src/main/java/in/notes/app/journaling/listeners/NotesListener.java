package in.notes.app.journaling.listeners;

import android.view.View;

import in.notes.app.journaling.database.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int pos);
}
