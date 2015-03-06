package cl.snatch.snatch.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import cl.snatch.snatch.R;
import cl.snatch.snatch.activities.AccountActivity;
import cl.snatch.snatch.helpers.RoundCornersTransformation;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        getPreferenceManager()
                .findPreference("button_account_category_key")
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), AccountActivity.class));
                        return true;
                    }
                });

        getPreferenceManager()
                .findPreference("button_toc_category_key")
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://jumpsterapp.com/en/terms/")));
                        return true;
                    }
                });
    }
}
