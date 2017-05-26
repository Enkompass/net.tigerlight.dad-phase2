package com.dad.registration.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dad.R;
import com.dad.home.BaseFragment;
import com.dad.recievers.BLEHelper;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.adapter.RecieveElementAdapter;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsCallDeleteContact;
import com.dad.settings.webservices.WsCallGetAlertCount;
import com.dad.settings.webservices.WsCallGetAllContacts;
import com.dad.settings.webservices.WsCallSendDanger;
import com.dad.settings.webservices.WsResetCount;
import com.dad.swipemenulistview.SwipeMenu;
import com.dad.swipemenulistview.SwipeMenuCreator;
import com.dad.swipemenulistview.SwipeMenuItem;
import com.dad.swipemenulistview.SwipeMenuListView;
import com.dad.util.BitMapHelper;
import com.dad.util.CheckForeground;
import com.dad.util.NetworkAvailability;
import com.dad.util.Preference;
import com.dad.util.Util;
import com.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import static android.app.Activity.RESULT_CANCELED;
import static com.dad.registration.util.Utills.isInternetConnected;

public class ContactFragment extends BaseFragment implements AdapterView.OnItemClickListener, RecieveElementAdapter.OnDeleteItemClickListner {

    private TextView tvRestoreFromTheServer;
    private TextView tvEmptyView;
    private ImageView ivAddMore;

    private SwipeMenuListView listView;
    private JSONArray jsonArray;
    private boolean isEditing;
    private JSONObject jsonobjectToChange;
    private RecieveElementAdapter recieveElementAdapter;
    private ProgressDialog progressDialog;
    private String timezoneID;
    private static boolean isBTRequestDenied = false;
    private static boolean isGPS_ReqDenied = false;
    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskResetCount asyncTaskResetCount;

    private boolean isDataAvailable = false;
    private int listSize = 0;
    private boolean isJustDataDeleted = false;

    private final String SUCCESS = "success";
    private BLEHelper bleHelper;
    private boolean isBleSupported;
    private Handler handler;
    private static boolean isBleDialogShown;
    public static boolean isServiceCall = false;
    private LinearLayout llMain;
    private LinearLayout llEmptyView;


    private DashBoardWithSwipableFragment dashBoardWithSwipableFragment;

    public ContactFragment() {

    }

    public ContactFragment(DashBoardWithSwipableFragment dashBoardWithSwipableFragment) {

        this.dashBoardWithSwipableFragment = dashBoardWithSwipableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		mLocationClient.connect();

        callResetCount();
        new AlertListLoaderThread().start();
        mHandler = new Handler();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }
//        dashBoardWithSwipableFragment.updateCount();
    }


    @Override
    public void onResume() {
        super.onResume();

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();

        CheckForeground.onResume(getActivity());

        isAllredyShown = false;

        if (!Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;


        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isBleSupported = false;
            if (isBleDialogShown) {
                return;
            }
            buildAlertDialogBLENotSupported();
            return;
        }
        isBleSupported = true;
        bleHelper = new BLEHelper(ContactFragment.this, false);

        if (isBleSupported && mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled() && !isBTRequestDenied) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (isBleSupported && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !isGPS_ReqDenied) {
//            Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Location is enabled ", getActivity());
            buildAlertMessageNoGps();
            return;
        } else {
//            Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Location is disabled", getActivity());
        }

        if (isBleSupported) {
            // scanLeDevice(true);
        }
    }

    private void buildAlertDialogBLENotSupported() {
        isBleDialogShown = true;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Constant.NO_BEACON_FUNCTION).setCancelable(false).setPositiveButton(getString(R.string.TAG_OK), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.TAG_GPS_ENABLE_MSG)).setCancelable(false).setPositiveButton(getString(R.string.TAG_YES), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton(getString(R.string.TAG_NO), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
                isGPS_ReqDenied = true;
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);

    }

    @Override
    public void initView(View view) {
        listView = (SwipeMenuListView) view.findViewById(R.id.listReciever);
        //tvRestoreFromTheServer = (TextView) view.findViewById(R.id.fragment_contact_tv_restore_from_the_server);
        ivAddMore = (ImageView) view.findViewById(R.id.fragment_contact_iv_add_more);
        llEmptyView = (LinearLayout) view.findViewById(R.id.fragment_contact_llEmptyView);
        tvEmptyView = (TextView) view.findViewById(R.id.fragment_contact_tvEmptyView);
        llMain = (LinearLayout) view.findViewById(R.id.fragment_contact_llMain);
        // int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
        // ((TextView) view.findViewById(R.id.alertCount)).setText("" + alertCount);

        //		mLocationClient = new LocationClient(this, this, this);

        // startBackgroundThreadForBLE();
        if (!Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        setSwipeMenu();
//        loadAlertCountUsingThread(true);
        loadRecieversListUsingThread(true);
        listView.setEmptyView(tvEmptyView);
        listView.setOnItemClickListener(this);
        //tvRestoreFromTheServer.setOnClickListener(this);
        ivAddMore.setOnClickListener(this);

    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    private void loadRecieversListUsingThread(boolean b) {
        handler = new Handler();
        if (b) {
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                final WsCallGetAllContacts wsCallGetAllContacts = new WsCallGetAllContacts(getActivity());
                JSONObject recieverList = wsCallGetAllContacts.executeService();
                if (recieverList != null) {
                    if (wsCallGetAllContacts.isSuccess()) {
                        isDataAvailable = true;
                        jsonArray = recieverList.optJSONArray("data");
                        listSize = jsonArray.length();

                    } else {
                        isDataAvailable = false;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if ((isDataAvailable || isJustDataDeleted) && jsonArray != null) {
                            isJustDataDeleted = false;

                            recieveElementAdapter = new RecieveElementAdapter(getActivity(), ContactFragment.this, jsonArray, isDataAvailable);
                            listView.setAdapter(recieveElementAdapter);
                            recieveElementAdapter.notifyDataSetChanged();

                            if (jsonArray.length() == 0) {
                                tvEmptyView.setVisibility(View.VISIBLE);
                                tvEmptyView.setText(getString(R.string.TAG_DATA_NA_MSG));
                                llMain.setVisibility(View.GONE);
                            } else {
                                llMain.setVisibility(View.VISIBLE);
                                tvEmptyView.setVisibility(View.GONE);
                            }

                            listView.setOnItemClickListener(ContactFragment.this);
                        }
                        progressDialog.dismiss();
                    }
                });

            }
        }).start();
    }

