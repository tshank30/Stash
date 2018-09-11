package com.fst.apps.ftelematics.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.HistoryData;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.MapInfoWindow;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Re-usable component.
 *
 * @author ddewaele
 */
public class AnimatingMarkersFragment extends Fragment {


    // Keep track of our markers
    private List<Marker> markers = new ArrayList<Marker>();

    private final Handler mHandler = new Handler();

    private Marker selectedMarker;

    private DatePickerDialog datePickerDialog_from, datePickerDialog_to;
    private TimePickerDialog timeFromDialog, timeToDialog;
    private Calendar calender;
    private LinearLayout dateLayout_from, dateLayout_to, timeFromLayout, timeToLayout;
    private RelativeLayout actionButtonsLayout, plotPath;
    private TextView fromDateTextView, toDateTextView, timeFromTextView, timeToTextView, historySpeed;
    private SimpleDateFormat dateFormatter;
    private GoogleMap googleMap;
    private MainActivity mActivity;
    private TextView playButton, pauseButton;
    private SeekBar speedSeekBar;
    private LastLocation lastLocation;
    private SharedPreferencesManager sharedPrefs;
    private String date_from, date_to, timeFrom, timeTo;
    private Runnable runnable;
    private int historyDelay = 1600;
    private ProgressDialog progressDialog;
    private Resources res;
    private RadioGroup radioGroup;
    private NetworkUtility networkUtility;
    private int ANIMATE_SPEEED = 1500;
    private MainActivity activity;
    private ArrayList<HistoryData> historyList;
    private boolean isStartAnimation;

