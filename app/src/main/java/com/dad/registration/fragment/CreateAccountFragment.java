package com.dad.registration.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dad.LocationBroadcastServiceNew;
import com.dad.R;
import com.dad.blework.BleReceiver;
import com.dad.cropimage.CropImage;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.registration.webservices.WsCallRegistrer;
import com.dad.settings.webservices.WsUploadImage;
import com.dad.simplecropping.CameraUtil;
import com.dad.simplecropping.Constants;
import com.dad.util.Preference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * CreateAccountFragment : new user can register
 */
public class CreateAccountFragment extends BaseFragment {

    private static final String TAG = "CreateAccountFragment";

    private String userChoosenTask;


    //TO check whether image taken or not
    private boolean isImageUpdated;
    //To store the cropped path
    private String path;


    //for lat and long
    private double lat;
    private double log;
    boolean result = true;

    private View view;
    private TextView tvCancel;
    private TextView tvSave;
    //    private TextView tvSHow;
//    private TextView tvHide;
    private TextView tvCheckEnteries;
    private ImageView imProfile;

    private EditText etUserName;
    private EditText etPhoneNo;
    private EditText etEmailId;
    private EditText etPassword;
    private EditText etRePassword;
    private CheckBox cbToggle;


    private AsyncTaskSignUp asyncTaskSignUp;
    private ProgressDialog progressDialog;
    String croppedFile;

    //gcm
    private GoogleCloudMessaging gcm;
    private String deviceToken;
    private String regid;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
//    private String SENDER_ID = "308732044105";
    private String SENDER_ID = "32989397760";
    private String tempPath;
    private File imageFile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_create_account, container, false);


        return view;
    }

    @Override
    public void initView(View view) {
        tvCancel = (TextView) view.findViewById(R.id.fragment_create_account_tv_cancel);
        tvSave = (TextView) view.findViewById(R.id.fragment_create_account_tv_save);
        tvCheckEnteries = (TextView) view.findViewById(R.id.fragment_create_account_tv_check_entries);
        etUserName = (EditText) view.findViewById(R.id.fragment_create_account_et_user_name);
        etEmailId = (EditText) view.findViewById(R.id.fragment_create_account_custom_et_email_id);
        etPhoneNo = (EditText) view.findViewById(R.id.fragment_create_account_custom_et_phone_no);
        etPassword = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_pwd);
        etRePassword = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_re_password);
        imProfile = (ImageView) view.findViewById(R.id.fragment_create_account_custom_iv_user_profile);
        imProfile.setImageResource(R.drawable.ic_pf_pic);

        cbToggle = (CheckBox) view.findViewById(R.id.fragment_create_account_toggle_cb);

