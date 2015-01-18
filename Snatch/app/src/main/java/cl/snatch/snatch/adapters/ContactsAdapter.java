package cl.snatch.snatch.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.RoundCornersTransformation;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<String[]> contacts = new ArrayList<>();

    public ContactsAdapter() {}

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_contact, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
        final String[] user = contacts.get(position);

        holder.name.setText(user[0] + " (" + user[2] + ")");
        holder.numbers.setText(user[1]);
        holder.snatched.setChecked(!Boolean.parseBoolean(user[3]));
        holder.snatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // send snatch
                    JSONArray snatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");

                    Map<String, Object> newSnatch = new HashMap<>();
                    newSnatch.put("firstName", user[0].split(" ")[0]);
                    newSnatch.put("fullName", user[0]);
                    newSnatch.put("hidden", false);
                    newSnatch.put("phoneNumber", user[1]);

                    snatched.put(newSnatch);
                    ParseUser.getCurrentUser().put("contactsSnatched", snatched);
                    ParseUser.getCurrentUser().saveInBackground();
                } else {
                    // remove from snatch
                    Log.d("cl.snatch.snatch", "removing contact");
                    JSONArray snatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");
                    JSONArray newSnatches = new JSONArray();

                    for (int i = 0; i < snatched.length(); i++) {
                        try {
                            //JSONObject snatch = (JSONObject) snatched.get(i);
                            Object s = snatched.get(i);
                            JSONObject snatch;
                            if (s instanceof JSONObject) snatch = (JSONObject) s;
                            else /*if (s instanceof Map)*/ snatch = new JSONObject((Map) s);

                            if (!snatch.getString("phoneNumber").equals(user[1])) {
                                newSnatches.put(snatch);
                                Log.d("cl.snatch.snatch", snatch.getString("phoneNumber") + "!=" + user[1]);
                            } else {
                                Log.d("cl.snatch.snatch", user[0]);
                            }
                        } catch (JSONException e) {
                            Log.d("cl.snatch.snatch", "json error: " + e.getMessage());
                        }
                    }

                    ParseUser.getCurrentUser().put("contactsSnatched", newSnatches);
                    ParseUser.getCurrentUser().saveInBackground();
                }
                String[] newUser = new String[] {user[0], user[1], user[2], String.valueOf(!isChecked)};
                contacts.set(position, newUser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<String[]> contacts) {
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(0, contacts.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        public TextView numbers;
        public Switch snatched;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            name = (TextView) itemView.findViewById(R.id.name);
            numbers = (TextView) itemView.findViewById(R.id.numbers);
            snatched = (Switch) itemView.findViewById(R.id.snatched);
        }
    }
}
