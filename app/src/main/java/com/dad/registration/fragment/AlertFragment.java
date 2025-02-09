package com.dad.registration.fragment;

import com.dad.LocationBroadcastServiceNew;
import com.dad.R;
import com.dad.home.BaseFragment;
import com.dad.registration.adapter.AlertAdapter;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsCallDADTest;
import com.dad.settings.webservices.WsCallDeleteAlert;
import com.dad.settings.webservices.WsCallGetAlertCount;
import com.dad.settings.webservices.WsCallSendDanger;
import com.dad.settings.webservices.WsCrowdAlert;
import com.dad.settings.webservices.WsResetCount;
import com.dad.swipemenulistview.SwipeMenu;
import com.dad.swipemenulistview.SwipeMenuCreator;
import com.dad.swipemenulistview.SwipeMenuItem;
import com.dad.swipemenulistview.SwipeMenuListView;
import com.dad.util.Constants;
import com.dad.util.Preference;
import com.dad.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AlertFragment : all alert listing
 */
public class AlertFragment extends BaseFragment implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = AlertFragment.class.getSimpleName();

    private TextView tvSendDanger;
    private TextView tvEmptyAlert;
    private Switch swCrowdALert;
    private Switch swTestMode;
    private SwipeMenuListView lvAlerts;
    private AlertAdapter alertAdapter;
    private ProgressDialog progressDialog;
    private static final String SUCCESS = "success";
    private boolean isDataAvailable = false;
    private boolean isJustDataDeleted = false;
    private String timezoneID;
    public static JSONObject jsonobjectToChange;
    private int listSize = 0;
    public static boolean isEditing;
    private JSONArray jsonArray;
    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskTestMode asyncTaskTestMode;
    private AsyncCrowdAlertModeOn asyncTaskCrowdAlertOn;
    private AsyncCrowdAlertModeOff asyncTaskCrowdAlertOff;
    private AsyncTaskResetCount asyncTaskResetCount;
    public static int count = 0;
    private boolean mIsSentAlertReceiverRegistered = false;

    private DashBoardWithSwipableFragment dashBoardWithSwipableFragment;

    public AlertFragment() {

    }

    public AlertFragment(DashBoardWithSwipableFragment dashBoardWithSwipableFragment) {

        this.dashBoardWithSwipableFragment = dashBoardWithSwipableFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void initView(View view) {
        callResetCount();

        tvSendDanger = (TextView) view.findViewById(R.id.fragment_alert_tvSendDanger);
        tvEmptyAlert = (TextView) view.findViewById(R.id.fragment_alert_tvEmptyView);
        swCrowdALert = (Switch) view.findViewById(R.id.fragment_alert_swCrowdAlert);

        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_CHECKED, false)) {
            swCrowdALert.setChecked(true);
        } else {
            swCrowdALert.setChecked(false);
        }

        swTestMode = (Switch) view.findViewById(R.id.fragment_alert_swTestMode);
        //updateTestModeSwitch();

