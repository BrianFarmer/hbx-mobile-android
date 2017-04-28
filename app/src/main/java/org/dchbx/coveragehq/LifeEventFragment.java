package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by plast on 3/19/2017.
 */

public class LifeEventFragment extends Fragment {
    private static String TAG = "LifeEventFragment";
    private View view;
    private WebView webView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.web_view, null);
        webView = (WebView)view.findViewById(R.id.webView);
        String data = BrokerApplication.getBrokerApplication().getString(R.string.life_event_html);
        webView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
        webView.setWebViewClient(new WebViewClient());
        return view;
    }
}
