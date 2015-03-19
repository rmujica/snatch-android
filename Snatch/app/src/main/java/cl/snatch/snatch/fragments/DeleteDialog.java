package cl.snatch.snatch.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import cl.snatch.snatch.R;
import cl.snatch.snatch.activities.AccountActivity;
import cl.snatch.snatch.activities.LoginActivity;

/**
 * Created by rene on 3/19/15.
 */
public class DeleteDialog extends DialogFragment {

    public interface CancelListener {
        public void CancelDialog();
        public void FinishJob();
    }

    // Use this instance of the interface to deliver action events
    CancelListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CancelListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_really_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        ParseQuery<ParseObject> deleteContacts = ParseQuery.getQuery("Contact");
                        deleteContacts.whereEqualTo("owner", ParseUser.getCurrentUser());
                        deleteContacts.setLimit(1000);
                        deleteContacts.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> list, ParseException e) {
                                if (e == null) {
                                    ParseObject.deleteAllInBackground(list);
                                }
                            }
                        });
                        ParseQuery<ParseObject> deleteFriends1 = ParseQuery.getQuery("Friend");
                        deleteFriends1.whereEqualTo("from", ParseUser.getCurrentUser());
                        ParseQuery<ParseObject> deleteFriends2 = ParseQuery.getQuery("Friend");
                        deleteFriends2.whereEqualTo("to", ParseUser.getCurrentUser());
                        ArrayList<ParseQuery<ParseObject>> q = new ArrayList<>();
                        q.add(deleteFriends1);
                        q.add(deleteFriends2);
                        ParseQuery<ParseObject> deleteFriends = ParseQuery.or(q);
                        deleteFriends.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> list, ParseException e) {
                                if (e == null) {
                                    ParseObject.deleteAllInBackground(list);
                                }
                            }
                        });
                        ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                ParseUser.logOut();
                                mListener.FinishJob();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        mListener.CancelDialog();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
