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

import java.util.List;

/**
 * Created by enigma-pc on 25/2/16.
 */
public class LoaderTaskVehicleList extends AsyncTask<Void, Void, List<LastLocation>> {
    private String url;
    private boolean showDialog;
    private NetworkUtility networkUtility;
    private ProgressDialog progressDialog;
    private Context context;
    private VehicleListInterface vehicleListInterface;
    private DatabaseHelper dbHelper;
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
    public LoaderTaskVehicleList(Context context, boolean showDialog, String url, VehicleListInterface vehicleListInterface, Boolean boolGetDataFromDB) {
        this.context = context;
        this.url = url;
        this.networkUtility = new NetworkUtility();
        this.vehicleListInterface = vehicleListInterface;
        this.showDialog = showDialog;
        this.boolGetDataFromDB = boolGetDataFromDB;
        this.dbHelper = new DatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog == null)
            progressDialog = new ProgressDialog(context);
        if (showDialog) {
            progressDialog.setTitle("Contacting Server");
            progressDialog.setMessage("Just a moment..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
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

        for (LastLocation lastLocation : lastLocationList) {
            lastLocation.setParkingStatus(dbHelper.isParking(lastLocation.getDeviceID()));
        }

        return lastLocationList;
    }

    @Override
    protected void onPostExecute(List<LastLocation> lastLocationList) {
        super.onPostExecute(lastLocationList);

        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (lastLocationList != null && lastLocationList.size() > 0) {
            isDataInServer = true; //cool response from server

            vehicleListInterface.onProcessComplete(lastLocationList);

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
            vehicleListInterface.onProcessComplete(lastLocationList);
        }


    }


    public interface VehicleListInterface {

        void onProcessComplete(List<LastLocation> lastLocationList);

        void noConnectionNoDB();
    }
}


