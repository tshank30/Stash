package com.fst.apps.ftelematics.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Fragment;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.List;

public class VehicleMapViewFragment extends Fragment implements LoaderTaskVehicleList.VehicleListInterface {


    private LoaderTaskVehicleList dataTask;
    private long autoRefreshInterval;
    private SharedPreferencesManager sharedPrefs;
    private String url;
    private AppUtils appUtils;
    private Handler handler = new Handler();
    private GoogleMap googleMap;
    private RadioGroup radioGroup;
    private int mapType;
    private boolean refresh;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.all_vehicle_map, container, false);
        radioGroup = (RadioGroup) v.findViewById(R.id.map_types);
        //DatabaseHelper databaseHelper = new DatabaseHelper(context);

        sharedPrefs = new SharedPreferencesManager(getActivity());
        autoRefreshInterval = sharedPrefs.getAutoRefresh();
        appUtils = new AppUtils(getActivity());
        url = appUtils.getLastLocationUrl();
        refresh = true;
        dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(this), true);
        // dataTask.getDataFromDB();
        if (ConnectionDetector.getInstance().isConnectingToInternet(getActivity().getApplicationContext())) {
            dataTask.execute();
        } else {
            Toast.makeText(getActivity(), "Please connect to working internet!", Toast.LENGTH_SHORT).show();
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.satellite_type) {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                } else {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;

                }
                if (googleMap != null) {
                    googleMap.setMapType(mapType);
                }
            }
        });

        if (autoRefreshInterval > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (refresh) {
                        Log.v("Auto Refresh", "Refreshing data after " + autoRefreshInterval + " seconds");
                        dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(VehicleMapViewFragment.this), false);
                        dataTask.execute();
                        handler.postDelayed(this, autoRefreshInterval);
                    }
                }
            }, autoRefreshInterval);
        }
        return v;

    }

    private void initilizeMap(final int mapType, final List<LastLocation> lastLocationList) {
        if (googleMap == null) {
            com.google.android.gms.maps.MapFragment fragment = (com.google.android.gms.maps.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (fragment != null)
                fragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        VehicleMapViewFragment.this.googleMap = googleMap;
                        VehicleMapViewFragment.this.googleMap.setInfoWindowAdapter(new MapInfoWindow(getActivity(), "HISTORY"));
                        if (VehicleMapViewFragment.this.googleMap == null) {
                            Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                        } else {
                            setUpMap(googleMap, mapType, lastLocationList);
                        }
                    }
                });
        }

        if (googleMap == null) {
            Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
        } else {
            setUpMap(googleMap, mapType, lastLocationList);
        }
    }

    private void setUpMap(final GoogleMap mMap, int mapType, List<LastLocation> lastLocationList) {
        mMap.clear();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (lastLocationList != null && lastLocationList.size() > 0) {
            for (LastLocation location : lastLocationList) {
                if (!TextUtils.isEmpty(location.getLatitude()) && !TextUtils.isEmpty(location.getLatitude())) {
                    double latitude = Double.valueOf(location.getLatitude());
                    double longitude = Double.valueOf(location.getLongitude());
                    String address = location.getAddress();
                    String status = location.getStatusCode();
                    String vehicleType = location.getVehicleType();
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
                            if (vehicleType.equalsIgnoreCase("Car"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                            else if (vehicleType.equalsIgnoreCase("Bus"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_nw));
                            else if (vehicleType.equalsIgnoreCase("truck"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_nw));
                            else if (vehicleType.equalsIgnoreCase("bike"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_nw));
                            else if (vehicleType.equalsIgnoreCase("jcb"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_nw));
                            else
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                        } else if (status.equalsIgnoreCase("61714")) {
                            if (vehicleType.equalsIgnoreCase("Car"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                            else if (vehicleType.equalsIgnoreCase("Bus"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_moving));
                            else if (vehicleType.equalsIgnoreCase("truck"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_moving));
                            else if (vehicleType.equalsIgnoreCase("bike"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_moving));
                            else if (vehicleType.equalsIgnoreCase("jcb"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_moving));
                            else
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                        } else if (status.equalsIgnoreCase("61715")) {
                            if (vehicleType.equalsIgnoreCase("Car"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                            else if (vehicleType.equalsIgnoreCase("Bus"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_stop));
                            else if (vehicleType.equalsIgnoreCase("truck"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_stop));
                            else if (vehicleType.equalsIgnoreCase("bike"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_stop));
                            else if (vehicleType.equalsIgnoreCase("jcb"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_stop));
                            else
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                        } else if (status.equalsIgnoreCase("61716")) {
                            if (vehicleType.equalsIgnoreCase("Car"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                            else if (vehicleType.equalsIgnoreCase("Bus"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_dormant));
                            else if (vehicleType.equalsIgnoreCase("truck"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_dormant));
                            else if (vehicleType.equalsIgnoreCase("bike"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_dormant));
                            else if (vehicleType.equalsIgnoreCase("jcb"))
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_dormant));
                            else
                                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                        }
                    } else {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                    }
                    //mMap.clear();
                    builder.include(marker.getPosition());
                    mMap.addMarker(marker);
                    mMap.setInfoWindowAdapter(new MapInfoWindow(getActivity(), "MAP"));
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
        initilizeMap(1, lastLocationList);
    }

    @Override
    public void noConnectionNoDB() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refresh = false;
    }
}
