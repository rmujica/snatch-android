package cl.snatch.snatch;

import android.app.Application;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.parse.Parse;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import cl.snatch.snatch.receivers.PhoneObserver;
import io.fabric.sdk.android.Fabric;

public class SnatchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "QS802Iea2y3vcdzbzBMu7DA69E5ypNvDBv5htvgr", "V8ds1CarINcpDBrVNKiHgo7qrjth9sUx7GhwfdPK");

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("cl.snatch.snatch", "successfully subscribed to the broadcast channel.");
                } else {
                    Crashlytics.log(Log.ERROR, "cl.snatch.snatch", e.getMessage());
                }
            }
        });

        ParseUser.getCurrentUser().fetchIfNeededInBackground();

        //getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, new PhoneObserver(null));

    }

}
