package com.fst.apps.ftelematics.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.MapInfoWindow;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class HistoryFragment extends Fragment{

	private DatePickerDialog datePickerDialog;
	private TimePickerDialog timeFromDialog,timeToDialog;
	private Calendar calender;
	private LinearLayout dateLayout,timeFromLayout,timeToLayout;
	private TextView dateTextView,timeFromTextView,timeToTextView,historySpeed;
	private SimpleDateFormat dateFormatter;
	private GoogleMap googleMap;
	private MainActivity mActivity;
	private RelativeLayout playButton,pauseButton;
	private SeekBar speedSeekBar;
	private LastLocation lastLocation;
	private SharedPreferencesManager sharedPrefs;
	private String date,timeFrom,timeTo;
	private Handler handler = new Handler();
	private Runnable runnable;
	private int historyDelay=1600;
	private ProgressDialog progressDialog;
	private Resources res;
	private RadioGroup radioGroup;
	private static final int MAP_TYPE_NORMAL=1;
	private static final int MAP_TYPE_SATELLITE=2;
	private int mapType;
	private NetworkUtility networkUtility;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle=getArguments();
		calender=Calendar.getInstance();
		dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
		lastLocation=bundle.getParcelable("lastLocation");
		sharedPrefs=new SharedPreferencesManager(getActivity());
		res=getActivity().getResources();
		networkUtility=new NetworkUtility();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity=(MainActivity)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.fragment_history, container,false);

		dateLayout=(LinearLayout) v.findViewById(R.id.date_layout);
		timeFromLayout=(LinearLayout) v.findViewById(R.id.timefrom_layout);
		timeToLayout=(LinearLayout) v.findViewById(R.id.timeto_layout);
		dateTextView=(TextView) v.findViewById(R.id.date_from_value);
		timeFromTextView=(TextView) v.findViewById(R.id.timefrom_value);
		timeToTextView=(TextView) v.findViewById(R.id.timeto_value);
		playButton=(RelativeLayout) v.findViewById(R.id.play_button);
		pauseButton=(RelativeLayout) v.findViewById(R.id.pause_button);
		speedSeekBar=(SeekBar) v.findViewById(R.id.speed);
		radioGroup=(RadioGroup) v.findViewById(R.id.map_types);
		//historySpeed=(TextView) v.findViewById(R.id.historySpeed);

		playButton.setBackgroundResource(R.drawable.ic_play);
		pauseButton.setBackgroundResource(R.drawable.ic_pause);
		progressDialog=new ProgressDialog(getActivity());
		
		initilizeMap();

		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handlePlayPauseButton(0);

				if(runnable!=null){
					handler.postDelayed(runnable, 100);
				}else{
					getHistoryData();
				}
			}
		});

		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handlePlayPauseButton(1);
				if(handler!=null && runnable!=null){
					handler.removeCallbacks(runnable);
				}
			}
		});

		dateLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				datePickerDialog.show();
			}
		});

		timeFromLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				timeFromDialog.show();
			}
		});

		timeToLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				timeToDialog.show();
			}
		});


		datePickerDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {

			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				Calendar newDate = Calendar.getInstance();
				newDate.set(year, monthOfYear, dayOfMonth);
				dateTextView.setText(dateFormatter.format(newDate.getTime()));
				date=dateFormatter.format(newDate.getTime());
				handler.removeCallbacks(runnable);
				runnable=null;
				handlePlayPauseButton(1);
			}

		},calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));

		timeFromDialog=new TimePickerDialog(getActivity(), new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				timeFromTextView.setText(getDisplayTime(hourOfDay,minute));
				timeFrom=getDisplayTime(hourOfDay,minute);
				handler.removeCallbacks(runnable);
				runnable=null;
				handlePlayPauseButton(1);
			}
		}, calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE), true);

		timeToDialog=new TimePickerDialog(getActivity(), new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				timeToTextView.setText(getDisplayTime(hourOfDay,minute));
				timeTo=getDisplayTime(hourOfDay,minute);
				handler.removeCallbacks(runnable);
				runnable=null;
				handlePlayPauseButton(1);
			}
		}, calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE), true);

		speedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				historyDelay=getHistoryDelay(seekBar.getProgress());
				Log.v("Progress", historyDelay+"");
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

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.satellite_type){
					mapType=GoogleMap.MAP_TYPE_SATELLITE;
				}else{
					mapType=GoogleMap.MAP_TYPE_NORMAL;

				}
				if(googleMap!=null){
					googleMap.setMapType(mapType);
				}else{
					initilizeMap();
				}
			}
		});

		
		return v;
	}

	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
		}

		if (googleMap == null) {
			Toast.makeText(mActivity, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}else{
			//setUpMap(googleMap);
		}
	}

	public void setUpMap(final GoogleMap map,final ArrayList<LastLocation> historyList){
		map.getUiSettings().setZoomControlsEnabled(true);
		map.setMapType(mapType);
		final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE);
		runnable=new Runnable() {
			int i=0;
			@Override
			public void run() {
				i++;
				if(i<historyList.size()){
					LastLocation lastHistoryLocation=historyList.get(i);
					double latitude=Double.valueOf(lastHistoryLocation.getLatitude());
					double longitude=Double.valueOf(lastHistoryLocation.getLongitude());
					String status=lastHistoryLocation.getStatusCode();
					String address=AppUtils.reverseGeocode(mActivity, latitude, longitude);
					LatLng point = new LatLng(latitude, longitude);
					options.add(point);
					MarkerOptions marker=new MarkerOptions().position(point).title(AppUtils.getStatusOfVehicle(status)).snippet(lastHistoryLocation.getStringTimestamp()+"\n"+address);
					if(!TextUtils.isEmpty(status)){
						if(status.equalsIgnoreCase("0")){
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_nw));
						}else if(status.equalsIgnoreCase("61714")){
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_moving));
						}else if(status.equalsIgnoreCase("61715")){
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_stop));
						}else if(status.equalsIgnoreCase("61716")){
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_dormant));
						}
					}else{
						marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_dormant));
					}
					
					map.setInfoWindowAdapter(new MapInfoWindow(mActivity,"HISTORY"));
					map.addMarker(marker);
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f));
					map.addPolyline(options);
					handler.postDelayed(this, historyDelay);
				}
			}
			//Polyline line = map.addPolyline(options);
		};

		handler.postDelayed(runnable, historyDelay);

	}


	public String getDisplayTime(int hours,int minutes){
		StringBuilder sb=new StringBuilder();
		if(hours<10){
			sb.append("0"+String.valueOf(hours));
		}else{
			sb.append(String.valueOf(hours));
		}

		sb.append(":");

		if(minutes<10){
			sb.append("0"+String.valueOf(minutes));
		}else{
			sb.append(String.valueOf(minutes));
		}

		sb.append(":00");
		return sb.toString();
	}

	public void getHistoryData(){
		progressDialog.setTitle(res.getString(R.string.refresh_dialog_title));
		progressDialog.setMessage(res.getString(R.string.refresh_dialog_msg));
		progressDialog.setCancelable(true);
		progressDialog.show();
		final String requestParams=getRequestJSONParams();
		if(!TextUtils.isEmpty(requestParams)){
			new AsyncTask<Void,Void,String>(){

				@Override
				protected String doInBackground(Void... params) {
					String response=networkUtility.sendPost("/fetchHistory",requestParams);
					return response;
				}

				@Override
				protected void onPostExecute(String response) {
					super.onPostExecute(response);
					if(response!=null && !response.isEmpty()){
						try{
							progressDialog.dismiss();
							ArrayList<LastLocation> historyList = new Gson().fromJson(response, new TypeToken<List<LastLocation>>(){}.getType());
							googleMap.clear();
							setUpMap(googleMap, historyList);
							Log.v("Response", response);
						}catch(Exception e){
							e.printStackTrace();
						}
					}else{
						Toast.makeText(mActivity, "No data for selected range!", Toast.LENGTH_SHORT).show();
						handlePlayPauseButton(1);
						progressDialog.dismiss();
					}
				}
			}.execute();
		}else{
			progressDialog.dismiss();
			handlePlayPauseButton(1);
			Toast.makeText(mActivity, "Please select date,time from and time to!", Toast.LENGTH_SHORT).show();
		}
	}

	public String getRequestJSONParams(){
		if(lastLocation==null){
			return null;
		}
		if(TextUtils.isEmpty(date)||TextUtils.isEmpty(timeFrom)||TextUtils.isEmpty(timeTo)){
			return null;
		}
		
		String fromTime=date+" "+timeFrom;
		String toTime=date+" "+timeTo;
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			//sf.format()
			Date date1=sf.parse(fromTime);
			Date date2=sf.parse(toTime);
			Date currentDateTime=new Date();
			if(date1.compareTo(currentDateTime)>0){
				Toast.makeText(mActivity, "Time from cannot be greater than current time!", Toast.LENGTH_SHORT).show();
				timeFromTextView.setText("00:00:00");
				return null;
			}
			else if(date1.compareTo(date2)>0){
				Toast.makeText(mActivity, "Time from cannot be greater than time to!", Toast.LENGTH_SHORT).show();
				return null;
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		JSONObject jsonParams=new JSONObject();
		try {
			jsonParams.put("AccountId", sharedPrefs.getAccountId());
			jsonParams.put("DeviceId", lastLocation.getDeviceID());
			jsonParams.put("FromTime", fromTime);
			jsonParams.put("ToTime", toTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonParams.toString();
	}



	public int getHistoryDelay(int progress){
		Log.v("Seek Bar Progress", progress+"");
		int newtime=1600-100*progress;
		return newtime;
	}

	public void handlePlayPauseButton(int flag){
		if(flag==0){
			if(playButton.getVisibility()==View.VISIBLE){
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
			}
		}else{
			if(pauseButton.getVisibility()==View.VISIBLE){
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
		}
	}
}
