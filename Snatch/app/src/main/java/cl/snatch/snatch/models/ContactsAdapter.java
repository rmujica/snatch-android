package cl.snatch.snatch.models;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;

public class ContactsAdapter extends BaseAdapter implements SectionIndexer {

    private List<ParseObject> contacts = new ArrayList<>();
    private String[] sections;
    private HashMap<String, Integer> mapIndex = new HashMap<>();

    public void updateContacts(List<ParseObject> contacts) {
        this.contacts.addAll(contacts);

        for (int i = 0; i < contacts.size(); i++) {
            ParseObject user = contacts.get(i);
            String ch = user.getString("firstName").substring(0, 1).toUpperCase();
            mapIndex.put(ch, i);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ParseObject user = contacts.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.detail_contact, parent, false);

            holder = new ViewHolder();
            holder.snatched = (Switch) convertView.findViewById(R.id.snatched);
            holder.context = parent.getContext();
            holder.name = (TextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

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

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        return mapIndex.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    public static class ViewHolder  {
        public TextView name;
        public Context context;
        public Switch snatched;
    }
}
