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
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class NewDashBoardFragment extends Fragment implements LoaderTaskVehicleList.VehicleListInterface, View.OnClickListener {

    private Typeface tf;
    // private PieChart mChart;
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
    private final String FILTER_TEXT = "filterText";
    private CardView dashboardCard, mapCard, reportCard, notificationCard, supportCard, settingCard;
    private TextView moving, stopped, idle, down;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        appUtils = new AppUtils(context);
        sharedPrefs = new SharedPreferencesManager(getActivity());
        rottweilerApplication = (RottweilerApplication) activity.getApplicationContext();
        cd = ConnectionDetector.getInstance();
        res = getResources();
        setHasOptionsMenu(true);
        url = appUtils.getLastLocationUrl();
        //bus.register(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_new_dashboard, container, false);
        dashboardCard = (CardView) v.findViewById(R.id.card_dashboard);
        mapCard = (CardView) v.findViewById(R.id.card_map);
        notificationCard = (CardView) v.findViewById(R.id.card_notification);
        supportCard = (CardView) v.findViewById(R.id.card_support);
        settingCard = (CardView) v.findViewById(R.id.card_setting);
        reportCard = (CardView) v.findViewById(R.id.card_report);

        moving = (TextView) v.findViewById(R.id.moving);
        stopped = (TextView) v.findViewById(R.id.stopped);
        idle = (TextView) v.findViewById(R.id.idle);
        down = (TextView) v.findViewById(R.id.down);

        moving.setOnClickListener(this);
        stopped.setOnClickListener(this);
        idle.setOnClickListener(this);
        down.setOnClickListener(this);

        dashboardCard.setOnClickListener(this);
        mapCard.setOnClickListener(this);
        notificationCard.setOnClickListener(this);
        supportCard.setOnClickListener(this);
        settingCard.setOnClickListener(this);
        reportCard.setOnClickListener(this);


        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        dataTask = new LoaderTaskVehicleList(context, !databaseHelper.isVehicleListDataInDB(), url, this, true);
        dataTask.getDataFromDB();
        if (ConnectionDetector.getInstance().isConnectingToInternet(context))
            dataTask.execute();
        return v;
    }


    @Override
    public void onProcessComplete(List<LastLocation> lastLocationList) {

        int movingNo = 0, stoppedNo = 0, idleNo = 0, downNo = 0;

        for (LastLocation location : lastLocationList) {
            if (location.getStatusCode().equalsIgnoreCase("61714")) {//move
                movingNo++;
            } else if (location.getStatusCode().equalsIgnoreCase("61715")) {//stop
                stoppedNo++;
            } else if (location.getStatusCode().equalsIgnoreCase("61716")) {//idle/dormant
                idleNo++;
            } else if (location.getStatusCode().equalsIgnoreCase("0")) {//not working/down
                downNo++;
            }

        }

        moving.setText("" + movingNo);
        stopped.setText("" + stoppedNo);
        idle.setText("" + idleNo);
        down.setText("" + downNo);

    }

    @Override
    public void noConnectionNoDB() {

    }

    @Override
    public void onClick(View v) {

        Fragment fragment;
        FragmentTransaction transaction;
        Bundle bundle;
        switch (v.getId()) {
            case R.id.card_dashboard:

                fragment = new VehiclesListFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.replace(R.id.content_frame, fragment);
                transaction.commit();

                break;

            case R.id.card_map:

                fragment = new VehicleMapViewFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.card_report:

                fragment = new NewReportFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.card_notification:
                fragment = new ParentViewPagerFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;


            case R.id.card_support:
                fragment = new SupportFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();


                break;

            case R.id.card_setting:
                fragment = new SettingsFragment();
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.moving:
                fragment = new VehiclesListFragment();
                bundle = new Bundle();
                bundle.putString(FILTER_TEXT, "move");
                fragment.setArguments(bundle);
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.stopped:

                fragment = new VehiclesListFragment();
                bundle = new Bundle();
                bundle.putString(FILTER_TEXT, "stop");
                fragment.setArguments(bundle);
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.idle:

                fragment = new VehiclesListFragment();
                bundle = new Bundle();
                bundle.putString(FILTER_TEXT, "idle");
                fragment.setArguments(bundle);
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();
                break;

            case R.id.down:

                fragment = new VehiclesListFragment();
                bundle = new Bundle();
                bundle.putString(FILTER_TEXT, "not working");
                fragment.setArguments(bundle);
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getClass().toString());
                transaction.commit();


                break;


        }

    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (cd.isConnectingToInternet(getActivity())) {
                Fragment fragment = new VehiclesListFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                //ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(NewDashBoardFragment.this.getClass().toString());
                ft.commit();
            } else {
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
        if (context == null)
            context = getActivity();
    }
}


