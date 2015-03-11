package cl.snatch.snatch.activities;

import android.content.ContentProviderOperation;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.MediaHelper;
import cl.snatch.snatch.helpers.RoundCornersTransformation;
import cl.snatch.snatch.models.SnatchingAdapter;

public class SnatchActivity extends ActionBarActivity {

    RecyclerView list;
    SnatchingAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    String userId;
    View pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snatch);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);

        // initialize list
        adapter = new SnatchingAdapter();
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        pb = findViewById(R.id.progressBar);

        // set toobar header
        getSupportActionBar().setTitle("  " + getIntent().getStringExtra("name"));
        int avatarSize = (int) MediaHelper.convertDpToPixel(48, this);

        if (getIntent().getStringExtra("avatar") != null && !getIntent().getStringExtra("avatar").equals("noavatar")) {
            Picasso.with(SnatchActivity.this)
                    .load(getIntent().getStringExtra("avatar"))
                    .transform(new RoundCornersTransformation())
                    .resize(avatarSize, avatarSize)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            toolbar.setLogo(new BitmapDrawable(getResources(), bitmap));
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }

        // get all contacts from user
        userId = getIntent().getStringExtra("userId");
        ParseQuery<ParseObject> contactsQuery = ParseQuery.getQuery("Contact");
        contactsQuery.setLimit(1000);
        contactsQuery.whereEqualTo("ownerId", userId);
        contactsQuery.orderByAscending("firstName");
        contactsQuery.addAscendingOrder("lastName");
        contactsQuery.findInBackground(new FindContacts());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_snatch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_snatch && adapter != null) {
            Set<ParseObject> checked = adapter.getChecked();
            for (ParseObject user : checked) addUserToPhonebook(user);
            Toast.makeText(this, getResources().getString(R.string.contacts_snatched), Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_unfriend) {
            // unfriend
            ParseQuery<ParseObject> meToFriend = ParseQuery.getQuery("Friend");
            meToFriend.whereEqualTo("from", ParseUser.getCurrentUser().getObjectId());
            meToFriend.whereEqualTo("to", userId);
            meToFriend.whereEqualTo("status", "accepted");

            ParseQuery<ParseObject> friendToMe = ParseQuery.getQuery("Friend");
            friendToMe.whereEqualTo("to", ParseUser.getCurrentUser().getObjectId());
            friendToMe.whereEqualTo("from", userId);
            friendToMe.whereEqualTo("status", "accepted");

            List<ParseQuery<ParseObject>> q = new ArrayList<>();
            q.add(meToFriend);
            q.add(friendToMe);

            ParseQuery<ParseObject> unfriend = ParseQuery.or(q);
            unfriend.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        for (ParseObject f : parseObjects) {
                            f.deleteInBackground();
                        }

                        ParseQuery<ParseUser> exFriend = ParseUser.getQuery();
                        exFriend.getInBackground(userId, new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser parseUser, ParseException e) {
                                if (e == null) {
                                    parseUser.unpinInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(SnatchActivity.this, getString(R.string.unfriended) + getIntent().getStringExtra("name") + ".", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void addUserToPhonebook(ParseObject user) {
        // create new contact using object
        // todo:refactor
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
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FindContacts extends FindCallback<ParseObject> {

        @Override
        public void done(List<ParseObject> contacts, ParseException e) {
            if (e == null) {
                if (pb.getVisibility() == View.VISIBLE) pb.setVisibility(View.GONE);
                adapter.addContacts(contacts);
                if (contacts.size() == 1000) {
                    // load more
                    ParseQuery<ParseObject> contactsQuery = ParseQuery.getQuery("Contacts");
                    contactsQuery.setLimit(1000);
                    contactsQuery.whereEqualTo("ownerId", userId);
                    contactsQuery.findInBackground(new FindContacts());
                }
            }
        }
    }
}
