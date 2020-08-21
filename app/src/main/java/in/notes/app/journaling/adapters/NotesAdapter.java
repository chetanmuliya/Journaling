package in.notes.app.journaling.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.notes.app.journaling.R;
import in.notes.app.journaling.database.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle,textSubtitle,textDateTime,textNote;
        LinearLayout layoutNote;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubTitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
        }

        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty())
                textSubtitle.setVisibility(View.GONE);
            else
                textSubtitle.setText(note.getSubtitle());
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null)
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            else
                gradientDrawable.setColor(Color.parseColor("#333333"));
        }
    }
}
