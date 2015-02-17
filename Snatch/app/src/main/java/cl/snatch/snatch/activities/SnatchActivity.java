package cl.snatch.snatch.activities;

import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cl.snatch.snatch.R;
import cl.snatch.snatch.models.SnatchingAdapter;

public class SnatchActivity extends ActionBarActivity {

    RecyclerView list;
    SnatchingAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snatch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);

        // initialize list
        adapter = new SnatchingAdapter();
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        list.setAdapter(adapter);
        list.setLayoutManager(layoutManager);

        // get all contacts from user
        userId = getIntent().getStringExtra("userId");
        ParseQuery<ParseObject> contactsQuery = ParseQuery.getQuery("Contact");
        contactsQuery.setLimit(1000);
        contactsQuery.whereEqualTo("ownerId", userId);
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
