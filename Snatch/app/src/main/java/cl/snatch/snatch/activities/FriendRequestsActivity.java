package cl.snatch.snatch.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.EmptyRecyclerView;
import cl.snatch.snatch.models.AddFriendsAdapter;
import cl.snatch.snatch.models.FriendRequestAdapter;

public class FriendRequestsActivity extends ActionBarActivity {

    EmptyRecyclerView list;
    FriendRequestAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    View pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);

        // recyclerview setup
        list = (EmptyRecyclerView) findViewById(R.id.list);
        list.setEmptyView(findViewById(R.id.empty));
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new FriendRequestAdapter(/*list*/);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

//        pb = findViewById(R.id.progressBar);

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
                                    //if (pb.getVisibility() == View.VISIBLE) pb.setVisibility(View.GONE);
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

        return super.onOptionsItemSelected(item);
    }
}
