package com.fst.apps.ftelematics.loaders;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;

/**
 * Created by enigma-pc on 25/2/16.
 */
public class LoaderTaskVehicleList extends AsyncTask<Void, Void, List<LastLocation>> {
    private String url;
    private NetworkUtility networkUtility;
    private Context context;
    private WeakReference<VehicleListInterface> vehicleListInterface;

    private Boolean boolGetDataFromDB;
    private Boolean isDataInDB = false;
    private Boolean isDataInServer = false;

    /**
     * Default Constructor for getting data in bg thread from service and db
     *
     * @param context              current context
     * @param showDialog           weather to show progress dialog or not
     * @param url                  url params
     * @param vehicleListInterface interface used to notify the fragment about latest list
     * @param boolGetDataFromDB    getting Data from DB if needed(false in case of autorefresh)
     */
    public LoaderTaskVehicleList(Context context, boolean showDialog, String url, WeakReference<VehicleListInterface> vehicleListInterface, Boolean boolGetDataFromDB) {
        this.context = context;
        this.url = url;
        this.networkUtility = new NetworkUtility();
        this.vehicleListInterface = vehicleListInterface;
        this.boolGetDataFromDB = boolGetDataFromDB;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //showDialog

    }

    @Override
    protected List<LastLocation> doInBackground(Void[] params) {

        String response = null;
        try {
            response = networkUtility.sendGet(url);
            Log.d("Result", response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<LastLocation> lastLocationList = new Gson().fromJson(response, new TypeToken<List<LastLocation>>() {
        }.getType());

        DatabaseHelper dbHelper = new DatabaseHelper(context.getApplicationContext());
        if (dbHelper != null) {
            HashSet<String> parkingSet = dbHelper.getParkingList();
            if (lastLocationList != null)
                for (LastLocation lastLocation : lastLocationList) {
                    if (dbHelper != null)
                        lastLocation.setParkingStatus(parkingSet.contains(lastLocation.getDeviceID()) ? 1 : 0);
                }
        }


        return lastLocationList;
    }

    @Override
    protected void onPostExecute(List<LastLocation> lastLocationList) {
        super.onPostExecute(lastLocationList);


        if (lastLocationList != null && lastLocationList.size() > 0) {
            isDataInServer = true; //cool response from server
            if (vehicleListInterface.get() != null)
                vehicleListInterface.get().onProcessComplete(lastLocationList);

            Log.e("hello", "" + lastLocationList.get(0).getDriverName());

            //for storing data in db
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.storeVehicleListData(lastLocationList);
        }


    }

    /**
     * Gets data from database
     */
    public void getDataFromDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<LastLocation> lastLocationList = dbHelper.getVehicleListData();
        Log.e("LTVL", "getDataFromDB:lastLocationList:" + lastLocationList.size());
        if (lastLocationList != null && lastLocationList.size() > 0) {
            isDataInDB = true; //cool data from db
            if (vehicleListInterface.get() != null)
                vehicleListInterface.get().onProcessComplete(lastLocationList);
        }


    }


    public interface VehicleListInterface {

        void onProcessComplete(List<LastLocation> lastLocationList);

        void noConnectionNoDB();
    }
}


