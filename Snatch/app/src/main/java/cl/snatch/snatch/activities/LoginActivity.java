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
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.MediaHelper;
import cl.snatch.snatch.models.ContactsLoader;


public class LoginActivity extends ActionBarActivity implements ContactsLoader.LoadFinishedCallback {

    private static final int ACTION_REQUEST_GALLERY = 0x1;
    private static final int ACTION_REQUEST_CAMERA = 0x2;
    private static final String salt = "f6gjdUIjehc654bs7dng9f7behY5dsv4sv6GnFCVjgu7yjhgr8sd65fd8se9GFb8";
    private static final int CONTACTS_LOADER_ID = 1;
    private Uri avatarUri;
    private Bitmap avatarBitmap = null;
    private String phoneNumber = "";
    private SharedPreferences sharedPref;

    @InjectView(R.id.avatar) ImageView avatar;
    @InjectView(R.id.register) Button register;
    @InjectView(R.id.verify) Button verify;
    @InjectView(R.id.dologin) Button login;
    @InjectView(R.id.code) EditText code;
    private boolean uploadFinished = false;
    private boolean isVerified = false;

    @OnClick(R.id.avatar)
    public void setAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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

    @OnClick(R.id.dologin)
    public void doLogin(final Button login) {
        Log.d("cl.snatch.snatch", "logging in");
        ParseUser user = ParseUser.getCurrentUser();
        code.setEnabled(false);
        String code = ((TextView) findViewById(R.id.code)).getText().toString();
        login.setEnabled(false);
        if (user != null && !code.isEmpty()) {
            if (user.getNumber("phoneVerificationCode").equals(Integer.parseInt(code))) {
                ParseUser u = ParseUser.getCurrentUser();
                u.put("verified", true);
                u.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("cl.snatch.snatch", "saving in background");
                            // save installation
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                            installation.saveInBackground();

                            // pull friend list and save
                            // get friend list
                            final ArrayList<String> friends =
                                    new ArrayList<>(ParseUser.getCurrentUser().getList("friends").size());
                            friends.addAll(ParseUser.getCurrentUser().<String>getList("friends"));

                            // getting friend data
                            ParseQuery<ParseUser> getFriends = ParseUser.getQuery();
                            getFriends.whereContainedIn("objectId", friends);
                            getFriends.orderByAscending("firstName");
                            getFriends.addAscendingOrder("lastName");
                            getFriends.findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(final List<ParseUser> parseUsers, ParseException e) {
                                    if (e == null) {
                                        Log.d("cl.snatch.snatch", "getting friends");
                                        ParseUser.pinAllInBackground("myFriends", parseUsers, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    // get contacts data
                                                    ParseQuery<ParseObject> getContacts = ParseQuery.getQuery("Contact");
                                                    getContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
                                                    getContacts.findInBackground(new FindCallback<ParseObject>() {
                                                        @Override
                                                        public void done(List<ParseObject> contacts, ParseException e) {
                                                            if (e == null) {
                                                                Log.d("cl.snatch.snatch", "getting contacts");
                                                                ParseObject.pinAllInBackground("myContacts", contacts, new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            Log.d("cl.snatch.snatch", "pinning contacts");
                                                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                            startActivity(intent); // for result??
                                                                            finish();
                                                                        } else {
                                                                            Log.d("cl.snatch.snatch", "error saving contacts: " + e.getMessage());
                                                                            Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error saving contacts: " + e.getMessage());
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Log.d("cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                                                                Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error loading contacts: " + e.getMessage());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.d("cl.snatch.snatch", "error saving friends: " + e.getMessage());
                                                    Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error saving friends: " + e.getMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.d("cl.snatch.snatch", "error loading friends: " + e.getMessage());
                                        Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error loading friends: " + e.getMessage());
                                    }
                                }
                            });
                        } else {
                            Log.d("cl.snatch.snatch", "err: " + e.getMessage());
                            Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error logging in: " + e.getMessage());
                        }
                    }
                });
            } else {
                // todo: tell user code doesn't match
                ParseUser.logOut();
                Toast.makeText(LoginActivity.this, getString(R.string.wrong_code), Toast.LENGTH_LONG).show();
                login.setEnabled(true);
                this.code.setEnabled(true);
                Log.d("cl.snatch.snatch", "neq: " + String.valueOf(user.getNumber("phoneVerificationCode")) + " " + ((TextView) findViewById(R.id.code)).getText().toString());
            }
        }
    }