//    private void loadAlertCountUsingThread(boolean b) {
//        handler = new Handler();
////        if (b) {
////            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
////        }
//
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                final WsCallGetAlertCount wsCallGetAllContacts = new WsCallGetAlertCount(getActivity());
//                JSONObject recieverList = wsCallGetAllContacts.executeService(Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, ""), "");
//                if (recieverList != null) {
//                    if (wsCallGetAllContacts.isSuccess()) {
//                        isDataAvailable = true;
//                        jsonArray = recieverList.optJSONArray("data");
//                        int alertCount = recieverList.optJSONArray("data").length();
//
//                        if (alertCount < 0) {
//                            alertCount = 0;
//                        }
//                        Preference.getInstance().savePreferenceData("total_count", alertCount);
//
//                        listSize = jsonArray.length();
//
//                    } else {
//                        isDataAvailable = false;
//                    }
//                }
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if ((isDataAvailable || isJustDataDeleted) && jsonArray != null) {
//                            isJustDataDeleted = false;
//                            dashBoardWithSwipableFragment.updateCount();
//
////                            recieveElementAdapter = new RecieveElementAdapter(getActivity(), ContactFragment.this, jsonArray, isDataAvailable);
////                            listView.setAdapter(recieveElementAdapter);
////                            recieveElementAdapter.notifyDataSetChanged();
////
////                            if (jsonArray.length() == 0) {
////                                tvEmptyView.setVisibility(View.VISIBLE);
////                                tvEmptyView.setText(getString(R.string.TAG_DATA_NA_MSG));
////                                llMain.setVisibility(View.GONE);
////                            } else {
////                                llMain.setVisibility(View.VISIBLE);
////                                tvEmptyView.setVisibility(View.GONE);
////                            }
////
////                            listView.setOnItemClickListener(ContactFragment.this);
//                        }
////                        progressDialog.dismiss();
//                    }
//                });
//
//            }
//        }).start();
//    }

    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }

    /**
     * Handling Navigation.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
//            case R.id.fragment_contact_tv_restore_from_the_server:
//                Toast.makeText(getActivity(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
//                break;

            case R.id.fragment_contact_iv_add_more:
                loadFragment(new AddMoreFragment(ContactFragment.this), AddMoreFragment.class.getSimpleName());
                break;

            default:
                break;
        }
    }

    /**
     * To add fragment in container
     *
     * @param newFragment
     * @param tagStr
     */
    private void loadFragment(final Fragment newFragment, final String tagStr) {
        Util.getInstance().hideSoftKeyboard(getActivity());
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .addToBackStack(newFragment.getClass().getSimpleName())
                .hide(ContactFragment.this)
                .commit();
    }

    private void callResetCount() {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskResetCount != null && asyncTaskResetCount.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskResetCount.execute();
            } else if (asyncTaskResetCount == null || asyncTaskResetCount.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskResetCount = new AsyncTaskResetCount();
                asyncTaskResetCount.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (jsonArray != null) {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
                if (isEditing()) {
                    return;
                }
                final AddMoreFragment addMoreFragment = new AddMoreFragment(ContactFragment.this);
                final Bundle bundle = new Bundle();
                final String jsonObject = jsonobjectToChange.toString();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                addMoreFragment.setArguments(bundle);
                loadFragment(addMoreFragment, AddMoreFragment.class.getSimpleName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            isBTRequestDenied = true;
        }
    }

    @Override
    public void onDeleteItemClick(int position) {
        //if (isEditing()) {
        try {
            jsonobjectToChange = (JSONObject) jsonArray.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deleteByThread();
        //}
    }

    Handler handlerDelete = new Handler();

    private void deleteByThread() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        new Thread(new Runnable() {

            @Override
            public void run() {
                String contact_user_id = jsonobjectToChange.optString("userid");
                WsCallDeleteContact wsCallDeleteContact = new WsCallDeleteContact(getActivity());
                wsCallDeleteContact.executeService(contact_user_id);
                if (wsCallDeleteContact.isSuccess()) {
                    String email = jsonobjectToChange.optString(new WsConstants().PARAMS_EMAIL);
                    BitMapHelper.deleteImageFromStorage(getActivity(), "" + email, Preference.getInstance().mSharedPreferences.getString(email, ""));
                    isJustDataDeleted = true;
                }

                handlerDelete.post(new Runnable() {

                    @Override
                    public void run() {
                        //progressDialog.dismiss();
                        if (isJustDataDeleted) {
                            if (!isInternetConnected(getActivity())) {
                                Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            loadRecieversListUsingThread(false);
                        }
                    }
                });

            }
        }).start();

    }

    @Override
    public void onStop() {
        super.onStop();
        CheckForeground.onPause();
    }

    private class ListCountThread extends Thread {

        private static final String SUCCESS = "success";
        private int issuccess;
        private WsCallGetAlertCount wsCallGetAlertCount;

        @Override
        public void run() {
            wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
            String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
            JSONObject recieverList = wsCallGetAlertCount.executeService(email, "0");
            if (recieverList != null) {
                if (wsCallGetAlertCount.isSuccess()) {
                    issuccess = 1;
                } else {
                    issuccess = 0;
                }
            } else {
                issuccess = 0;
            }
            getActivity().runOnUiThread(new ListCountHandler(issuccess, recieverList));
        }
    }

    private class ListCountHandler implements Runnable {

        private JSONObject result;
        private int issuccess;

        public ListCountHandler(int issuccess, JSONObject result) {
            this.issuccess = issuccess;
            this.result = result;
        }

        @Override
        public void run() {
            if (issuccess == 1) {
                int alertCount = result.optJSONArray("data").length();
                if (alertCount < 0) {
                    alertCount = 0;
                }
//                Preference.getInstance().savePreferenceData(Constant.ALERT_COUNT, alertCount);
            } else if (issuccess == 0) {
                //Preference.getInstance().savePreferenceData(Constant.ALERT_COUNT, 0);
                //((TextView) getView().findViewById(R.id.alertCount)).setText("" + 0);
            }
        }
    }

    // ///////////////////// BLE Scanning//////////////////////

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    public static String TEST_UUID_PREVIOUS = "E2C56DB5DFFB48D2B060D0F5A71096E0";

    public static String TEST_UUID = "FD8C0AA6D40411E5AB30625662870761";

//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//                    getActivity().invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);
//            mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
//        } else {
//            mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//        }
//    }

    private boolean isAllredyShown;

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
//            new ServerResponseHelper().requestToPush(getLatitude() + "", getLongitude() + "", userId, timezoneID);

            callSenDangerServiceRecievingListScreen();


        }


    }

    //
