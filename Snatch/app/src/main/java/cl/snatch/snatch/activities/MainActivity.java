package cl.snatch.snatch.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.codec.binary.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.EmptyRecyclerView;
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

    EmptyRecyclerView list;
    SnatchResultAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    String lastSearch = "";
    Handler changeHandler;
    TextView pb;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeHandler = new Handler();
        pb = (TextView) findViewById(R.id.empty);

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
        list = (EmptyRecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            searchView.setQueryHint("Reach: 0");
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
                public boolean onQueryTextChange(final String s) {
                    if (s.isEmpty() || s.equals("@")) {
                        lastSearch = "";
                        changeHandler.removeCallbacksAndMessages(null);
                        adapter.replaceContacts(new ArrayList<ParseObject>());
                        list.setVisibility(View.GONE);
                        pb.setVisibility(View.GONE);
                        return false;
                    }

                    list.setVisibility(View.VISIBLE);
                    list.setEmptyView(pb);
                    pb.setText(R.string.searching);
                    Log.d("cl.snatch.snatch", "strs: " + lastSearch + " " + s);

                    if (!lastSearch.equals(s)) {
                        changeHandler.removeCallbacksAndMessages(null);
                    } else {
                        return false;
                    }
                    lastSearch = s;
                    Log.d("cl.snatch.snatch", "delaying");
                    changeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("cl.snatch.snatch", "running delayed");

                            if (s.startsWith("@") && s.length() > 1) {
                                // commercial search
                                ParseQuery<ParseObject> search1 = ParseQuery.getQuery("Commercial");
                                search1.whereContains("name", s.substring(1));
                                ParseQuery<ParseObject> search2 = ParseQuery.getQuery("Commercial");
                                String s2 = s.substring(2);
                                s2 = s.substring(1, 2).toUpperCase() + s2;
                                search2.whereContains("name", s2);

                                ArrayList<ParseQuery<ParseObject>> ors = new ArrayList<>();
                                ors.add(search1);
                                ors.add(search2);

                                ParseQuery<ParseObject> search = ParseQuery.or(ors);
                                search.orderByAscending("name");
                                search.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> search, ParseException e) {
                                        if (e == null && !searchView.isIconified()) {
                                            adapter.replaceContacts(search);
                                            if (search.size() == 0) {
                                                pb.setText(getString(R.string.no_result));
                                            }
                                        }
                                    }
                                });
                            } else {
                                // normal search
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
                                mainSearch.whereEqualTo("hidden", false);
                                mainSearch.orderByAscending("firstName");
                                mainSearch.addAscendingOrder("lastName");
                                mainSearch.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> search, ParseException e) {
                                        if (e == null && !searchView.isIconified()) {
                                            adapter.replaceContacts(search);
                                            Log.d("cl.snatch.snatch", "result: " + String.valueOf(search.size()));
                                            if (search.size() == 0) {
                                                pb.setText(getString(R.string.no_result));
                                            }
                                        } else {
                                            Log.d("cl.snatch.snatch", "error: " + e.getMessage());

                                        }
                                    }
                                });
                            }
                        }
                    }, 1000);

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

    @Override
    public void onResume() {
        super.onResume();

        // fetch user
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    if (searchView != null) {
                        int i;
                        if (user.containsKey("reach") && user.getNumber("reach").intValue() >= 0) {
                            i = user.getNumber("reach").intValue();
                        } else {
                            i = 0;
                        }
                        searchView.setQueryHint(getString(R.string.reach_s) + String.valueOf(i));
                    }
                }
            }
        });
    }

}