//        lat = ((BaseActivity) getActivity()).getLatitude();
//        log = ((BaseActivity) getActivity()).getLongitude();

        gcmRegistrationProcess();


        cbToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int start, end;
                Log.d("inside checkbox chnge", "" + isChecked);

                if (!isChecked) {
                    cbToggle.setText(getString(R.string.show));
                    start = etPassword.getSelectionStart();
                    end = etPassword.getSelectionEnd();
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etPassword.setSelection(start, end);
                } else {

                    cbToggle.setText(getString(R.string.hide));
                    start = etPassword.getSelectionStart();
                    end = etPassword.getSelectionEnd();
                    etPassword.setTransformationMethod(null);
                    etPassword.setSelection(start, end);
                }
            }
        });

        imProfile.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvCheckEnteries.setOnClickListener(this);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        if (v.getId() == tvSave.getId()) {
            validateFragment();
        } else if (v.getId() == tvCancel.getId()) {
            getActivity().onBackPressed();
        } else if (v.getId() == tvSave.getId()) {

        } else if (v.getId() == tvCheckEnteries.getId()) {


        } else if (v.getId() == imProfile.getId()) {
            selectImage();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.REQUEST_CODE_GALLERY:
                try {
                    final InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    final FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    CameraUtil.copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    startCropImage();
                } catch (Exception e) {
//                    Utils.displayMessageDialog(this, e.getMessage());
                    e.printStackTrace();
                }
                break;
            case Constants.REQUEST_CODE_TAKE_PICTURE:
                startCropImage();
                break;
            case Constants.REQUEST_CODE_CROP_IMAGE:
                path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                Glide.with(this).load(imageFile).asBitmap().centerCrop().into(new BitmapImageViewTarget(imProfile) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        final RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imProfile.setImageDrawable(circularBitmapDrawable);
                        isImageUpdated = true;
                    }
                });

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void selectImage() {
        final CharSequence[] items = {getString(R.string.TAG_TAKE_PHOTO), getString(R.string.TAG_CHOOSE_FROM_GALLERY),
                getString(R.string.fragment_create_account_tv_cancel)};


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.TAG_ADD_Photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getString(R.string.TAG_TAKE_PHOTO))) {
                    userChoosenTask = getString(R.string.TAG_TAKE_PHOTO);
                    gotoCamera();
//


                } else if (items[item].equals(getString(R.string.TAG_CHOOSE_FROM_GALLERY))) {
                    userChoosenTask = getString(R.string.TAG_CHOOSE_FROM_GALLERY);
                    gotoGallery();
//

                } else if (items[item].equals(getString(R.string.fragment_create_account_tv_cancel))) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    public void gotoCamera() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = CameraUtil.getOutputMediaFile(1);
            final Uri mImageCaptureUri = Uri.fromFile(imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, Constants.REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {
            Log.d("TAG", "cannot take picture", e);
        }
    }

    public void gotoGallery() {
        imageFile = CameraUtil.getOutputMediaFile(1);
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constants.REQUEST_CODE_GALLERY);
    }


    // Crop Image
    private void startCropImage() {
        if (imageFile != null) {
            final int rotation = CameraUtil.checkExIfInfo(imageFile.getPath());
            if (rotation != 0) {
                CameraUtil.rotateImage(imageFile.getPath(), rotation);
            }
            final Intent intent = new Intent(getActivity(), CropImage.class);
            intent.putExtra(CropImage.IMAGE_PATH, imageFile.getPath());
            intent.putExtra(CropImage.SCALE, true);
            intent.putExtra(CropImage.ASPECT_X, 2);
            intent.putExtra(CropImage.ASPECT_Y, 2);
            startActivityForResult(intent, Constants.REQUEST_CODE_CROP_IMAGE);
        }
    }


    private void validateFragment() {

        if (etUserName.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_FIRSTNAME_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();

        } else if (etPhoneNo.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etPhoneNo.requestFocus();

        }

//        else if (etPhoneNo.getText().toString().trim().length() < 10) {
//            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_MIN_TEN_MSG), getString(android.R.string.ok), "", false, false);
//            etPhoneNo.requestFocus();
//
//        }
//
        else if (etEmailId.getText().toString().trim().equals("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_EMAIL_ID), getString(R.string.TAG_OK), "", false, false);
            etEmailId.requestFocus();

        } else if (!Utills.isValidEmail(etEmailId.getText().toString().trim())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_VALID_EMAIL), getString(R.string.TAG_OK), "", false, false);
            etEmailId.requestFocus();

        } else if (etPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PASSWORD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();

        } else if (etPassword.getText().toString().trim().length() < 7) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PASSWORD_LENGTHMSG), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();
        }

