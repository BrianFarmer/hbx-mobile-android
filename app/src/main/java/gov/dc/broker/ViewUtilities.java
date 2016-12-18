package gov.dc.broker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by plast on 11/10/2016.
 */

public class ViewUtilities {
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

    public static void setVisibility(ViewGroup viewGroup, int tagResourceId, boolean visible) {
        ArrayList<View> views = new ArrayList<>();
        int state = View.GONE;
        if (visible){
            state = View.VISIBLE;
        }
        getViewsByTag(views, viewGroup, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        for (View view : views) {
            view.setVisibility(state);
        }
    }

    public static void setVisibility(ViewGroup viewGroup, int tagResourceId, boolean visible, ArrayList<Integer> idsToInclude, ArrayList<Integer> idsToSkip) {
        ArrayList<View> views = new ArrayList<>();
        int state = View.GONE;
        if (visible){
            state = View.VISIBLE;
        }
        HashSet<Integer> idsToIncludeSet = null;
        HashSet<Integer> idsToSkipSet = null;
        if (idsToInclude != null){
            idsToIncludeSet = new HashSet<>(idsToInclude);
        } else {
            idsToSkipSet = new HashSet<>(idsToSkip);
        }

        getViewsByTag(views, viewGroup, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        for (View view : views) {
            if (idsToIncludeSet != null) {
                if (idsToIncludeSet.contains(view.getId())){
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            } else {
                if (!idsToSkipSet.contains(view.getId())){
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public static void invertGroup(ViewGroup viewGroup, int tagResourceId, int imageViewId, int openImageId, int closedImageId) {
        ArrayList<View> views = new ArrayList<>();
        getViewsByTag(views, viewGroup, BrokerApplication.getBrokerApplication().getString(tagResourceId));
        boolean open = false;
        for (View view : views) {
            if (view.getVisibility() == View.VISIBLE){
                view.setVisibility(View.GONE);
                open = false;
            } else {
                view.setVisibility(View.VISIBLE);
                open = true;
            }
        }
        ImageView imageView = (ImageView) viewGroup.findViewById(imageViewId);
        if (open){
            imageView.setImageResource(openImageId);
        } else {
            imageView.setImageResource(closedImageId);
        }
    }

}
