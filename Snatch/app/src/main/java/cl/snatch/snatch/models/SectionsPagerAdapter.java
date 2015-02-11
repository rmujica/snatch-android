package cl.snatch.snatch.models;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

import cl.snatch.snatch.activities.MainActivity;
import cl.snatch.snatch.fragments.ContactsFragment;
import cl.snatch.snatch.fragments.FriendsFragment;
import cl.snatch.snatch.fragments.PlaceholderFragment;
import cl.snatch.snatch.fragments.SnatchFragment;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private MainActivity mainActivity;

    public SectionsPagerAdapter(MainActivity mainActivity, FragmentManager fm) {
        super(fm);
        this.mainActivity = mainActivity;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return FriendsFragment.newInstance();
            case 2:
                return ContactsFragment.newInstance();
            case 1:
                return SnatchFragment.newInstance();
            default:
                return PlaceholderFragment.newInstance(position + 1);
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Amigos".toUpperCase(l);
            case 1:
                return "Snatch".toUpperCase(l);
            case 2:
                return "Contactos".toUpperCase(l);
        }
        return null;
    }
}
