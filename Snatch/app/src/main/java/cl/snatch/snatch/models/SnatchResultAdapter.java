package cl.snatch.snatch.models;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;

public class SnatchResultAdapter extends RecyclerView.Adapter<SnatchResultAdapter.ViewHolder> {

    // todo: change to set? if it duplicates contacts
    private List<ParseObject> contacts = new ArrayList<>();
    private Context context;

    public SnatchResultAdapter() {}

    @Override
    public SnatchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_snatch_result, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final SnatchResultAdapter.ViewHolder holder, final int position) {
        if (contacts.get(position).has("address")) {
            final ParseObject comm = contacts.get(position);
            holder.name.setText(comm.getString("name"));
            holder.numbers.setText(comm.getString("phoneNumber"));
            holder.snatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCommToPhonebook(comm);
                    Toast.makeText(context, context.getResources().getString(R.string.contacts_snatched), Toast.LENGTH_SHORT).show();
                }
            });
            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + comm.getString("phoneNumber")));
                    context.startActivity(intent);
                }
            });
        } else {
            final ParseObject user;
            try {
                user = contacts.get(position).fetchIfNeeded();
                final ParseObject owner = user.getParseUser("owner").fetchIfNeeded();

                holder.name.setText(user.getString("fullName"));
                holder.numbers.setText(user.getString("phoneNumber") + " (" + owner.getString("fullName") + ")");
                holder.snatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addUserToPhonebook(user);
                        Toast.makeText(context, context.getResources().getString(R.string.contacts_snatched), Toast.LENGTH_SHORT).show();
                    }
                });
                holder.call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + user.getString("phoneNumber")));
                        context.startActivity(intent);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addContacts(List<ParseObject> contacts) {
        int current = getItemCount();
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(current, contacts.size());
    }

    public void replaceContacts(List<ParseObject> contacts) {
        int current = getItemCount();
        this.contacts.clear();
        notifyItemRangeRemoved(0, current);
        this.contacts.addAll(contacts);
        notifyItemRangeInserted(0, contacts.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        public TextView numbers;
        public View container;
        public View call;
        public View snatch;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            container = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            numbers = (TextView) itemView.findViewById(R.id.numbers);
            snatch = itemView.findViewById(R.id.snatch);
            ((ImageView) snatch).setColorFilter(Color.argb(255, 43, 160, 191));
            call = itemView.findViewById(R.id.call);
            ((ImageView) call).setColorFilter(Color.argb(255, 43, 160, 191));
        }
    }

    private void addCommToPhonebook(ParseObject comm) {
        // create new contact using object
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        comm.getString("name")).build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        comm.getString("phoneNumber"))
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            context.getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addUserToPhonebook(ParseObject user) {
        // create new contact using object
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        user.getString("fullName")).build());

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        user.getString("phoneNumber"))
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            context.getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
