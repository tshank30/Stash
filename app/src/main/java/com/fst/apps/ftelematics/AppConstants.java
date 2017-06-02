package com.fst.apps.ftelematics;

public class AppConstants {
	public static final String PUSH_SERVER_BASE_URL="http://23.94.21.18:8080/gcm_server";
	public static final String PUSH_SERVER_URL="http://23.94.21.18:8080/gcm_server/register.php";
	//public static final String PUSH_SERVER_BASE_URL="http://192.168.1.4/gcm_server";
	//public static final String PUSH_SERVER_URL="http://192.168.1.4/gcm_server/register.php";
	public static final String GCM_SENDER_ID="726667513847";
	public static final String BASE_SERVICE_URL="http://23.94.21.18:9999";
	public enum ALERT_SUB_TYPES{IN,OUT,ON,OFF,OVERSPEED}
	public enum ALERT_TYPES{Ignition,Overspeed,Geofence};
	public static final String PREFERRED_DATE_TIME_FORMAT="dd/MM/yyyy HH:mm:ss a";
	public static final String ACTION_SMS_SENT = "com.fst.apps.rottweilertrackers.SMS_SENT";
	public static final String ACTION_SMS_DELIVERED = "com.fst.apps.rottweilertrackers.SMS_DELIVERED";
	public static final String APP_ACTIVATION_CODE="1";
	public static final String SCREEN_NAME_HOME="HOME";
	public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS=11;
	public static final int REQUEST_CODE_ASK_PERMISSIONS=10;
	public static final String BASE_REPORTS_URL="http://23.94.21.18:81/";
}
