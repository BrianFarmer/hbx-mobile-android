package gov.dc.broker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by plast on 10/27/2016.
 */

public class BrokerActivity extends AppCompatActivity {
    private Messages messages = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (messages != null) {
            messages.release();
            messages = null;
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
}
