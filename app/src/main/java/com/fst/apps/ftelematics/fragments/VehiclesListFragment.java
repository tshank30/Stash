package com.fst.apps.ftelematics.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.adapters.VehiclesListAdapter;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.events.NetworkConnectedEvent;
import com.fst.apps.ftelematics.loaders.LoaderTaskVehicleList;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class VehiclesListFragment extends Fragment implements LoaderTaskVehicleList.VehicleListInterface {

    private VehiclesListAdapter adapter;
    private AppUtils appUtils;
    private List<LastLocation> locationList;
    private NetworkUtility networkUtility;
    private long autoRefreshInterval;
    private SharedPreferencesManager sharedPrefs;
    private ProgressDialog progressDialog;
    private ConnectionDetector cd;
    private RecyclerView recyclerView;
    private RelativeLayout noInternet;
    private LoaderTaskVehicleList dataTask;
    private Handler handler = new Handler();
    private boolean refresh;

    private String url;
    List<LastLocation> vehiclesList;
    private EditText filter;
    private final String FILTER_TEXT = "filterText";
    private String filterText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.recycler_view, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.list);
        noInternet = (RelativeLayout) v.findViewById(R.id.no_internet);
        filter = (EditText) v.findViewById(R.id.filter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        refresh = true;
        appUtils = new AppUtils(getActivity());
        sharedPrefs = new SharedPreferencesManager(getActivity());
        autoRefreshInterval = sharedPrefs.getAutoRefresh();
        progressDialog = new ProgressDialog(getActivity());
        url = appUtils.getLastLocationUrl();
        cd = new ConnectionDetector();
        Bundle bundle = getArguments();
        if (bundle != null) {
            filterText = bundle.getString(FILTER_TEXT);
        }
        vehiclesList = new ArrayList<>();
        adapter = new VehiclesListAdapter(vehiclesList, R.layout.fragment_home, getActivity());
        recyclerView.setAdapter(adapter);
        filter.addTextChangedListener(new VehicleFilterTextWatcher());
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), !databaseHelper.isVehicleListDataInDB(), url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(this), true);
        dataTask.getDataFromDB();
        if (ConnectionDetector.getInstance().isConnectingToInternet(getActivity()))
            dataTask.execute();

        if (autoRefreshInterval > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (refresh) {
                        Log.v("Auto Refresh", "Refreshing data after " + autoRefreshInterval + " seconds");
                        dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url, new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(VehiclesListFragment.this), false);
                        dataTask.execute();
                        handler.postDelayed(this, autoRefreshInterval);
                    }
                }
            }, autoRefreshInterval);
        }
        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(0);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeMessages(0);
        }
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_refresh);
        item.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (ConnectionDetector.getInstance().isConnectingToInternet(getActivity().getApplicationContext())) {
                Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
                dataTask = new LoaderTaskVehicleList(getActivity().getApplicationContext(), false, url,  new WeakReference<LoaderTaskVehicleList.VehicleListInterface>(VehiclesListFragment.this), false);
                dataTask.execute();
                return true;
            } else {
                Toast.makeText(getActivity(), "Not connected to internet!", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProcessComplete(List<LastLocation> lastLocationList) {
        if (vehiclesList == null) {
            vehiclesList = new ArrayList<LastLocation>();
        }
        if (lastLocationList != null && lastLocationList.size() > 0) {
            vehiclesList.clear();
            vehiclesList.addAll(lastLocationList);
            adapter.notifyDataSetChanged();
        }

        if (filterText != null && !filterText.isEmpty()) {
            filter.setText(filterText);
        }
    }

    @Override
    public void noConnectionNoDB() {
        recyclerView.setVisibility(View.GONE);
        noInternet.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_LONG).show();
    }

    class VehicleFilterTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.getFilter().filter(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refresh = false;
    }
}
