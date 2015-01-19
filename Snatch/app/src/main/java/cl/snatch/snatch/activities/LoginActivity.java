package cl.snatch.snatch.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import cl.snatch.snatch.helpers.MediaHelper;
import cl.snatch.snatch.R;


public class LoginActivity extends ActionBarActivity {

    private static final int ACTION_REQUEST_GALLERY = 0x1;
    private static final int ACTION_REQUEST_CAMERA = 0x2;
    private ImageView avatar;
    private Uri avatarUri;
    private Bitmap avatarBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getBoolean("verified")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        avatar = (ImageView) findViewById(R.id.avatar);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                        Intent getCameraImage = new Intent("android.media.action.IMAGE_CAPTURE");

                                        avatarUri = MediaHelper.getOutputMediaFileUri(MediaHelper.MEDIA_TYPE_IMAGE);
                                        getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);

                                        startActivityForResult(getCameraImage, ACTION_REQUEST_CAMERA);

                                        break;

                                    default:
                                        break;
                                }
                            }
                        });

                builder.show();
            }
        });

        final Button register = (Button) findViewById(R.id.register);
        final Button verify = (Button) findViewById(R.id.verify);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify.setEnabled(false);
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
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent); // for result??
                                            finish();
                                        } else {
                                            Log.d("cl.snatch.snatch", "err: "+ e.getMessage());
                                        }
                                    }
                                });
                            } else {
                                Log.d("cl.snatch.snatch", "neq: " + String.valueOf(user.getNumber("phoneVerificationCode")) + " " + ((TextView) findViewById(R.id.code)).getText().toString());
                            }
                        } else {
                            Log.d("cl.snatch.snatch", "err: "+ e.getMessage());
                        }
                    }
                });
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setEnabled(false);
                // send data to parse
                final ParseUser myUser = new ParseUser();
                myUser.setUsername(((TextView) findViewById(R.id.phone)).getText().toString());
                myUser.setPassword("asdf");
                myUser.put("firstName", ((TextView) findViewById(R.id.firstName)).getText().toString());
                myUser.put("lastName", ((TextView) findViewById(R.id.lastName)).getText().toString());
                myUser.put("fullName", ((TextView) findViewById(R.id.firstName)).getText().toString() + " " + ((TextView) findViewById(R.id.lastName)).getText().toString());
                myUser.put("phoneNumber", ((TextView) findViewById(R.id.phone)).getText().toString());
                myUser.put("reach", 0);
                myUser.put("verified", false);
                try {
                    myUser.put("friends", new JSONArray("[\"FL8A99SeGR\",\"1sMQiLxN4I\", \"ljIIcIGZUL\"]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
                                            verify.setVisibility(View.VISIBLE);
                                            findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
                                            findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
                                            findViewById(R.id.phone).setVisibility(View.INVISIBLE);
                                            findViewById(R.id.code).setVisibility(View.VISIBLE);
                                            findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
                                            findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
                                            register.setVisibility(View.INVISIBLE);

                                            Map<String, Object> params = new HashMap<>();
                                            params.put("phoneNumber", myUser.getString("phoneNumber"));
                                            ParseCloud.callFunctionInBackground("sendVerificationCode", params, new FunctionCallback<Object>() {
                                                @Override
                                                public void done(Object o, ParseException e) {
                                                    if (e != null)
                                                    Log.d("cl.snatch.snatch", "err: "+ e.getMessage());
                                                    else Log.d("cl.snatch.snatch", "noerr: " + o.toString());
                                                }
                                            });
                                            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent); // for result??
                                            finish();*/
                                        } else {
                                            Log.d("cl.snatch.snatch", "pp: " + e.getMessage());
                                        }
                                    }
                                });
                            } else {
                                Log.d("cl.snatch.snatch", "pu: " + e.getMessage());
                            }
                        }
                    });

                } else {
                    myUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                verify.setVisibility(View.VISIBLE);
                                findViewById(R.id.firstName).setVisibility(View.INVISIBLE);
                                findViewById(R.id.lastName).setVisibility(View.INVISIBLE);
                                findViewById(R.id.phone).setVisibility(View.INVISIBLE);
                                findViewById(R.id.code).setVisibility(View.VISIBLE);
                                findViewById(R.id.sms_txt).setVisibility(View.INVISIBLE);
                                findViewById(R.id.avatar).setVisibility(View.INVISIBLE);
                                register.setVisibility(View.INVISIBLE);

                                Map<String, Object> params = new HashMap<>();
                                params.put("phoneNumber", myUser.getString("phoneNumber"));
                                ParseCloud.callFunctionInBackground("sendVerificationCode", params,  new FunctionCallback<Object>() {
                                    @Override
                                    public void done(Object o, ParseException e) {
                                        if (e != null)
                                            Log.d("cl.snatch.snatch", "err: "+ e.getMessage());
                                        else Log.d("cl.snatch.snatch", "noerr: " + o.toString());
                                    }
                                });
                                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent); // for result??
                                finish();*/
                            } else {
                                Log.d("cl.snatch.snatch", "np: " + e.getMessage());
                            }
                        }
                    });
                    register.setEnabled(true);
                }
            }
        });

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
