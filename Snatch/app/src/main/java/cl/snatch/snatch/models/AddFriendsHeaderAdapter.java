package cl.snatch.snatch.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;

import cl.snatch.snatch.R;

public class AddFriendsHeaderAdapter implements StickyHeadersAdapter<AddFriendsHeaderAdapter.ViewHolder> {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

    }

    @Override
    public long getHeaderId(int i) {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

            TextView txt;

            public ViewHolder(View itemView) {
                super(itemView);
                txt = (TextView) itemView.findViewById(R.id.title);
            }
        }
}
