package cl.snatch.snatch.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.adapters.ContactsAdapter;
import cl.snatch.snatch.adapters.FriendsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACTS_LOADER_ID = 1;
    RecyclerView list;
    ContactsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    public static ContactsFragment newInstance(int sectionNumber) {
        ContactsFragment fragment = new ContactsFragment();
        /*Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);*/
        return fragment;
    }

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new ContactsAdapter();

        // Inflate the layout for this fragment if needed
        //JSONArray contactsSnatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");
        getLoaderManager().initLoader(CONTACTS_LOADER_ID,
                null,
                this);/* else {
            // load contacts from parse
            List<String[]> contacts = new ArrayList<>();
            for (int i = 0; i < contactsSnatched.length(); i++) {
                try {
                    JSONObject contact = contactsSnatched.getJSONObject(i);
                    String name = contact.getString("firstName") + (contact.has("lastName") ? contact.getString("lastName") : "");
                    String phone = contact.getString("phoneNumber");
                    String type = (String) ContactsContract.CommonDataKinds.Phone
                            .getTypeLabel(getResources(), ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, "");
                    contacts.add(new String[] {name, phone, type});
                } catch (JSONException e) {
                    Log.d("cl.snatch.snatch", "json error: " + e.getMessage());
                }
            }
            adapter.updateContacts(contacts);
        }*/
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.

        if (id == CONTACTS_LOADER_ID) {
            return contactsLoader();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //The framework will take care of closing the
        // old cursor once we return.
        adapter.updateContacts(contactsFromCursor(cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    private Loader<Cursor> contactsLoader() {
        Uri contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // The content URI of the phone contacts

        String[] projection = {                                  // The columns to return for each row
                Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
        } ;

        String selection = Contacts.HAS_PHONE_NUMBER;                                 //Selection criteria
        String[] selectionArgs = {};                             //Selection criteria
        String sortOrder = Contacts.DISPLAY_NAME + " ASC";                                 //The sort order for the returned rows

        return new CursorLoader(
                getActivity().getApplicationContext(),
                contactsUri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    private List<String[]> contactsFromCursor(Cursor cursor) {
        List<String[]> contacts = new ArrayList<>();
        JSONArray snatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");
        Set<String> phones = new HashSet<>(snatched.length());
        for (int i = 0; i < snatched.length(); i++) {
            try {
                Object s = snatched.get(i);
                JSONObject snatch;
                if (s instanceof JSONObject) snatch = (JSONObject) s;
                else /*if (s instanceof Map)*/ snatch = new JSONObject((Map) s);
                phones.add(snatch.getString("phoneNumber"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String type = (String) ContactsContract.CommonDataKinds.Phone
                        .getTypeLabel(getResources(), cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)), "");
                String hidden = !phones.contains(number) ? "true" : "false";
                contacts.add(new String[] {name, number, type, hidden});
            } while (cursor.moveToNext());
        }

        return contacts;
    }
}
