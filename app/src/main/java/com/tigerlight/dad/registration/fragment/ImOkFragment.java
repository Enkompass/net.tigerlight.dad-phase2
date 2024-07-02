package com.tigerlight.dad.registration.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.tigerlight.dad.R;
import com.tigerlight.dad.home.BaseFragment;
import com.tigerlight.dad.registration.util.Utills;
import com.tigerlight.dad.settings.webservices.WsCreatePin;


public class ImOkFragment extends BaseFragment {

    private View view;
    private boolean isPinCreated = false;
    private ViewFlipper viewFlipper;

    //Here id first view

    private EditText etPin;
    private TextView tvSendImOkMessage;

    //Here Second view
    private EditText etNewPin;
    private EditText etConfirmPin;
    private TextView tvResetPin;
    private TextView tvForgotPin;
    private TextView tvValidatePin;
    private TextView tvSavePin;
    private AsyncTaskCreatePinn asyncTaskCreatePinn;
    private AsyncTaskForgotPin asyncTaskForgotPin;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_i_am_ok_vf, container, false);
    }


    @Override
    public void initView(View view) {
        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);


        if (isPinCreated) {
            viewFlipper.setDisplayedChild(0);
            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.first)));
        } else {
            viewFlipper.setDisplayedChild(1);
            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.second)));
        }

        //first view binding
        etPin = (EditText) view.findViewById(R.id.fragment_i_m_ok_send_pin_et_pinn);
        etNewPin = (EditText) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_et_new_pin);
        etConfirmPin = (EditText) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_et_confirm_pin);
        tvSendImOkMessage = (TextView) view.findViewById(R.id.fragment_i_m_ok_send_pin_tv_sendd);
        tvResetPin = (TextView) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_reset_pin);
        tvForgotPin = (TextView) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_forgot_pin);
        tvValidatePin = (TextView) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_validate_pin);
        tvSavePin = (TextView) view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_save_pin);
        tvSendImOkMessage.setOnClickListener(this);
        tvResetPin.setOnClickListener(this);
        tvForgotPin.setOnClickListener(this);
        tvValidatePin.setOnClickListener(this);
        tvSavePin.setOnClickListener(this);


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

        final int fragmentId = v.getId();

        if (fragmentId == R.id.fragment_i_m_ok_send_pin_tv_sendd) {
            Toast.makeText(getActivity(), getString(R.string.TAG_SEND), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_reset_pin) {
            Toast.makeText(getActivity(), getString(R.string.TAG_RESET), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_forgot_pin) {
            callForgotPinService();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_validate_pin) {
            Toast.makeText(getActivity(), getString(R.string.TAG_VALIDATE), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_save_pin) {
            ValidateNewAndConfirmFeild();
        }
    }

    private void ValidateNewAndConfirmFeild() {

        if (etNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();

        } else if (etConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (etNewPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALIDATE), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
        } else if (etConfirmPin.getText().toString().length() < 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_MIN_FOUR), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (etNewPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
        } else if (etConfirmPin.getText().toString().length() > 4) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (!etNewPin.getText().toString().trim().equalsIgnoreCase("") && !etConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etNewPin.getText().toString().trim(), etConfirmPin.getText().toString().trim())) {
                Log.d("From here", "Call service");
                if (Utills.isOnline(getActivity(), true)) {
                    createPin();
                    // Utils.displayDialog(this, getString(R.string.app_name), "Account has been created", getString(android.R.string.ok), "", false, true);
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PWD_RE_PWD_EMPTYMSG), getString(R.string.ok), "", false, false);
                etNewPin.requestFocus();
            }
        }
    }


    public boolean checkPassWordAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
            }
        }
        return pstatus;
    }


    private void callForgotPinService() {

        if (Utills.isInternetAvailable(getActivity())) {

            Log.d("START", "internet availavble");

            if (asyncTaskCreatePinn != null && asyncTaskCreatePinn.getStatus() == AsyncTask.Status.PENDING) {

                asyncTaskCreatePinn.execute();
            } else if (asyncTaskCreatePinn == null || asyncTaskCreatePinn.getStatus() == AsyncTask.Status.FINISHED) {

                asyncTaskCreatePinn = new AsyncTaskCreatePinn(etConfirmPin.getText().toString());
                asyncTaskCreatePinn.execute();
            }
        } else {

            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }


    private void createPin() {


        if (Utills.isInternetAvailable(getActivity())) {

            Log.d("START", "internet availavble");

            if (asyncTaskCreatePinn != null && asyncTaskCreatePinn.getStatus() == AsyncTask.Status.PENDING) {

                asyncTaskCreatePinn.execute();
            } else if (asyncTaskCreatePinn == null || asyncTaskCreatePinn.getStatus() == AsyncTask.Status.FINISHED) {

                asyncTaskCreatePinn = new AsyncTaskCreatePinn(etConfirmPin.getText().toString());
                asyncTaskCreatePinn.execute();
            }
        } else {

            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private class AsyncTaskCreatePinn extends AsyncTask<String, Void, String> {
        private WsCreatePin wsCreatePin;
        private String pin;
        private ProgressDialog progressDialog;


        public AsyncTaskCreatePinn(String pin) {
            this.pin = pin;

            wsCreatePin = new WsCreatePin(getActivity());


        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));


            progressDialog.show();
//            progressDialog.setContentView(R.layout.progress_layout);
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
                    viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.first)));
//                    closefragment();


                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCreatePin.getMessage(), getString(R.string.ok), "", false, false);
                }


            }


        }


    }


    private class AsyncTaskForgotPin extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
