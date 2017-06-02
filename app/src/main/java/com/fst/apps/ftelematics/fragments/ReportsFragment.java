package com.fst.apps.ftelematics.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.fst.apps.ftelematics.AppConstants;
import com.fst.apps.ftelematics.R;

import org.w3c.dom.Text;

/**
 * Created by welcome on 5/26/2016.
 */
public class ReportsFragment extends Fragment {
    private String url;
    private TextView loadingText;
    private WebView myWebView;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        url=bundle.getString("url");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        loadingText=(TextView) view.findViewById(R.id.loading);
        myWebView = (WebView) view.findViewById(R.id.reports_web_view);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new MyWebViewClient());
       /* if(url.contains("FuelMonitoring")){
            Fragment fragment = new CominSoonFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
        }else {*/

        Log.e("URL",url);
            myWebView.loadUrl(url);
        /*}*/
        return view;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loadingText.setVisibility(View.VISIBLE);
            myWebView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loadingText.setVisibility(View.GONE);
            myWebView.setVisibility(View.VISIBLE);
        }
    }
}
