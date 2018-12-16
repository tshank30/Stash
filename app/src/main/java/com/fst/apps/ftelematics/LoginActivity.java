package com.fst.apps.ftelematics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fst.apps.ftelematics.entities.User;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AlertDialogManager;
import com.fst.apps.ftelematics.utils.ConnectionDetector;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText usernameEditText;
    private EditText accountIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SharedPreferencesManager sharedPref;
    AlertDialogManager alert = new AlertDialogManager();
    static String accountId;
    static String userId;
    static String password;
    private NetworkUtility networkUtility;
    ProgressDialog progressDialog;
    private ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = (EditText) findViewById(R.id.username);
        accountIdEditText = (EditText) findViewById(R.id.account_id);
        passwordEditText = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.btn_login);
        sharedPref = new SharedPreferencesManager(this);
        progressDialog = new ProgressDialog(this);
        cd = new ConnectionDetector();
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                accountId = accountIdEditText.getText().toString();
                userId = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                attemptLogin(accountId, userId, password);
            }
        });


    }

    private void attemptLogin(String accountId, String userName, String password) {
        if (mAuthTask != null) {
            return;
        }

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(accountId) || TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "All the fields are mandatory!", Toast.LENGTH_SHORT);
            return;
        }

        boolean cancel = false;
        if (!cd.isConnectingToInternet(this)) {
            cancel = true;
        }


        if (cancel) {
            usernameEditText.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(userName, accountId, password);
            mAuthTask.execute((Void) null);
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, String> {


        private String url;

        UserLoginTask(String username, String accountId, String password) {
            networkUtility = new NetworkUtility();
            url = "/" + username + "/" + accountId + "/" + password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Contacting Server");
            progressDialog.setMessage("Just a moment..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {
                response = networkUtility.sendGet(url);
                Log.d("Result", response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            progressDialog.dismiss();
            List<User> userList = new ArrayList<User>();
            if (response != null) {
                try {
                    try {
                        userList = new Gson().fromJson(response, new TypeToken<List<User>>() {
                        }.getType());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (userList != null && userList.size() > 0) {
                        User user = userList.get(0);
                        if (user.getIsMobAppUser().equalsIgnoreCase(AppConstants.APP_ACTIVATION_CODE)) {
                            sharedPref.setAccountId(user.getAccountID());
                            sharedPref.setUserId(user.getUserID());
                            sharedPref.setIsMobAppUser(user.getIsMobAppUser());
                            sharedPref.setContactName(user.getContactName());
                            sharedPref.setRole(user.getRoleID());
                            sharedPref.setDealerName(user.getDealer());
                            sharedPref.setIsLoggedIn(true);
                            if (user.getIsSchool()==0)
                                sharedPref.setSchoolAccount(false);
                            else
                                sharedPref.setSchoolAccount(true);
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            LoginActivity.this.finish();
                        } else {
                            alert.showAlertDialog(LoginActivity.this,
                                    "Invalid Activation",
                                    "Your mobile application account has not been activated.Please contact administrator!", false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                Toast.makeText(getApplicationContext(), "User not found, try with different credentials!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


}