    @OnClick(R.id.register)
    public void doRegister(final Button register) {
        register.setEnabled(false);
        // send data to parse
        final ParseUser myUser = new ParseUser();
        myUser.setUsername(((TextView) findViewById(R.id.phone)).getText().toString());
        try {
            myUser.setPassword(MediaHelper.computeHash(salt+((TextView) findViewById(R.id.phone)).getText().toString()));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        myUser.put("firstName", ((TextView) findViewById(R.id.firstName)).getText().toString());
        myUser.put("lastName", ((TextView) findViewById(R.id.lastName)).getText().toString());
        myUser.put("fullName", ((TextView) findViewById(R.id.firstName)).getText().toString() + " " + ((TextView) findViewById(R.id.lastName)).getText().toString());
        myUser.put("phoneNumber", ((TextView) findViewById(R.id.phone)).getText().toString());
        myUser.put("reach", 0);
        myUser.put("friends", new ArrayList<JSONObject>());
        myUser.put("verified", false);

        findViewById(R.id.firstName).setEnabled(false);
        findViewById(R.id.lastName).setEnabled(false);
        findViewById(R.id.phone).setEnabled(false);
        register.setEnabled(false);

        phoneNumber = ((TextView) findViewById(R.id.phone)).getText().toString();

        // save bitmap as byte
        if (avatarBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            byte[] bitmapByte = stream.toByteArray();
            final ParseFile avatar = new ParseFile(((TextView) findViewById(R.id.phone)).getText().toString().substring(1)+".jpg", bitmapByte);
            avatar.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        myUser.put("profilePicture", avatar);
                        myUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                // go to login
                                if (e == null) {
                                    stepTwo();

                                    // save boolean
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("waitingSMS", 1);
                                    editor.apply();

                                    // upload contacts
                                    getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,
                                            null,
                                            new ContactsLoader(LoginActivity.this));

                                    Map<String, Object> params = new HashMap<>();
                                    params.put("phoneNumber", myUser.getString("phoneNumber"));
                                    ParseCloud.callFunctionInBackground("sendVerificationCode", params, new FunctionCallback<Object>() {
                                        @Override
                                        public void done(Object o, ParseException e) {
                                            if (e != null)
                                                Log.d("cl.snatch.snatch", "err: " + e.getMessage());
                                            else
                                                Log.d("cl.snatch.snatch", "noerr: " + o.toString());
                                        }
                                    });
                                } else {
                                    // save boolean
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("waitingSMS", 2);
                                    editor.apply();

                                    stepTwoLogin();
                                    Log.d("cl.snatch.snatch", "pp: " + e.getMessage());
                                }


                            }
                        });
                    } else {
                        findViewById(R.id.firstName).setEnabled(true);
                        findViewById(R.id.lastName).setEnabled(true);
                        findViewById(R.id.phone).setEnabled(true);
                        register.setEnabled(true);
                        Log.d("cl.snatch.snatch", "pu: " + e.getMessage());
                    }
                }
            });

        } else {
            myUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        stepTwo();

                        // save boolean
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("waitingSMS", 1);
                        editor.apply();

                        // upload contacts
                        getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,
                                null,
                                new ContactsLoader(LoginActivity.this));

                        Map<String, Object> params = new HashMap<>();
                        params.put("phoneNumber", myUser.getString("phoneNumber"));

                        ParseCloud.callFunctionInBackground("sendVerificationCode", params,  new FunctionCallback<Object>() {
                            @Override
                            public void done(Object o, ParseException e) {
                                if (e != null)
                                    Log.d("cl.snatch.snatch", "err snd: "+ e.getMessage());
                                else Log.d("cl.snatch.snatch", "noerr: " + o.toString());
                            }
                        });
                    } else {
                        // save boolean
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("waitingSMS", 2);
                        editor.apply();

                        stepTwoLogin();
                        Log.d("cl.snatch.snatch", "np: " + e.getMessage());
                    }
                }
            });
            register.setEnabled(true);
        }
    }

    private void stepTwoLogin() {
        login.setVisibility(View.VISIBLE);
        findViewById(R.id.pb).setVisibility(View.VISIBLE);
        findViewById(R.id.contacts).setVisibility(View.VISIBLE);
        findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
        findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
        findViewById(R.id.phone).setVisibility(View.INVISIBLE);
        findViewById(R.id.code).setVisibility(View.VISIBLE);
        findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
        findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
        register.setVisibility(View.INVISIBLE);

        // resend sms

        String username = phoneNumber;
        String password = "";
        try {
            password = MediaHelper.computeHash(salt+phoneNumber);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {

                if (e == null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("phoneNumber", phoneNumber);

                    ParseCloud.callFunctionInBackground("resendVerificationCode", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            if (e != null) {
                                Log.d("cl.snatch.snatch", "err rs: " + e.getMessage());
                                Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "err rs: " + e.getMessage());
                                Toast.makeText(LoginActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                                stepTwoLogin();
                            } else {
                                Log.d("cl.snatch.snatch", "noerr: " + o.toString());
                            }
                        }
                    });
                } else {
                    Log.d("cl.snatch.snatch", "error logging in: " + e.getMessage());
                    Crashlytics.log(Log.ERROR, "cl.snatch.snatch", "error logging in: " + e.getMessage());
                }
            }
        });
    }

    private void stepTwo() {
        verify.setVisibility(View.VISIBLE);
        findViewById(R.id.pb).setVisibility(View.VISIBLE);
        findViewById(R.id.contacts).setVisibility(View.VISIBLE);
        findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
        findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
        findViewById(R.id.phone).setVisibility(View.INVISIBLE);
        findViewById(R.id.code).setVisibility(View.VISIBLE);
        findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
        findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
        register.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.verify)
    public void doVerify(final Button verify) {
        verify.setEnabled(false);
        code.setEnabled(false);
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    if (user.getNumber("phoneVerificationCode").equals(Integer.parseInt(((TextView) findViewById(R.id.code)).getText().toString()))) {

                        ParseUser u = ParseUser.getCurrentUser();
                        u.put("verified", true);
                        u.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    isVerified = true;

                                    if (uploadFinished) {
                                        // save installation
                                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                        installation.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                                        installation.saveInBackground();

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent); // for result??
                                        finish();
                                    } else {
                                        verify.setText(getString(R.string.still_uploading));
                                    }
                                } else {
                                    Log.d("cl.snatch.snatch", "err sav:  " + e.getMessage());
                                }
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_code), Toast.LENGTH_LONG).show();
                        verify.setEnabled(true);
                        code.setEnabled(true);
                        Log.d("cl.snatch.snatch", "neq: " + String.valueOf(user.getNumber("phoneVerificationCode")) + " " + ((TextView) findViewById(R.id.code)).getText().toString());
                    }
                } else {
                    Log.d("cl.snatch.snatch", "err fib: "+ e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getBoolean("verified")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        sharedPref = getSharedPreferences("p", Context.MODE_PRIVATE);

        if (!sharedPref.getBoolean("tutorial", false)) {
            Intent intent = new Intent(LoginActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
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
                    break;
                case ACTION_REQUEST_CAMERA:
                    avatarBitmap = MediaHelper
                            .decodeSampledBitmapFromUri(avatarUri, size, size);
                    avatar.setImageBitmap(avatarBitmap);
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
    public void onLoadFinished(Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                // upload to parse
                ParseObject contact = new ParseObject("Contact");
                contact.put("firstName", name.split(" ")[0]);
                try {
                    contact.put("lastName", name.split(" ")[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    contact.put("lastName", name.split(" ")[0]);
                }
                contact.put("fullName", name);
                contact.put("hidden", false);
                contact.put("phoneNumber", number.replaceAll(" ", ""));
                contact.put("owner", ParseUser.getCurrentUser());
                contact.put("ownerId", ParseUser.getCurrentUser().getObjectId());
                contact.saveInBackground();
                contact.pinInBackground("myContacts");
                Log.d("cl.snatch.snatch", "added: " + name);
            } while (cursor.moveToNext());
        }

        uploadFinished = true;

        if (isVerified) {
            // save installation
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("ownerId", ParseUser.getCurrentUser().getObjectId());
            installation.saveInBackground();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent); // for result??
            finish();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // check if we are waiting for sms
    }

    @Override
    protected void onStart() {
        super.onStart();

        // check if we are waiting for sms
        switch (sharedPref.getInt("waitingSMS", 0)) {
            case 0:
            default:
                break;
            case 1:
                uploadFinished = true;
                verify.setVisibility(View.VISIBLE);
                login.setVisibility(View.INVISIBLE);
                findViewById(R.id.pb).setVisibility(View.VISIBLE);
                findViewById(R.id.contacts).setVisibility(View.VISIBLE);
                findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
                findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
                findViewById(R.id.phone).setVisibility(View.INVISIBLE);
                findViewById(R.id.code).setVisibility(View.VISIBLE);
                findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
                findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
                register.setVisibility(View.INVISIBLE);
                break;
            case 2:
                uploadFinished = true;
                login.setVisibility(View.VISIBLE);
                verify.setVisibility(View.INVISIBLE);
                findViewById(R.id.pb).setVisibility(View.VISIBLE);
                findViewById(R.id.contacts).setVisibility(View.VISIBLE);
                findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
                findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
                findViewById(R.id.phone).setVisibility(View.INVISIBLE);
                findViewById(R.id.code).setVisibility(View.VISIBLE);
                findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
                findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
                register.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
