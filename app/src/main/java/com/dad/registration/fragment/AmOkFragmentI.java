package com.dad.registration.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dad.R;
import com.dad.home.BaseFragment;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsCallSendOk;
import com.dad.settings.webservices.WsCreatePin;
import com.dad.settings.webservices.WsForgotPin;
import com.dad.settings.webservices.WsResetCount;
import com.dad.util.DisplayDialog;
import com.dad.util.GPSTracker;
import com.dad.util.Preference;

import org.json.JSONException;
import org.json.JSONObject;

public class AmOkFragmentI extends BaseFragment {

    private TextView tvResetPin;
    private TextView tvSendImOkMessage;
    private TextView tvForgotPin;
    private TextView tvCancel;

    private EditText etPin;
    private EditText etOldPin;
    private EditText etNewPin;
    private EditText etReEnterPin;
    private EditText etMainNewPin;
    private EditText etMainConfirmPin;

    private LinearLayout llMain;
    private LinearLayout llFirst;
    private LinearLayout llSecond;

    private TextView tvValidatePin;
    private TextView tvSavePin;
    private TextView tvMainValidatePin;
    private TextView tvMainSavePin;

    private AsyncSendOk asyncSendOk;
    private AsyncTaskCreatePin asyncTaskCreatePin;
    private AsyncTaskResetCount asyncTaskResetCount;
    private GPSTracker gpsTracker;
    private String lattdLastKnown;
    private String longtdLastKnown;

    private boolean isPinCreated = false;
    protected static final String SUCCESS = "success";
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iamok, container, false);
    }

    @Override
    public void initView(View view) {

        callResetCount();
        llMain = (LinearLayout) view.findViewById(R.id.fragment_iamok_llMain);
        llFirst = (LinearLayout) view.findViewById(R.id.fragment_iamok_llFirst);
        llSecond = (LinearLayout) view.findViewById(R.id.fragment_iamok_llSecond);

        etPin = (EditText) view.findViewById(R.id.fragment_iamok_etPin);
        etOldPin = (EditText) view.findViewById(R.id.fragment_iamok_etOldPin);
        etNewPin = (EditText) view.findViewById(R.id.fragment_iamok_etNewPin);
        etReEnterPin = (EditText) view.findViewById(R.id.fragment_iamok_etReEnterPin);
        etMainNewPin = (EditText) view.findViewById(R.id.fragment_iamok_llMain_etNewPin);
        etMainConfirmPin = (EditText) view.findViewById(R.id.fragment_iamok_llMain_etReenterPin);

        tvSendImOkMessage = (TextView) view.findViewById(R.id.fragment_iamok_tvSendIamokMsg);
        tvResetPin = (TextView) view.findViewById(R.id.fragment_iamok_tvResetPin);
        tvForgotPin = (TextView) view.findViewById(R.id.fragment_iamok_tvForgotPin);

        tvValidatePin = (TextView) view.findViewById(R.id.fragment_iamok_tvValidatePin);
        tvSavePin = (TextView) view.findViewById(R.id.fragment_iamok_tvSavePin);
        tvMainValidatePin = (TextView) view.findViewById(R.id.fragment_iamok_llMain_tvValidatePin);
        tvMainSavePin = (TextView) view.findViewById(R.id.fragment_iamok_llMain_tvSavePin);

        tvCancel = (TextView) view.findViewById(R.id.fragment_iamok_tvCancel);

        isPinCreated = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_PIN_CREATED, false);

        if (isPinCreated) {
            llMain.setVisibility(View.GONE);
            llFirst.setVisibility(View.VISIBLE);
        } else {
            llMain.setVisibility(View.VISIBLE);
            llFirst.setVisibility(View.GONE);
        }

        tvSendImOkMessage.setOnClickListener(this);
        tvResetPin.setOnClickListener(this);
        tvForgotPin.setOnClickListener(this);
        tvValidatePin.setOnClickListener(this);
        tvSavePin.setOnClickListener(this);
        tvMainValidatePin.setOnClickListener(this);
        tvMainSavePin.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

            case R.id.fragment_iamok_llMain_tvValidatePin:
                ValidateNewAndConfirmFeild(false);
                break;

            case R.id.fragment_iamok_llMain_tvSavePin:
                ValidateNewAndConfirmFeild(true);
                break;

            case R.id.fragment_iamok_tvSendIamokMsg:
                if (!TextUtils.isEmpty(etPin.getText().toString())) {
                    // if (etPin.equals(etMainConfirmPin)) {
                    if (etPin.getText().length() == 4) {
                        updateLatLong();
                        callSendOkService(etPin.getText().toString());

                    } else {
                        DisplayDialog.getInstance().displayMessageDialog(getActivity(), getString(R.string.TAG_PING_SHORT_MSG));
                    }
                    //}
//                    else {
//                        DisplayDialog.getInstance().displayMessageDialog(getActivity(), "Please enter correct value for Pin.");
//                    }

                } else {
                    DisplayDialog.getInstance().displayMessageDialog(getActivity(), getString(R.string.TAG_PIN_NOT_EMPTY_MSG));
                }
                break;

            case R.id.fragment_iamok_tvResetPin:
                llFirst.setVisibility(View.GONE);
                llSecond.setVisibility(View.VISIBLE);
                break;

            case R.id.fragment_iamok_tvForgotPin:
