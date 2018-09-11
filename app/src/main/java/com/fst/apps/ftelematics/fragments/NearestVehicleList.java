package com.fst.apps.ftelematics.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.adapters.NearestVehicleAdapter;
import com.fst.apps.ftelematics.adapters.VehiclesListAdapter;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.loaders.LoaderTaskSortedVehicleList;
import com.fst.apps.ftelematics.loaders.LoaderTaskVehicleList;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.LocationComparator;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;

public class NearestVehicleList extends Fragment implements LoaderTaskSortedVehicleList.VehicleListInterface {

    private Context context;
    private AppUtils appUtils;
    private String url;
    private LoaderTaskSortedVehicleList dataTask;
    private List<LastLocation> locationList;
    private Double fromLat, fromLong;
    private RecyclerView recyclerView;
    private LatLng latLng;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        appUtils = new AppUtils(context);
        url = appUtils.getLastLocationUrl();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.nearest_vehicle_list, container, false);
        recyclerView = rootView.findViewById(R.id.list);
        latLng = getArguments().getParcelable("latLong");
        fromLong = latLng.longitude;
        fromLat = latLng.latitude;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dataTask = new LoaderTaskSortedVehicleList(context, true, url, this, fromLat, fromLong);
        if (ConnectionDetector.getInstance().isConnectingToInternet(context))
            dataTask.execute();

        return rootView;
    }

    @Override
    public void onProcessComplete(List<LastLocation> lastLocationList) {
        locationList = lastLocationList;
        NearestVehicleAdapter adapter = new NearestVehicleAdapter(locationList, context,latLng);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void noConnectionNoDB() {

    }
}
