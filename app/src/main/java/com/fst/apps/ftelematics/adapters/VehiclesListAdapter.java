package com.fst.apps.ftelematics.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fst.apps.ftelematics.BuildConfig;
import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.RottweilerApplication;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.fragments.AnimatingMarkersFragment;
import com.fst.apps.ftelematics.fragments.MapFragment;
import com.fst.apps.ftelematics.fragments.VehicleMapViewFrag;
import com.fst.apps.ftelematics.fragments.VehicleMapViewFragment;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.fst.apps.ftelematics.utils.TtsProviderFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class VehiclesListAdapter extends RecyclerView.Adapter<HomeViewHolder> implements Filterable {

    private List<LastLocation> lastLocationList;
    private List<LastLocation> filteredList;
    private int rowLayout;
    private Context context;
    boolean isDualSIM;
    private RottweilerApplication rottweilerApplication;
    private int buffKey;
    private TtsProviderFactory ttsProviderImpl;
    private SharedPreferencesManager sharedPrefs;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog fuelDialog;
    private LinearLayout dialogClose;
    private ImageView close;
    private ProgressBar dialogProgress;
    private TextView fuelValue, expectedLeftRun;
    private boolean hideFuel= true;


    public VehiclesListAdapter(List<LastLocation> lastLocationList, int rowLayout, Context context) {
        this.lastLocationList = lastLocationList;
        this.filteredList = lastLocationList;
        this.rowLayout = rowLayout;
        this.context = context;
        isDualSIM = AppUtils.isDualSIM(context);
        rottweilerApplication = (RottweilerApplication) context.getApplicationContext();
        ttsProviderImpl = TtsProviderFactory.getInstance();
        sharedPrefs = new SharedPreferencesManager(context);

        if(BuildConfig.FLAVOR.equals("gosafe"))
        {
            hideFuel=false;
        }
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new HomeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        final LastLocation lastLocation = filteredList.get(position);
        String address = lastLocation.getAddress();
        if (TextUtils.isEmpty(address) || address.equalsIgnoreCase("NF") || address.equalsIgnoreCase("timeout")) {
            new AddresTask(holder.address, lastLocation).execute();
        } else {
            holder.address.setText(address);
        }
        holder.firstLine.setText(lastLocation.getDisplayName());
        holder.secondLine.setText(AppUtils.getDateFromUnixTimestsmp(lastLocation.getUnixTime()));
        holder.speed.setText("Speed: " + lastLocation.getSpeedKPH() + " KPH");
        holder.address.setSelected(true);
        String statusSince = AppUtils.getDatesDifference(lastLocation.getStatusSince(), lastLocation.getCurrentTime(), "STRING");
        holder.acStatus.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(lastLocation.getInputMask())) {
            if (lastLocation.getInputMask().equalsIgnoreCase("1")) {
                holder.acStatus.setText("AC Status: ON");
            } else {
                holder.acStatus.setText("AC Status: OFF");
            }
        } else {
            holder.acStatus.setText("AC Status: OFF");
        }

        if (lastLocation.getDriverName() == null || lastLocation.getDriverNumber().equalsIgnoreCase("NA") || lastLocation.getDriverNumber().equalsIgnoreCase("")) {
            holder.driverName.setVisibility(View.GONE);
        } else {
            holder.driverName.setVisibility(View.VISIBLE);
            holder.driverName.setText("Driver : " + lastLocation.getDriverName());
        }

        if (lastLocation.getPrevKms() == null || lastLocation.getPrevKms().equalsIgnoreCase("NA") || lastLocation.getPrevKms().equalsIgnoreCase("")/*||lastLocation.getPrevKms().equalsIgnoreCase("0")*/) {
            holder.distance.setVisibility(View.GONE);
        } else {
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText("Odometer : " + lastLocation.getPrevKms());
        }

        if (hideFuel || lastLocation.getCalibrationValues() == null || lastLocation.getCalibrationValues().equalsIgnoreCase("NA"))
            holder.fuel.setVisibility(View.INVISIBLE);
        else {
            holder.fuel.setVisibility(View.VISIBLE);
            holder.fuel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createFuelDialog();
                    new Fuel(sharedPrefs.getAccountId(), lastLocation.getDeviceID(), lastLocation.getCalibrationValues(), lastLocation.getMaxTankCapicity()).execute();
                }
            });

        }

        if (lastLocation.getDriverNumber() == null || lastLocation.getDriverNumber().equalsIgnoreCase("NA")) {
            holder.callButton.setAlpha(0.2f);
            holder.callButton.setOnClickListener(null);
        } else {
            holder.callButton.setAlpha(1.0f);
            holder.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + lastLocation.getDriverNumber()));
                    context.startActivity(intent);
                }
            });
        }

        if (lastLocation.getStatusCode().equalsIgnoreCase("61714")) {
            holder.thumbText.setText("M");
            setShapeBackground(holder.thumbnail.getBackground(), context.getResources().getColor(R.color.parrot));
            if (holder.statusSince.getVisibility() == View.VISIBLE) {
                holder.statusSince.setVisibility(View.GONE);
            }
            /*if(!TextUtils.isEmpty(statusSince)){
                holder.statusSince.setVisibility(View.VISIBLE);
                holder.statusSince.setText("Moving Since: "+statusSince);
            }*/
        } else if (lastLocation.getStatusCode().equalsIgnoreCase("61715")) {
            holder.thumbText.setText("S");
            setShapeBackground(holder.thumbnail.getBackground(), Color.RED);
            if (!TextUtils.isEmpty(statusSince)) {
                holder.statusSince.setVisibility(View.VISIBLE);
                holder.statusSince.setText("Stop Since: " + statusSince);
            }

        } else if (lastLocation.getStatusCode().equalsIgnoreCase("61716")) {
            holder.thumbText.setText("D");
            setShapeBackground(holder.thumbnail.getBackground(), context.getResources().getColor(R.color.orange));
            if (!TextUtils.isEmpty(statusSince)) {
                holder.statusSince.setVisibility(View.VISIBLE);
                holder.statusSince.setText("Dormant Since: " + statusSince);
            }
        } else if (lastLocation.getStatusCode().equalsIgnoreCase("0")) {
            holder.thumbText.setText("NW");
            setShapeBackground(holder.thumbnail.getBackground(), Color.BLUE);
            if (!TextUtils.isEmpty(statusSince)) {
                holder.statusSince.setVisibility(View.VISIBLE);
                holder.statusSince.setText("NW Since: " + statusSince);
            }
        }

        if (!TextUtils.isEmpty(lastLocation.getBattery()) || !TextUtils.isEmpty(lastLocation.getSignal())) {
            holder.batterySignalLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(lastLocation.getBattery())) {
                holder.battery.setText(lastLocation.getBattery() + "%");
            } else {
                holder.battery.setText(R.string.battery_full + " NA");
            }

            if (!TextUtils.isEmpty(lastLocation.getSignal())) {
                holder.signal.setText(lastLocation.getSignal() + "%");
            } else {
                holder.signal.setText(R.string.signal + " NA");
            }

        } else {
            holder.batterySignalLayout.setVisibility(View.GONE);
        }
        setShapeBackground(holder.mapButton.getBackground(), context.getResources().getColor(R.color.parrot));
        setShapeBackground(holder.historyButton.getBackground(), context.getResources().getColor(R.color.purpler));
        setShapeBackground(holder.ignitionButton.getBackground(), context.getResources().getColor(R.color.brown));

        holder.mapButton.setOnClickListener(new ClickListener(1, lastLocation));
        holder.historyButton.setOnClickListener(new ClickListener(2, lastLocation));
        holder.ignitionButton.setOnClickListener(new ClickListener(3, lastLocation));
        holder.tts.setOnClickListener(new ClickListener(4, lastLocation));
    }

    @Override
    public int getItemCount() {
        return filteredList == null ? 0 : filteredList.size();
    }

    public void setShapeBackground(Drawable background, int color) {
        if (background instanceof ShapeDrawable) {
            // cast to 'ShapeDrawable'
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = lastLocationList;
                    results.count = lastLocationList.size();
                } else {
                    List<LastLocation> filteredList = new ArrayList<>();
                    if (constraint.toString().equalsIgnoreCase("move")) {
                        for (LastLocation location : lastLocationList) {
                            if (location.getStatusCode().equalsIgnoreCase("61714")) {
                                filteredList.add(location);
                            }
                        }
                    } else if (constraint.toString().equalsIgnoreCase("stop")) {
                        for (LastLocation location : lastLocationList) {
                            if (location.getStatusCode().equalsIgnoreCase("61715")) {
                                filteredList.add(location);
                            }
                        }
                    } else if (constraint.toString().equalsIgnoreCase("dormant") || constraint.toString().equalsIgnoreCase("idle")) {
                        for (LastLocation location : lastLocationList) {
                            if (location.getStatusCode().equalsIgnoreCase("61716")) {
                                filteredList.add(location);
                            }
                        }
                    } else if (constraint.toString().equalsIgnoreCase("not working")) {
                        for (LastLocation location : lastLocationList) {
                            if (location.getStatusCode().equalsIgnoreCase("0")) {
                                filteredList.add(location);
                            }
                        }
                    } else {
                        for (LastLocation location : lastLocationList) {
                            if (location.getDisplayName().toUpperCase().contains(constraint.toString())) {
                                filteredList.add(location);
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<LastLocation>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ClickListener implements View.OnClickListener {

        private int id;
        private LastLocation lastLocation;

        public ClickListener(int id, LastLocation lastLocation) {
            this.id = id;
            this.lastLocation = lastLocation;
        }

        @Override
        public void onClick(View v) {
            if (ConnectionDetector.getInstance().isConnectingToInternet(context)) {
                if (id == 1) {
                    if (!TextUtils.isEmpty(lastLocation.getLatitude()) && !TextUtils.isEmpty(lastLocation.getLatitude())) {
                        AppCompatActivity activity = (AppCompatActivity) context;
                        // Fragment fragment = new MapFragment();
                        Fragment fragment = new VehicleMapViewFrag();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("lastLocation", lastLocation);
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
                        ft.replace(R.id.content_frame, fragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    } else {
                        Toast.makeText(context, "Data not found for this vehicle!", Toast.LENGTH_SHORT).show();
                    }

                } else if (id == 2) {
                    if (!TextUtils.isEmpty(lastLocation.getLatitude()) && !TextUtils.isEmpty(lastLocation.getLatitude())) {
                        AppCompatActivity activity = (AppCompatActivity) context;
                        Fragment fragment = new AnimatingMarkersFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("lastLocation", lastLocation);
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit);
                        ft.replace(R.id.content_frame, fragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    } else {
                        Toast.makeText(context, "Data not found for this vehicle!", Toast.LENGTH_SHORT).show();
                    }
                } else if (id == 3) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("CHOOSE");
                    builder.setView(R.layout.ignition_dialog);
                    final CharSequence[] choiceList = {"ON", "OFF"};
                    int selected = -1;
                    builder.setSingleChoiceItems(
                            choiceList,
                            selected,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    buffKey = which;

                                }
                            });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String choice = choiceList[buffKey].toString();
                            if (choice.equalsIgnoreCase(choiceList[0].toString())) {
                                doSendSMS(1, lastLocation);
                            } else {
                                doSendSMS(2, lastLocation);
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (id == 4) {
                    if (ttsProviderImpl != null) {
                        ttsProviderImpl.init(context);
                        String text = "Vehicle Number " + lastLocation.getDisplayName() + " is " + AppUtils.getStatusOfVehicle(lastLocation.getStatusCode()) +
                                " at " + lastLocation.getAddress();
                        ttsProviderImpl.say(text);
                    }
                }
            } else {
                Toast.makeText(context, "No Internet Connecton.", Toast.LENGTH_SHORT).show();
            }
        }


        private void doSendSMS(int type, LastLocation lastLocation) {
            if (type == 1) {

                if (BuildConfig.FLAVOR.equalsIgnoreCase("ffever")) {
                    if (isDualSIM) {

                        AppUtils.sendSMSDualSIM(lastLocation.getSimPhoneNumber(), "#* 1234 IGON#", context);
                    } else {
                        AppUtils.sendSMS(lastLocation.getSimPhoneNumber(), "#* 1234 IGON#", context);
                    }
                } else {
                    if (isDualSIM) {

                        AppUtils.sendSMSDualSIM(lastLocation.getSimPhoneNumber(), rottweilerApplication.get(lastLocation.getDeviceType() + "_ignition_start"), context);
                    } else {
                        AppUtils.sendSMS(lastLocation.getSimPhoneNumber(), rottweilerApplication.get(lastLocation.getDeviceType() + "_ignition_start"), context);
                    }
                }
            } else {

                if (BuildConfig.FLAVOR.equalsIgnoreCase("ffever")) {
                    if (isDualSIM) {

                        AppUtils.sendSMSDualSIM(lastLocation.getSimPhoneNumber(), "#* 1234 IGOFF#", context);
                    } else {
                        AppUtils.sendSMS(lastLocation.getSimPhoneNumber(), "#* 1234 IGOFF#", context);
                    }

                } else {
                    if (isDualSIM) {
                        AppUtils.sendSMSDualSIM(lastLocation.getSimPhoneNumber(), rottweilerApplication.get(lastLocation.getDeviceType() + "_ignition_stop"), context);
                    } else {
                        AppUtils.sendSMS(lastLocation.getSimPhoneNumber(), rottweilerApplication.get(lastLocation.getDeviceType() + "_ignition_stop"), context);
                    }
                }
            }
        }

    }

    class AddresTask extends AsyncTask {

        private TextView targetView;
        private LastLocation lastLocation;
        private String address;

        AddresTask(TextView targetView, LastLocation lastLocation) {
            this.targetView = targetView;
            this.lastLocation = lastLocation;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            String latitude = lastLocation.getLatitude();
            String longitude = lastLocation.getLongitude();
            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                address = AppUtils.reverseGeocode(context, Double.parseDouble(lastLocation.getLatitude()), Double.parseDouble(lastLocation.getLongitude()));
            }
            return address;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            targetView.setText((String) o);
        }
    }

    void createFuelDialog() {
        dialogBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fuel_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setCancelable(false);

        dialogProgress = (ProgressBar) dialogView.findViewById(R.id.loading);
        dialogClose = (LinearLayout) dialogView.findViewById(R.id.cross);
        close = (ImageView) dialogView.findViewById(R.id.close);
        dialogProgress.setVisibility(View.VISIBLE);
        close.setVisibility(View.GONE);
        fuelValue = (TextView) dialogView.findViewById(R.id.fuel_value);
        expectedLeftRun = (TextView) dialogView.findViewById(R.id.expected_left_run);


        fuelDialog = dialogBuilder.create();
        fuelDialog.show();
    }


    class Fuel extends AsyncTask<Void, Void, String> {

        String accountID, deviceID, calibrationValue, maxTankCapacity;

        public Fuel(String accountID, String deviceID, String calibrationValue, String maxTankCapacity) {
            this.accountID = accountID;
            this.deviceID = deviceID;
            this.calibrationValue = calibrationValue;
            this.maxTankCapacity = maxTankCapacity;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = ((MainActivity) context).getSoapServiceInstance().getFuel(accountID, deviceID, calibrationValue, maxTankCapacity);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            dialogBuilder.setCancelable(true);
            dialogProgress.setVisibility(View.GONE);
            close.setVisibility(View.VISIBLE);

            fuelValue.setText("Fuel : " + result + " Ltrs");

            result=result.replace("\"","");

            try {
                int from = (int)(Float.parseFloat(result) * 3.5);
                int to = (int)(Float.parseFloat(result) * 4.5);
                expectedLeftRun.setText("Your Vehicle can run for " + from + " - " + to + " Kms more (*)");
            } catch (NumberFormatException e) {
                expectedLeftRun.setText("Vehicle can run for ##-## Kms more (*)");
            }

            dialogClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fuelDialog.dismiss();
                }
            });

        }
    }

}
