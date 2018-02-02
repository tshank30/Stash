package com.fst.apps.ftelematics.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.loaders.LoaderTaskVehicleList;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.MapInfoWindow;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class VehicleMapViewFragment extends Fragment implements LoaderTaskVehicleList.VehicleListInterface{

    private MainActivity activity;
    private Context context;
    private LoaderTaskVehicleList dataTask;
    private long autoRefreshInterval;
    private SharedPreferencesManager sharedPrefs;
    private String url ;
    private AppUtils appUtils;
    private Handler handler = new Handler();
    private GoogleMap googleMap;
    private RadioGroup radioGroup;
    private int mapType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs=new SharedPreferencesManager(getActivity());
        autoRefreshInterval=sharedPrefs.getAutoRefresh();
        appUtils=new AppUtils(context);
        url = appUtils.getLastLocationUrl();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_vehicle_map_view,container,false);
        radioGroup=(RadioGroup) v.findViewById(R.id.map_types);
        //DatabaseHelper databaseHelper = new DatabaseHelper(context);

        dataTask = new LoaderTaskVehicleList(context, false, url, this, true);
       // dataTask.getDataFromDB();
        if (ConnectionDetector.getInstance().isConnectingToInternet(context)) {
            dataTask.execute();
        }else{
            Toast.makeText(context,"Please connect to working internet!",Toast.LENGTH_SHORT).show();
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.satellite_type){
                    mapType=GoogleMap.MAP_TYPE_SATELLITE;
                }else{
                    mapType=GoogleMap.MAP_TYPE_NORMAL;

                }
                if(googleMap!=null){
                    googleMap.setMapType(mapType);
                }
            }
        });

        if (autoRefreshInterval > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.v("Auto Refresh", "Refreshing data after " + autoRefreshInterval + " seconds");
                    dataTask = new LoaderTaskVehicleList(context, false, url, VehicleMapViewFragment.this, false);
                    dataTask.execute();
                    handler.postDelayed(this, autoRefreshInterval);
                }
            }, autoRefreshInterval);
        }
        return v;

    }

    private void initilizeMap(int mapType,List<LastLocation> lastLocationList) {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        if (googleMap == null) {
            Toast.makeText(context, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
        }else{
            setUpMap(googleMap, mapType,lastLocationList);
        }
    }

    private void setUpMap(final GoogleMap mMap,int mapType,List<LastLocation> lastLocationList) {
        mMap.clear();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(lastLocationList!=null && lastLocationList.size()>0) {
            for(LastLocation location:lastLocationList) {
                if (!TextUtils.isEmpty(location.getLatitude()) && !TextUtils.isEmpty(location.getLatitude())) {
                    double latitude = Double.valueOf(location.getLatitude());
                    double longitude = Double.valueOf(location.getLongitude());
                    String address = location.getAddress();
                    String status = location.getStatusCode();
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(location.getDisplayName()).snippet(address);
                    // For showing a move to my loction button
                    //mMap.setMyLocationEnabled(true);
               /* if (mapType == MAP_TYPE_SATELLITE) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }*/

                    // For dropping a marker at a point on the Map
                    if (!TextUtils.isEmpty(status)) {
                        if (status.equalsIgnoreCase("0")) {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_nw));
                        } else if (status.equalsIgnoreCase("61714")) {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_moving));
                        } else if (status.equalsIgnoreCase("61715")) {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_stop));
                        } else if (status.equalsIgnoreCase("61716")) {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_dormant));
                        }
                    } else {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_dormant));
                    }
                    //mMap.clear();
                    builder.include(marker.getPosition());
                    mMap.addMarker(marker);
                    mMap.setInfoWindowAdapter(new MapInfoWindow(context, "MAP"));
                }
                final LatLngBounds bounds = builder.build();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    public void onMapLoaded() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }
        }
    }


    @Override
    public void onProcessComplete(List<LastLocation> lastLocationList) {
        initilizeMap(1,lastLocationList);
    }

    @Override
    public void noConnectionNoDB() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        this.context = context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(context == null)
            context = getActivity();
    }
}
