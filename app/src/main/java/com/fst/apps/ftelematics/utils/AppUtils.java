package com.fst.apps.ftelematics.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fst.apps.ftelematics.AppConstants;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.adapters.VehiclesListAdapter;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.restclient.NetworkUtility;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppUtils {

	private static ArrayList<LastLocation> freshList;
	private static ProgressDialog progressDialog;
    private SharedPreferencesManager sharedPrefs;
    private Context context;
	private static NetworkUtility networkUtility;

    public AppUtils(Context context){
        this.context=context;
        sharedPrefs=new SharedPreferencesManager(context);
    }

	public static String reverseGeocode(Context context,double latitude,double longitude){
		if(context==null){
			return null;
		}
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		String result = null;
		try {
			List<Address> addressList = geocoder.getFromLocation(
					latitude, longitude, 1);
			if (addressList != null && addressList.size() > 0) {
				Address address = addressList.get(0);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					sb.append(address.getAddressLine(i)).append("\n");
				}
				//sb.append(address.getLocality()).append("\n");
				//sb.append(address.getPostalCode()).append("\n");
				sb.append(address.getCountryName());
				result = sb.toString();
			}
		} catch (Exception e) {
			Log.e("Rottweiler Trackers", "Unable connect to Geocoder", e);
		}
		return result;
	}


	public static String getStatusOfVehicle(String status){
		String vehicleStatus=null;
		if(!TextUtils.isEmpty(status)){

			if(status.equalsIgnoreCase("0")){
				vehicleStatus="Not Working";
			}else if(status.equalsIgnoreCase("61714")){
				vehicleStatus="Moving";
			}else if(status.equalsIgnoreCase("61715")){
				vehicleStatus="Stop";
			}else if(status.equalsIgnoreCase("61716")){
				vehicleStatus="Dormant";
			}
		}
		return vehicleStatus;
	}

	public static String getAlertTypes(int type){
		String alertType=null;
		if(type==1){
			alertType="IGNITION";
		}else if(type==2){
			alertType="OVERSPEED";
		}else if(type==3){
			alertType="GEOFENCE";
		}
		return alertType;
	}

	public static int sendSMS(String phoneNumber,String message,Context c){
		//phoneNumber="9999933907";
		Intent iSent = new Intent(AppConstants.ACTION_SMS_SENT);
		PendingIntent piSent = PendingIntent.getBroadcast(c, 0, iSent, 0);
		Intent iDel = new Intent(AppConstants.ACTION_SMS_DELIVERED);
		PendingIntent piDel = PendingIntent.getBroadcast(c, 0, iDel, 0);
		SmsManager smsManager = SmsManager.getDefault();
		if(TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(message)){
			Toast.makeText(c, "Empty device number or command!", Toast.LENGTH_LONG).show();
			return 0;
		}
		smsManager.sendTextMessage(phoneNumber, null, message, piSent, piDel); 
		Toast.makeText(c, "Command Sent Successfully!", Toast.LENGTH_LONG).show();
		return 1;
	}

	public static String getDeviceIMEI(Context context){
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	public static void unregisterFromPush(String userId) {
		final String serverUrl = AppConstants.PUSH_SERVER_BASE_URL + "/unregister.php";
		networkUtility=new NetworkUtility();
		final Map<String, String> params = new HashMap<String, String>();
		params.put("name", userId);
		try {
			Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						networkUtility.sendPost(serverUrl, params);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();

		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public static String getPrefferedDateTimeFormat(String timestamp){
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output=new SimpleDateFormat(AppConstants.PREFERRED_DATE_TIME_FORMAT);
		String finalDateTime;
		Date date=new Date();
		try {
			date=sf.parse(timestamp);
			finalDateTime=output.format(date);
		} catch (ParseException e) {
			finalDateTime=new Timestamp(date.getTime()).toString();
			e.printStackTrace();
		}
		return finalDateTime;
	}

	public static String getDateFromUnixTimestsmp(String timestamp){
		String finalDateTime="";
		if(!TextUtils.isEmpty(timestamp)) {
			long unixtimestamp = Long.parseLong(timestamp);
			Date date = new Date(unixtimestamp * 1000L); // *1000 is to convert seconds to milliseconds
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a"); // the format of your date
			//sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
			finalDateTime = sdf.format(date);
		}
		return finalDateTime;
	}

	public static String getAlertNameByType(int alertType){
		String name="";
		if(alertType==AlertTypes.IGNITION){
			name=AppConstants.ALERT_TYPES.Ignition.name();
		}else if(alertType==AlertTypes.OVERSPEED){
			name=AppConstants.ALERT_TYPES.Overspeed.name();
		}else if(alertType==AlertTypes.GEOFENCE){
			name=AppConstants.ALERT_TYPES.Geofence.name();
		}
		return name;
	}

	public static void manualRefresh(HashMap<String, Object> params){
		String url=(String) params.get("url");
		String screen=(String) params.get("screen");
		Context context=(Context) params.get("context");

		if(context!=null){
			Resources res=context.getResources();
			progressDialog=new ProgressDialog(context);
			progressDialog.setTitle(res.getString(R.string.refresh_dialog_title));
			progressDialog.setMessage(res.getString(R.string.refresh_dialog_msg));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}


		if(freshList!=null && freshList.size()>0){
			if(!TextUtils.isEmpty(screen)){
				if(screen.equalsIgnoreCase(AppConstants.SCREEN_NAME_HOME)){
					ArrayList<LastLocation> currentList=(ArrayList<LastLocation>) params.get("currentList");
					VehiclesListAdapter adapter=(VehiclesListAdapter) params.get("adapter");
					if(currentList!=null && currentList.size()>0){
						currentList.clear();
					}
					currentList.addAll(freshList);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	public static String getLastLocationUrl(Context context){
		if(context==null){
			return null;
		}
		SharedPreferencesManager sharedPrefs=new SharedPreferencesManager(context);
		String accountId = null,userId = null,url=null;
		if(sharedPrefs!=null){
			accountId=sharedPrefs.getAccountId();
			userId=sharedPrefs.getAccountId();
		}

		if(TextUtils.isEmpty(accountId) ||TextUtils.isEmpty(userId)){
		//	AppUtils.logout(getApplicationContext());
			return null;
		}
		url="/"+accountId+"/"+userId;
		return url;
	}
	
	public static boolean isDualSIM(Context context){
        if(context!=null && checkForPermission((Activity)context,Manifest.permission.READ_PHONE_STATE)) {
            TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
            //Toast.makeText(context, "dual: "+telephonyInfo.isDualSIM(), Toast.LENGTH_SHORT).show();
            return telephonyInfo.isDualSIM();
        }
        return false;
	}
	
	public static void sendSMSDualSIM(String phoneNumber,String text,Context context){
		//phoneNumber="9999933907";
		Intent i = new Intent(Intent.ACTION_VIEW);
        i.putExtra("address", phoneNumber);
        i.putExtra("sms_body",text);
        i.setType("vnd.android-dir/mms-sms");
        context.startActivity(i);
	}

    public String getLastLocationUrl(){
        String accountId = null,userId = null,url=null,role=null;
        if(sharedPrefs!=null){
            accountId=sharedPrefs.getAccountId();
            userId=sharedPrefs.getUserId();
			role=sharedPrefs.getRole();
            /*accountId="shreeashtech";
            userId="shreeashtech";*/
        }

		if(!TextUtils.isEmpty(role) && role.equalsIgnoreCase("PartialControl")){
			url="/subuser/vehiclelist"+"/"+accountId+"/"+userId+"/Partial";
		}else {
			url="/subuser/vehiclelist"+"/"+accountId+"/"+userId+"/Full";
		}
        return url;
    }

	public static boolean checkForPermission(Activity activity,String permission){
		if(ActivityCompat.checkSelfPermission(activity, permission)== PackageManager.PERMISSION_GRANTED){
			return true;
		}

		return false;
	}

	public static void askForMultiplePermissions(final Activity activity, final List<String> permissionsList) {
		List<String> permissionsNeeded = new ArrayList<String>();

		if (!addPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
			permissionsNeeded.add("Send SMS");
		if (!addPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION))
			permissionsNeeded.add("Read Phone State");
		if (!addPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
			permissionsNeeded.add("Storage");
		if (!addPermission(activity, Manifest.permission.READ_PHONE_STATE))
			permissionsNeeded.add("Read Contacts");

		if (permissionsList.size() > 0) {
			if (permissionsNeeded.size() > 0) {
				// Need Rationale
				String message = "You need to grant access to " + permissionsNeeded.get(0);
				for (int i = 1; i < permissionsNeeded.size(); i++)
					message = message + ", " + permissionsNeeded.get(i);
				showMessageOKCancel(activity,message,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.e("AU:","askForMultiplePermissions:which:"+which);
								switch (which) {
									case -1: //positive
										ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
											AppConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
										break;
									case -2 : //negative
										Toast.makeText(activity, "User has not granted permission for this operation!", Toast.LENGTH_SHORT).show();
										activity.finish();
										break;
								}
							}
						});
				return;
			}
			ActivityCompat.requestPermissions(activity,permissionsList.toArray(new String[permissionsList.size()]),
					AppConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
			return;
		}

	}

	private static boolean addPermission(Activity activity, String permission) {
		if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
//			if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
				return false;
		}
		return true;
	}

	private static void showMessageOKCancel(Activity activity,String message, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(activity)
				.setMessage(message)
				.setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", okListener)
				.create()
				.show();
	}

	public static String getDatesDifference(String startDate,String endDate,String action){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
        long duration = 0;
        String difference=null;
        if(!TextUtils.isEmpty(startDate) && !TextUtils.isEmpty(endDate)) {
            try {
                Date date1 = sdf.parse(startDate);
                Date date2 = sdf.parse(endDate);
                duration = date2.getTime() - date1.getTime();


                if (duration > 0) {
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(duration);
                    long seconds = diffInSec % 60;
                    diffInSec /= 60;
                    long minutes = diffInSec % 60;
                    diffInSec /= 60;
                    long hours = diffInSec % 24;
                    diffInSec /= 24;
                    long days = diffInSec;
                    difference = hours + " hrs " + minutes + " mins " + seconds + " sec";

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(action)) {
            if("STRING".equalsIgnoreCase(action)) {
                return difference;
            }else if("DIFF".equalsIgnoreCase(action)){
                return duration+"";
            }
        }
		return difference;
	}
}