//                callForgotPinService();
                forgotPin();
                break;

            case R.id.fragment_iamok_tvValidatePin:
                ValidateOldNewAndConfirmFeild(false);
                break;

            case R.id.fragment_iamok_tvSavePin:
                ValidateOldNewAndConfirmFeild(true);
                callCreatePinService(false);
                break;

            case R.id.fragment_iamok_tvCancel:
                etOldPin.setText("");
                etNewPin.setText("");
                etReEnterPin.setText("");
                tvSavePin.setEnabled(false);
                tvSavePin.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_gray));
                llSecond.setVisibility(View.GONE);
                llFirst.setVisibility(View.VISIBLE);

//            case R.id.fragment_i_m_ok_requiew_pin_tv_validate_pin:
//                Toast.makeText(getActivity(), "validate", Toast.LENGTH_SHORT).show();
//                break;
//
//            case R.id.fragment_i_m_ok_requiew_pin_tv_save_pin:
//                ValidateNewAndConfirmFeild();
//                //                Toast.makeText(getActivity(), "save", Toast.LENGTH_SHORT).show();
//                break;

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

    private void callCreatePinService(final boolean isMainScreenOrNot) {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskCreatePin != null && asyncTaskCreatePin.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCreatePin.execute();
            } else if (asyncTaskCreatePin == null || asyncTaskCreatePin.getStatus() == AsyncTask.Status.FINISHED) {
                if (isMainScreenOrNot) {
                    asyncTaskCreatePin = new AsyncTaskCreatePin(etMainConfirmPin.getText().toString());
                } else {
                    asyncTaskCreatePin = new AsyncTaskCreatePin(etReEnterPin.getText().toString());
                }
                asyncTaskCreatePin.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }


    private class AsyncTaskCreatePin extends AsyncTask<String, Void, String> {
        private WsCreatePin wsCreatePin;
        private String pin;
        private ProgressDialog progressDialog;


        public AsyncTaskCreatePin(String pin) {
            this.pin = pin;
            wsCreatePin = new WsCreatePin(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));

            progressDialog.show();
            progressDialog.setCancelable(false);
        }


        @Override
        protected String doInBackground(String... strings) {
            wsCreatePin.executeService(pin);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsCreatePin.isSuccess()) {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_NEW_PIN_CREATED_MSG), getString(R.string.ok), "", false, false);
                    isPinCreated = true;
                    Preference.getInstance().savePreferenceData(Constant.IS_PIN_CREATED, isPinCreated);
                    llMain.setVisibility(View.GONE);
                    llSecond.setVisibility(View.GONE);
                    llFirst.setVisibility(View.VISIBLE);

                } else {
                    if (!wsCreatePin.getMessage().trim().equals("")) {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCreatePin.getMessage(), getString(R.string.ok), "", false, false);
                    } else {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SOME_WENT_WRONG_MSG), getString(R.string.ok), "", false, false);
                    }
                }
            }
        }
    }

    public void updateLatLong() {
        gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.canGetLocation()) {
            //lattdLastKnown = "" + gpsTracker.getLatitude();
            //longtdLastKnown = "" + gpsTracker.getLongitude();

            lattdLastKnown = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
            longtdLastKnown = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");

        }
    }

    private void callSendOkService(final String pin) {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncSendOk != null && asyncSendOk.getStatus() == AsyncTask.Status.PENDING) {
                asyncSendOk.execute(pin);

            } else if (asyncSendOk == null || asyncSendOk.getStatus() == AsyncTask.Status.FINISHED) {
                asyncSendOk = new AsyncSendOk();
                asyncSendOk.execute(pin);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private class AsyncSendOk extends AsyncTask<String, Void, JSONObject> {

        private WsCallSendOk wsCallSendOk;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallSendOk = new WsCallSendOk(getActivity());
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            if (params.length > 0) {
                return wsCallSendOk.executeService(params[0], Double.parseDouble(lattdLastKnown), Double.parseDouble(longtdLastKnown));
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.cancel();
            if (!isCancelled()) {
                if (jsonObject != null) {
                    if (wsCallSendOk.isSuccess()) {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IM_OK_ALERT_SENT), getString(R.string.ok), "", false, false);
                        etPin.setText("");
                    } else {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_RECORDED_PIN_NOT_MATCH), getString(R.string.ok), "", false, false);

                    }
                }
            }
        }
    }

    private void ValidateNewAndConfirmFeild(boolean b) {
        if (etMainNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (etMainNewPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (etMainNewPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (!etMainNewPin.getText().toString().trim().equalsIgnoreCase("") && !etMainConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etMainNewPin.getText().toString().trim(), etMainConfirmPin.getText().toString().trim())) {

//
                if (b) {

                    if (Utills.isOnline(getActivity(), true)) {
                        callCreatePinService(true);
                    } else {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                    }
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CORRECT_PIN), getString(R.string.ok), "", false, false);
                    tvMainSavePin.setEnabled(true);
                    tvMainSavePin.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_blue));
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INCORRECT_PIN), getString(R.string.ok), "", false, false);
                etMainNewPin.requestFocus();
            }
        }
    }

    private void ValidateOldNewAndConfirmFeild(final boolean b) {
        if (etOldPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_OLD_TEMP_PIN), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();

        } else if (etNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();

        } else if (etReEnterPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();

        } else if (etOldPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();

        } else if (etNewPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(android.R.string.ok), "", false, false);
            etNewPin.requestFocus();

        } else if (etReEnterPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();

        } else if (etOldPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();

        } else if (etNewPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();

        } else if (etReEnterPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();

        } else if (!etNewPin.getText().toString().trim().equalsIgnoreCase("") && !etReEnterPin.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etNewPin.getText().toString().trim(), etReEnterPin.getText().toString().trim())) {
                if (b) {
                    if (Utills.isOnline(getActivity(), true)) {
                        callCreatePinService(false);
                    } else {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                    }
                } else {
                    tvSavePin.setEnabled(true);
                    tvSavePin.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_blue));
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INCORRECT_PIN), getString(R.string.ok), "", false, false);
                etNewPin.requestFocus();
            }
        }
    }


    public boolean checkPassWordAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
