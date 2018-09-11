package com.fst.apps.ftelematics.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fst.apps.ftelematics.BuildConfig;
import com.fst.apps.ftelematics.R;


public class VersionFragment extends Fragment {

    private TextView checkUpdate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_version,container,false);
        checkUpdate=(TextView) v.findViewById(R.id.check_for_update);
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+ getActivity().getPackageName())));
            }
        });
        return v;
    }
}
