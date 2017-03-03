package org.dchbx.coveragehq;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by plast on 3/1/2017.
 */

public class HamburgerHelper {
    static private String TAG = "HamburgerHelper";

    static public boolean handleHamburgerListener(MenuItem item, Activity activity, Messages messages){
        switch (item.getItemId()){
            case R.id.nav_call_healthlink:
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                phoneIntent.setData(Uri.parse("tel:" + Constants.HbxPhoneNumber));
                Log.d(TAG, "onClick: launching phone");
                activity.startActivity(phoneIntent);
                return true;
            case R.id.nav_email_healthlink:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + Constants.HbxEmail));
                Log.d(TAG, "onClick: launching email client");
                activity.startActivity(emailIntent);
                return true;
            case R.id.nav_logout:
                messages.logoutRequest();
                Intent i = new Intent(activity, RootActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d(TAG, "onClick: logging out");
                activity.startActivity(i);
                return true;
            case R.id.nav_aca_glossary:
                messages.logoutRequest();
                Intent acaGlossaryActivity = new Intent(activity, AcaGlossaryActivity.class);
                Log.d(TAG, "onClick: launching ACA glossary activity");
                activity.startActivity(acaGlossaryActivity);
                return true;
            case R.id.nav_carriers:
                Intent carrierIntent = new Intent(activity, CarriersActivity.class);
                Log.d(TAG, "onClick: launching carriers activitiy");
                activity.startActivity(carrierIntent);
                return true;
        }
        return false;
    }
}
