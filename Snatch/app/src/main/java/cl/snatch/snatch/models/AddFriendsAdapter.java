package cl.snatch.snatch.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.activities.SnatchActivity;
import cl.snatch.snatch.helpers.RoundCornersTransformation;

public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.ViewHolder> {

    public static final String SMS_MESSAGE = "Hey! I'm using Jumpster! http://jumpsterapp.com";
    private List<ParseObject> friends = new ArrayList<>(500); //todo: magic number
    private Context context;

    public AddFriendsAdapter() {}

    @Override
    public AddFriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_add_friend, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final AddFriendsAdapter.ViewHolder holder, int position) {
        final ParseObject parseUser = friends.get(position);

        if (parseUser.has("sms") && !parseUser.getBoolean("sms")) {
            ParseQuery<ParseObject> hasSent = ParseQuery.getQuery("Friend");
            hasSent.fromPin("FriendRequests");
            hasSent.whereEqualTo("toNumber", parseUser.getString("phoneNumber").replaceAll(" ", ""));
            hasSent.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null) {
                        // request sent
                        holder.befriend.setEnabled(false);
                        holder.befriend.setText("Req. sent");
                        holder.sms.setVisibility(View.GONE);
                    } else {
                        holder.befriend.setVisibility(View.VISIBLE);
                        holder.sms.setVisibility(View.GONE);
                        holder.befriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.befriend.setEnabled(false);
                                holder.befriend.setText("Req. sent");

                                ParseQuery<ParseUser> befriend = ParseUser.getQuery();
                                befriend.whereEqualTo("phoneNumber", parseUser.getString("phoneNumber").replaceAll(" ", ""));
                                befriend.getFirstInBackground(new GetCallback<ParseUser>() {
                                    @Override
                                    public void done(ParseUser p, ParseException e) {
                                        if (e == null) {
                                            ParseObject request = new ParseObject("Friend");
                                            request.put("from", ParseUser.getCurrentUser().getObjectId());
                                            request.put("to", p.getObjectId());
                                            request.put("status", "pending");
                                            request.saveInBackground();
                                            request.put("toNumber", p.getString("phoneNumber"));
                                            request.pinInBackground("FriendRequests");
                                        } else {
                                            Log.d("cl.snatch.snatch", "error: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
        } else {
            holder.sms.setVisibility(View.VISIBLE);
            holder.befriend.setVisibility(View.GONE);
            holder.sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);

                        intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(parseUser.getString("phoneNumber"))));
                        intent.putExtra("sms_body", SMS_MESSAGE);

                        if (defaultSmsPackageName != null) {
                            // Can be null in case that there is no default, then the user would be able to choose any app that supports this intent.
                            intent.setPackage(defaultSmsPackageName);
                        }
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setType("vnd.android-dir/mms-sms");
                        intent.putExtra("address", parseUser.getString("phoneNumber"));
                        intent.putExtra("sms_body", SMS_MESSAGE);
                    }
                    context.startActivity(intent);
                }
            });
        }

        holder.name.setText(parseUser.getString("fullName"));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    /*public void updateFriends(List<ParseUser> friends) {
        this.friends.addAll(friends);
        notifyItemRangeInserted(0, friends.size());
    }*/

    public void addFriend(int i, ParseObject p) {
        //this.friends.add(i, p);
        //notifyItemInserted(i);
        this.friends.add(p);
        notifyItemInserted(this.friends.size()-1);
    }

    public void addFriends(List<ParseObject> friends) {
        int from = this.friends.size();
        for (ParseObject o : friends) {
            o.put("sms", true);
        }
        this.friends.addAll(friends);
        notifyItemRangeInserted(from, friends.size());
    }

    public void addUsers(List<ParseUser> friends) {
        int from = this.friends.size();
        for (ParseObject o : friends) {
            o.put("sms", false);
        }
        this.friends.addAll(friends);
        notifyItemRangeInserted(from, friends.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        public View container;
        public Button befriend;
        public Button sms;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            container = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            sms = (Button) itemView.findViewById(R.id.sms);
            befriend = (Button) itemView.findViewById(R.id.befriend);
        }
    }

}