    Handler handler = new Handler();
    Random random = new Random();
    Runnable runner = new Runnable() {
        @Override
        public void run() {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        calender = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
        lastLocation = bundle.getParcelable("lastLocation");
        sharedPrefs = new SharedPreferencesManager(getActivity());
        res = getActivity().getResources();
        networkUtility = new NetworkUtility();
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    public static AnimatingMarkersFragment newInstance(int position, String title) {
        AnimatingMarkersFragment fragment = new AnimatingMarkersFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("title", title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //handler.postDelayed(runner, random.nextInt(2000));

        View v = inflater.inflate(R.layout.fragment_history, container, false);
        //setHasOptionsMenu(true);
        dateLayout_from = (LinearLayout) v.findViewById(R.id.date_layout);
        dateLayout_to = (LinearLayout) v.findViewById(R.id.date_to_layout);
        timeFromLayout = (LinearLayout) v.findViewById(R.id.timefrom_layout);
        timeToLayout = (LinearLayout) v.findViewById(R.id.timeto_layout);
        fromDateTextView = (TextView) v.findViewById(R.id.date_from_value);
        toDateTextView = (TextView) v.findViewById(R.id.date_to_value);
        timeFromTextView = (TextView) v.findViewById(R.id.timefrom_value);
        timeToTextView = (TextView) v.findViewById(R.id.timeto_value);
        playButton = (TextView) v.findViewById(R.id.play_button);
        pauseButton = (TextView) v.findViewById(R.id.pause_button);
        speedSeekBar = (SeekBar) v.findViewById(R.id.speed);
        radioGroup = (RadioGroup) v.findViewById(R.id.map_types);
        actionButtonsLayout = (RelativeLayout) v.findViewById(R.id.action_buttons);
        plotPath = (RelativeLayout) v.findViewById(R.id.plot_path);
        progressDialog = new ProgressDialog(getActivity());
        getMap();


        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handlePlayPauseButton(0);
                if (markers != null && markers.size() > 0) {
                    if (animator.currentIndex > 0) {
                        animator.start = SystemClock.uptimeMillis();
                        mHandler.postDelayed(animator, 16);
                    } else {
                        animator.startAnimation(true);
                    }
                } else {
                    getHistoryData();
                }
                isStartAnimation = true;
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handlePlayPauseButton(1);
                if (animator != null) {
                    animator.stopAnimation();
                }
            }
        });

        plotPath.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isStartAnimation = false;
                getHistoryData();
            }
        });

        dateLayout_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog_from.show();
            }
        });


        dateLayout_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog_to.show();
            }
        });

        timeFromLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                timeFromDialog.show();
            }
        });

        timeToLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                timeToDialog.show();
            }
        });


        datePickerDialog_from = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if (year >= 2000)
                    fromDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + (year - 2000));
                else if (year < 2000)
                    fromDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + (year - 1900));
                date_from = dateFormatter.format(newDate.getTime());

                fromDateTextView.setText(date_from);
                handler.removeCallbacks(runnable);
                if (markers != null && markers.size() > 0) {
                    markers.clear();
                }
                if (animator.trackingMarker != null) {
                    animator.trackingMarker.remove();
                }
                handlePlayPauseButton(1);
            }

        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));

        datePickerDialog_to = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if (year >= 2000)
                    toDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + (year - 2000));
                else if (year < 2000)
                    toDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + (year - 1900));
                date_to = dateFormatter.format(newDate.getTime());

                toDateTextView.setText(date_to);
                handler.removeCallbacks(runnable);
                if (markers != null && markers.size() > 0) {
                    markers.clear();
                }
                if (animator.trackingMarker != null) {
                    animator.trackingMarker.remove();
                }
                handlePlayPauseButton(1);
            }

        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));

        timeFromDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timeFromTextView.setText(getDisplayTime(hourOfDay, minute));
                timeFrom = getDisplayTime(hourOfDay, minute);
                handler.removeCallbacks(runnable);
                runnable = null;
                if (markers != null && markers.size() > 0) {
                    markers.clear();
                }
                if (animator.trackingMarker != null) {
                    animator.trackingMarker.remove();
                }
                handlePlayPauseButton(1);
            }
        }, calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE), true);

        timeToDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timeToTextView.setText(getDisplayTime(hourOfDay, minute));
                timeTo = getDisplayTime(hourOfDay, minute);
                handler.removeCallbacks(runnable);
                runnable = null;
                if (markers != null && markers.size() > 0) {
                    markers.clear();
                }
                if (animator.trackingMarker != null) {
                    animator.trackingMarker.remove();
                }
                handlePlayPauseButton(1);
            }
        }, calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE), true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (googleMap != null) {
                    toggleStyle();
                }
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ANIMATE_SPEEED = getHistoryDelay(seekBar.getProgress());
                Log.v("Progress", historyDelay + "");
				/*if(runnable!=null){
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, historyDelay);
				}*/
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                //historySpeed.setText(progress);
            }
        });


        return v;
    }

    private void getMap() {
        if (googleMap == null) {
           ((com.google.android.gms.maps.MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    AnimatingMarkersFragment.this.googleMap = googleMap;
                    AnimatingMarkersFragment.this.googleMap.setInfoWindowAdapter(new MapInfoWindow(getActivity(), "HISTORY"));
                    if (AnimatingMarkersFragment.this.googleMap == null) {
                        Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private Animator animator = new Animator();


    int currentPt;

    CancelableCallback MyCancelableCallback =
            new CancelableCallback() {

                @Override
                public void onCancel() {
                    System.out.println("onCancelled called");
                }

                @Override
                public void onFinish() {


                    if (++currentPt < markers.size()) {

                        float targetBearing = bearingBetweenLatLngs(googleMap.getCameraPosition().target, markers.get(currentPt).getPosition());

                        LatLng targetLatLng = markers.get(currentPt).getPosition();
                        //float targetZoom = zoomBar.getProgress();


                        System.out.println("currentPt  = " + currentPt);
                        System.out.println("size  = " + markers.size());
                        //Create a new CameraPosition
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(targetLatLng)
                                        .tilt(currentPt < markers.size() - 1 ? 90 : 0)
                                        .bearing(targetBearing)
                                        .zoom(googleMap.getCameraPosition().zoom)
                                        .build();


                        googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                3000,
                                MyCancelableCallback);
                        System.out.println("Animate to: " + markers.get(currentPt).getPosition() + "\n" +
                                "Bearing: " + targetBearing);

                        markers.get(currentPt);
                        /*.showInfoWindow();*/

                    } else {
                        //info.setText("onFinish()");
                    }

                }

            };

    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;
        float zoom = 15.5f;
        boolean upward = true;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() {
            //trackingMarker.remove();
            mHandler.removeCallbacks(animator);

        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();

            setupCameraPositionForMovement(markerPos, secondPos);

        }

        private void setupCameraPositionForMovement(LatLng markerPos,
                                                    LatLng secondPos) {

            IconDrawable d = new IconDrawable(getActivity(), FontAwesomeIcons.fa_truck).colorRes(R.color.bloodRed)
                    .actionBarSize();
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            d.draw(canvas);

            float bearing = bearingBetweenLatLngs(markerPos, secondPos);

            trackingMarker = googleMap.addMarker(new MarkerOptions().position(markerPos)
                    .title("title")
                    .snippet("snippet"));
            trackingMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));


            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(markerPos)
                            .bearing(bearing + BEARING_OFFSET)
                            .tilt(90)
                            .zoom(googleMap.getCameraPosition().zoom >= 16 ? googleMap.getCameraPosition().zoom : 16)
                            .build();

            googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(markers.get(0).getPosition());
            return googleMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }


        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size() > 2) {
                animator.initialize(showPolyLine);
            }
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);

