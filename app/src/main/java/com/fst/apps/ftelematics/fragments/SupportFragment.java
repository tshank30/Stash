package com.fst.apps.ftelematics.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fst.apps.ftelematics.R;

/**
 * Created by welcome on 4/23/2016.
 */
public class SupportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_support,container,false);
        return v;
    }
}
