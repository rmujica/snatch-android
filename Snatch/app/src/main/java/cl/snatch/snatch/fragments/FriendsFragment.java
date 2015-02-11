package cl.snatch.snatch.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.FriendsAdapter;

public class FriendsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    //private static final String ARG_SECTION_NUMBER = "section_number";
    RecyclerView list;
    FriendsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    public FriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        // recyclerview setup
        list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new FriendsAdapter(/*list*/);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        // get friend list
        // todo: use friends class
        // todo: pin
        Log.d("cl.snatch.snatch", "current friends: " + String.valueOf(ParseUser.getCurrentUser().getJSONArray("friends")));
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                JSONArray jsonFriends = parseObject.getJSONArray("friends");
                Log.d("cl.snatch.snatch", "oid: " +parseObject.getObjectId() + " " + parseObject.getUpdatedAt().toString());

                Log.d("cl.snatch.snatch", "friends l: " + String.valueOf(jsonFriends.length()));
                final ArrayList<String> friends = new ArrayList<>(jsonFriends.length());
                for (int i = 0; i < jsonFriends.length(); i++) {
                    try {
                        Log.d("cl.snatch.snatch", "friend: " + jsonFriends.getString(i));
                        friends.add(jsonFriends.getString(i));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                // getting friend data
                ParseQuery<ParseUser> getFriends = ParseUser.getQuery();
                getFriends.whereContainedIn("objectId", friends);
                getFriends.orderByAscending("firstName");
                getFriends.addAscendingOrder("lastName");
                getFriends.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(final List<ParseUser> parseUsers, ParseException e) {
                        if (e == null) {
                    /*if (parseUsers.size() == 0) {
                        // update local datastore
                        ParseQuery<ParseUser> findUsers = ParseUser.getQuery();
                        findUsers.whereContainedIn("objectId", friends);
                        findUsers.orderByAscending("firstName");
                        findUsers.addAscendingOrder("lastName");
                        findUsers.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(final List<ParseUser> newUsers, ParseException e) {
                                if (e == null) {
                                    adapter.updateFriends(parseUsers);
                                    ParseUser.unpinAllInBackground("friends", new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                ParseUser.pinAllInBackground("friends", newUsers);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }*/
                            Log.d("cl.snatch.snatch", "friends: " + parseUsers.toString());
                            adapter.updateFriends(parseUsers);
                        } else {
                            Log.d("cl.snatch.snatch", "error: " + e.getMessage());
                        }
                    }
                });
            }
        });


        return rootView;
    }
}
