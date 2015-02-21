package cl.snatch.snatch.models;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

import cl.snatch.snatch.activities.MainActivity;
import cl.snatch.snatch.fragments.ContactsFragment;
import cl.snatch.snatch.fragments.FriendsFragment;

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
            case 1:
                return ContactsFragment.newInstance();
            default:
                return FriendsFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Friends".toUpperCase(l);
            case 1:
                return "Contacts".toUpperCase(l);
        }
        return null;
    }
}
