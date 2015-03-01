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
import cl.snatch.snatch.helpers.EmptyRecyclerView;
import cl.snatch.snatch.models.AddFriendsAdapter;
import cl.snatch.snatch.models.ContactsLoader;
import cl.snatch.snatch.models.FriendsAdapter;

public class AddFriendActivity extends ActionBarActivity {
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

        final ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
        getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
        getContacts.setLimit(1000);
        ParseQuery<ParseUser> isInSnatch = ParseUser.getQuery();
        isInSnatch.whereMatchesKeyInQuery("phoneNumber", "phoneNumber", getContacts);
        isInSnatch.whereNotContainedIn("objectId", ParseUser.getCurrentUser().getList("friends"));
        isInSnatch.orderByAscending("firstName");
        isInSnatch.addAscendingOrder("lastName");
        isInSnatch.setLimit(1000);
        isInSnatch.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    adapter.addUsers(parseUsers);
                    ParseQuery<ParseUser> notInSnatch = ParseUser.getQuery();
                    getContacts.whereDoesNotMatchKeyInQuery("phoneNumber", "phoneNumber", notInSnatch);
                    getContacts.orderByAscending("firstName");
                    getContacts.addAscendingOrder("lastName");
                    getContacts.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (e == null) {
                                adapter.addFriends(parseObjects);
                            }
                        }
                    });
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

        return super.onOptionsItemSelected(item);
    }
}
