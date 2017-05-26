//package com.dad;
//
//import android.app.Service;
//import android.content.Intent;
//import android.content.IntentSender;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.dad.home.BaseActivity;
//import com.dad.registration.util.Constant;
//import com.dad.registration.util.Utills;
//import com.dad.settings.webservices.WsCallUpdateLocation;
//import com.dad.util.GPSTracker;
//import com.dad.util.Preference;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.PendingResult;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsResult;
//import com.google.android.gms.location.LocationSettingsStates;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
//import com.google.android.gms.location.places.Places;
//
//import static android.app.Activity.RESULT_OK;
//import static com.dad.home.BaseActivity.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
//import static com.dad.home.BaseActivity.UPDATE_INTERVAL_IN_MILLISECONDS;
//import static com.dad.util.CheckForeground.getActivity;
//
//public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//
//    //public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60  * 1000;
//    //public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
//    private static final int LOCATION_CHANGE_DISTANCE_METER = 1000;
//
//    protected static final String TAG = "location-updates-sample";
//    protected final static String LOCATION_KEY = "location-key";
//
//    protected GoogleApiClient mGoogleApiClient;
//    protected LocationRequest mLocationRequest;
//    protected Location mCurrentLocation;
//
//    private double latitude;
//    private double longitude;
//    private double commonLatitude;
//    private double commonLongitude;
//
//    private GPSTracker gpsTracker;
//    private String longtdLastKnown;
//    private String lattdLastKnown;
//   // private LocationSettingsRequest.Builder builder;
//
//
//    /**
//     * Get the latitude of user's last known location
//     *
//     * @return latitude of user's last known location
//     */
//    public double getLatitude() {
//        return latitude;
//    }
//
//    /**
//     * Get the longitude of user's last known location
//     *
//     * @return longitude of user's last known location
//     */
//    public double getLongitude() {
//        return longitude;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        //updateValuesFromBundle(savedInstanceState);
//        updateLatLong();
//        buildGoogleApiClient();
//        //createLocationRequest();
//        //checkGPS();
//    }
//
//    public void updateLatLong() {
//        gpsTracker = new GPSTracker(this);
//        if (gpsTracker.canGetLocation()) {
//            lattdLastKnown = "" + gpsTracker.getLatitude();
//            longtdLastKnown = "" + gpsTracker.getLongitude();
//            commonLatitude=gpsTracker.getLatitude();
//            commonLongitude=gpsTracker.getLongitude();
//            Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(commonLongitude));
//            Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(commonLatitude));
//            callLocationUpdateService(commonLongitude,commonLatitude);
//
//        }
//    }
//
//
//    /**
//     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
//     * LocationServices API.
//     */
//    protected synchronized void buildGoogleApiClient() {
//        Log.i(TAG, "Building GoogleApiClient");
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//
//        createLocationRequest();
//    }
//
////    /**
////     * checks the location is on or not in phone
////     * <p>
////     * This method will check the location is on or not,
////     * and display alert dialog for user's input application reacts
////     * </p>
////     */
////    public void checkGPS() {
////        if (mGoogleApiClient != null) {
////            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
////            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
////                @Override
////                public void onResult(LocationSettingsResult result) {
////                    final Status status = result.getStatus();
////                    final LocationSettingsStates state = result.getLocationSettingsStates();
////                    switch (status.getStatusCode()) {
////                        case LocationSettingsStatusCodes.SUCCESS:
////                            //                            startLocationService();
////                            createLocationRequest();
////                            //                            startNextActivity();
////                            break;
////                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
////                            break;
////                    }
////                }
////            });
////        }
////    }
//
//    /**
//     * Creates {@link LocationRequest} object from the {@link LocationSettingsRequest}, also can customise request object by setting its few parameters
//     */
//    protected void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(0);
//        mLocationRequest.setFastestInterval(0);
//        mLocationRequest.setSmallestDisplacement(LOCATION_CHANGE_DISTANCE_METER);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        //builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
//        //**************************
//        //builder.setAlwaysShow(true); //this is the key ingredient
//        //**************************
//    }
//
//    /**
//     * Submits the request for location updates from google api services
//     * <p>this method must be called in onResume method.
//     * It is very heave operation so use with caution
//     */
//    protected void startLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//    }
//
//    /**
//     * stops location updates from google api services
//     * <p>this method must be called in onPause method.
//     * It is very heave operation so must called this method in onPause method.
//     */
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//    }
//
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        Log.e(TAG, "Connected to GoogleApiClient");
//        if (mCurrentLocation == null) {
//            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            if (mCurrentLocation != null) {
//                longitude = mCurrentLocation.getLongitude();
//                latitude = mCurrentLocation.getLatitude();
//                if (longitude != 0.0 && latitude != 0.0) {
//                }
//            }
//        }
//        startLocationUpdates();
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.e("@@@","Location chnaged called");
//        mCurrentLocation = location;
//        longitude = mCurrentLocation.getLongitude();
//        latitude = mCurrentLocation.getLatitude();
//        commonLatitude=latitude;
//        commonLongitude=longitude;
//        Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(commonLongitude));
//        Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(commonLatitude));
//        if (commonLongitude != 0.0 && commonLatitude != 0.0) {
//            callLocationUpdateService(commonLongitude,commonLatitude);
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int cause) {
//        Log.i(TAG, "Connection suspended");
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
//    }
//
//    public GoogleApiClient getmGoogleApiClient() {
//        return mGoogleApiClient;
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // If we get killed, after returning from here, restart
//        return START_STICKY;
//    }
//
//
//
//
//}
