package com.fst.apps.ftelematics.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.RottweilerApplication;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.loaders.LoaderTaskVehicleList;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.PieChartValueFormatter;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements LoaderTaskVehicleList.VehicleListInterface{

	private Typeface tf;
	private PieChart mChart;
	private RottweilerApplication rottweilerApplication;
	private List<LastLocation> locationList;
	private SharedPreferencesManager sharedPrefs;
	private Button viewListbutton;
	private ConnectionDetector cd;
	private static String totalVehicles;
	private String vehicleStatus[];
	private int vehicleStatusColors[];
	private Resources res;
	private TextView totalVehiclesTextView;
	ProgressDialog progressDialog;
	private LoaderTaskVehicleList dataTask;
	private MainActivity activity;
	private Context context;
	private String url;
	private AppUtils appUtils;
    private final String FILTER_TEXT="filterText";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity=(MainActivity) getActivity();
		appUtils=new AppUtils(context);
		sharedPrefs=new SharedPreferencesManager(getActivity());
		rottweilerApplication=(RottweilerApplication) activity.getApplicationContext();
		cd=ConnectionDetector.getInstance();
		res=getResources();
		setHasOptionsMenu(true);
		url = appUtils.getLastLocationUrl();
		//bus.register(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
		mChart = (PieChart) v.findViewById(R.id.pieChart1);
		viewListbutton=(Button) v.findViewById(R.id.viewList);
		totalVehiclesTextView=(TextView) v.findViewById(R.id.total_vehicles);
		viewListbutton.setOnClickListener(new ClickListener());
		progressDialog=new ProgressDialog(getActivity());
		progressDialog.setTitle(res.getString(R.string.refresh_dialog_title));
		progressDialog.setMessage(res.getString(R.string.refresh_dialog_msg));
		progressDialog.setCancelable(false);
		vehicleStatus=res.getStringArray(R.array.vehicle_status);
		vehicleStatusColors=res.getIntArray(R.array.vehicle_status_color);
		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		dataTask = new LoaderTaskVehicleList(context, !databaseHelper.isVehicleListDataInDB(), url, this, true);
		dataTask.getDataFromDB();
		if (ConnectionDetector.getInstance().isConnectingToInternet(context))
			dataTask.execute();
		return v;
	}

	protected PieData generatePieData() {

		int count = 4;
		float countNW=0;
		float countMoving = 0;
		float countStop=0;
		float countDormant=0;
		String total=null;

		String dashboardStats=getDashboardStats();

		if(!TextUtils.isEmpty(dashboardStats)){
			String parts[]=dashboardStats.split(":");
			if(parts!=null && parts.length>0){
				countNW=Float.parseFloat(parts[0]);
				countMoving=Float.parseFloat(parts[1]);
				countStop=Float.parseFloat(parts[2]);
				countDormant=Float.parseFloat(parts[3]);
				totalVehicles=parts[4];
				if(!TextUtils.isEmpty(totalVehicles)){
					totalVehiclesTextView.setText("Total Vehicles: "+totalVehicles);
				}
			}
		}

		ArrayList<Entry> entries1 = new ArrayList<Entry>();
		ArrayList<String> xVals = new ArrayList<String>();
		/*if(countMoving>0){*/
		entries1.add(new Entry(countMoving,0));
		xVals.add("");
		/*}*/
		/*if(countStop>0){*/
		entries1.add(new Entry(countStop,1));
		xVals.add("");
		/*}*/
		/*if(countDormant>0){*/
		entries1.add(new Entry(countDormant,2));
		xVals.add("");
		/*}*/
		/*if(countNW>0){*/
		entries1.add(new Entry(countNW,3));
		xVals.add("");
		/*}*/



		PieDataSet ds1 = new PieDataSet(entries1, "");
		ds1.setValueFormatter(new PieChartValueFormatter());
		ds1.setColors(vehicleStatusColors);
		ds1.setSliceSpace(2f);
		ds1.setValueTextColor(Color.WHITE);
		ds1.setValueTextSize(16f);

		PieData d = new PieData(xVals, ds1);
		d.setValueTypeface(tf);
		return d;
	}

	public String getDashboardStats(){
		int countMoving=0,countNW = 0,countStop=0,countDormant=0,total=0;

		if(locationList!=null && locationList.size()>0){
			total=locationList.size();
			for(LastLocation lastLocation:locationList){
				if(lastLocation.getStatusCode().equalsIgnoreCase("0")){
					countNW++;
				}else if(lastLocation.getStatusCode().equalsIgnoreCase("61714")){
					countMoving++;
				}else if(lastLocation.getStatusCode().equalsIgnoreCase("61715")){
					countStop++;
				}else if(lastLocation.getStatusCode().equalsIgnoreCase("61716")){
					countDormant++;
				}
			}
		}

		return countNW+":"+countMoving+":"+countStop+":"+countDormant+":"+total;
	}


	@Override
	public void onProcessComplete(List<LastLocation> lastLocationList) {
		mChart.setDescription("");
        locationList=lastLocationList;
		Typeface tf = Typeface.DEFAULT;
		mChart.setCenterTextTypeface(tf);
		//mChart.setCenterText("Vehicles");
		mChart.setCenterTextSize(22f);
		mChart.setCenterTextTypeface(tf);
		mChart.setNoDataTextDescription(null);
		mChart.setNoDataText("Loading,please wait..");
		mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Fragment fragment = new VehiclesListFragment();
                Bundle bundle = new Bundle();
                if(e.getXIndex()==0) {
                    bundle.putString(FILTER_TEXT,"move");
                }else if(e.getXIndex()==1) {
                    bundle.putString(FILTER_TEXT,"stop");
                }else if(e.getXIndex()==2) {
                    bundle.putString(FILTER_TEXT,"idle");
                }else if(e.getXIndex()==3){
                    bundle.putString(FILTER_TEXT,"not working");
                }
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }

            @Override
            public void onNothingSelected() {

            }
        });

		// radius of the center hole in percent of maximum radius
		mChart.setHoleRadius(45f);
		mChart.setTransparentCircleRadius(50f);
		Legend l = mChart.getLegend();
		l.setPosition(LegendPosition.PIECHART_CENTER);
		l.setCustom(vehicleStatusColors,vehicleStatus);
		mChart.setData(generatePieData());
		mChart.invalidate();
	}

	@Override
	public void noConnectionNoDB() {

	}

	private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {

			if(cd.isConnectingToInternet(getActivity())){
				Fragment fragment=new VehiclesListFragment();
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				FragmentTransaction ft = fragmentManager.beginTransaction();
				//ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
				ft.replace(R.id.content_frame, fragment);
				ft.addToBackStack(fragment.getClass().toString());
				ft.commit();
			}else{
				Toast.makeText(activity, "Not connected to internet!", Toast.LENGTH_SHORT).show();
			}
		}

	}



	@Override
	public void onDestroy() {
		//bus.unregister(activity);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		return false;
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
