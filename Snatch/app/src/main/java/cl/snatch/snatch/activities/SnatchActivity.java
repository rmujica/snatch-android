package cl.snatch.snatch.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.adapters.ContactsAdapter;
import cl.snatch.snatch.adapters.SnatchingAdapter;

public class SnatchActivity extends ActionBarActivity {

    RecyclerView list;
    SnatchingAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snatch);

        // initialize list
        adapter = new SnatchingAdapter();
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        String userId = getIntent().getStringExtra("userId");
        ParseQuery<ParseUser> getUser = ParseUser.getQuery();
        getUser.whereEqualTo("objectId", userId);
        getUser.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                // add contacts
                adapter.updateContacts(parseUser.getJSONArray("contactsSnatched"));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_snatch, menu);
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
}
