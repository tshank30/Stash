package com.fst.apps.ftelematics.fragments;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.MapInfoWindow;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MapFragment extends Fragment implements TextToSpeech.OnInitListener{

	private GoogleMap googleMap;
	private Double latitude,longitude;
	private LastLocation lastLocation;
	private String vehicleNumber;
	private long autoRefreshTime;
	private SharedPreferencesManager sharedPrefs;
	final Handler handler = new Handler();
	private MarkerOptions marker;
	private MainActivity mActivity;
	private AppUtils appUtils;
	private List<LastLocation> locationList;
	private NetworkUtility networkUtility;
	private RadioGroup radioGroup;
	private PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE);
	private static final int MAP_TYPE_NORMAL=1;
	private static final int MAP_TYPE_SATELLITE=2;
	private int mapType;
    private MainActivity activity;
    private TextView speedTextView,timestampText,totalDistanceTextView;
	private SimpleDateFormat sdf;
	private Marker trackingMarker;
	private TextToSpeech tts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		sharedPrefs=new SharedPreferencesManager(mActivity);
		autoRefreshTime=sharedPrefs.getAutoRefresh();
		lastLocation=bundle.getParcelable("lastLocation");
        activity=(MainActivity) getActivity();
		if(lastLocation!=null){
			latitude=Double.parseDouble(lastLocation.getLatitude());
			longitude=Double.parseDouble(lastLocation.getLongitude());
			vehicleNumber=lastLocation.getDisplayName();
		}

		appUtils=new AppUtils(getActivity());
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
        if(sharedPrefs.getSpeechMode()) {
            tts = new TextToSpeech(activity, this);
        }
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity=(MainActivity)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView= inflater.inflate(R.layout.fragment_map, container,false);
		radioGroup=(RadioGroup) rootView.findViewById(R.id.map_types);
        speedTextView=(TextView) rootView.findViewById(R.id.speedText);
        totalDistanceTextView=(TextView) rootView.findViewById(R.id.totalDistanceText);
        timestampText=(TextView) rootView.findViewById(R.id.timestampText);


        new DistanceTask(sharedPrefs.getAccountId(),lastLocation.getDeviceID()).execute();
        speedTextView.setText(lastLocation.getSpeedKPH() + " KMPH");
        timestampText.setText(AppUtils.getDateFromUnixTimestsmp(lastLocation.getUnixTime()));
		try {
			// Loading map
			initilizeMap(lastLocation,mapType);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
					initilizeMap(lastLocation, mapType);
				}
			}
		});

		

		if(autoRefreshTime>0){
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					Log.v("MapFragment", "Refreshing map after" + autoRefreshTime + "seconds");
					new GetDataTask().execute();
					handler.postDelayed(this, autoRefreshTime);
				}

			}, autoRefreshTime);

		}
		return rootView;
	}


	private void initilizeMap(LastLocation lastLocation,int mapType) {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
		}

		if (googleMap == null) {
			Toast.makeText(mActivity, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}else{
			setUpMap(googleMap, lastLocation, mapType);
		}
	}

	private void setUpMap(GoogleMap mMap,LastLocation lastLocation,int mapType) {

		String address=AppUtils.reverseGeocode(mActivity, latitude, longitude);
		String status=lastLocation.getStatusCode();
		// For showing a move to my loction button
		//mMap.setMyLocationEnabled(true);
		if(mapType==MAP_TYPE_SATELLITE){
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}else{
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		mMap.getUiSettings().setZoomControlsEnabled(true);
		marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(vehicleNumber).snippet(address).flat(true);
		// For dropping a marker at a point on the Map
		if(!TextUtils.isEmpty(status)){
			if(status.equalsIgnoreCase("0")){
				marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_nw));
			}else if(status.equalsIgnoreCase("61714")){
				marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_moving));
			}else if(status.equalsIgnoreCase("61715")){
				marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_stop));
			}else if(status.equalsIgnoreCase("61716")){
				marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_dormant));
			}
		}else{
			marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_dormant));
		}
		mMap.clear();
		trackingMarker=mMap.addMarker(marker);
		mMap.setInfoWindowAdapter(new MapInfoWindow(mActivity,"MAP"));

		// For zooming automatically to the Dropped PIN Location
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 12.0f));
	}

	private void setUpMap(GoogleMap mMap,Marker marker,double latitude,double longitude,String status,int mapType) {
	//	String address=AppUtils.reverseGeocode(mActivity, latitude, longitude);
		// For showing a move to my loction button
		//mMap.setMyLocationEnabled(true);
		// mMap.getUiSettings().setZoomControlsEnabled(true);

		//marker.position(point).title(vehicleNumber).snippet(address);
		// For dropping a marker at a point on the Map


		if(mapType==MAP_TYPE_SATELLITE){
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}else{
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		//mMap.clear();
		//Marker marker1=mMap.addMarker(marker);
		LatLng latLng=new LatLng(latitude,longitude);
		//marker.setRotation(bearing);
		animateMarker(marker, latLng, status);
		options.add(latLng);
		googleMap.addPolyline(options);
		mMap.setInfoWindowAdapter(new MapInfoWindow(mActivity,"MAP"));
		// For zooming automatically to the Dropped PIN Location
		//CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).build();

	}

	/*private float bearingBetweenLatLngs(LastLocation lastLocation,LastLocation freshLocation) {

		Location beginL= convertLatLngToLocation(lastLocation.getLatitude(),lastLocation.getLongitude());
		Location endL= convertLatLngToLocation(freshLocation.getLatitude(),freshLocation.getLongitude());

		return beginL.bearingTo(endL);
	}

	private Location convertLatLngToLocation(String latitude,String longitude) {
		Location loc = new Location("someLoc");
		loc.setLatitude(Double.parseDouble(latitude));
		loc.setLongitude(Double.parseDouble(longitude));
		return loc;
	}*/

	public void animateMarker(final Marker marker, final LatLng toPosition,final String status) {
		final long start = SystemClock.uptimeMillis();
		Projection proj = googleMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 30*1000;
		final Interpolator interpolator = new LinearInterpolator();
		handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                LatLng point = new LatLng(lat, lng);
                marker.setPosition(point);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                if (t < 1.0) {
                    // Post again 16ms later.
					handler.postDelayed(this, 16);
				}else{
                    if(status.equalsIgnoreCase("0")){
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_nw));
                    }else if(status.equalsIgnoreCase("61714")){
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_moving));
                    }else if(status.equalsIgnoreCase("61715")){
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_stop));
                    }else if(status.equalsIgnoreCase("61716")){
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_dormant));
                    }
                }
			}
		});


	}



	@Override
	public void onResume() {
		super.onResume();
		initilizeMap(lastLocation,mapType);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onInit(int status) {
		if(tts!=null && status==TextToSpeech.SUCCESS){
			Locale loc=new Locale("en","IN");
            tts.setSpeechRate(0.7f);
            tts.setLanguage(loc);
            String text="Vehicle Number "+lastLocation.getDisplayName()+" is "+AppUtils.getStatusOfVehicle(lastLocation.getStatusCode())+
                    " at "+lastLocation.getAddress();
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        }
	}

	class GetDataTask extends AsyncTask<Void,Void,String> {
		String url;
		public GetDataTask(){
			url=appUtils.getLastLocationUrl();
			networkUtility=new NetworkUtility();
		}

		@Override
		protected String doInBackground(Void[] params) {
			String response = null;
			try {
				response=networkUtility.sendGet(url);
				Log.d("Result",response);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(String json) {
			super.onPostExecute(json);
			List<LastLocation> freshList=new Gson().fromJson(json, new TypeToken<List<LastLocation>>(){}.getType());
			if(freshList!=null && freshList.size()>0){
				for(LastLocation freshLastLocation:freshList){
					if(lastLocation.getDeviceID().equalsIgnoreCase(freshLastLocation.getDeviceID())){
						String diff=AppUtils.getDatesDifference(freshLastLocation.getCurrentTime(),lastLocation.getCurrentTime(),"DIFF");
						if(!TextUtils.isEmpty(diff) && Integer.parseInt(diff)<0) {
							latitude = Double.parseDouble(freshLastLocation.getLatitude());
							longitude = Double.parseDouble(freshLastLocation.getLongitude());
							//float bearing=bearingBetweenLatLngs(lastLocation,freshLastLocation);
							Log.v("MapFragment", "New coordinates: " + latitude + ":" + longitude);
							if (trackingMarker != null) {
								setUpMap(googleMap, trackingMarker, latitude, longitude, freshLastLocation.getStatusCode(), mapType);
							}
							speedTextView.setText(freshLastLocation.getSpeedKPH() + " KMPH");
							timestampText.setText(AppUtils.getDateFromUnixTimestsmp(freshLastLocation.getUnixTime()));
							lastLocation = freshLastLocation;
						}
					}
				}
			}
		}
	}

    class DistanceTask extends AsyncTask<Void, Void, String>{

        String accountID,deviceID;

        public DistanceTask(String accountID,String deviceID){
            this.accountID=accountID;
            this.deviceID=deviceID;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result=activity.getSoapServiceInstance().getTotalDistance(accountID, deviceID);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            totalDistanceTextView.setText(result+" Kms");
        }
    }


}
