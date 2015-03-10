package cl.snatch.snatch.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.MediaHelper;
import cl.snatch.snatch.models.ContactsLoader;


public class AccountActivity extends ActionBarActivity implements ContactsLoader.LoadFinishedCallback {

    private static final int ACTION_REQUEST_GALLERY = 0x1;
    private static final int ACTION_REQUEST_CAMERA = 0x2;
    private static final int CONTACTS_LOADER_ID = 1;
    private Uri avatarUri;
    private Bitmap avatarBitmap = null;
    private String phoneNumber = "";
    private SharedPreferences sharedPref;
    private boolean imageChanged = false;

    @InjectView(R.id.avatar) ImageView avatar;
    @InjectView(R.id.firstName) EditText firstName;
    @InjectView(R.id.lastName) EditText lastName;
    @InjectView(R.id.pb) ProgressBar syncing;
    @InjectView(R.id.sync) Button sync;

    private boolean uploadFinished = false;
    private boolean isVerified = false;

    @OnClick(R.id.avatar)
    public void setAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        builder.setTitle(R.string.choose_image_origin);
        builder.setItems(new CharSequence[]{getString(R.string.gallery), getString(R.string.camera)},
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // GET IMAGE FROM THE GALLERY
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                Intent chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture));
                                startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
                                break;

                            case 1:
                            default:
                                Intent getCameraImage = new Intent("android.media.action.IMAGE_CAPTURE");
                                avatarUri = MediaHelper.getOutputMediaFileUri(MediaHelper.MEDIA_TYPE_IMAGE);
                                getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);
                                startActivityForResult(getCameraImage, ACTION_REQUEST_CAMERA);
                                break;
                        }
                    }
                });

        builder.show();
    }

    @OnClick(R.id.update)
    public void updateData(final Button update) {
        update.setEnabled(false);
        final ParseUser u = ParseUser.getCurrentUser();
        if (imageChanged) {
            if (avatarBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] bitmapByte = stream.toByteArray();
                final ParseFile avatar = new ParseFile(ParseUser.getCurrentUser().getString("phoneNumber")+".jpg", bitmapByte);
                avatar.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            u.put("profilePicture", avatar);
                            u.put("firstName", firstName.getText().toString());
                            u.put("lastName", lastName.getText().toString());
                            u.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    update.setEnabled(true);
                                    Toast.makeText(AccountActivity.this, getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        } else {
            u.put("firstName", firstName.getText().toString());
            u.put("lastName", lastName.getText().toString());
            u.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    update.setEnabled(true);
                    Toast.makeText(AccountActivity.this, getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @OnClick(R.id.sync)
    public void syncContacts(final Button sync) {
        syncing.setVisibility(View.VISIBLE);
        sync.setEnabled(false);
        // upload contacts
        getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,
                null,
                new ContactsLoader(AccountActivity.this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();

        setContentView(R.layout.activity_account);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);

        firstName.setText(currentUser.getString("firstName"));
        lastName.setText(currentUser.getString("lastName"));
        if (currentUser.getParseFile("profilePicture") != null) {
            Picasso.with(this)
                    .load(currentUser.getParseFile("profilePicture").getUrl())
                    .into(avatar);
        } else {
            Picasso.with(this)
                    .load(R.drawable.ic_avatar)
                    .into(avatar);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            int size = (int) MediaHelper.convertDpToPixel(144, this);
            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:
                    avatarUri = data.getData();
                    avatarBitmap = MediaHelper
                            .decodeSampledBitmapFromContentUri(this, avatarUri, size, size);
                    avatar.setImageBitmap(avatarBitmap);
                    imageChanged = true;
                    break;
                case ACTION_REQUEST_CAMERA:
                    avatarBitmap = MediaHelper
                            .decodeSampledBitmapFromUri(avatarUri, size, size);
                    avatar.setImageBitmap(avatarBitmap);
                    imageChanged = true;
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadFinished(final Cursor cursor) {
        final HashSet<String> newNumbers = new HashSet<>();
        final HashMap<String, String> newContacts = new HashMap<>();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String name = cursor
                        .getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor
                        .getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        .replaceAll(" ", "");

                newNumbers.add(number);
                newContacts.put(number, name);
            } while (cursor.moveToNext());
        }

        final HashSet<String> oldNumbers = new HashSet<>();
        final HashMap<String, String> oldContacts = new HashMap<>();

        ParseQuery<ParseObject> oldC = ParseQuery.getQuery("Contact")
                .whereEqualTo("owner", ParseUser.getCurrentUser())
                .setLimit(10000);
        oldC.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    Log.d("cl.snatch.snatch", "found: " + parseObjects.toString());
                    for (ParseObject o : parseObjects) {
                        oldNumbers.add(o.getString("phoneNumber"));
                        oldContacts.put(o.getString("phoneNumber"), o.getString("fullName"));
                    }

                    oldNumbers.removeAll(newNumbers);
                    Log.d("cl.snatch.snatch", "toremove: " + String.valueOf(oldNumbers.size()));
                    Log.d("cl.snatch.snatch", "toremove: " + oldNumbers.toString());
                    for (String toRemove : oldNumbers) {
                        ParseQuery.getQuery("Contact")
                                .whereEqualTo("owner", ParseUser.getCurrentUser())
                                .whereEqualTo("phoneNumber", toRemove)
                                .getFirstInBackground(new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(final ParseObject parseObject, ParseException e) {
                                        if (e == null) {
                                            parseObject.deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        parseObject.unpinInBackground("myContacts");
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d("cl.snatch.snatch", "error remove: " + e.getMessage());
                                        }
                                    }
                                });
                    }

                    Log.d("cl.snatch.snatch", "toadd s: " + String.valueOf(oldContacts.keySet().size()) +  " " + String.valueOf(newNumbers.size()));

                    newNumbers.removeAll(oldContacts.keySet());
                    Log.d("cl.snatch.snatch", "toadd: " + newNumbers.toString());
                    for (String toAdd : newNumbers) {
                        ParseObject contact = new ParseObject("Contact");
                        contact.put("firstName", newContacts.get(toAdd).split(" ")[0]);
                        try {
                            contact.put("lastName", newContacts.get(toAdd).split(" ")[1]);
                        } catch (ArrayIndexOutOfBoundsException xe) {
                            contact.put("lastName", newContacts.get(toAdd).split(" ")[0]);
                        }
                        contact.put("fullName", newContacts.get(toAdd));
                        contact.put("hidden", false);
                        contact.put("phoneNumber", toAdd.replaceAll(" ", ""));
                        contact.put("owner", ParseUser.getCurrentUser());
                        contact.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                        contact.saveInBackground();
                        contact.pinInBackground("myContacts");
                    }

                    uploadFinished = true;
                    syncing.setVisibility(View.INVISIBLE);
                    sync.setEnabled(true);
                } else {
                    Log.d("cl.snatch.snatch", "error find: " + e.getMessage());
                }
            }
        });


        /*

                        uploadFinished = true;
                        syncing.setVisibility(View.INVISIBLE);
                        sync.setEnabled(true);

                ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
                getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
                getContacts.whereEqualTo("phoneNumber", number.replaceAll(" ", ""));
                Log.d("cl.snatch.snatch", "count query: " + getContacts.toString());
                getContacts.countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, ParseException e) {
                        if (e == null && i == 0) {
                            Log.d("cl.snatch.snatch", "count: " + String.valueOf(i));
                            // upload to parse
                            ParseObject contact = new ParseObject("Contact");
                            contact.put("firstName", name.split(" ")[0]);
                            try {
                                contact.put("lastName", name.split(" ")[1]);
                            } catch (ArrayIndexOutOfBoundsException xe) {
                                contact.put("lastName", name.split(" ")[0]);
                            }
                            contact.put("fullName", name);
                            contact.put("hidden", false);
                            contact.put("phoneNumber", number.replaceAll(" ", ""));
                            contact.put("owner", ParseUser.getCurrentUser());
                            contact.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                            contact.saveInBackground();
                            contact.pinInBackground("myContacts");
                        } else {
                            if (e != null) Log.d("cl.snatch.snatch", "count error: " + e.getMessage());
                            else Log.d("cl.snatch.snatch", "count is: " + String.valueOf(i));
                        }
                    }
                });*/

        //contact.pinInBackground("myContacts");
    }

    @Override
    public Context getContext() {
        return this;
    }

}
