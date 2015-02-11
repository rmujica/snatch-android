package cl.snatch.snatch.models;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<ParseObject> friends = new ArrayList<>();

    public FriendRequestAdapter() {}

    @Override
    public FriendRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_friend_request, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final FriendRequestAdapter.ViewHolder holder, int position) {
        final ParseObject parseUser = friends.get(position);
        holder.name.setText(parseUser.getString("fullName"));
        holder.number.setText(parseUser.getString("phoneNumber"));
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> acceptFriend = ParseQuery.getQuery("Friend");
                acceptFriend.whereEqualTo("from", parseUser.getObjectId());
                acceptFriend.whereEqualTo("to", ParseUser.getCurrentUser().getObjectId());
                acceptFriend.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            parseObject.put("status", "accepted");
                            parseObject.saveInBackground();
                            int i = friends.indexOf(parseUser);
                            friends.remove(parseUser);
                            notifyItemRemoved(i);
                        }
                    }
                });
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> acceptFriend = ParseQuery.getQuery("Friend");
                acceptFriend.whereEqualTo("from", parseUser.getObjectId());
                acceptFriend.whereEqualTo("to", ParseUser.getCurrentUser().getObjectId());
                acceptFriend.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            parseObject.deleteInBackground();
                            int i = friends.indexOf(parseUser);
                            friends.remove(parseUser);
                            notifyItemRemoved(i);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    /*public void updateFriends(List<ParseUser> friends) {
        this.friends.addAll(friends);
        notifyItemRangeInserted(0, friends.size());
    }*/

    public void addFriend(ParseObject p) {
        this.friends.add(p);
        notifyItemInserted(friends.size()-1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView number;
        public Context context;
        public View container;
        public Button accept;
        public Button reject;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            container = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            accept = (Button) itemView.findViewById(R.id.accept);
            reject = (Button) itemView.findViewById(R.id.reject);
            number = (TextView) itemView.findViewById(R.id.number);
        }
    }
}
