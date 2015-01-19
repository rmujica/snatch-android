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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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

        Log.d("cl.snatch.snatch", "u: " + user[0] + " ac: " + user[3]);

        holder.name.setText(user[0] + " (" + user[2] + ")");
        holder.numbers.setText(user[1]);
        holder.snatched.setChecked(!Boolean.parseBoolean(user[3]));
        holder.snatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isChecked = holder.snatched.isChecked();
                if (isChecked) {
                    // send contact
                    ParseObject contact = new ParseObject("Contact");
                    contact.put("firstName", user[0].split(" ")[0]);
                    try {
                        contact.put("lastName", user[0].split(" ")[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        contact.put("lastName", user[0].split(" ")[0]);
                    }
                    contact.put("fullName", user[0]);
                    contact.put("hidden", false);
                    contact.put("phoneNumber", user[1]);
                    contact.put("owner", ParseUser.getCurrentUser());
                    contact.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                    contact.saveInBackground();
                } else {
                    // remove contact
                    Log.d("cl.snatch.snatch", "removing contact");
                    ParseQuery<ParseObject> hideContact = ParseQuery.getQuery("Contact");
                    hideContact.whereEqualTo("phoneNumber", user[1]);
                    hideContact.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            parseObject.put("hidden", true);
                            parseObject.saveInBackground();
                        }
                    });
                }
                String[] newUser = new String[]{user[0], user[1], user[2], String.valueOf(!isChecked)};
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
