package org.dchbx.coveragehq;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class BrokerFragment extends Fragment {
    private EventBus eventBus;
    private Messages messages;

    protected BrokerActivity getBrokerActivity(){
        return (BrokerActivity) getActivity();
    }

    protected Messages getMessages(){
        return messages;
    }

    public void init() {
        if (messages != null){
            return;
        }
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }

    private static void getViewsByTag(ArrayList<View> views, ViewGroup root, String tag){
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                getViewsByTag(views, (ViewGroup) child, tag);
            }
            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
    }

    protected void setVisibility(View contentView, int tagResourceId, boolean visible, int openButtonId, int openButtonImageId, int closedButtonImageId) {
        //final ViewGroup viewGroup = (ViewGroup) ((ViewGroup)contentView
        //        .findViewById(android.R.id.content)).getChildAt(0);

        ArrayList<View> views = new ArrayList<>();
        int state = visible? View.VISIBLE: View.GONE;
        getViewsByTag(views, (ViewGroup)contentView, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        for (View view : views) {
            view.setVisibility(state);
        }
    }

    protected boolean invertDrawer(View rootView, int tagResourceId, int openButtonId, int openButtonImageId, int closedButtonImageId){
        ArrayList<View> views = new ArrayList<>();
        getViewsByTag(views, (ViewGroup)rootView, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        boolean open= false;
        for (View view : views) {
            if (view.getVisibility() == View.VISIBLE){
                view.setVisibility(View.GONE);
                open = false;
            } else {
                view.setVisibility(View.VISIBLE);
                open = true;
            }
        }
        ImageView imageView = (ImageView) rootView.findViewById(openButtonId);
        if (open){
            imageView.setImageResource(openButtonImageId);
        } else {
            imageView.setImageResource(closedButtonImageId);
        }
        return open;

    }

    protected boolean invertGroup(View rootView, int tagResourceId, int openButtonId, int openButtonImageId, int closedButtonImageId) {
        ArrayList<View> views = new ArrayList<>();
        getViewsByTag(views, (ViewGroup)rootView, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        boolean open= false;
        for (View view : views) {
            if (view.getVisibility() == View.VISIBLE){
                view.setVisibility(View.GONE);
                open = false;
            } else {
                view.setVisibility(View.VISIBLE);
                open = true;
            }
        }
        ImageView imageView = (ImageView) rootView.findViewById(openButtonId);
        if (open){
            imageView.setImageResource(openButtonImageId);
        } else {
            imageView.setImageResource(closedButtonImageId);
        }
        return open;
    }
}