//        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {
//            swTestMode.setChecked(true);
//        } else {
//            swTestMode.setChecked(false);
//        }
//
        lvAlerts = (SwipeMenuListView) view.findViewById(R.id.fragment_alert_lvAlerts);

        tvSendDanger.setOnClickListener(this);
        swCrowdALert.setOnCheckedChangeListener(this);
        //swTestMode.setOnCheckedChangeListener(mTestModeOnCheckChangeListener);
        lvAlerts.setOnItemClickListener(this);
        lvAlerts.setEmptyView(tvEmptyAlert);
        isEditing = false;
        setSwipeMenu();

        if (!Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        progressDialog.show();

        new AlertListLoaderThread().start();


    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (!mIsSentAlertReceiverRegistered)
        {
            context.registerReceiver(mAlertSentReceiver, new IntentFilter(Constants.Actions.SENT_ALERT_ACTION));
            mIsSentAlertReceiverRegistered = true;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        if (mIsSentAlertReceiverRegistered)
        {
            getActivity().unregisterReceiver(mAlertSentReceiver);
            mIsSentAlertReceiverRegistered = false;
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
            case R.id.fragment_alert_tvSendDanger:
                if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {
                    callTestModeService();

                } else {
                    callSenDangerServiceRecievingListScreen();
                }
                break;

//            case R.id.fragment_alert_swCrowdAlert:
////                Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, true);
//
//                swCrowdALert.setOnCheckedChangeListener(this);//Commented this line on 19 dec 2016 need to ask parth for confirmation
//                break;

//            case R.id.fragment_alert_swTestMode:
//
//                if (swTestMode.isChecked()) {
//                    final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
//                    dialog.setContentView(R.layout.custom_dialog_test_mode);
//
//                    final TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tvTitle);
//                    final TextView tvMessage = (TextView) dialog.findViewById(R.id.dialog_tvMessage);
//                    final TextView tvPosButton = (TextView) dialog.findViewById(R.id.dialog_tvPosButton);
//                    final TextView tvNegButton = (TextView) dialog.findViewById(R.id.dialog_tvNegButton);
//
//                    tvTitle.setText(getString(R.string.dialog_test_mode_title));
//                    tvMessage.setText(getString(R.string.dialog_test_mode_msg));
//                    tvPosButton.setText(getString(R.string.dialog_test_mode_pos_btn));
//                    tvNegButton.setText(getString(R.string.dialog_test_mode_neg_btn));
//
//                    tvPosButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dialog.dismiss();
//                            Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, true);
//                            swTestMode.setChecked(true);
//                            callTestModeService();
//                        }
//                    });
//                    tvNegButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, false);
//                            dialog.dismiss();
//
//                            swTestMode.setChecked(false);
//                        }
//                    });
//                    dialog.show();
//                }
//                break;
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (jsonArray != null) {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
                if (isEditing) {
                    return;
                }

                final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
                final Bundle bundle = new Bundle();
                final String jsonObject = jsonobjectToChange.toString();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                alertDetailFragment.setArguments(bundle);
//                ((MainActivity)getActivity()).addFragment(alertDetailFragment,AlertFragment.this);
                loadFragment(alertDetailFragment, AlertDetailFragment.class.getSimpleName());

//                Intent i = new Intent(getActivity(), AlertDetailFragment.class);
//                startActivity(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onDeleteItemClick(int position) {
//        //if (isEditing) {
//        try {
//            jsonobjectToChange = (JSONObject) jsonArray.get(position);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        deleteUsingThread(position);
//        //}
//    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        if (!Utills.isInternetConnected(getActivity())) {
            swCrowdALert.setChecked(!isChecked);
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isChecked) {
            Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, isChecked);

            callCrowdAlertModeServiceON(1);

//            new LacaleHelpStatusThread(1).start();
        } else {
            Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, isChecked);
//            Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, false);
//            new LacaleHelpStatusThread(0).start();

            callCrowdAlertModeServiceOFF(0);

        }
    }

    private CompoundButton.OnCheckedChangeListener mTestModeOnCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
                dialog.setContentView(R.layout.custom_dialog_test_mode);
                final TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tvTitle);
                final TextView tvMessage = (TextView) dialog.findViewById(R.id.dialog_tvMessage);
                final TextView tvPosButton = (TextView) dialog.findViewById(R.id.dialog_tvPosButton);
                final TextView tvNegButton = (TextView) dialog.findViewById(R.id.dialog_tvNegButton);


                tvTitle.setText(getString(R.string.dialog_test_mode_title));
                tvMessage.setText(getString(R.string.dialog_test_mode_msg));
                tvPosButton.setText(getString(R.string.dialog_test_mode_pos_btn));
                tvNegButton.setText(getString(R.string.dialog_test_mode_neg_btn));

                tvPosButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        updateTestModeValue(true);
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
//                            callTestModeService();


//                            Toast.makeText(getActivity(), "positive", Toast.LENGTH_SHORT).show();

                    }
                });

                tvNegButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, false);
                        dialog.dismiss();
//                            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_OFF), getString(R.string.ok), "", false, false);
                        Preference.getInstance().mSharedPreferences.edit().putBoolean(Constant.IS_TEST_MODE, false);
                        updateTestModeSwitch();
