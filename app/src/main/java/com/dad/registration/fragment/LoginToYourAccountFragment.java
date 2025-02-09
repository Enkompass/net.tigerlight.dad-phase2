package com.dad.registration.fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.dad.LocationBroadcastServiceNew;
import com.dad.R;
import com.dad.blework.AlarmReceiver;
import com.dad.blework.BleReceiver;
import com.dad.home.BaseActivity;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.model.GetUserInfoModel;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.registration.webservices.WsCallLogin;
import com.dad.settings.webservices.WsGetUserData;
import com.dad.util.Preference;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class LoginToYourAccountFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = LoginToYourAccountFragment.class.getSimpleName();
    private View view;
    private EditText etUserName;
    private EditText etPassword;
    private TextView tvLogin;
    private TextView tvForgotPwd;
    private AppCompatCheckBox cbRememberMe;
    boolean isChecked;
    private AsyncTaskLocalLogin asyncTaskLocalLogin;
    private ProgressDialog progressDialog;
    private TextView tvCancel;
    private Context context;
    private double lat;
    private double log;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private AlarmManager alarmManager;
    private PendingIntent broadcast;
    private final String TAG_REFRESH_LOC = "resfresh_Location";
    private GetUserInfoModel profileModel;
    private AsyncTaskGetUserInfo asyncTaskGetUserInfo;
    //gcm
    private GoogleCloudMessaging gcm;
    private String deviceToken;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
//    private String SENDER_ID = "308732044105";
    private String SENDER_ID = "32989397760";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragemnt_login_to_your_account, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        etUserName = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_user_name);
        etPassword = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_password);
        tvLogin = (TextView) view.findViewById(R.id.fragment_login_to_your_account_tv_login);
        tvCancel = (TextView) view.findViewById(R.id.fragment_login_to_your_account_tv_cancel);
        tvForgotPwd = (TextView) view.findViewById(R.id.fragment_login_to_your_account_tv_forgot_pwd);
        cbRememberMe = (AppCompatCheckBox) view.findViewById(R.id.fragment_login_to_your_account_cb_remember_me);
        //Set the click lister
        tvLogin.setOnClickListener(this);
        tvForgotPwd.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        lat = ((BaseActivity) getActivity()).getLatitude();
        log = ((BaseActivity) getActivity()).getLongitude();
        gcmRegistrationProcess();

        startRefreshTimeTimer();

        setLogIndetails();

//        /*  This is used for clear text from the edittex when press the cross icoc*/
//        etUserName.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (event.getRawX() >= (etUserName.getRight() - etUserName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        etUserName.setText("");
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

        cbRememberMe.setOnCheckedChangeListener(this);

    }

    private void setLogIndetails() {
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_REMEMBER, false)) {
            etUserName.setText(Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, ""));
            etPassword.setText(Preference.getInstance().mSharedPreferences.getString(Constant.KEY_PASSWORD, ""));
            cbRememberMe.setChecked(true);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        loginPreferences = getActivity().getSharedPreferences("loginPrefs", getActivity().MODE_PRIVATE);
//        loginPrefsEditor = loginPreferences.edit();
//        saveLogin = loginPreferences.getBoolean("saveLogin", false);
//        if (saveLogin == true) {
//            etUserName.setText(loginPreferences.getString("username", ""));
//            etPassword.setText(loginPreferences.getString("password", ""));
//            cbRememberMe.setChecked(true);
//        } else {
//            loginPrefsEditor.clear();
//            loginPrefsEditor.commit();
//        }
    }


    private void startRefreshTimeTimer() {
        if (broadcast != null) {
            alarmManager.cancel(broadcast);
        }
        int refreshTimeInterval = Preference.getInstance().mSharedPreferences.getInt(Constant.KEY_REFRESH_LOC, 5000);
        if (refreshTimeInterval != 0) {
            alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);
            broadcast = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), refreshTimeInterval * 60 * 1000, broadcast);
        }
    }

    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.fragment_login_to_your_account_tv_login:
