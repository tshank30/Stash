package com.fst.apps.ftelematics.fragments;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.loaders.LoaderTaskVehicleList;
import com.fst.apps.ftelematics.soapclient.IAppManager;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.MapInfoWindow;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class VehicleMapViewFrag extends Fragment implements LoaderTaskVehicleList.VehicleListInterface {


    private LoaderTaskVehicleList dataTask;
    private LastLocation lastLocation;
    private long autoRefreshInterval;
    private SharedPreferencesManager sharedPrefs;
    private String url;
    private AppUtils appUtils;
    private Handler handler = new Handler();
    private GoogleMap googleMap;
    private RadioGroup radioGroup;
    private FloatingActionButton shareLive;
    private TextView speedTextView, timestampText, totalDistanceTextView;
    private int mapType;
    private ArrayList MarkerPoints;
    //private TextView distance, duration, current_time, clear, arrival_status, vehicle_no;
    private String x, t;
    private Calendar calendar = Calendar.getInstance();
    private Calendar newDate;
    static int updateVariable = 0;
    private Marker marker;
    private int no_of_hits = 0;
    private LatLng origin, dest, newloc, prevloc, stop;
    private Polyline polyline;
    private boolean isMarkerRotating = false;
    private ImageView notification, logout;
    private AlertDialog dialog;
    private PolylineOptions options;
    private int year, monthOfYear, dayOfMonth;
    private ValueAnimator markerAnimator;
    private DatePickerDialog datePickerDialog_from;
    private TimePickerDialog timeFromDialog;
    private View rootView;
    private int retry = 0;
    private boolean refresh = true;
    private IAppManager distanceManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (markerAnimator != null)
            markerAnimator.cancel();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_vehicle_map_view, container, false);
        radioGroup = rootView.findViewById(R.id.map_types);
        speedTextView = rootView.findViewById(R.id.speedText);
        totalDistanceTextView = rootView.findViewById(R.id.totalDistanceText);
        timestampText = rootView.findViewById(R.id.timestampText);
        shareLive = rootView.findViewById(R.id.share_live);
        refresh = true;

        sharedPrefs = new SharedPreferencesManager(getActivity());
        autoRefreshInterval = 30000;
        appUtils = new AppUtils(getActivity());
        url = appUtils.getLastLocationUrl();
        Bundle bundle = getArguments();
        lastLocation = bundle.getParcelable("lastLocation");

        if (lastLocation != null) {
            origin = new LatLng(Double.valueOf(lastLocation.getLatitude()), Double.valueOf(lastLocation.getLongitude()));
            //vehicleNumber=lastLocation.getDisplayName();
        }

        //DatabaseHelper databaseHelper = new DatabaseHelper(context);

        MarkerPoints = new ArrayList<LatLng>();
        no_of_hits = 0;


        distanceManager = ((MainActivity) getActivity()).getSoapServiceInstance();
        new DistanceTask(sharedPrefs.getAccountId(), lastLocation.getDeviceID(), distanceManager).execute();
        speedTextView.setText(lastLocation.getSpeedKPH() + " KMPH");
        timestampText.setText(AppUtils.getDateFromUnixTimestsmp(lastLocation.getUnixTime()));


        if (googleMap == null) {
            com.google.android.gms.maps.MapFragment fragment = (com.google.android.gms.maps.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (fragment != null)
                fragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        if ( googleMap != null) {
                            VehicleMapViewFrag.this.googleMap = googleMap;

                            setUpMap(lastLocation, false);

                            Log.e("Location", lastLocation.getLatitude() + " & " + lastLocation.getLongitude());
                            no_of_hits++; // to differentiate first hit


                            if (googleMap == null) {
                                Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        }

        options = new PolylineOptions().width(10).color(Color.rgb(3, 102, 13));


        mapType = GoogleMap.MAP_TYPE_NORMAL;

        // new GoogleDirectionApiLoader(context, "C+block+sector+53+noida", "I+block+sector+22+noida").execute();

        //new VehicleStopLoader(context, this).execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (refresh) {
                    dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(VehicleMapViewFrag.this), true);
                    if (ConnectionDetector.getInstance().isConnectingToInternet(getActivity())) {
                        dataTask.execute();
                    }
                }/*else {

                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "Please connect to working internet!", Toast.LENGTH_SHORT).show();
                }*/
            }
        }, 1500);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.satellite_type) {
                    mapType = GoogleMap.MAP_TYPE_HYBRID;
                } else {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;

                }
                if (googleMap != null) {
                    googleMap.setMapType(mapType);
                    //setUpMap(googleMap, lastLocation, mapType);
                } else {
                    ArrayList locationList = new ArrayList<LastLocation>();
                    locationList.add(lastLocation);
                    initilizeMap(mapType, locationList);
                }
            }
        });

        shareLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog_from.show();
            }
        });


        if (autoRefreshInterval > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (refresh) {
                        Log.v("Auto Refresh", "Refreshing data after " + autoRefreshInterval + " seconds");
                        dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(VehicleMapViewFrag.this), false);
                        dataTask.execute();
                        handler.postDelayed(this, autoRefreshInterval);
                    }
                }
            }, autoRefreshInterval);
        }

        datePickerDialog_from = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                if (calendar.getTime().getTime() < newDate.getTime().getTime()) {
                    VehicleMapViewFrag.this.year = year;
                    VehicleMapViewFrag.this.monthOfYear = monthOfYear;
                    VehicleMapViewFrag.this.dayOfMonth = dayOfMonth;
                    timeFromDialog.show();
                } else {
                    Snackbar.make(rootView, "Date should be greater than current date", Snackbar.LENGTH_LONG).show();
                }

            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));


        timeFromDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                newDate.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);

                if (calendar.getTime().getTime() < newDate.getTime().getTime()) {
                    //timeFromDialog.show();
                    long currentTime = System.currentTimeMillis() / 1000;
                    new LiveUrlUpdate(lastLocation.getAccountID(), lastLocation.getDeviceID(), Long.toString(currentTime), Long.toString(newDate.getTime().getTime() / 1000)).execute();
                    String shareBody = "http://205.147.110.119:81/LiveLink.aspx?id=" + currentTime;

                    //String shareBody = "Visit <a href=\<%shareBody%>\">Track Your Vehicle</a> for more info.";

                    String shareLink = Html.fromHtml("<a href=\"" + shareBody + "\">" + "Track Your Vehicle" + "</a>").toString();


                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Track you vehicle");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

                } else
                    Snackbar.make(rootView, "Time should be greater than current Time", Snackbar.LENGTH_LONG).show();


            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);


        return rootView;

    }


    public String getDisplayTime(int hours, int minutes) {
        StringBuilder sb = new StringBuilder();
        if (hours < 10) {
            sb.append("0" + String.valueOf(hours));
        } else {
            sb.append(String.valueOf(hours));
        }

        sb.append(":");

        if (minutes < 10) {
            sb.append("0" + String.valueOf(minutes));
        } else {
            sb.append(String.valueOf(minutes));
        }

        return sb.toString();
    }

    /* "statusCode": 61715 for Stop
                     61714 for moving
                     61714 for idle
    */
    private void initilizeMap(int mapType, List<LastLocation> lastLocationList) {
        if (googleMap == null) {
            com.google.android.gms.maps.MapFragment fragment = (com.google.android.gms.maps.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (fragment != null)
                fragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        VehicleMapViewFrag.this.googleMap = googleMap;
                    }
                });
        }

        if (googleMap != null) {
            //vehicle_no.setText("Vehicle : " + lastLocationList.get(0).getDisplayName());
            if (no_of_hits > 0) {

                for (LastLocation freshLastLocation : lastLocationList) {
                    if (lastLocation.getDeviceID().equalsIgnoreCase(freshLastLocation.getDeviceID())) {
                        newloc = new LatLng(Double.valueOf(freshLastLocation.getLatitude()), Double.valueOf(freshLastLocation.getLongitude()));
                        if (freshLastLocation.getStatusCode().equalsIgnoreCase("61714")) {
                            origin = marker.getPosition();
                            dest = newloc;
                            bearingBetweenLocations(marker, origin, dest, freshLastLocation.getStatusCode(), freshLastLocation.getTIMESTAMP());
                            //makePolyLine(freshLastLocation);
                            Log.e("Location", dest.toString() + "\n" + origin.toString() + "\n\n");
                        }
                        no_of_hits++;
                        prevloc = new LatLng(Double.valueOf(freshLastLocation.getLatitude()), Double.valueOf(freshLastLocation.getLongitude()));
                        speedTextView.setText(freshLastLocation.getSpeedKPH() + " KMPH");
                        timestampText.setText(AppUtils.getDateFromUnixTimestsmp(freshLastLocation.getUnixTime()));
                        if (distanceManager != null)
                            new DistanceTask(sharedPrefs.getAccountId(), freshLastLocation.getDeviceID(), distanceManager).execute();

                        String status = freshLastLocation.getStatusCode();
                        String vehicleType = freshLastLocation.getVehicleType();
                        if (!TextUtils.isEmpty(status)) {
                            if (status.equalsIgnoreCase("0")) {
                                if (vehicleType.equalsIgnoreCase("Car"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                                else if (vehicleType.equalsIgnoreCase("Bus"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_nw));
                                else if (vehicleType.equalsIgnoreCase("truck"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_nw));
                                else if (vehicleType.equalsIgnoreCase("bike"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_nw));
                                else if (vehicleType.equalsIgnoreCase("jcb"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_nw));
                                else
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                            } else if (status.equalsIgnoreCase("61714")) {
                                if (vehicleType.equalsIgnoreCase("Car"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                                else if (vehicleType.equalsIgnoreCase("Bus"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_moving));
                                else if (vehicleType.equalsIgnoreCase("truck"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_moving));
                                else if (vehicleType.equalsIgnoreCase("bike"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_moving));
                                else if (vehicleType.equalsIgnoreCase("jcb"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_moving));
                                else
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                            } else if (status.equalsIgnoreCase("61715")) {
                                if (vehicleType.equalsIgnoreCase("Car"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                                else if (vehicleType.equalsIgnoreCase("Bus"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_stop));
                                else if (vehicleType.equalsIgnoreCase("truck"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_stop));
                                else if (vehicleType.equalsIgnoreCase("bike"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_stop));
                                else if (vehicleType.equalsIgnoreCase("jcb"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_stop));
                                else
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                            } else if (status.equalsIgnoreCase("61716")) {
                                if (vehicleType.equalsIgnoreCase("Car"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                                else if (vehicleType.equalsIgnoreCase("Bus"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_dormant));
                                else if (vehicleType.equalsIgnoreCase("truck"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_dormant));
                                else if (vehicleType.equalsIgnoreCase("bike"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_dormant));
                                else if (vehicleType.equalsIgnoreCase("jcb"))
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_dormant));
                                else
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                            }
                        } else
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.school_bus));

                        //lastLocation = freshLastLocation;
                        break;

                    }
                }


                // googleMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
            }



            /*setUpMap(googleMap, mapType, lastLocationList);*/
            //mapOnclickLister();
        }
    }


    @Override
    public void onProcessComplete(List<LastLocation> lastLocationList) {

        initilizeMap(mapType, lastLocationList);
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


    public class DataParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;
            JSONObject distance;
            JSONObject duration;


            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");


                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        distance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                        duration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");

                       /* for (int z=0;z<distance.length();z++) {
                            x = ((JSONObject) distance.get(j)).getJSONArray("text").toString();
                            Log.e("distance",x);
                        }*/

                        x = distance.optString("text");
                        distance.optString("value");
                        Log.e("distance", x);

                        t = duration.optString("text");
                        duration.optString("value");
                        Log.e("duration", t);


                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }


            return routes;
        }


        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

    void setUpMap(LastLocation lastLocation, boolean stop) {
        origin = new LatLng(Double.valueOf(lastLocation.getLatitude()), Double.valueOf(lastLocation.getLongitude()));

        MarkerPoints.add(origin);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(origin);
        String status = lastLocation.getStatusCode();
        String vehicleType = lastLocation.getVehicleType();

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED.
         */
        if (stop) {
            //options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.loc_point));
        } else /*if (MarkerPoints.size() == 2) */ {

            if (!TextUtils.isEmpty(status)) {
                if (status.equalsIgnoreCase("0")) {
                    if (vehicleType.equalsIgnoreCase("Car"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                    else if (vehicleType.equalsIgnoreCase("Bus"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_nw));
                    else if (vehicleType.equalsIgnoreCase("truck"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_nw));
                    else if (vehicleType.equalsIgnoreCase("bike"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_nw));
                    else if (vehicleType.equalsIgnoreCase("jcb"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_nw));
                    else
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_nw));
                } else if (status.equalsIgnoreCase("61714")) {
                    if (vehicleType.equalsIgnoreCase("Car"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                    else if (vehicleType.equalsIgnoreCase("Bus"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_moving));
                    else if (vehicleType.equalsIgnoreCase("truck"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_moving));
                    else if (vehicleType.equalsIgnoreCase("bike"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_moving));
                    else if (vehicleType.equalsIgnoreCase("jcb"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_moving));
                    else
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_moving));
                } else if (status.equalsIgnoreCase("61715")) {
                    if (vehicleType.equalsIgnoreCase("Car"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                    else if (vehicleType.equalsIgnoreCase("Bus"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_stop));
                    else if (vehicleType.equalsIgnoreCase("truck"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_stop));
                    else if (vehicleType.equalsIgnoreCase("bike"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_stop));
                    else if (vehicleType.equalsIgnoreCase("jcb"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_stop));
                    else
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_stop));
                } else if (status.equalsIgnoreCase("61716")) {
                    if (vehicleType.equalsIgnoreCase("Car"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                    else if (vehicleType.equalsIgnoreCase("Bus"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bus_dormant));
                    else if (vehicleType.equalsIgnoreCase("truck"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_truck_dormant));
                    else if (vehicleType.equalsIgnoreCase("bike"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_bike_dormant));
                    else if (vehicleType.equalsIgnoreCase("jcb"))
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_jcb_dormant));
                    else
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_car_dormant));
                }
            } else
                options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.school_bus));
        }


        options.title(lastLocation.getDisplayName()).snippet(lastLocation.getAddress());


        // Add new marker to the Google Map Android API V2
        marker = googleMap.addMarker(options);
        marker.setAnchor(0.5f, 0.5f);

        // Checks, whether start and end locations are captured
        if (MarkerPoints.size() == 1) {
            LatLng origin = (LatLng) MarkerPoints.get(0);

            // Getting URL to the Google Directions API
            // String url = getDirectionsUrl(origin, dest);
            Log.d("onMapClick", url.toString());

            // FetchUrl FetchUrl = new FetchUrl();
            // Start downloading json data from Google Directions API
            // FetchUrl.execute(url);
            // move map camera

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(origin, 17);
            googleMap.animateCamera(cameraUpdate);
            // googleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
            // googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        }
    }

    public static void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint, final Bitmap bitmap) {


        Marker marker = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(directionPoint.get(0))
                .flat(true));

        // myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));

        animateMarker(myMap, marker, directionPoint, false);

    }


    private static void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                                      final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = null;

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            Double lat = startValue.latitude
                    + ((endValue.latitude - startValue.latitude) * fraction);
            Double lng = startValue.longitude
                    + ((endValue.longitude - startValue.longitude) * fraction);
            latLng = new LatLng(lat, lng);
            return latLng;
        }
    }

    private double bearingBetweenLocations(Marker marker, LatLng latLng1, LatLng latLng2, String statusCode, String timeStamp) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        Log.e("Bearing", "" + brng);

        rotateMarker(marker, (float) brng, latLng1, latLng2, statusCode, timeStamp);
        return brng;
    }

    private void rotateMarker(final Marker marker, final float toRotation, final LatLng origin, final LatLng dest, final String statusCode, final String timeStamp) {
        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 500;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setAnchor(0.5f, 0.5f);
                    marker.setRotation(-rot > 180 ? rot / 2 : rot);


                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;

                        float dist = distance(origin.latitude, origin.longitude, dest.latitude, dest.longitude);
                        Log.e("distance", "" + dist);
                        if (dist > 10) {

                            markerAnimator = ObjectAnimator.ofObject(marker, "position",
                                    new LatLngEvaluator(), origin, dest);
                            markerAnimator.setInterpolator(new LinearInterpolator());

                            updateVariable = 0;
                            // static int x=0;
                            markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {

                                    Log.e("locUpdate", "" + animation.getAnimatedFraction() + " " + animation.getAnimatedValue());

//                                if(animation.getAnimatedValue() instanceof  LatLng)
//                                //makePolyLine((LatLng)animation.getAnimatedValue());
//                                {
                                    updateVariable++;
                                    if (updateVariable > 40) {
                                        updateVariable = 0;
                                        Log.e("loctn", "" + animation.getAnimatedFraction() + " " + animation.getAnimatedValue());
                                        Log.e("polyline", "added " + updateVariable);
                                        double latitude = ((LatLng) animation.getAnimatedValue()).latitude;

                                        double longitude = ((LatLng) animation.getAnimatedValue()).longitude;

                                        LatLng point = new LatLng(latitude, longitude);
                                        //PolylineOptions options=new PolylineOptions();
                                        options.add(point);
//                                        if (animation.getAnimatedFraction() == 1) {
//                                            String status = statusCode;
//                                            String address = AppUtils.reverseGeocode(getActivity(), latitude, longitude);
//                                            MarkerOptions marker = new MarkerOptions().position(point).title(AppUtils.getStatusOfVehicle(status)).snippet(timeStamp + "\n" + address);
//
//                                            googleMap.setInfoWindowAdapter(new MapInfoWindow(getActivity(), "LIVE"));
//                                            Marker markerPin = googleMap.addMarker(marker);
//                                            markerPin.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.loc_point));
//                                            //markerPin.setVisible(false);
//                                        }
                                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f));
                                        googleMap.addPolyline(options);
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom((LatLng) animation.getAnimatedValue(), googleMap.getCameraPosition().zoom);
                                        googleMap.animateCamera(cameraUpdate);
                                    }

                                }
                            });
                            markerAnimator.setDuration(28000);
                            markerAnimator.start();

                        }
                        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(dest);
                        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(dest, googleMap.getCameraPosition().zoom);
                        //googleMap.animateCamera(cameraUpdate);
                    }
                }
            });
        }//25.881291,79.214996)
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }




    private void makePolyLine(LastLocation lastHistoryLocation) {
        //googleMap.setMapType(mapType);

        double latitude = Double.valueOf(lastHistoryLocation.getLatitude());
        double longitude = Double.valueOf(lastHistoryLocation.getLongitude());
        String status = lastHistoryLocation.getStatusCode();
        String address = AppUtils.reverseGeocode(getActivity(), latitude, longitude);
        LatLng point = new LatLng(latitude, longitude);
        //PolylineOptions options=new PolylineOptions();
        options.add(point);
        MarkerOptions marker = new MarkerOptions().position(point).title(AppUtils.getStatusOfVehicle(status)).snippet(lastHistoryLocation.getStringTimestamp() + "\n" + address);

        googleMap.setInfoWindowAdapter(new MapInfoWindow(getActivity(), "LIVE"));
        Marker markerPin = googleMap.addMarker(marker);
        markerPin.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.loc_point));
        //markerPin.setVisible(false);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f));
        googleMap.addPolyline(options);
    }


    class DistanceTask extends AsyncTask<Void, Void, String> {

        String accountID, deviceID;
        private IAppManager distanceManager;

        public DistanceTask(String accountID, String deviceID, IAppManager manager) {
            this.accountID = accountID;
            this.deviceID = deviceID;
            this.distanceManager = manager;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            if (distanceManager != null)
                result = distanceManager.getTotalDistance(accountID, deviceID);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null)
                totalDistanceTextView.setText(result + " Kms");
        }
    }

    private float distance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    class LiveUrlUpdate extends AsyncTask<Void, Void, String> {

        String accountID, deviceID, currentTime, activationTime;

        public LiveUrlUpdate(String accountID, String deviceID, String currentTime, String activationTime) {
            this.accountID = accountID;
            this.deviceID = deviceID;
            this.currentTime = currentTime;
            this.activationTime = activationTime;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = ((MainActivity) getActivity()).getSoapServiceInstance().setLiveUrlParams(accountID, deviceID, currentTime, activationTime);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!"success".equalsIgnoreCase(result) && retry <= 3) {
                retry++;
                new LiveUrlUpdate(lastLocation.getAccountID(), lastLocation.getDeviceID(), Long.toString(System.currentTimeMillis()), Long.toString(newDate.getTime().getTime())).execute();
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refresh = false;
    }
}
