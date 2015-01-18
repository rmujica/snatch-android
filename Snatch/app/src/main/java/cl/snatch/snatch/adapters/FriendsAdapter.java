package cl.snatch.snatch.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.activities.SnatchActivity;
import cl.snatch.snatch.helpers.RoundCornersTransformation;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<ParseUser> friends = new ArrayList<>();

    public FriendsAdapter() {}

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_friend, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final FriendsAdapter.ViewHolder holder, int position) {
        final ParseUser parseUser = friends.get(position);

        holder.name.setText(parseUser.getString("firstName") + " " + parseUser.getString("lastName"));
        Picasso.with(holder.context)
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
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void updateFriends(List<ParseUser> friends) {
        this.friends.addAll(friends);
        notifyItemRangeInserted(0, friends.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView avatar;
        public Context context;
        public View container;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            container = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }
}
