package cl.snatch.snatch.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import cl.snatch.snatch.helpers.SlidingTabLayout;
import cl.snatch.snatch.models.SectionsPagerAdapter;
import cl.snatch.snatch.models.SnatchResultAdapter;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    SlidingTabLayout mSlidingTabLayout;

    RecyclerView list;
    SnatchResultAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                if (id == R.id.action_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.action_add_friend) {
                    Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                    startActivity(intent);
                } else if (id == R.id.action_friend_requests) {
                    Intent intent = new Intent(MainActivity.this, FriendRequestsActivity.class);
                    startActivity(intent);
                }

                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_main);
        adapter = new SnatchResultAdapter();
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem != null) {
            final SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Reach: 626");
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    list.setVisibility(View.GONE);
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (s.isEmpty()) {
                        adapter.replaceContacts(new ArrayList<ParseObject>());
                        list.setVisibility(View.GONE);
                        return false;
                    }
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
                    searchFirstNameL.whereStartsWith("firstName", s.toLowerCase());
                    ParseQuery<ParseObject> searchFirstNameU = ParseQuery.getQuery("Contact");
                    searchFirstNameU.whereStartsWith("firstName", s.toUpperCase());
                    ParseQuery<ParseObject> searchFirstNameF = ParseQuery.getQuery("Contact");
                    ParseQuery<ParseObject> searchLastNameF = ParseQuery.getQuery("Contact");
                    try {
                        searchFirstNameF.whereStartsWith("firstName", s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
                        searchLastNameF.whereStartsWith("lastName", s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
                    } catch (StringIndexOutOfBoundsException e) {
                        searchFirstNameF.whereStartsWith("firstName", s);
                        searchLastNameF.whereStartsWith("lastName", s);
                    }
                    ParseQuery<ParseObject> searchLastNameL = ParseQuery.getQuery("Contact");
                    searchLastNameL.whereStartsWith("lastName", s.toLowerCase());
                    ParseQuery<ParseObject> searchLastNameU = ParseQuery.getQuery("Contact");
                    searchLastNameU.whereStartsWith("lastName", s.toUpperCase());

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
                                list.setVisibility(View.VISIBLE);
                                adapter.replaceContacts(search);
                                Log.d("cl.snatch.snatch", "result: " + String.valueOf(search.size()));
                            } else {
                                Log.d("cl.snatch.snatch", "error: " + e.getMessage());
                            }
                        }
                    });
                    return false;
                }
            });
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.highlight));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return false;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

}
