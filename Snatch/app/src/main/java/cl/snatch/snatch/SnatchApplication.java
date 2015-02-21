package cl.snatch.snatch;

import android.app.Application;

import com.parse.Parse;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SnatchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "QS802Iea2y3vcdzbzBMu7DA69E5ypNvDBv5htvgr", "V8ds1CarINcpDBrVNKiHgo7qrjth9sUx7GhwfdPK");
    }

}