//                if (cbRememberMe.isChecked()) {
//                    loginPrefsEditor.putBoolean("saveLogin", true);
//                    loginPrefsEditor.putString("username", etUserName.getText().toString().trim());
//                    loginPrefsEditor.putString("password", etPassword.getText().toString().trim());
//                    loginPrefsEditor.commit();
//                    //cbRememberMe.setBackgroundResource(R.drawable.img_checkbox);
//                } else {
//                    loginPrefsEditor.clear();
//                    loginPrefsEditor.commit();
//                }
                validateFields();
                break;
            case R.id.fragment_login_to_your_account_tv_forgot_pwd:
                getFragmentManager().beginTransaction().add(R.id.activity_registartion_fl_container, new ForgotPasswordFragment(), ForgotPasswordFragment.class.getSimpleName()).hide(this).addToBackStack(ForgotPasswordFragment.class.getSimpleName()).commit();
                break;
            case R.id.fragment_login_to_your_account_tv_cancel:
                getLocalFragmentManager().popBackStack();
                break;
        }
    }

    private void validateFields() {
        String email = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.trim().equals("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALID_USERNAME), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();
        } else if (!Utills.isValidEmail(etUserName.getText().toString().trim())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_VALID_EMAIL), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();
        } else if (password.trim().equals("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALID_PASSWORD), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();
        } else if (Utills.isValidEmail(etUserName.getText().toString().trim())) {
            Log.d("LoginSuceess", "start logintask from here");
            if (Utills.isOnline(getActivity(), true)) {
                startLocalLoginTask(email, password);
            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
            }
        }
    }

    /**
     * To load Fragment.
     */
    private void openDashBoardFragment() {
        final android.app.FragmentManager manager = getFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.activity_registartion_fl_container, new DashBoardWithSwipableFragment());
        transaction.hide(this);
//        transaction.addToBackStack(str);
        transaction.commit();


