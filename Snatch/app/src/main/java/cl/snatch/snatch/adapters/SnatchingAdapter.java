package cl.snatch.snatch.adapters;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.snatch.snatch.R;

public class SnatchingAdapter extends RecyclerView.Adapter<SnatchingAdapter.ViewHolder> {

    private List<JSONObject> contacts = new ArrayList<>();
    private List<JSONObject> checked = new ArrayList<>();

    public SnatchingAdapter() {}

    @Override
    public SnatchingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_snatch, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final SnatchingAdapter.ViewHolder holder, final int position) {
        final JSONObject user = contacts.get(position);

        try {
            holder.name.setText(user.getString("fullName"));
            holder.numbers.setText(user.getString("phoneNumber"));
            holder.snatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // add to directory
                        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                        ops.add(ContentProviderOperation
                                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                .build());
                        try {
                            ops.add(ContentProviderOperation
                                    .newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(
                                            ContactsContract.Data.MIMETYPE,
                                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                    .withValue(
                                            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                            user.getString("fullName")).build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            holder.context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // send snatch
                        JSONArray snatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");

                        Map<String, Object> newSnatch = new HashMap<>();
                        try {
                            newSnatch.put("firstName", user.getString("firstName"));
                            newSnatch.put("fullName", user.getString("fullName"));
                            newSnatch.put("hidden", false);
                            newSnatch.put("phoneNumber", user.getString("phoneNumber"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        snatched.put(newSnatch);
                        ParseUser.getCurrentUser().put("contactsSnatched", snatched);
                        ParseUser.getCurrentUser().saveInBackground();
                    } else {
                        // remove from directory
                        /*ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                        String[] args = new String[0];
                        try {
                            args = new String[] { String.valueOf(getContactID(
                                    holder.context.getContentResolver(), user.getString("phoneNumber"))) };
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
                        try {
                            holder.context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (OperationApplicationException e) {
                            e.printStackTrace();
                        }


                        // remove from snatch
                        Log.d("cl.snatch.snatch", "removing contact");
                        JSONArray snatched = ParseUser.getCurrentUser().getJSONArray("contactsSnatched");
                        JSONArray newSnatches = new JSONArray();

                        for (int i = 0; i < snatched.length(); i++) {
                            try {
                                //JSONObject snatch = (JSONObject) snatched.get(i);
                                Object s = snatched.get(i);
                                JSONObject snatch;
                                if (s instanceof JSONObject) snatch = (JSONObject) s;
                                else /*if (s instanceof Map)// snatch = new JSONObject((Map) s);

                                if (!snatch.getString("phoneNumber").equals(user.getString("phoneNumber"))) {
                                    newSnatches.put(snatch);
                                    Log.d("cl.snatch.snatch", snatch.getString("phoneNumber") + "!=" + user.getString("phoneNumber"));
                                } else {
                                    Log.d("cl.snatch.snatch", user.getString("firstName"));
                                }
                            } catch (JSONException e) {
                                Log.d("cl.snatch.snatch", "json error: " + e.getMessage());
                            }
                        }

                        ParseUser.getCurrentUser().put("contactsSnatched", newSnatches);
                        ParseUser.getCurrentUser().saveInBackground();*/
                    }
                    //String[] newUser = new String[] {user[0], user[1], user[2], String.valueOf(!isChecked)};
                    /*try {
                        user.put("hidden", !isChecked);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    contacts.set(position, user);*/
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(JSONArray contacts) {
        int i;
        for (i = 0; i < contacts.length(); i++) {
            try {
                Object s = contacts.get(i);
                JSONObject snatch;
                if (s instanceof JSONObject) snatch = (JSONObject) s;
                else /*if (s instanceof Map)*/ snatch = new JSONObject((Map) s);
                if (!snatch.getBoolean("hidden")) {
                    this.contacts.add(snatch);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //this.contacts.addAll(contacts);
        notifyItemRangeInserted(0, /*contacts.size()*/ i);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Context context;
        public TextView numbers;
        public Switch snatched;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            name = (TextView) itemView.findViewById(R.id.name);
            numbers = (TextView) itemView.findViewById(R.id.numbers);
            snatched = (Switch) itemView.findViewById(R.id.snatched);
        }
    }

    private static long getContactID(ContentResolver contactHelper,
                                     String number) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] projection = { ContactsContract.PhoneLookup._ID };
        Cursor cursor = null;

        try {
            cursor = contactHelper.query(contactUri, projection, null, null,
                    null);

            if (cursor.moveToFirst()) {
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }

            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return -1;
    }
}
