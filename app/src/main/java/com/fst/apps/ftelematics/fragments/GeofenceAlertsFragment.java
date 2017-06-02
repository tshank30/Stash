package com.fst.apps.ftelematics.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fst.apps.ftelematics.AppConstants;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.adapters.AlertsAdapter;
import com.fst.apps.ftelematics.entities.Alerts;
import com.fst.apps.ftelematics.utils.AlertTypes;
import com.fst.apps.ftelematics.utils.AlertsFabClickListener;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class GeofenceAlertsFragment extends Fragment{

	private DatabaseHelper db;
	private List<Alerts> alerts=new ArrayList<Alerts>();
	private ListView list;
	private AlertsAdapter alertsAdapter;
	private SharedPreferencesManager sf;
	private TextView nodataFound;
	private static final String TAG=AppConstants.ALERT_TYPES.Geofence.name();
	private FloatingActionButton fab;
	private HashMap<String,Object> objList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		db=new DatabaseHelper(getActivity());
		sf=new SharedPreferencesManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.fragment_alerts, container,false);
		nodataFound=(TextView) v.findViewById(R.id.noDataFound);
		list=(ListView) v.findViewById(R.id.list);
		objList=new HashMap<String,Object>();
		fab=(FloatingActionButton) v.findViewById(R.id.fab);
		//fab.attachToListView(list);
		objList.put("alertType", AlertTypes.GEOFENCE);
		objList.put("database", db);
		objList.put("listView", list);
		objList.put("noDataFoundView", nodataFound);
		objList.put("userId", sf.getUserId());
		fab.setOnClickListener(new AlertsFabClickListener(objList,getActivity()));
		alerts=db.getAlertsByAlertType(AlertTypes.GEOFENCE,sf.getUserId());
		if(alerts!=null && alerts.size()>0){
			if(nodataFound.getVisibility()==View.VISIBLE){
				nodataFound.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
			}
			alertsAdapter=new AlertsAdapter(getActivity(), alerts);
			list.setAdapter(alertsAdapter);
		}else{
			list.setVisibility(View.GONE);
			nodataFound.setVisibility(View.VISIBLE);
		}


		return v;
	}

	public void onEventMainThread(Alerts alert){
		if(alert!=null && AppUtils.getAlertTypes(alert.getAlertType()).equalsIgnoreCase(TAG)){
			alerts.add(0,alert);
			if(alertsAdapter!=null){
				alertsAdapter.notifyDataSetChanged();
				list.smoothScrollToPositionFromTop(0, 0);
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}