//    private class PushForCrowdAlert extends Thread {
//        @Override
//        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
////            new ServerResponseHelper().requestToPushForCrowdAlert(getLatitude() + "", getLongitude() + "", userId, timezoneID);
//
//            callSenDangerServiceRecievingListScreen();
//        }
//    }
//
    private void callSenDangerServiceRecievingListScreen() {

        if (NetworkAvailability.isOnline(getActivity(), true, true, true)) {
            if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSendPush.execute();
            } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSendPush = new AsyncTaskSendPush();
                asyncTaskSendPush.execute();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.TAG_INTERNET_AVAILABILITY), Toast.LENGTH_SHORT).show();
        }

    }


    public void sendPushNotification() {
        if (isAllredyShown) {
            return;
        }
        isAllredyShown = true;
        Toast.makeText(getActivity(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).updateLatLong();
        new PushForReciever().start();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
        }
    }

    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {

        private WsCallSendDanger wsCallSendDanger;
//        private ProgressDialog progressDialog;

        //        private String strOldPassword = etOldPassword.getText().toString().trim();
//        private String strNewPassword = etNewPassword.getText().toString().trim();
        //double log = ((MainActivity) getActivity()).getLongitude();
        // double lat = ((MainActivity) getActivity()).getLatitude();
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");


        //        AsyncTaskSendPush(double latitude, double longitude String timezoneID)
//        {
//
//        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.show();
////            progressDialog.setContentView(R.layout.progress_layout);
//            progressDialog.setCancelable(false);


        }

        @Override
        protected Void doInBackground(Void... params) {

            wsCallSendDanger = new WsCallSendDanger(getActivity());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess()) {
                    // From here do further logic
                }
            }
        }
    }

    /**
     * Setup swipe menu on listview and apply click event on it
     */
    private void setSwipeMenu() {
        final SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                final SwipeMenuItem swipeMenuItemDelete = new SwipeMenuItem(getActivity());
                swipeMenuItemDelete.setBackground(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.color_alert_red)));
                swipeMenuItemDelete.setWidth(Utills.dpToPx(getActivity(), 100));
                swipeMenuItemDelete.setIcon(R.drawable.img_notification_delete);
                swipeMenuItemDelete.setTitleColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
                menu.addMenuItem(swipeMenuItemDelete);
            }
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                try {
                    jsonobjectToChange = (JSONObject) jsonArray.get(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (index) {
                    case 0:
                        if (menu.getMenuItems().size() == 1) {
                            displayDeleteDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IS_SURE_MSG), getString(R.string.TAG_YES), getString(R.string.fragment_create_account_tv_cancel));
                        }
                        break;
                }
                return false;
            }
        });
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
            }

            @Override
            public void onSwipeEnd(int position) {

            }
        });
    }


    private void displayDeleteDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if (Utills.isOnline(getActivity(), true)) {
                    deleteByThread();
                    //callDeleteNotificationService(position, notificationListDataModel.getMType(), notificationListDataModel.getMMemberMessageBoardId());
                } else {
                    Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
                }
            }
        });
        dialog.setNegativeButton(strNegativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isServiceCall) {
                loadRecieversListUsingThread(true);
            }
        }
    }


    private class AlertListLoaderThread extends Thread {
        @Override
        public void run() {
            try {
                final WsCallGetAlertCount wsCallGetAlertCount;
                wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
                String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
                JSONObject jsonRecieved = wsCallGetAlertCount.executeService(email, "" + 0);
                if (jsonRecieved != null) {


                    if (jsonRecieved.getInt(SUCCESS) == 1) {
                        isDataAvailable = true;
//                        jsonArray = jsonRecieved.getJSONArray("data");
//                        listSize = jsonArray.length();
                        int alertCount = jsonRecieved.optJSONArray("data").length();

                        if (alertCount < 0) {
                            alertCount = 0;
                        }
                        Preference.getInstance().savePreferenceData("total_count", alertCount);

//                        getActivity().runOnUiThread(new AlertListDataHandler(jsonRecieved));
                    } else {
                        isDataAvailable = false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
//            getActivity().runOnUiThread(new AlertListDataHandler(null));
        }
    }

    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private WsResetCount wsResetCount;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//

        }

        @Override
        protected Void doInBackground(Void... params) {
            wsResetCount = new WsResetCount(getActivity());
            wsResetCount.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsResetCount.isSuccess()) {
                    Log.d("Count", "Updated");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();

                }
            }
        }


    }


//    private class AlertListDataHandler implements Runnable {
//
//        private JSONObject result;
//
//        public AlertListDataHandler(JSONObject result) {
//            this.result = result;
//        }
//
//        @Override
//        public void run() {
//            if ((isDataAvailable || isJustDataDeleted) && result != null) {
//                try {
//                    isJustDataDeleted = false;
//                    jsonArray = result.getJSONArray("data");
//                    int alertCount = result.optJSONArray("data").length();
//                    Preference.getInstance().savePreferenceData("total_count", alertCount);
//
//                    if (alertCount < 0) {
//                        alertCount = 0;
//                    }
//                    Preference.getInstance().savePreferenceData("alert_count", alertCount);
//                    dashBoardWithSwipableFragment.updateCount();
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//
//    }


}



