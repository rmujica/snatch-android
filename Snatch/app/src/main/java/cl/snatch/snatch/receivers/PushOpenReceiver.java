package cl.snatch.snatch.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cl.snatch.snatch.activities.FriendRequestsActivity;
import cl.snatch.snatch.activities.SnatchActivity;

public class PushOpenReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushOpen(final Context context, final Intent intent) {
        try {
            final JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            if (json.has("friendId")) {
                Intent open = new Intent(context, SnatchActivity.class);
                try {
                    open.putExtra("userId", json.getString("friendId"));
                    open.putExtra("name", json.getString("friendName"));
                    if (!"".equals(json.getString("avatar")))
                        open.putExtra("avatar", json.getString("avatar"));
                    else
                        open.putExtra("avatar", "noavatar");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(open);
            } else {
                Intent open = new Intent(context, FriendRequestsActivity.class);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(open);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPushReceive(final Context context, final Intent intent) {
        try {
            final JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            if (json.has("friendId")) {
                // fetch friend
                final String id = json.getString("friendId");
                ParseQuery<ParseUser> newFriend = ParseUser.getQuery();
                newFriend.getInBackground(id, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        parseUser.pinInBackground("myFriends");
                        ParseUser.getCurrentUser().addUnique("friends", id);
                        ParseUser.getCurrentUser().saveEventually();
                    }
                });
            } else if (json.has("deletedFriend")) {
                // friend deleted, remove from pin
                final String id = json.getString("deletedFriend");
                Log.d("cl.snatch.snatch", "friend deleted");
                ParseQuery<ParseUser> newFriend = ParseUser.getQuery();
                newFriend.getInBackground(id, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        parseUser.unpinInBackground("myFriends");
                        Log.d("cl.snatch.snatch", "friend deleted: " + parseUser.getString("fullName"));
                        ArrayList<String> f = new ArrayList<>();
                        f.add(id);
                        ParseUser.getCurrentUser().removeAll("friends", f);
                        ParseUser.getCurrentUser().saveEventually();
                    }
                });
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        super.onPushReceive(context, intent);
    }

}
