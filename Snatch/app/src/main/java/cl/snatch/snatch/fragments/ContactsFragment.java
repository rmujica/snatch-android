package cl.snatch.snatch.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.ContactsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends ListFragment {

    ContactsAdapter adapter = new ContactsAdapter();

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment if needed
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setAdapter(adapter);

        // get contacts
        ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
        getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
        getContacts.orderByAscending("firstName");
        getContacts.addDescendingOrder("lastName");
        getContacts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    adapter.updateContacts(parseObjects);
                } else {
                    Log.d("cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                }
            }
        });

        return rootView;
    }
}
