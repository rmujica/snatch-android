package cl.snatch.snatch.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
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
import cl.snatch.snatch.helpers.EmptyRecyclerView;
import cl.snatch.snatch.models.FriendsAdapter;

public class FriendsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    //private static final String ARG_SECTION_NUMBER = "section_number";
    EmptyRecyclerView list;
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
        list = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new FriendsAdapter(/*list*/);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);
        list.setEmptyView(rootView.findViewById(R.id.empty));

        // get friend list
        final ArrayList<String> friends =
                new ArrayList<>(ParseUser.getCurrentUser().getList("friends").size());
        friends.addAll(ParseUser.getCurrentUser().<String>getList("friends"));

        Log.d("cl.snatch.snatch", "friends: " + ParseUser.getCurrentUser().getList("friends").toString());

        // getting friend data
        // todo: save in localdatastore
        // on friend add, fetch & pin friend
        ParseQuery<ParseUser> getFriends = ParseUser.getQuery();
        getFriends.whereContainedIn("objectId", friends);
        getFriends.orderByAscending("firstName");
        getFriends.addAscendingOrder("lastName");
        //getFriends.fromLocalDatastore();
        getFriends.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    adapter.updateFriends(parseUsers);
                } else {
                    Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error loading friends: " + e.getMessage());
                }
            }
        });




        return rootView;
    }
}
