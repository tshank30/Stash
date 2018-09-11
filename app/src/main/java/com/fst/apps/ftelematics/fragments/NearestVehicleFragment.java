package com.fst.apps.ftelematics.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.LOCATION_SERVICE;
import static com.fst.apps.ftelematics.fragments.ParentViewPagerFragment.TAG;

public class NearestVehicleFragment extends Fragment implements View.OnClickListener, LocationListener {


    private GoogleMap googleMap;
    private PlaceAutocompleteFragment autocompleteFragment;
    private Button findNearestVehicle;
    private LatLng latLong;
    View rootView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.nearest_vehicle_layout, container, false);

        autocompleteFragment = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        findNearestVehicle = rootView.findViewById(R.id.nearest_vehicle);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setHint("Enter place to search");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                if (googleMap != null)
                    setUpMap(place.getLatLng(), place.getName().toString(), place.getAddress().toString());

                latLong = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        if (googleMap == null) {
            ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    NearestVehicleFragment.this.googleMap = googleMap;

                    //setUpMap(new LatLng(Double.valueOf(0), Double.valueOf(0)), "", "");

                    if (googleMap == null) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                    } else {
                        checkLocationPermission();
                    }
                }
            });

        }

        findNearestVehicle.setOnClickListener(this);


        return rootView;
    }


    void setUpMap(LatLng lastLocation, String displayName, String address) {

        LatLng origin = lastLocation;
        MarkerOptions options = new MarkerOptions();
        options.position(origin);
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.maplocation));
        options.title(displayName).snippet(address);

        Marker marker = googleMap.addMarker(options);
        marker.setAnchor(0.5f, 0.5f);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(origin, 17);
        googleMap.animateCamera(cameraUpdate);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getFragmentManager().beginTransaction().remove(autocompleteFragment).commit();
    }

    @Override
    public void onClick(View v) {
        if (latLong != null) {
            NearestVehicleList fragment = new NearestVehicleList();
            Bundle bundle = new Bundle();
            bundle.putParcelable("latLong", latLong);
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else {
            Snackbar snackbar = Snackbar
                    .make(rootView, "Select location first", Snackbar.LENGTH_LONG);
            snackbar.show();


        }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission")
                        .setMessage("Please give location permission for better experience")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {

            LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            Location location = mLocationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location==null)
                 location = mLocationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location==null)
                location = mLocationManager
                        .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                String address = AppUtils.reverseGeocode(getActivity(), location.getLatitude(), location.getLongitude());
                latLong = new LatLng(location.getLatitude(), location.getLongitude());
                if (latLong != null)
                    autocompleteFragment.setText(address);
                setUpMap(new LatLng(location.getLatitude(), location.getLongitude()), "Location", address);
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        //Request location updates:
                        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                        Location location = mLocationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        String address = AppUtils.reverseGeocode(getActivity(), location.getLatitude(), location.getLongitude());
                        latLong = new LatLng(location.getLatitude(), location.getLongitude());
                        if (latLong != null)
                            autocompleteFragment.setText(address);
                        setUpMap(new LatLng(location.getLatitude(), location.getLongitude()), "Location", address);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.e("Location", "onLocationChanged");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("Location", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("Location", "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("Location", "onProviderDisabled");
    }
}