//                Utills.displayDialog(getActivity(), getString(R.string.app_name), "New PIN And Confirm PIN Have Been Matched,Click On Save PIN To Create New PIN", getString(R.string.ok), "", false, false);
            }
        }
        return pstatus;
    }

    private void forgotPin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle((getString(R.string.TAG_FORGOT_PIN)));
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.TAG_AUTO_GEN_PIN));
        dialog.setPositiveButton(getString(R.string.TAG_YES), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                new ForGotPinTask().execute();

            }

        });

        dialog.setNegativeButton(getString(R.string.TAG_NO), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class ForGotPinTask extends AsyncTask<String, String, String> {

        int response = 3;
        ProgressDialog dialog;
        private WsForgotPin wsForgotPin;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsForgotPin = new WsForgotPin(getActivity());
            dialog = new ProgressDialog(getActivity());
            dialog.show();
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.TAG_WAIT));

        }

        @Override
        protected String doInBackground(String... params) {
            if (Utills.isInternetConnected(getActivity())) {

                JSONObject loginjson = wsForgotPin.executeService();
                try {
                    if (loginjson == null) {
                        return "fail";
                    }
                    String msg = wsForgotPin.getMessage();
                    if (wsForgotPin.isSuccess()) {
                        response = 1;
                        return SUCCESS;
                    } else if (loginjson.getInt(SUCCESS) == 2) {
                        response = 2;
                        return msg;
                    } else if (loginjson.getInt(SUCCESS) == 0) {
                        response = 0;
                        return msg;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                response = 3;
            }
            return "fail";

        }

        @Override
        protected void onPostExecute(String result) {
            dialog.cancel();
            switch (response) {
                case 0:
                    Toast.makeText(getActivity(), getString(R.string.TAG_PIN_CAN_NOT_GET), Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    Toast.makeText(getActivity(), getString(R.string.TAG_EMAIL_HAS_SENT_MSG), Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }

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
//                    progressDialog.dismiss();
                    Log.d("Count", "Updated");


                } else {

                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();

                }
            }
        }


    }


    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }
}
