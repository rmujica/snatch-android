package cl.snatch.snatch;

import android.app.Application;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.Parse;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Bus;

import cl.snatch.snatch.receivers.PhoneObserver;
import io.fabric.sdk.android.Fabric;

public class SnatchApplication extends Application {

    private static final Bus BUS = new Bus();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "1U2LFANC7maXJsJ8fdfnrVJFVPnLRo8fgwp3LaF1", "PK45p00Ep07FB4HhTUwOVYuv1ebAXXCb4Hnbh5M3");

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

        if (ParseUser.getCurrentUser() != null) {
            Log.d("cl.snatch.snatch", "fetching in background");
            ParseUser.getCurrentUser().fetchIfNeededInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null)
                        ParseUser.becomeInBackground(parseUser.getSessionToken());
                }
            });
        }

        //getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, new PhoneObserver(null));

    }

    public static Bus getInstance() {
        return BUS;
    }

}