//                            Toast.makeText(getActivity(), "Negative", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();


            } else {

                Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, false);
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_OFF), getString(R.string.ok), "", false, false);
            }

        }
    };

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        updateTestModeSwitch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSendPush.cancel(true);
        }
        if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskTestMode.cancel(true);
        }
        if (asyncTaskCrowdAlertOn != null && asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskCrowdAlertOn.cancel(true);
        }
        if (asyncTaskCrowdAlertOff != null && asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskCrowdAlertOff.cancel(true);
        }

    }

    private class LacaleHelpStatusThread extends Thread {

        private int staus;
        private WsCrowdAlert wsCrowdAlert;

        private LacaleHelpStatusThread(int staus) {
            wsCrowdAlert = new WsCrowdAlert(getActivity());
            this.staus = staus;
        }

        @Override
        public void run() {
            wsCrowdAlert.executeService(staus);

            if (wsCrowdAlert.isSuccess()) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), "Has Been On", getString(R.string.ok), "", false, false);
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
                        jsonArray = jsonRecieved.getJSONArray("data");
                        listSize = jsonArray.length();
                        getActivity().runOnUiThread(new AlertListDataHandler(jsonRecieved));
                    } else {
                        isDataAvailable = false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(new AlertListDataHandler(null));
        }
    }

    private class AlertListDataHandler implements Runnable {

        private JSONObject result;

        public AlertListDataHandler(JSONObject result) {
            this.result = result;
        }

        @Override
        public void run() {
            if ((isDataAvailable || isJustDataDeleted) && result != null) {
                try {
                    isJustDataDeleted = false;
                    jsonArray = result.getJSONArray("data");
                    int alertCount = result.optJSONArray("data").length();
                    count = result.optJSONArray("data").length();
                    if (alertCount < 0) {
                        alertCount = 0;
                    }
                    Preference.getInstance().savePreferenceData("alert_count", alertCount);

                    alertAdapter = new AlertAdapter(getActivity(), AlertFragment.this, jsonArray);
                    lvAlerts.setAdapter(alertAdapter);
                    lvAlerts.setOnItemClickListener(AlertFragment.this);


                    if (jsonArray.length() == 0) {
                        tvEmptyAlert.setVisibility(View.VISIBLE);
                        tvEmptyAlert.setText(getString(R.string.TAG_DATA_NA_MSG));
                        lvAlerts.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            progressDialog.cancel();
        }

    }


    private void deleteUsingThread(final int position) {
        final Handler handler = new Handler();
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_DELETING));
        progressDialog.show();
        new Thread(new Runnable() {

            private int response = 5;

            @Override
            public void run() {
                String helpId = jsonobjectToChange.optString("fld_help_id");
                WsCallDeleteAlert wsCallDeleteAlert = new WsCallDeleteAlert(getActivity());
                wsCallDeleteAlert.executeService(helpId);
                if (wsCallDeleteAlert.isSuccess()) {
                    isJustDataDeleted = true;
                    response = 1;
                } else {
                    response = 2;
                }

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (response == 2) {
                            Toast.makeText(getActivity(), getString(R.string.TAG_COULD_NOT_DELETE_MSG), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            return;
                        }

                        if (response == 1) {
                            isJustDataDeleted = false;
                            Toast.makeText(getActivity(), getString(R.string.dialog_delete_alert_title), Toast.LENGTH_SHORT).show();
                            upadateJsonArray(position);
                            int alertCount = Preference.getInstance().mSharedPreferences.getInt("total_count", 0);

                            alertCount = alertCount - 1;
                            if (alertCount < 0) {
                            }
                            Preference.getInstance().savePreferenceData("total_count", alertCount);
                            dashBoardWithSwipableFragment.updateCount();


//                            TextView textCount = (TextView) getActivity().findViewById(R.id.framgent_alert_tv_badge_count);

//                            textCount.setText(String.valueOf(Preference.getInstance().mSharedPreferences.getInt("total_count", 0)));
//                            TextView textCount = getView().findViewById(R.id.framgent_alert_tv_badge_count);
//                            textCount.setText(String.valueOf(preference.mSharedPreferences.getInt("alert_count", 0)));

//                            ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
                            alertAdapter.remove(position);
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).start();
    }


    private void upadateJsonArray(int position) {
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i == position) {
                continue;
            }
            try {
                newArray.put(jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        jsonArray = null;
        jsonArray = new JSONArray();
        jsonArray = newArray;
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
                .commit();
    }

    private void callSenDangerServiceRecievingListScreen() {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
                //asyncTaskSendPush.execute();
                sendAlert(asyncTaskSendPush);
            } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSendPush = new AsyncTaskSendPush();
                sendAlert(asyncTaskSendPush);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
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

    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private WsResetCount wsResetCount;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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


    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {
        private WsCallSendDanger wsCallSendDanger;
        //double log =((MainActivity) getActivity()).getLongitude();
        //double lat = ((MainActivity) getActivity()).getLatitude();
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(getActivity());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID, accuracy);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess()) {
                    progressDialog.dismiss();


                    final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
                    dialog.setContentView(R.layout.custom_progress_layout);
                    final TextView tvTitlee = (TextView) dialog.findViewById(R.id.dialog_tvTitlee);
                    final TextView tvMessagee = (TextView) dialog.findViewById(R.id.dialog_tvMessagee);
                    final TextView tvMsgLeve = (TextView) dialog.findViewById(R.id.dialog_tvMsgLevel);
                    final TextView tvPosButtonn = (TextView) dialog.findViewById(R.id.dialog_tvPosButtonn);
                    tvTitlee.setText(getString(R.string.custom_progess_dialog_tv_title));
                    tvMessagee.setText(getString(R.string.custom_progess_dialog_tv_msg));
                    tvPosButtonn.setText(getString(R.string.custom_progess_dialog_tv_ok));
//
//                    Log.d("Al_UUID", Preference.getInstance().mSharedPreferences.getString("UUIDHex", ""));
//                    Preference preference = Preference.getInstance();
//                    if (preference != null) {
//
//                        final String major = preference.mSharedPreferences.getString("major", "");
//                        final String minor = preference.mSharedPreferences.getString("minor", "");
//
//                        if (!minor.equals("") && !major.equals("")) {
//                            double doubleminor = Double.parseDouble(minor) / 1000;
//                            double doublemajor = Double.parseDouble(major) / 1000;
//
//                            if (doubleminor >= -3.0 && doublemajor >= 2.5) {
//
////                                tvMsgLeve.setText("GOOD D.A.D BATTERY");
////                                tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
//                            } else if (doubleminor >= -2.499 && doublemajor >= 2.0) {
////
////                                tvMsgLeve.setText("LOW D.A.D BATTERY");
////                                tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
//                            } else if (doubleminor < 2.0 && doublemajor < 2.0) {
////                                tvMsgLeve.setText("REPLACE D.A.D BATTERY");
////                                tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
//                            } else {
////                                tvMsgLeve.setText("GOOD D.A.D BATTERY");
////                                tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
//                            }
//
//
//                        }
//                    }

//                    final int temp = 3;

//                    3 separate levels (Good -3.0V to 2.5V), (Low – 2.499V to 2.0V), (Replace <2.0V)
//                    final double minor = -2;
//                    final double major = 1.0;
////
//
//                    if (minor >= -3.0 && major >= 2.5) {
//
//                        tvMsgLeve.setText("GOOD D.A.D BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
//                    } else if (minor >= -2.499 && major >= 2.0) {
//
//                        tvMsgLeve.setText("LOW D.A.D BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
//                    } else if (minor < 2.0 && major < 2.0) {
//                        tvMsgLeve.setText("REPLACE D.A.D BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
//                    } else {
//                        tvMsgLeve.setText("GOOD D.A.D BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
//                    }


                    tvPosButtonn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } else {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SOME_WENT_WRONG_MSG), getString(R.string.ok), "", false, false);
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }
    }


    private void callTestModeService() {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskTestMode.execute();
            } else if (asyncTaskTestMode == null || asyncTaskTestMode.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskTestMode = new AsyncTaskTestMode();
                sendAlert(asyncTaskTestMode);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private void sendAlert(final AsyncTask<Void, Void, Void> task)
    {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_SENDING_ALERT));
        progressDialog.show();

        final Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
        serviceIntent.putExtra(Constants.Extras.SMALLEST_DISPLACEMENT_VALUE, 0f);

        getActivity().stopService(serviceIntent);
        getActivity().startService(serviceIntent);
        final CountDownTimer countDownTimer = new CountDownTimer(120000, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);
                //Log.d(TAG, "Location Accuracy = " + accuracy);

                if (accuracy >= Constants.MINIMUM_ACCEPTABLE_ACCURACY)
                {
                    cancel();
                    onFinish();
                }
            }

            @Override
            public void onFinish()
            {
                if (task.getStatus() == AsyncTask.Status.FINISHED)
                {
                    Log.e(TAG, "Trying to execute duplicate task.");
                }
                else
                {
                    task.execute();
                }
                //Stop the LocationService after allowing a few seconds to ensure that the service has time connect. This is necessary since the accuracy could already meet the criteria from a request. It will automatically be restarted by the repeating AlarmManager every 1 minute.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getActivity().stopService(serviceIntent);
                    }
                }, 3000);
            }
        };

        countDownTimer.start();
    }

    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
        private WsCallDADTest wsCallDADTest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallDADTest = new WsCallDADTest(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    progressDialog.dismiss();
                    // From here do further logic
                    //Toast.makeText(getActivity(), "Successfully ON Test Mode ", Toast.LENGTH_SHORT).show();
                    /*Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_OFF), getString(R.string.ok), "", false, false);*/
                    updateTestModeValue(false);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void callCrowdAlertModeServiceON(int status) {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskCrowdAlertOn != null && asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCrowdAlertOn.execute(new Integer(status));
            } else if (asyncTaskCrowdAlertOn == null || asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskCrowdAlertOn = new AsyncCrowdAlertModeOn();
                asyncTaskCrowdAlertOn.execute(new Integer(status));
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private class AsyncCrowdAlertModeOn extends AsyncTask<Integer, Void, Void> {

        private int staus;
        private WsCrowdAlert wsCrowdAlert;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCrowdAlert = new WsCrowdAlert(getActivity());


        }

        @Override
        protected Void doInBackground(Integer... integers) {

            staus = integers[0].intValue();
            wsCrowdAlert.executeService(staus);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (wsCrowdAlert.isSuccess()) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CROWD_ALERT_ON), getString(R.string.ok), "", false, false);
            }

        }
    }


    private void callCrowdAlertModeServiceOFF(int status) {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskCrowdAlertOff != null && asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCrowdAlertOff.execute(new Integer(status));
            } else if (asyncTaskCrowdAlertOff == null || asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskCrowdAlertOff = new AsyncCrowdAlertModeOff();
                asyncTaskCrowdAlertOff.execute(new Integer(status));
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private class AsyncCrowdAlertModeOff extends AsyncTask<Integer, Void, Void> {

        private int staus;
        private WsCrowdAlert wsCrowdAlert;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCrowdAlert = new WsCrowdAlert(getActivity());


        }

        @Override
        protected Void doInBackground(Integer... integers) {

            staus = integers[0].intValue();
            wsCrowdAlert.executeService(staus);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (wsCrowdAlert.isSuccess()) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CROWD_ALERT_OFF), getString(R.string.ok), "", false, false);
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
        lvAlerts.setMenuCreator(creator);
        lvAlerts.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
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
                            displayDeleteDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IS_SURE_ALERT), getString(R.string.TAG_OK), getString(R.string.fragment_create_account_tv_cancel), position);
                        }
                        break;
                }
                return false;
            }
        });
        lvAlerts.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {

                Log.d("Swipe", "Start");
            }

            @Override
            public void onSwipeEnd(int position) {

            }
        });
    }

    private void displayDeleteDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText, final int position) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if (Utills.isOnline(getActivity(), true)) {
                    deleteUsingThread(position);
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


    private BroadcastReceiver mAlertSentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateTestModeValue(false);
            updateTestModeSwitch();
        }
    };

    private void updateTestModeValue(boolean isChecked)
    {
        Preference.getInstance().mSharedPreferences.edit().putBoolean(Constant.IS_TEST_MODE, isChecked).commit();
        updateTestModeSwitch();
    }

    private void updateTestModeSwitch()
    {
        boolean isInTestMode =  Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false);
        swTestMode.setOnCheckedChangeListener(null);
        swTestMode.setChecked(isInTestMode);
        swTestMode.setOnCheckedChangeListener(mTestModeOnCheckChangeListener);
    }
}
