package com.fst.apps.ftelematics.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fst.apps.ftelematics.AppConstants;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;

/**
 * Created by shashanktiwari on 24/09/17.
 */

public class NewReportFragment extends Fragment implements View.OnClickListener {

    private TextView dailyReport,stoppageReport,tripReport,idlingReport,fuelReport,distanceReport;
    private SharedPreferencesManager sharedPrefs;
    private  String reportQueryString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_report_frag, container, false);

        sharedPrefs = new SharedPreferencesManager(getActivity());
        reportQueryString="?user="+sharedPrefs.getUserId()+"&role="+sharedPrefs.getRole()+"&account="+sharedPrefs.getAccountId();


        dailyReport = (TextView) v.findViewById(R.id.daily);
        stoppageReport = (TextView) v.findViewById(R.id.stoppage);
        tripReport = (TextView) v.findViewById(R.id.trip);
        idlingReport = (TextView) v.findViewById(R.id.idling);
        fuelReport = (TextView) v.findViewById(R.id.fuel);
        distanceReport = (TextView) v.findViewById(R.id.distance);

        dailyReport.setOnClickListener(this);
        stoppageReport.setOnClickListener(this);
        tripReport.setOnClickListener(this);
        idlingReport.setOnClickListener(this);
        fuelReport.setOnClickListener(this);
        distanceReport.setOnClickListener(this);




        return v;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment  = new ReportsFragment();
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        Bundle bundle;
        switch (v.getId())
        {
            case R.id.daily:
                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DailyReport.aspx" + reportQueryString);
                Log.i("URL",AppConstants.BASE_REPORTS_URL+"DailyReport.aspx"+reportQueryString);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();
                break;

            case R.id.stoppage:

                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "StoppageReport.aspx" + reportQueryString);
                Log.i("URL",AppConstants.BASE_REPORTS_URL+"StoppageReport.aspx"+reportQueryString);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();

                break;

            case R.id.trip:

                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DailyTripReport.aspx" + reportQueryString);
                Log.i("URL",AppConstants.BASE_REPORTS_URL+"DailyTripReport.aspx"+reportQueryString);
                fragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();
                break;

            case R.id.idling:

                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "IdlingReport.aspx" + reportQueryString);
                Log.i("URL",AppConstants.BASE_REPORTS_URL+"IdlingReport.aspx"+reportQueryString);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();
                break;

            case R.id.fuel:

                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "FuelMonitoring.aspx" + reportQueryString);
                Log.i("URL",AppConstants.BASE_REPORTS_URL+"FuelMonitoring.aspx"+reportQueryString);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();
                break;

            case R.id.distance:

                bundle = new Bundle();
                bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DistanceReport.aspx" + reportQueryString);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commit();
                break;

        }

    }
}