//			LatLng endLatLng = getEndLatLng();
//			LatLng beginLatLng = getBeginLatLng();
            if (endLatLng == null) {
                endLatLng = getEndLatLng();
            }

            if (beginLatLng == null) {
                beginLatLng = getBeginLatLng();
            }

            if (trackingMarker == null) {
                trackingMarker = googleMap.addMarker(new MarkerOptions().position(beginLatLng)
                        .title("title")
                        .snippet("snippet"));
            }
            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (showPolyline) {
                updatePolyLine(newPosition);
            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < markers.size() - 2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngs(begin, end);
                    if (historyList != null && historyList.size() > 0) {
                        HistoryData historyData = historyList.get(currentIndex);
                        //   highLightMarker(currentIndex,historyData.getStatusCode());
                    } else {
                        //   highLightMarker(currentIndex);
                    }

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build();


                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    //currentIndex++;
                    start = SystemClock.uptimeMillis();
                    //highLightMarker(currentIndex);
                    stopAnimation();
                }

            }
        }


        private LatLng getEndLatLng() {
            return markers.get(currentIndex + 1).getPosition();
        }

        private LatLng getBeginLatLng() {
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {
            //System.out.println("tilt = " + tilt);
            //System.out.println("upward = " + upward);
            //System.out.println("zoom = " + zoom);
            if (upward) {

                if (tilt < 90) {
                    tilt++;
                    zoom -= 0.01f;
                } else {
                    upward = false;
                }

            } else {
                if (tilt > 0) {
                    tilt--;
                    zoom += 0.01f;
                } else {
                    upward = true;
                }
            }
        }
    }

    ;

    /**
     * Allows us to navigate to a certain point.
     */
    public void navigateToPoint(LatLng latLng, float tilt, float bearing, float zoom, boolean animate) {
        CameraPosition position =
                new CameraPosition.Builder().target(latLng)
                        .zoom(zoom)
                        .bearing(bearing)
                        .tilt(tilt)
                        .build();

        changeCameraPosition(position, animate);

    }

    public void navigateToPoint(LatLng latLng, boolean animate) {
        CameraPosition position = new CameraPosition.Builder().target(latLng).build();
        changeCameraPosition(position, animate);
    }

    private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        if (animate) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }

    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == googleMap.getMapType()) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    public void addMarkersToMap(List<HistoryData> historyPoints) {
        /*if(googleMap!=null){
            googleMap.r;
        }*/
        for (HistoryData location : historyPoints) {
            Marker marker = null;
            String status = location.getStatusCode();
            String latitude = location.getLatitude();
            String longitude = location.getLongitude();
            String address = location.getAddress();
            StringBuilder snippet = new StringBuilder("Address~" + address + "\n" + "Speed~" + location.getSpeedKph() + " KPH\n");
            if (status.equalsIgnoreCase("61715")) {
                snippet.append("Stop Duration~" + location.getStayedDuration() + "\n");
            }
            snippet.append("Time~" + location.getTimestampString() + "\n");
            snippet.append("Distance Covered~" + location.getDistanceCovered() + " Kms");
            if (!TextUtils.isEmpty(status) && "61715".equalsIgnoreCase(status)) {
                if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                    LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                    marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_red))
                            .title(lastLocation.getDisplayName())
                            .snippet(snippet.toString()));

                }
            } else {
                LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                        .alpha(0)
                        .title(lastLocation.getDisplayName())
                        .snippet(snippet.toString()));
            }
            if (marker != null) {
                markers.add(marker);
            }
        }

        if (!isStartAnimation || historyPoints.size() == 1) {
            String lat = historyPoints.get(0).getLatitude();
            String lon = historyPoints.get(0).getLongitude();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(lat), Double.valueOf(lon)), 10.0f));
            if (historyPoints.size() == 1) {
                handlePlayPauseButton(1);
                markers.get(0).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_red));
            }
        }

    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        googleMap.clear();
        markers.clear();
    }

    /**
     * Remove the currently selected marker.
     */
    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }

    /**
     * Highlight the marker by index.
     */
    private void highLightMarker(int index) {
        if (markers != null && markers.size() > 0 && index < markers.size()) {
            highLightMarker(markers.get(index));
        }
    }

    private void highLightMarker(int index, String status) {
        Marker marker = markers.get(index);
        if (!TextUtils.isEmpty(status)) {
            if (status.equalsIgnoreCase("0")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_blue));
            } else if (status.equalsIgnoreCase("61714")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_green));
            } else if (status.equalsIgnoreCase("61715")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_red));
            } else if (status.equalsIgnoreCase("61716")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_orange));
            }
        } else {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_orange));
        }
        this.selectedMarker = marker;
    }

    /**
     * Highlight the marker by marker.
     */
    private void highLightMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        /*marker.showInfoWindow();*/

        //Utils.bounceMarker(googleMap, marker);
        this.selectedMarker = marker;
    }


    private void resetMarkers() {
        for (Marker marker : this.markers) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_dot_red));
        }
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

    public void getHistoryData() {
        progressDialog.setTitle(res.getString(R.string.refresh_dialog_title));
        progressDialog.setMessage(res.getString(R.string.refresh_dialog_msg));
        progressDialog.setCancelable(true);
        progressDialog.show();
        final Hashtable requestParams = getRequestJSONParams();
        if (requestParams != null && requestParams.size() > 0) {
            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    Log.d("HistoryFragment", "Fetching history data...");
                    String response = null;
                    try {
                        response = activity.getSoapServiceInstance().getHistoryData(requestParams);
                        //Log.d("HistoryFragment",response);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mActivity, "Couldn't get data at this moment, try again later!!", Toast.LENGTH_SHORT).show();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(String response) {
                    super.onPostExecute(response);
                    if (response != null && !response.isEmpty()) {
                        try {
                            /*clearMarkers();*/
                            progressDialog.dismiss();
                            historyList = new Gson().fromJson(response, new TypeToken<List<HistoryData>>() {
                            }.getType());
                            addMarkersToMap(historyList);
                            if (isStartAnimation) {
                                handlePlayPauseButton(0);
                                animator.startAnimation(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(mActivity, "No data for selected range!", Toast.LENGTH_SHORT).show();
                        handlePlayPauseButton(1);
                        progressDialog.dismiss();
                    }
                }
            }.execute();
        } else {
            progressDialog.dismiss();
            handlePlayPauseButton(1);
            Toast.makeText(mActivity, "Please select date,time from and time to!", Toast.LENGTH_SHORT).show();
        }
    }

    public Hashtable getRequestJSONParams() {
        Hashtable<String, Object> params = new Hashtable<>();
        if (lastLocation == null) {
            return null;
        }
        if (TextUtils.isEmpty(date_from) || TextUtils.isEmpty(date_to) || TextUtils.isEmpty(timeFrom) || TextUtils.isEmpty(timeTo)) {
            return null;
        }

        String fromTime = date_from + " " + timeFrom;
        String toTime = date_to + " " + timeTo;
        SimpleDateFormat sf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        try {
            Date date1 = sf.parse(fromTime);
            Date date2 = sf.parse(toTime);
            Date currentDateTime = new Date();
            if (date1.compareTo(currentDateTime) > 0) {
                Toast.makeText(mActivity, "Time from cannot be greater than current time!", Toast.LENGTH_SHORT).show();
                timeFromTextView.setText("00:00");
                return null;
            } else if (date1.compareTo(date2) > 0) {
                Toast.makeText(mActivity, "Time from cannot be greater than time to!", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        params.put("accountId", sharedPrefs.getAccountId());
        params.put("deviceId", lastLocation.getDeviceID());
        params.put("fromTime", fromTime);
        params.put("toTime", toTime);

        return params;
    }


    public int getHistoryDelay(int progress) {
        Log.v("Seek Bar Progress", progress + "");
        int newtime = 1600 - 100 * progress;
        return newtime;
    }

    public void handlePlayPauseButton(int flag) {
        if (flag == 0) {
            if (playButton.getVisibility() == View.VISIBLE) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (pauseButton.getVisibility() == View.VISIBLE) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
        }
    }

}