package com.fst.apps.ftelematics;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;

import android.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fst.apps.ftelematics.adapters.ExpandableListAdapter;
import com.fst.apps.ftelematics.entities.ExpandedMenuModel;
import com.fst.apps.ftelematics.fragments.DashboardFragment;
import com.fst.apps.ftelematics.fragments.NearestVehicleFragment;
import com.fst.apps.ftelematics.fragments.NewDashBoardFragment;
import com.fst.apps.ftelematics.fragments.ParentViewPagerFragment;
import com.fst.apps.ftelematics.fragments.ReportsFragment;
import com.fst.apps.ftelematics.fragments.SettingsFragment;
import com.fst.apps.ftelematics.fragments.SupportFragment;
import com.fst.apps.ftelematics.fragments.VehicleMapViewFragment;
import com.fst.apps.ftelematics.fragments.VehiclesListFragment;
import com.fst.apps.ftelematics.fragments.VersionFragment;
import com.fst.apps.ftelematics.service.QuickstartPreferences;
import com.fst.apps.ftelematics.service.RegistrationIntentService;
import com.fst.apps.ftelematics.soapclient.IAppManager;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.fst.apps.ftelematics.utils.TtsProviderFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends BaseActivity implements TextToSpeech.OnInitListener {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private ConnectionDetector cd;
    private SharedPreferencesManager sharedPrefs;
    private TextView contactName, accountId;
    private DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    MainActivity activity;
    private String reportQueryString;
    private TextToSpeech tts;
    private String welcomeText;
    private boolean school;

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
        sharedPrefs = new SharedPreferencesManager(this);
        reportQueryString = "?user=" + sharedPrefs.getUserId() + "&role=" + sharedPrefs.getRole() + "&account=" + sharedPrefs.getAccountId();
        if (sharedPrefs.getSpeechMode()) {
            tts = new TextToSpeech(this, this);
        }
        cd = new ConnectionDetector();

        school = sharedPrefs.getSchoolAccount();
        //school = true;

        if (!sharedPrefs.getIsLoggedIn()) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {

            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                }
            };


            if (checkPlayServices() && !sharedPrefs.getRegisteredForPush()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }


            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
            navigationView.addHeaderView(header);
            contactName = (TextView) header.findViewById(R.id.contact_name);
            accountId = (TextView) header.findViewById(R.id.account_id);
            if (!TextUtils.isEmpty(sharedPrefs.getContactName())) {
                contactName.setText(sharedPrefs.getContactName());
                welcomeText = "Welcome " + sharedPrefs.getContactName();
            } else {
                contactName.setText(sharedPrefs.getAccountId());
                welcomeText = "Welcome " + sharedPrefs.getAccountId();
            }

            if (!TextUtils.isEmpty(sharedPrefs.getDealerName())) {
                welcomeText = "Welcome " + sharedPrefs.getDealerName();
            }

            if (!TextUtils.isEmpty(sharedPrefs.getAccountId())) {
                accountId.setText(sharedPrefs.getAccountId());
            }

            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }

            setNavMenuIcons(navigationView);
            mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList,school);

            // setting list adapter
            expandableList.setAdapter(mMenuAdapter);

            expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                    Fragment fragment = null;
                    Bundle bundle = new Bundle();
                    switch (i1) {
                        case 0:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DailyReport.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "DailyReport.aspx" + reportQueryString);
                            break;
                        case 1:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "StoppageReport.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "StoppageReport.aspx" + reportQueryString);
                            break;
                        case 2:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DailyTripReport.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "DailyTripReport.aspx" + reportQueryString);
                            break;
                        case 3:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "IdlingReport.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "IdlingReport.aspx" + reportQueryString);
                            break;
                        case 4:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "FuelMonitoring.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "FuelMonitoring.aspx" + reportQueryString);
                            break;
                        case 5:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DistanceReport.aspx" + reportQueryString);
                            break;
                        case 6:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "MonthlyReport.aspx" + reportQueryString);
                            break;
                        default:
                            bundle.putString("url", AppConstants.BASE_REPORTS_URL + "DailyTripReport.aspx" + reportQueryString);
                            Log.i("URL", AppConstants.BASE_REPORTS_URL + "DailyTripReport.aspx" + reportQueryString);
                            break;
                    }
                    fragment = new ReportsFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(fragment.getClass().toString()).commitAllowingStateLoss();
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
            expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                    Fragment fragment = null;
                    switch (i) {
                        case 0:

                            if (BuildConfig.FLAVOR.equals("brandwatch") || BuildConfig.FLAVOR.equals("rkdk")) {
                                fragment = new NewDashBoardFragment();
                            } else {
                                fragment = new DashboardFragment();
                            }
                            break;
                        case 1:
                            fragment = new VehiclesListFragment();
                            break;
                        case 2:
                            fragment = new ParentViewPagerFragment();
                            break;
                        case 3:
                            if (school)
                                fragment = new NearestVehicleFragment();
                            else
                                return false;
                            break;
                        case 4:
                            if (school)
                                fragment = new SettingsFragment();
                            else
                                fragment = new NearestVehicleFragment();
                            break;

                        case 5:
                            if (school)
                                fragment = new SupportFragment();
                            else
                                fragment = new SettingsFragment();
                            break;

                        case 6:
                            if (school)
                                fragment = new VersionFragment();
                            else
                                fragment = new SupportFragment();
                            break;

                        case 7:
                            if (school) {
                                if (cd.isConnectingToInternet(activity)) {
                                    sharedPrefs.clearSharedPreferences();
                                    DatabaseHelper dh = new DatabaseHelper(activity);
                                    dh.clearVehicleList();
                                    AppUtils.unregisterFromPush(AppUtils.getDeviceIMEI(activity));
                                    activity.finish();
                                    startActivity(new Intent(activity, LoginActivity.class));
                                } else {
                                    Toast.makeText(activity, "Please connect to working internet connection!", Toast.LENGTH_SHORT).show();
                                }
                            } else
                                fragment = new VersionFragment();
                            break;

                        case 8:
                            if (!school) {
                                if (cd.isConnectingToInternet(activity)) {
                                    sharedPrefs.clearSharedPreferences();
                                    DatabaseHelper dh = new DatabaseHelper(activity);
                                    dh.clearVehicleList();
                                    AppUtils.unregisterFromPush(AppUtils.getDeviceIMEI(activity));
                                    activity.finish();
                                    startActivity(new Intent(activity, LoginActivity.class));
                                } else {
                                    Toast.makeText(activity, "Please connect to working internet connection!", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }

                        default:
                            fragment = new VehiclesListFragment();
                            break;
                    }

                    if (fragment != null /*&& (fragment instanceof DashboardFragment || fragment instanceof NewDashBoardFragment)*/) {
                        Fragment currentFrag = getFragmentManager().findFragmentById(R.id.content_frame);
                        if ( currentFrag!=null && fragment!=null && !currentFrag.getClass().getName().equals(fragment.getClass().getName())) {
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            if ((currentFrag instanceof DashboardFragment || currentFrag instanceof NewDashBoardFragment) && getFragmentManager().getBackStackEntryCount() == 0)
                                ft.addToBackStack(fragment.getClass().toString());
                            ft.replace(R.id.content_frame, fragment).commitAllowingStateLoss();
                        }
                    }

                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                    return true;
                }
            });

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

            {
                List<String> permissions = new ArrayList<String>();
                if (!AppUtils.checkForPermission(this, Manifest.permission.SEND_SMS)) {
                    permissions.add(Manifest.permission.SEND_SMS);
                }
                if (!AppUtils.checkForPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                    permissions.add(android.Manifest.permission.READ_PHONE_STATE);
                }
                if (!AppUtils.checkForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (!AppUtils.checkForPermission(this, Manifest.permission.GET_ACCOUNTS)) {
                    permissions.add(Manifest.permission.GET_ACCOUNTS);
                }
                if (permissions != null) {
                    if (permissions.size() > 0) {
                        AppUtils.askForMultiplePermissions(MainActivity.this, permissions);
//                    Fragment fragment = new VehiclesListFragment();
//                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                    transaction.replace(R.id.content_frame, fragment);
//                    transaction.commit();
                    } else {
                        //permission already granted..replacing fragment

                        if (BuildConfig.FLAVOR.equals("brandwatch") || BuildConfig.FLAVOR.equals("rkdk")) {
                            Fragment fragment = new NewDashBoardFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, fragment);
                            transaction.commitAllowingStateLoss();
                        } else {
                            Fragment fragment = new DashboardFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, fragment);
                            transaction.commitAllowingStateLoss();
                        }

                        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("ListFragment")) {
                            Fragment fragment = new VehiclesListFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, fragment);
                            transaction.commitAllowingStateLoss();
                        }

                    }
                }

            } else

            {

                if (BuildConfig.FLAVOR.equals("brandwatch") || BuildConfig.FLAVOR.equals("rkdk")) {
                    Fragment fragment = new NewDashBoardFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commitAllowingStateLoss();
                } else {
                    Fragment fragment = new DashboardFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commitAllowingStateLoss();
                }

                if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("ListFragment")) {
                    Fragment fragment = new VehiclesListFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commitAllowingStateLoss();
                }
            }
        }

    }


    public void setNavMenuIcons(NavigationView navigationView) {
        navigationView.setItemIconTintList(null);
        navigationView.setItemTextAppearance(R.style.MenuText);
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        ExpandedMenuModel menuItem1 = new ExpandedMenuModel();
        menuItem1.setIconName("Dashboard");
        menuItem1.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_home)
                .colorRes(R.color.orange)
                .actionBarSize());
        listDataHeader.add(menuItem1);

        ExpandedMenuModel menuItem2 = new ExpandedMenuModel();
        menuItem2.setIconName("Vehicles");
        menuItem2.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_cab)
                .colorRes(R.color.bloodRed)
                .actionBarSize());
        listDataHeader.add(menuItem2);

        ExpandedMenuModel menuItem3 = new ExpandedMenuModel();
        menuItem3.setIconName("Alerts");
        menuItem3.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_info)
                .colorRes(R.color.colorPrimaryDark)
                .actionBarSize());
        listDataHeader.add(menuItem3);

        if (!school) {
            ExpandedMenuModel menuItem4 = new ExpandedMenuModel();
            menuItem4.setIconName("Reports");
            menuItem4.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_book)
                    .colorRes(R.color.brown)
                    .actionBarSize());
            listDataHeader.add(menuItem4);

            List<String> reportChilds = new ArrayList<String>();
            reportChilds.add("Daily Report");
            reportChilds.add("Stoppage Report");
            reportChilds.add("Trip Report");
            reportChilds.add("Idling Report");
            reportChilds.add("Fuel Report");
            reportChilds.add("Distance Report");
            reportChilds.add("Monthly Report");
            listDataChild.put(listDataHeader.get(3), reportChilds);
        }

        ExpandedMenuModel menuItem9 = new ExpandedMenuModel();
        menuItem9.setIconName("Nearest Vehicle");
        menuItem9.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_location_arrow)
                .colorRes(R.color.blue)
                .actionBarSize());
        listDataHeader.add(menuItem9);


        ExpandedMenuModel menuItem5 = new ExpandedMenuModel();
        menuItem5.setIconName("Settings");
        menuItem5.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_cog)
                .colorRes(R.color.purpler)
                .actionBarSize());
        listDataHeader.add(menuItem5);

        ExpandedMenuModel menuItem6 = new ExpandedMenuModel();
        menuItem6.setIconName("Support");
        menuItem6.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_phone)
                .colorRes(R.color.teal)
                .actionBarSize());
        listDataHeader.add(menuItem6);

        ExpandedMenuModel menuItem7 = new ExpandedMenuModel();
        menuItem7.setIconName("Version/Update");
        menuItem7.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_user_secret)
                .colorRes(R.color.cardview_dark_background)
                .actionBarSize());
        listDataHeader.add(menuItem7);

        ExpandedMenuModel menuItem8 = new ExpandedMenuModel();
        menuItem8.setIconName("Logout");
        menuItem8.setIconImg(new IconDrawable(this, FontAwesomeIcons.fa_arrow_left)
                .colorRes(R.color.colorAccent)
                .actionBarSize());
        listDataHeader.add(menuItem8);


    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() == 0 && ((fragment instanceof DashboardFragment) || (fragment instanceof NewDashBoardFragment))) {
            finish();
        } else {
            // Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment != null && (!(fragment instanceof DashboardFragment) || !(fragment instanceof NewDashBoardFragment))) {
                Log.d("MainActivity", "onBackPressed: " + fragment.getTag());
                getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();

            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Fragment fragment = new SettingsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
            return true;
        }

        if (id == R.id.action_map_view) {
            Fragment fragment = new VehicleMapViewFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case AppConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                        perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices() && !sharedPrefs.getRegisteredForPush()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "User has not granted permission for this operation!", Toast.LENGTH_SHORT)
                            .show();
                }

                Log.e("MA:", "PackageManager.PERMISSION_GRANTED:" + PackageManager.PERMISSION_GRANTED);
                Log.e("MA:", "PackageManager.PERMISSION_DENIED:" + PackageManager.PERMISSION_DENIED);
                Log.e("MA:", "onRequestPermissionsResult:WRITE_EXTERNAL_STORAGE:" + perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Fragment fragment = new VehiclesListFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commitAllowingStateLoss();
                } else {
                    Toast.makeText(this, "User has not granted permission for this operation!", Toast.LENGTH_SHORT).show();
                    finish();
                }


            }
            break;
            case AppConstants.REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "User has not granted permission for this operation!", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public IAppManager getSoapServiceInstance() {
        return imService;
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }


    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onInit(int status) {
        if (tts != null && status == TextToSpeech.SUCCESS) {
            Locale loc = new Locale("en", "IN");
            tts.setSpeechRate(0.7f);
            tts.setLanguage(loc);
            if (welcomeText != null)
                tts.speak(welcomeText, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}

