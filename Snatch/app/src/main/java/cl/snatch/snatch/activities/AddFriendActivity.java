package cl.snatch.snatch.activities;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.AddFriendsAdapter;
import cl.snatch.snatch.models.ContactsLoader;
import cl.snatch.snatch.models.FriendsAdapter;

public class AddFriendActivity extends ActionBarActivity /*implements ContactsLoader.LoadFinishedCallback*/ {

    private static final int CONTACTS_LOADER_ID = 1;
    RecyclerView list;
    AddFriendsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);

        // recyclerview setup
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new AddFriendsAdapter();
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        /*getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,
                null,
                new ContactsLoader(this));*/

        ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
        getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
        Log.d("cl.snatch.snatch", "user is: " + ParseUser.getCurrentUser().toString() + " id: " + ParseUser.getCurrentUser().getObjectId());
        getContacts.orderByAscending("firstName");
        getContacts.addAscendingOrder("lastName");
        // todo: URGENTE verificar bien condiciones para mostrar contactos
        getContacts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (final ParseObject p : parseObjects) {
                        ParseQuery<ParseUser> isInSnatch = ParseUser.getQuery();
                        isInSnatch.whereEqualTo("phoneNumber", p.getString("phoneNumber").replaceAll(" ", ""));
                        isInSnatch.getFirstInBackground(new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser u, ParseException e) {
                                if (e == null) {
                                    p.put("sms", false);
                                    ParseQuery<ParseObject> requestSent = ParseQuery.getQuery("Friend");
                                    requestSent.whereEqualTo("from", ParseUser.getCurrentUser().getObjectId());
                                    requestSent.whereEqualTo("to", u.getObjectId());
                                    requestSent.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject parseObject, ParseException e) {
                                            if (e == null) {
                                                Log.d("cl.snatch.snatch", "status: " + parseObject.getString("status"));
                                                p.put("fst", parseObject.getString("status"));
                                                if (parseObject.get("status").equals("accepted")) {
                                                    parseObjects.remove(p);
                                                }
                                            } else {
                                                Log.d("cl.snatch.snatch", "user is: " + ParseUser.getCurrentUser().toString() + " id: " + ParseUser.getCurrentUser().getObjectId());
                                                Log.d("cl.snatch.snatch", "status: " + e.getMessage() + " " + ParseUser.getCurrentUser().getObjectId() + " " + p.getObjectId());
                                            }
                                            if (!p.has("fst") || !p.getString("fst").equals("accepted")) {
                                                adapter.addFriend(parseObjects.indexOf(p), p);
                                            }
                                        }
                                    });
                                } else {
                                    p.put("sms", true);
                                    adapter.addFriend(parseObjects.indexOf(p), p);
                                }
                            }
                        });
                    }
                } else {
                    Log.d("cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onLoadFinished(final Cursor cursor) {
        // todo: iterar sobre contactos, ver quienes tienen snatch.


        // iterar cursor y ver qué contactos son agregables y cuales son sms-ables.
        final Set<String> contactNumbers = new HashSet<>(cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                contactNumbers.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            } while (cursor.moveToNext());
        }

        ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
        getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
        getContacts.orderByAscending("firstName");
        getContacts.addDescendingOrder("lastName");
        getContacts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject p : parseObjects) {
                        if (contactNumbers.contains(p.getString("phoneNumber"))) {
                            p.put("")
                        }
                    }
                } else {
                    Log.d("cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public Context getContext() {
        return this;
    }*/
}
