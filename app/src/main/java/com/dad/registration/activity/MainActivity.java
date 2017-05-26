package com.dad.registration.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dad.R;
import com.dad.home.BaseActivity;
import com.dad.home.BaseFragment;
import com.dad.registration.fragment.AlertDetailFragment;
import com.dad.registration.fragment.DADLicenseFragment;
import com.dad.registration.fragment.DashBoardWithSwipableFragment;
import com.dad.registration.fragment.RegistartionFragment;
import com.dad.registration.util.Constant;
import com.dad.util.Preference;
import com.dad.util.Util;

public class MainActivity extends BaseActivity {

    private boolean isLogin;
    private boolean isFirstAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final boolean isAccepted = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ACCEPT, false);
        isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);
        isFirstAccount = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_FIRST_ACCOUNT, false);

        final Intent intent = getIntent();
        String jsonObject = intent.getStringExtra(Constant.JSON_OBJECT);
        Log.e("notification", "oncreate ----json object:" + intent.getStringExtra(Constant.JSON_OBJECT));
        if (jsonObject != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                Log.e("notification", "oncreate if " + intent.getStringExtra(Constant.JSON_OBJECT));
                // Get the Back Entry
                final FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
                // Find the Fragment from the Back Entry by it's Tag
                final Fragment fragment = getFragmentManager().findFragmentByTag(backEntry.getName());
                if (fragment != null) {
                    Log.e("notification", "fragment " + fragment);
                    // Fetch the Fragment currently added in the Stack
                    final BaseFragment currentFragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
                    if (currentFragment != null) {
                        Log.e("notification", "currentFragment " + fragment);
                        addFragment(new AlertDetailFragment(), currentFragment);
                    }
                }
            } else {
                Log.e("notification", "oncreate else " + intent.getStringExtra(Constant.JSON_OBJECT));
                final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                alertDetailFragment.setArguments(bundle);
                replaceFragment(alertDetailFragment);
            }
        } else {
            if (isLogin) {
                //if (isAccepted) {
                replaceFragment(new DashBoardWithSwipableFragment());
                //}
//            else if (!isAccepted) {
//                replaceFragment(new TermAndConditionFragment());
//            }
            } else {
                if (isAccepted) {
                    replaceFragment(new RegistartionFragment());
                } else {
                    replaceFragment(new DADLicenseFragment());
                }
            }
        }

//        if(jsonObject!=null){
//            Log.e("notification","oncreate inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//            BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
//            if(fragment!=null){
//                Log.e("notification","if oncreate inside inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//                final Bundle  bundle = new Bundle();
//                bundle.putString(Constant.JSON_OBJECT,jsonObject);
//                fragment.setArguments(bundle);
//                addFragment(new AlertDetailFragment(),fragment);
//            }else {
//                Log.e("notification","else oncreate inside inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//                AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
//                final Bundle  bundle = new Bundle();
//                bundle.putString(Constant.JSON_OBJECT,jsonObject);
//                alertDetailFragment.setArguments(bundle);
//                replaceAlertDetailFragment(alertDetailFragment);
//            }
//        }


    }

    private BroadcastReceiver temp = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("onrecive", "called");


//            if (getFragmentManager().getBackStackEntryCount() > 0) {
//
//                // Get the Back Entry
//                final FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
//
//                // Find the Fragment from the Back Entry by it's Tag
//                final Fragment fragment = getFragmentManager().findFragmentByTag(backEntry.getName());
//                if (fragment != null) {
//                    Log.e("HomeActivity", "Fragment By TAG : " + fragment);
//                    // Fetch the Fragment currently added in the Stack
//                    final BaseFragment currentFragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
//                    if (currentFragment != null) {
//                        addFragment(new AlertDetailFragment(),currentFragment);
//                    }
//                }
//            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("notification", "called" + intent.getStringExtra(Constant.JSON_OBJECT));
        // BaseFragment fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
        //if (fragment != null) {
        final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
        String jsonObject = intent.getStringExtra(Constant.JSON_OBJECT);
        final Bundle bundle = new Bundle();
        bundle.putString(Constant.JSON_OBJECT, jsonObject);
        alertDetailFragment.setArguments(bundle);
        Log.e("notification", " ifcalled" + intent.getStringExtra(Constant.JSON_OBJECT));
        addFragment(alertDetailFragment);
    }

    @Override
    public void onBackPressed() {
        if (getLocalFragmentManager().getBackStackEntryCount() > 0) {
            Util.getInstance().hideSoftKeyboard(this);
            getLocalFragmentManager().popBackStack();
        } else {
            buildAlertMessageExit();
        }
    }

    /**
     * asks user for the confirmation before exiting of the app
     */
    private void buildAlertMessageExit() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.TAG_EXIT_WARN_MSG)).setCancelable(false).setPositiveButton(getString(R.string.TAG_YES), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                callToFinish();
            }
        }).setNegativeButton(getString(R.string.TAG_NO), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void callToFinish() {
        super.finish();
    }

    /***
     * Add new fragment in given container.
     * <p>
     * This method will add new fragment in container and hide the current fragment.
     * And also will add current fragment in backstack.
     * </p>
     *
     * @param newFragment  This parameter will take new fragment name which need to be add.
     * @param hideFragment This parameter will take fragmnet name which you want to hide.
     */
    public void addFragment(final BaseFragment newFragment, final BaseFragment hideFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .hide(hideFragment)
                .addToBackStack(hideFragment.getClass().getSimpleName())
                .commit();
    }


    public void addFragment(final BaseFragment newFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .addToBackStack(newFragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * removes current fragment from container and replace with the new Fragment recieves in parameter
     *
     * @param newFragment a fragment object that replaces current fragment
     */
    public void replaceFragment(final Fragment newFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * removes all fragment from container and add with the new Fragment recieves in parameter
     *
     * @param newFragment a fragment object that replaces current fragment
     */
    public void replaceFragmentPopBackstack(final Fragment newFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getLocalFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }


}
