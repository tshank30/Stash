package com.fst.apps.ftelematics.loaders;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fst.apps.ftelematics.entities.DistanceModel;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.LocationComparator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.abs;


/**
 * Created by enigma-pc on 25/2/16.
 */
public class LoaderTaskSortedVehicleList extends AsyncTask<Void, Void, List<LastLocation>> {
    private String url;
    private boolean showDialog;
    private NetworkUtility networkUtility;
    private ProgressDialog progressDialog;
    private Context context;
    private WeakReference<VehicleListInterface> vehicleListInterface;
    private DatabaseHelper dbHelper;
    private Double fromLat, fromLong;
    private AppUtils appUtils;


    /**
     * Default Constructor for getting data in bg thread from service and db
     *
     * @param context              current context
     * @param showDialog           weather to show progress dialog or not
     * @param url                  url params
     * @param vehicleListInterface interface used to notify the fragment about latest list
     */
    public LoaderTaskSortedVehicleList(Context context, boolean showDialog, String url, WeakReference<VehicleListInterface> vehicleListInterface, Double fromLat, Double fromLong) {
        this.context = context;
        this.url = url;
        this.networkUtility = new NetworkUtility();
        this.vehicleListInterface = vehicleListInterface;
        this.showDialog = showDialog;
        this.fromLat = fromLat;
        this.fromLong = fromLong;
        this.dbHelper = new DatabaseHelper(context);
        appUtils = new AppUtils(context);
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

        for (LastLocation location : lastLocationList) {
            location.setDistanceFromLoc(abs(appUtils.distance(fromLat, fromLong, Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()))));

            try {


                //String s="https://maps.googleapis.com/maps/api/directions/json?origin="+ fromLat + "," + fromLong +"&destination="+ Double.parseDouble(location.getLatitude()) + "," + Double.parseDouble(location.getLongitude()) +"&key=AIzaSyA4ofHYDv6xs_AToHZM3ZdjV4FTeq9AW0w";

                String s = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + fromLat + "," + fromLong + "&destinations=" + Double.parseDouble(location.getLatitude()) + "," + Double.parseDouble(location.getLongitude()) + "&departure_time=now&key=AIzaSyA4ofHYDv6xs_AToHZM3ZdjV4FTeq9AW0w";
                //String s = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + fromLat + "," + fromLong + "&destinations=" + Double.parseDouble(location.getLatitude()) + "," + Double.parseDouble(location.getLongitude()) + "&mode=driving&language=en-EN&sensor=false";
                DistanceModel distanceModel = networkUtility.sendDistanceRequest(s);
                /* JSONObject jsonObject=new JSONObject(response);*/
                location.setDistanceFromLoc(distanceModel.getDistance());
                location.setDistanceText(distanceModel.getDistanceText());
                location.setDurationText(distanceModel.getDurationText());
                /*String okResponse=jsonObject.get("Status").toString();*/
                /*  Log.d("Result", okResponse);*/
            } catch (Exception e) {
                location.setDistanceFromLoc(abs(appUtils.distance(fromLat, fromLong, Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()))));
            }

            //https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=Washington,DC&destinations=New+York+City,NY&key=YOUR_API_KEY

        }

        if (lastLocationList != null && lastLocationList.size() > 0) {
            Collections.sort(lastLocationList, new LocationComparator());
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

        if (vehicleListInterface.get() != null && lastLocationList != null && lastLocationList.size() > 0)
            vehicleListInterface.get().onProcessComplete(lastLocationList);

    }


    public interface VehicleListInterface {

        void onProcessComplete(List<LastLocation> lastLocationList);

        void noConnectionNoDB();
    }
}


