package cl.snatch.snatch.models;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;

public class SnatchingAdapter extends RecyclerView.Adapter<SnatchingAdapter.ViewHolder> {

    // todo: change to set? if it duplicates contacts
    private List<ParseObject> contacts = new ArrayList<>();
    private Set<ParseObject> checked = new HashSet<>();

    public SnatchingAdapter() {}

    @Override
    public SnatchingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_snatch, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final SnatchingAdapter.ViewHolder holder, final int position) {
        final ParseObject user = contacts.get(position);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.snatched.performClick();
            }
        });
        holder.name.setText(user.getString("fullName"));
        holder.numbers.setText(user.getString("phoneNumber"));
        holder.snatched.setChecked(checked.contains(user));
        holder.snatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.snatched.isChecked()) checked.add(user);
                else checked.remove(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addContacts(List<ParseObject> contacts) {
        int current = getItemCount();
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(current, contacts.size());
    }

    public void replaceContacts(List<ParseObject> contacts) {
        int current = getItemCount();
        this.contacts.clear();
        this.checked.clear();
        notifyItemRangeRemoved(0, current);
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(0, contacts.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        public TextView numbers;
        public CheckBox snatched;
        public View container;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            container = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            numbers = (TextView) itemView.findViewById(R.id.numbers);
            snatched = (CheckBox) itemView.findViewById(R.id.snatched);
        }
    }

    public Set<ParseObject> getChecked() {
        return checked;
    }
}
