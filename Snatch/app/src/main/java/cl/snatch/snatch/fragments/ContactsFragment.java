package cl.snatch.snatch.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.ContactsAdapter;
import cl.snatch.snatch.models.ContactsLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends ListFragment implements ContactsLoader.LoadFinishedCallback {

    ContactsAdapter adapter = new ContactsAdapter();
    private SwipeRefreshLayout swipe;
    private static final int CONTACTS_LOADER_ID = 1;

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



        /*swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(true);
                getActivity().getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,
                        null,
                        new ContactsLoader(ContactsFragment.this));
            }
        });*/

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get contacts

        // todo: save in localdatastore
        ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
        getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
        getContacts.orderByAscending("firstName");
        getContacts.addDescendingOrder("lastName");
        getContacts.fromLocalDatastore();
        getContacts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    adapter.updateContacts(parseObjects);
                    Log.d("cl.snatch.snatch", "catched contacts: " + String.valueOf(parseObjects.size()));
                } else {
                    Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onLoadFinished(Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                final String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                // upload to parse if new
                final ParseQuery<ParseObject> isNew = ParseQuery.getQuery("Contact");
                //isNew.fromPin("myContacts");
                isNew.whereEqualTo("phoneNumber", number.replaceAll(" ", ""));
                isNew.whereEqualTo("owner", ParseUser.getCurrentUser());
                isNew.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject == null) {
                            Log.d("cl.snatch.snatch", "agregando " + name);
                            ParseObject contact = new ParseObject("Contact");
                            contact.put("firstName", name.split(" ")[0]);
                            try {
                                contact.put("lastName", name.split(" ")[1]);
                            } catch (ArrayIndexOutOfBoundsException er) {
                                contact.put("lastName", name.split(" ")[0]);
                            }
                            contact.put("fullName", name);
                            contact.put("hidden", false);
                            contact.put("phoneNumber", number.replaceAll(" ", ""));
                            contact.put("owner", ParseUser.getCurrentUser());
                            contact.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                            contact.saveInBackground();
                            adapter.addContact(contact);
                        } else {
                            Log.d("cl.snatch.snatch", "exists: " + parseObject.getString("fullName") + " " + parseObject.getString("owner") + " " + parseObject.getString("phoneNumber"));
                        }
                    }
                });
            } while (cursor.moveToNext());
        }

        swipe.setRefreshing(false);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
