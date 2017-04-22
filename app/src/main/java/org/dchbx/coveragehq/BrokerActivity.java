package org.dchbx.coveragehq;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by plast on 10/27/2016.
 */

public class BrokerActivity extends BaseActivity {
    private static final String TAG = "BrokerActivity";
    private boolean shuttingDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isXLargeScreen(getApplicationContext())) { //set phones to portrait;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            //I set background images here depending on portrait or landscape orientation
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getMessages().testTimeOut();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.TestTimeoutResult testTimeoutResult) {
        if (testTimeoutResult.timedOut && !shuttingDown){
            shuttingDown = true;
            Intents.restartApp(this);
            finish();
        }
    }

    protected void setVisibility(int tagResourceId, boolean visible, int imageViewId, int openImageId, int closedImageId) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        ViewUtilities.setVisibility(viewGroup, tagResourceId, visible);
        ImageView imageView = (ImageView) viewGroup.findViewById(imageViewId);
        if (visible){
            imageView.setImageResource(openImageId);
        } else {
            imageView.setImageResource(closedImageId);
        }
    }

    protected void setVisibility(int tagResourceId, boolean visible, int imageViewId, int openImageId, int closedImageId, ArrayList<Integer> idsToInclcude, ArrayList<Integer> idsToSkip) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        ViewUtilities.setVisibility(viewGroup, tagResourceId, visible, idsToInclcude, idsToSkip);
        ImageView imageView = (ImageView) viewGroup.findViewById(imageViewId);
        if (visible){
            imageView.setImageResource(openImageId);
        } else {
            imageView.setImageResource(closedImageId);
        }
    }

    protected void invertGroup(int tagResourceId, int imageViewId, int openImageId, int closedImageId) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        ViewUtilities.invertGroup(viewGroup, tagResourceId, imageViewId, openImageId, closedImageId);
    }

    public Messages getMessages() {
        return messages;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Finish eventFinish){
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SessionAboutToTimeout sessionAboutToTimeout){
        SessionTimeoutDialog sessionTimeoutDialog = SessionTimeoutDialog.build();
        sessionTimeoutDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        sessionTimeoutDialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");

    }

    interface DialogClosed {
        void closed();
    }
    public AlertDialog alertDialog(String string, final DialogClosed closedHelper){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string)
            .setCancelable(false)
            .setPositiveButton(R.string.ok, new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closedHelper.closed();
                }
            });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public static boolean isXLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (!isXLargeScreen(getApplicationContext()) ) {
            return; //keep in portrait mode if a phone
        }
    }
}