//        *//* else if (!Utills.validatePassword(etPassword.getText().toString())) {
//            Utills.displayDialog(getActivity(), getString(R.string.app_name), "Valid Password", getString(android.R.string.ok), "", false, false);
//        }*/

        else if (etRePassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_RE_PASSWORD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etRePassword.requestFocus();

        } else if (!etPassword.getText().toString().trim().equalsIgnoreCase("") && !etRePassword.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etPassword.getText().toString().trim(), etRePassword.getText().toString().trim())) {
                Log.d("From here", "Call service");
                if (Utills.isOnline(getActivity(), true)) {
                    signUp();
                    // Utils.displayDialog(this, getString(R.string.app_name), "Account has been created", getString(android.R.string.ok), "", false, true);
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PWD_RE_PWD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
                etPassword.requestFocus();
            }
        }


    }

    private void signUp() {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskSignUp != null && asyncTaskSignUp.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSignUp.execute();
            } else if (asyncTaskSignUp == null || asyncTaskSignUp.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSignUp = new AsyncTaskSignUp();
                asyncTaskSignUp.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    /**
     * This method is used ot check pwd and repwd values.
     * if match returens true else false.
     *
     * @param password
     * @param confirmPassword
     * @return
     */

    public boolean checkPassWordAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
            }
        }
        return pstatus;
    }

    private class AsyncTaskSignUp extends AsyncTask<Void, Void, Void> {

        private WsCallRegistrer wsCreateAccount;
        private String etUserNameStr = etUserName.getText().toString().trim();
        private String etPhoneNoStr = etPhoneNo.getText().toString().trim();
        private String etEmailIdStr = etEmailId.getText().toString().trim();
        private String etPasswordStr = etPassword.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsCreateAccount = new WsCallRegistrer(getActivity());
            wsCreateAccount.executeService(etEmailIdStr, etPasswordStr, String.valueOf(lat), String.valueOf(log), etUserNameStr, etPhoneNoStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.cancel();
            if (!isCancelled()) {
                if (wsCreateAccount.isSuccess()) {
                    final Preference preference = Preference.getInstance();
                    Preference.getInstance().savePreferenceData(Constant.KEY_EMAIL, etEmailIdStr);
                    Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, etPasswordStr);
                    preference.savePreferenceData(Constant.USER_ID, wsCreateAccount.getUserid());
                    preference.savePreferenceData(Constant.IS_FIRST_ACCOUNT, true);
                    preference.savePreferenceData(Constant.USER_NAME, etUserNameStr);
                    Preference.getInstance().savePreferenceData(Constant.IS_LOGIN, true);
                    startBackgroundThreadForBLE();
//
//                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {
//                        final Intent intent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//                        getActivity().startService(intent);
//                    }


                    long time = 1000 * 3;  //For repiting 30 second

                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {

                        Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
                        PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);


//                getActivity().startService(intent);
                    }

//                    if (!Utills.isMyServiceRunning(BleService.class, getActivity())) {
//                        Intent serviceIntentBle = new Intent(getActivity(), BleService.class);
//                        PendingIntent pendingIntentBle = PendingIntent.getService(getActivity(), 1001, serviceIntentBle, PendingIntent.FLAG_CANCEL_CURRENT);
//                        AlarmManager alarmManagerBle = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                        alarmManagerBle.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntentBle);
//
//                    }


                    if (isImageUpdated) {
                        new updateProfilePicture().execute();
                    } else {

                        Toast.makeText(getActivity(), getString(R.string.TAG_REG_SUC_MSG), Toast.LENGTH_SHORT).show();
//                        ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());
                    }

                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCreateAccount.getMessage(), getString(android.R.string.ok), "", false, false);
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_UNABLE_CREATE_ACCOUNT), getString(R.string.ok), "", false, false);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // cancel async task if any pending.
        if (asyncTaskSignUp != null && asyncTaskSignUp.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSignUp.cancel(true);
        }
    }

    private class updateProfilePicture extends AsyncTask<Void, Void, Void> {
        private static final String KEY_SUCCESS = "success";
        private int response;
        private WsUploadImage wsUploadImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsUploadImage = new WsUploadImage(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Utills.isInternetConnected(getActivity())) {

                wsUploadImage.executeService(path);
//                response = 0;
//                JSONObject addedJson = wsUploadImage.executeService(path);
//                try {
//                    if (addedJson!= null && addedJson.getInt(KEY_SUCCESS) != 0) {
//                        int response = addedJson.getInt(KEY_SUCCESS);
//                        if (response == 1) {
//                            return KEY_SUCCESS;
//                        }
//                    } else {
//                        response = 2;
//                        return wsUploadImage.getMessage();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                response = 1;
//            }
//            return KEY_SUCCESS;
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsUploadImage.isSuccess()) {
                    ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());
//                    replaceChildFragment(new DashBoardWithSwipableFragment(), R.id.activity_registartion_fl_container);

                }
            }

//            switch (response) {
//                case 2:
//                    Toast.makeText(getActivity(), "" + result, Toast.LENGTH_SHORT).show();
//                    break;
//
//                case 1:
//                    Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
//                    break;
//
//                default:
//                    if (result.equals("fail")) {
//                        Toast.makeText(getActivity(), getString(R.string.TAG_FET_ERROR), Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        Toast.makeText(getActivity(), getString(R.string.TAG_REG_SUC_MSG), Toast.LENGTH_SHORT).show();
//                        ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());
//
//                    }
//                    break;
//            }
        }

    }

    private void displayDialog(final Activity context, final String title, final String msg, final String strPositiveText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                getFragmentManager().popBackStack();
            }
        });
        dialog.show();
    }

    // ///////////////////////////////////////////////// GCM Implementation
    // ///////////////////////////

    private void gcmRegistrationProcess() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getActivity());
            regid = getRegistrationId(getActivity());

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                storeRegistrationId(getActivity(), regid);
            }
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
                regid = gcm.register(SENDER_ID);
                storeRegistrationId(getActivity(), regid);
            } catch (IOException e) {
                // TODO Auto-generated catch block
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
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(PROPERTY_REG_ID, regId);
//        editor.putInt(PROPERTY_APP_VERSION, appVersion);
//        editor.commit();
//        Preference.getInstance().savePreferenceData(C.DEVICE_TOKEN, regId);
    }


    private static final long SCAN_PERIOD = 1000;

    private void startBackgroundThreadForBLE() {
        AlarmManager alarmManagerForBLE = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), BleReceiver.class);
        PendingIntent broadcastIntentBle = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManagerForBLE.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 2 * 60 * SCAN_PERIOD, broadcastIntentBle);
    }

}