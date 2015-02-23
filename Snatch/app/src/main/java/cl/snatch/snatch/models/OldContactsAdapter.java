package cl.snatch.snatch.models;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;

public class OldContactsAdapter extends RecyclerView.Adapter<OldContactsAdapter.ViewHolder> {

    private List<ParseObject> contacts = new ArrayList<>();

    public OldContactsAdapter() {}

    @Override
    public OldContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_contact, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final OldContactsAdapter.ViewHolder holder, final int position) {
        final ParseObject user = contacts.get(position);

        holder.name.setText(user.getString("fullName"));
        if (holder.snatched.isChecked()) {
            holder.name.setTextColor(Color.BLACK);
        } else {
            holder.name.setTextColor(Color.LTGRAY);
        }
        //holder.numbers.setText(user.getString("phoneNumber"));
        holder.snatched.setChecked(!user.getBoolean("hidden"));
        holder.snatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isChecked = holder.snatched.isChecked();
                if (isChecked) {
                    // show contact
                    user.put("hidden", false);
                    user.saveInBackground();
                } else {
                    // hide contact
                    user.put("hidden", true);
                    user.saveInBackground();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<ParseObject> contacts) {
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(0, contacts.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        //public TextView numbers;
        public Switch snatched;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            name = (TextView) itemView.findViewById(R.id.name);
            //numbers = (TextView) itemView.findViewById(R.id.numbers);
            snatched = (Switch) itemView.findViewById(R.id.snatched);
        }
    }
}