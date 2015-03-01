package cl.snatch.snatch.receivers;

import android.content.Context;
import android.content.Intent;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import cl.snatch.snatch.activities.FriendRequestsActivity;
import cl.snatch.snatch.activities.SnatchActivity;

public class PushOpenReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            if (json.has("friendId")) {
                Intent open = new Intent(context, SnatchActivity.class);
                open.putExtra("userId", json.getString("friendId"));
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(open);

                // fetch friend
                ParseQuery<ParseUser> newFriend = ParseUser.getQuery();
                newFriend.getInBackground(json.getString("friendId"), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        parseUser.pinInBackground("myFriends");
                    }
                });
            } else {
                Intent open = new Intent(context, FriendRequestsActivity.class);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(open);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
