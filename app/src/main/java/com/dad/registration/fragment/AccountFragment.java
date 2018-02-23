package com.dad.registration.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dad.R;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsLogout;
import com.dad.settings.webservices.WsResetCount;
import com.dad.util.Preference;

public class AccountFragment extends BaseFragment {

    private TextView tvWelcome;
    private TextView tvEditAccount;
    private TextView tvLogin;
    private TextView tvShowEula;
    private TextView tvLogOut;
    private AsyncTaskLogOut asyncTaskLogOut;
    private AsyncTaskResetCount asyncTaskResetCount;

    private String currentUserName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void initView(View view) {

        callResetCount();
        tvWelcome = (TextView) view.findViewById(R.id.fragment_settings_tvWelcome);
        tvEditAccount = (TextView) view.findViewById(R.id.fragment_settings_tvEditAccount);
        tvLogin = (TextView) view.findViewById(R.id.fragment_settings_tvLogin);
        tvShowEula = (TextView) view.findViewById(R.id.fragment_settings_tvShowEula);
        tvLogOut = (TextView) view.findViewById(R.id.fragment_settings_tvLogOut);
        currentUserName = Preference.getInstance().mSharedPreferences.getString(Constant.USER_NAME, "");
        tvWelcome.setText(getString(R.string.TAG_WELLCOME) + " " + currentUserName);

//       tvWelcome.setText(String.format("Welcome ", currentUserName));
        tvEditAccount.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        tvShowEula.setOnClickListener(this);
        tvLogOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.fragment_settings_tvEditAccount:
                ((MainActivity) getActivity()).addFragment(new EditProfileFragment(), AccountFragment.this);
                break;

            case R.id.fragment_settings_tvLogOut:
                displayMyDialog(getActivity(), getString(R.string.TAG_LOGOUT_CONFIRMATION), getString(R.string.TAG_LOGOUT_CONFIRMATION_DES), getString(R.string.TAG_OK), getString(R.string.fragment_create_account_tv_cancel));
                break;

            case R.id.fragment_settings_tvLogin:
                displayMyDialog(getActivity(), getString(R.string.TAG_LOGOUT_CONFIRMATION), getString(R.string.TAG_LOGOUT_CONFIRMATION_DES), getString(R.string.TAG_OK), getString(R.string.fragment_create_account_tv_cancel));
                break;

            case R.id.fragment_settings_tvShowEula:
                final FragmentManager fm = getFragmentManager();
                final TermAndConditionFragment termAndConditionFragment = new TermAndConditionFragment();
                termAndConditionFragment.show(fm, RegistartionFragment.class.getSimpleName());
                break;
        }
    }

    private void logOut() {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskLogOut != null && asyncTaskLogOut.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLogOut.execute();
            } else if (asyncTaskLogOut == null || asyncTaskLogOut.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLogOut = new AsyncTaskLogOut();
                asyncTaskLogOut.execute();
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

    private class AsyncTaskLogOut extends AsyncTask<Void, Void, Void> {

        private WsLogout wsLogout;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.show();
            progressDialog.setCancelable(false);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsLogout = new WsLogout(getActivity());
            wsLogout.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsLogout.isSuccess()) {
                    Preference.getInstance().savePreferenceData(Constant.IS_PIN_CREATED, true);
                    final Preference preference = Preference.getInstance();
                    boolean isRemember = preference.mSharedPreferences.getBoolean(Constant.IS_REMEMBER, false);
                    String email = "";
                    String pwd = "";
                    if (isRemember) {
                        email = preference.mSharedPreferences.getString(Constant.KEY_EMAIL, "");
                        pwd = preference.mSharedPreferences.getString(Constant.KEY_PASSWORD, "");
                    }
                    //preference.clearPreferenceData();
                    preference.savePreferenceData(Constant.IS_REMEMBER, isRemember);
                    preference.savePreferenceData(Constant.USER_NAME, currentUserName);
                    preference.savePreferenceData(Constant.KEY_EMAIL, email);
                    preference.savePreferenceData(Constant.KEY_PASSWORD, pwd);
                    preference.savePreferenceData(Constant.IS_LOGIN, false);
                    preference.savePreferenceData(Constant.IS_PIN_CREATED, false);
                    ((MainActivity) getActivity()).replaceFragment(new RegistartionFragment());

                } else {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsLogout.getMessage(), getString(R.string.ok), "", false, false);
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_COULD_LOGOUT), getString(R.string.ok), "", false, false);
                }
            }
        }
    }

    private void displayMyDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);

        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logOut();
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
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            currentUserName = Preference.getInstance().mSharedPreferences.getString(Constant.USER_NAME, "");
            tvWelcome.setText(String.format(getString(R.string.TAG_WELLCOME) + "%s", currentUserName));
        }
    }
}
