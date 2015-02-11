package cl.snatch.snatch.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.AddFriendsAdapter;
import cl.snatch.snatch.models.FriendRequestAdapter;

public class FriendRequestsActivity extends ActionBarActivity {

    private static final int CONTACTS_LOADER_ID = 1;
    RecyclerView list;
    FriendRequestAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        // recyclerview setup
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new FriendRequestAdapter(/*list*/);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Friend");
        getContacts.whereEqualTo("to", ParseUser.getCurrentUser().getObjectId());
        getContacts.whereEqualTo("status", "pending");
        getContacts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (final ParseObject p : parseObjects) {
                        ParseQuery<ParseUser> isInSnatch = ParseUser.getQuery();
                        isInSnatch.whereEqualTo("objectId", p.getString("from"));
                        isInSnatch.getFirstInBackground(new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser u, ParseException e) {
                                if (e == null) {
                                    adapter.addFriend(u);
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
