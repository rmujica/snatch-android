package cl.snatch.snatch.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

    private List<ParseObject> friends = new ArrayList<>(500); //todo: magic number

    public AddFriendsAdapter() {}

    @Override
    public AddFriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_add_friend, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final AddFriendsAdapter.ViewHolder holder, int position) {
        final ParseObject parseUser = friends.get(position);

        if (parseUser.has("sms") && !parseUser.getBoolean("sms")) {
            Log.d("cl.snatch.snatch", "fst: " + parseUser.getString("fst"));
            if (parseUser.has("fst") && parseUser.getString("fst").equals("pending")) {
                holder.befriend.setEnabled(false);
                holder.befriend.setText("Req. sent");
            }
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
                            } else {
                                Log.d("cl.snatch.snatch", "error: " + e.getMessage());
                            }
                        }
                    });
                }
            });
        } else {
            holder.sms.setVisibility(View.VISIBLE);
            holder.befriend.setVisibility(View.GONE);
        }

        holder.name.setText(parseUser.getString("fullName"));
        /*Picasso.with(holder.context)
                .load(parseUser.getParseFile("profilePicture").getUrl())
                .transform(new RoundCornersTransformation())
                .into(holder.avatar);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.context, SnatchActivity.class);
                intent.putExtra("userId", parseUser.getObjectId());
                holder.context.startActivity(intent);
            }
        });*/
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