//        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE).beginTransaction().add(R.id.activity_registartion_fl_container, new DashBoardWithSwipableFragment(), DashBoardWithSwipableFragment.class.getSimpleName()).hide(this).addToBackStack(DashBoardWithSwipableFragment.class.getSimpleName()).commit();
    }

    private void startLocalLoginTask(String email, String password) {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskLocalLogin != null && asyncTaskLocalLogin.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLocalLogin.execute();
            } else if (asyncTaskLocalLogin == null || asyncTaskLocalLogin.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLocalLogin = new AsyncTaskLocalLogin(email, password);
                asyncTaskLocalLogin.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Preference.getInstance().savePreferenceData(Constant.IS_REMEMBER, b);
        if (b) {
            Preference.getInstance().savePreferenceData(Constant.KEY_EMAIL, etUserName.getText().toString());
            Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, etPassword.getText().toString());
        } else {
            Preference.getInstance().savePreferenceData(Constant.KEY_EMAIL, "");
            Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, "");
        }
    }

    private class AsyncTaskLocalLogin extends AsyncTask<String, Void, String> {
        private WsCallLogin wsLogin;
        private String userName;
        private String password;


        public AsyncTaskLocalLogin(String userName, String password) {
            this.userName = userName;
            this.password = password;
            wsLogin = new WsCallLogin(getActivity());

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }


        @Override
        protected String doInBackground(String... strings) {
            wsLogin.executeService(userName, password, String.valueOf(lat), String.valueOf(log));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
            if (!isCancelled()) {
                if (wsLogin.isSuccess()) {
//                    final boolean isAccepted = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ACCEPT, false);
//                    if(isAccepted){
//
//                    }
                    //Utills.displayDialog(getActivity(), getString(R.string.reday_to_use), wsLogin.getMessage(), getString(android.R.string.ok), "", false, false);
                    Preference.getInstance().savePreferenceData(Constant.KEY_EMAIL, userName);
                    Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, password);
                    Preference.getInstance().savePreferenceData(Constant.IS_LOGIN, true);
                    Preference.getInstance().savePreferenceData(Constant.USER_ID, wsLogin.getUser_id());
                    Log.d("Login_ID", wsLogin.getMessage());

//                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {
//                        final Intent intent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//                        getActivity().startService(intent);
//                    }

                    long time = 1000 * 5;  //For repiting 30 second

                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {

                        Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
                        PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);


//                getActivity().startService(intent);
                    }

//                    if(!Utills.isMyServiceRunning(BleService.class, getActivity()))
//                    {
//
//                        Intent serviceIntentBle = new Intent(getActivity(), BleService.class);
//                        PendingIntent pendingIntentBle = PendingIntent.getService(getActivity(), 1001, serviceIntentBle, PendingIntent.FLAG_CANCEL_CURRENT);
//                        AlarmManager alarmManagerble = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                        alarmManagerble.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntentBle);
//
//
//                    }


                    getUserInfo();
                    //openDashBoardFragment();
//                    if (cbRememberMe.isChecked()) {
////                        Store the credential here
//                        if (cbRememberMe.isChecked()) {
//                            loginPrefsEditor.putBoolean("saveLogin", true);
//                            loginPrefsEditor.putString("username", etUserName.getText().toString().trim());
//                            loginPrefsEditor.putString("password", etPassword.getText().toString().trim());
//                            loginPrefsEditor.commit();
//                        } else {
//                            loginPrefsEditor.clear();
//                            loginPrefsEditor.commit();
//                        }
//                    }

                } else {
                    progressDialog.dismiss();
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsLogin.getMessage(), getString(R.string.TAG_OK), "", false, false);
                }


            }


        }


    }

    private void getUserInfo() {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskGetUserInfo != null && asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskGetUserInfo.execute();
            } else if (asyncTaskGetUserInfo == null || asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskGetUserInfo = new AsyncTaskGetUserInfo();
                asyncTaskGetUserInfo.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private class AsyncTaskGetUserInfo extends AsyncTask<Void, Void, Void> {

        private WsGetUserData wsGetUserData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = ProgressDialog.show(getActivity(), "", "Loading, Please wait");
//            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsGetUserData = new WsGetUserData(getActivity());
            wsGetUserData.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            //}
            if (!isCancelled()) {
                if (wsGetUserData.isSuccess()) {
                    profileModel = wsGetUserData.getGetUserInfoModel();
                    Preference.getInstance().savePreferenceData(Constant.USER_NAME, profileModel.getUsername());
                    startBackgroundThreadForBLE();
                    ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());

                } else {
                    if (!wsGetUserData.getMessage().trim().isEmpty()) {
                        Utills.displayDialogNormalMessage(getString(R.string.app_name), wsGetUserData.getMessage(), getActivity());
                    } else {
                        Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.alert_something_wrong), getActivity());

                    }
                }
            }
        }
    }


    private static final long SCAN_PERIOD = 1000;

    private void startBackgroundThreadForBLE() {
        AlarmManager alarmManagerForBLE = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), BleReceiver.class);
        PendingIntent broadcastIntentBle = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManagerForBLE.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 2 * 60 * SCAN_PERIOD, broadcastIntentBle);
    }


    // ////////////// GCM Implementation/////////////////////
    private void gcmRegistrationProcess() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getActivity());
            deviceToken = getRegistrationId(getActivity());
            if (deviceToken.isEmpty()) {
                registerInBackground();
            } else {
                storeRegistrationId(getActivity(), deviceToken);
            }
            Log.i(TAG, "Device Token : " + deviceToken);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private boolean checkPlayServices() {
        int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        return false;
    }

    private void registerInBackground() {
        new GcmRegistrationtask().execute();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getActivity().getSharedPreferences(LoginToYourAccountFragment.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private class GcmRegistrationtask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(getActivity());
                }
                deviceToken = gcm.register(SENDER_ID);
                Log.i(TAG, "Device Token found: " + deviceToken);
                storeRegistrationId(getActivity(), deviceToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final int appVersion = getAppVersion(context);
        Preference preference = Preference.getInstance();
        preference.savePreferenceData(preference.KEY_DEVICE_TOKEN, regId);
        preference.savePreferenceData(PROPERTY_APP_VERSION, appVersion);

//        final SharedPreferences prefs = getGCMPreferences(context);
//        int appVersion = getAppVersion(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(PROPERTY_REG_ID, regId);
//        editor.putInt(PROPERTY_APP_VERSION, appVersion);
//        editor.commit();
//        Preference.getInstance().savePreferenceData(C.DEVICE_TOKEN, regId);
    }


}

