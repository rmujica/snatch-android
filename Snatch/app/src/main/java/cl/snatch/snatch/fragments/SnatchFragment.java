package cl.snatch.snatch.fragments;

import android.content.ContentProviderOperation;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.adapters.ContactsAdapter;
import cl.snatch.snatch.adapters.SnatchingAdapter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class SnatchFragment extends Fragment {

    RecyclerView list;
    SnatchingAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    public static SnatchFragment newInstance() {
        return new SnatchFragment();
    }

    public SnatchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new SnatchingAdapter();

        // Inflate the layout for this fragment if needed
        View rootView = inflater.inflate(R.layout.fragment_snatch, container, false);
        list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);
        EditText query = (EditText) rootView.findViewById(R.id.query);
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                JSONArray jFriends = ParseUser.getCurrentUser().getJSONArray("friends");
                Set<String> friends = new HashSet<>(jFriends.length());
                for (int i = 0; i < jFriends.length(); i++) {
                    try {
                        friends.add(jFriends.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ParseQuery<ParseObject> searchFirstNameL = ParseQuery.getQuery("Contact");
                searchFirstNameL.whereStartsWith("firstName", s.toString().toLowerCase());
                ParseQuery<ParseObject> searchFirstNameU = ParseQuery.getQuery("Contact");
                searchFirstNameU.whereStartsWith("firstName", s.toString().toUpperCase());
                ParseQuery<ParseObject> searchFirstNameF = ParseQuery.getQuery("Contact");
                searchFirstNameF.whereStartsWith("firstName", s.toString().substring(0, 1).toUpperCase() + s.toString().substring(1).toLowerCase());
                ParseQuery<ParseObject> searchLastNameF = ParseQuery.getQuery("Contact");
                searchLastNameF.whereStartsWith("lastName", s.toString().substring(0, 1).toUpperCase() + s.toString().substring(1).toLowerCase());
                ParseQuery<ParseObject> searchLastNameL = ParseQuery.getQuery("Contact");
                searchLastNameL.whereStartsWith("lastName", s.toString().toLowerCase());
                ParseQuery<ParseObject> searchLastNameU = ParseQuery.getQuery("Contact");
                searchLastNameU.whereStartsWith("lastName", s.toString().toUpperCase());

                ArrayList<ParseQuery<ParseObject>> search = new ArrayList<>();
                search.add(searchFirstNameL);
                search.add(searchFirstNameU);
                search.add(searchFirstNameF);
                search.add(searchLastNameL);
                search.add(searchLastNameU);
                search.add(searchLastNameF);

                ParseQuery<ParseObject> mainSearch = ParseQuery.or(search);
                mainSearch.whereContainedIn("ownerId", friends);
                mainSearch.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> search, ParseException e) {
                        if (e == null) {
                            adapter.replaceContacts(search);
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button snatch = (Button) rootView.findViewById(R.id.snatch);
        snatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<ParseObject> checked = adapter.getChecked();
                for (ParseObject user : checked) addUserToPhonebook(user);
                Toast.makeText(getActivity(), getResources().getString(R.string.contacts_snatched), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void addUserToPhonebook(ParseObject user) {
        // create new contact using object
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        user.getString("fullName")).build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        user.getString("phoneNumber"))
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}