package com.tigerlight.dad.registration.fragment;

import com.tigerlight.dad.R;
import com.tigerlight.dad.home.BaseFragment;
import com.tigerlight.dad.registration.adapter.ViewPagerAdapter;
import com.tigerlight.dad.registration.util.Constant;
import com.tigerlight.dad.settings.webservices.WsCallGetAlertCount;
import com.tigerlight.dad.util.Preference;
import com.tigerlight.dad.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DashBoardWithSwipableFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    //private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.img_contact_dis,
            R.drawable.img_alerts_dis,
            R.drawable.img_iamok_dis,
            R.drawable.img_account_dis,
    };
    private ContactFragment contactFragment;
    private AlertFragment alertFragment;
    private AmOkFragmentI fragmentImOk;
    private AccountFragment accountFragment;
    final Preference preference = Preference.getInstance();

    private static final String SUCCESS = "success";
    private boolean isDataAvailable = false;
    private boolean isJustDataDeleted = false;
    private JSONArray jsonArray;
    private int listSize = 0;
    private Context context;
    private TextView textCount;
    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_with_swipable, container, false);
    }

    @Override
    public void initView(View view) {
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        contactFragment = new ContactFragment();
        alertFragment = new AlertFragment(this);
        fragmentImOk = new AmOkFragmentI();
        accountFragment = new AccountFragment();
//        new AlertListLoaderThread().start();


        setupCustomViewForTab();
        //viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        //setupViewPager(viewPager);
        //tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
        setCurrentTabFragment(0);
        //viewPager.setCurrentItem(0);
        tabLayout.addOnTabSelectedListener(this);
        new AlertListLoaderThread().start();

    }

    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {

    }

    public void selectAlertsTab() {
        setCurrentTabFragment(1);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new ContactFragment(), getString(R.string.dashbord_contact));
        adapter.addFragment(new AlertFragment(), getString(R.string.dashbord_alert));
        adapter.addFragment(new ImOkFragment(), getString(R.string.dashbord_i_m_ok));
        adapter.addFragment(new AccountFragment(), getString(R.string.dashbord_account));
        viewPager.setAdapter(adapter);
    }

    /**
     * @param tabPosition setCurrentTabFragment to replace fragmet with current fragment
     */
    private void setCurrentTabFragment(int tabPosition) {
        Log.d("tab", "select" + tabPosition);
        switch (tabPosition) {
            case 0:
                final TextView tabOne = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_contact, 0, 0);
                loadFragment(contactFragment);
                break;

            case 1:
                final TextView tabTwo = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
//                tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_alerts, 0, 0);
                loadFragment(alertFragment);
                break;

            case 2:
                final TextView tabThree = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_iamok, 0, 0);
                loadFragment(fragmentImOk);
                break;

            case 3:
                final TextView tabFour = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_account, 0, 0);
                loadFragment(accountFragment);
                break;
        }
    }

    private void setDefaultTab(int tabPosition) {
        Log.d("tab", "unselect" + tabPosition);
        switch (tabPosition) {
            case 0:
                final TextView tabOne = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_contact_dis, 0, 0);
                break;

            case 1:
                final TextView tabTwo = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
//                tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_alerts_dis, 0, 0);
                break;

            case 2:
                final TextView tabThree = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_iamok_dis, 0, 0);
                break;

            case 3:
                final TextView tabFour = (TextView) tabLayout.getTabAt(tabPosition).getCustomView().findViewById(R.id.tab);
                tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_account_dis, 0, 0);
                break;
        }
    }

    private void setupTabIcons() {
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[0]).setText(R.string.dashbord_contact));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[1]).setText(R.string.dashbord_alert));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[2]).setText(R.string.dashbord_i_m_ok));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[3]).setText(R.string.dashbord_account));

        //tabLayout.setTabTextColors(ContextCompat.getColor(getActivity(),R.color.tab_unselected_text),ContextCompat.getColor(getActivity(),R.color.tab_selected_text));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        setCurrentTabFragment(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        setDefaultTab(tab.getPosition());
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * setupCustomViewForTab where we set name icon and custom view for bottom tabs
     */
    private void setupCustomViewForTab() {
        if (tabLayout != null) {
            final TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_contact_dis, 0, 0);
            tabOne.setText(R.string.dashbord_contact);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
//
            final View tabTwo = (View) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_alert_with_badge, null);
//            tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_alerts_dis, 0, 0);
            TextView textTwo = (TextView) tabTwo.findViewById(R.id.framgent_alert_tv_title);
            textTwo.setText(R.string.dashbord_alert);

            textCount = (TextView) tabTwo.findViewById(R.id.framgent_alert_tv_badge_count);

//            textCount.setText(String.valueOf(Preference.getInstance().mSharedPreferences.getInt("total_count",0)));

            tabLayout.addTab(tabLayout.newTab().setCustomView(tabTwo));

            final TextView tabThree = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_iamok_dis, 0, 0);
            tabThree.setText(R.string.dashbord_i_m_ok);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabThree));
            final TextView tabFour = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_account_dis, 0, 0);
            tabFour.setText(R.string.dashbord_account);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabFour));

        }
    }

    public void updateCount() {
        textCount.setText(String.valueOf(Preference.getInstance().mSharedPreferences.getInt("total_count", 0)));
    }


    /**
     * removes current fragment from container and replace with the new Fragment recieves in parameter
     *
     * @param newFragment a fragment object that replaces current fragment
     */
    public void loadFragment(final Fragment newFragment) {
        Util.getInstance().hideSoftKeyboard(getActivity());
        getLocalFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_dashboard_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .commit();
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
                        if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
                        {
                            getActivity().runOnUiThread(new AlertListDataHandler(jsonRecieved));
                        }
                    } else {
                        isDataAvailable = false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
            {
                getActivity().runOnUiThread(new AlertListDataHandler(null));
            }
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


                    if (alertCount < 0) {
                        alertCount = 0;
                    }
                    Preference.getInstance().savePreferenceData("total_count", alertCount);

                    if (alertCount < 0) {
                        alertCount = 0;
                    }
                    Preference.getInstance().savePreferenceData("alert_count", alertCount);
                    textCount.setText(String.valueOf(Preference.getInstance().mSharedPreferences.getInt("total_count", 0)));

//                    alertAdapter = new AlertAdapter(getActivity(), AlertFragment.this, jsonArray);
//                    lvAlerts.setAdapter(alertAdapter);
//                    lvAlerts.setOnItemClickListener(AlertFragment.this);


                    if (jsonArray.length() == 0) {
//                        tvEmptyAlert.setVisibility(View.VISIBLE);
//                        tvEmptyAlert.setText(getString(R.string.TAG_DATA_NA_MSG));
//                        lvAlerts.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            progressDialog.cancel();
        }

    }


//    private class AlertListLoaderThread extends Thread {
//        @Override
//        public void run() {
//            try {
//                final WsCallGetAlertCount wsCallGetAlertCount;
//                wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
//                String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
//                JSONObject jsonRecieved = wsCallGetAlertCount.executeService(email, "" + 0);
//                if (jsonRecieved != null) {
//
//
//                    if (jsonRecieved.getInt(SUCCESS) == 1) {
//                        isDataAvailable = true;
////                        jsonArray = jsonRecieved.getJSONArray("data");
////                        listSize = jsonArray.length();
//                        int alertCount = jsonRecieved.optJSONArray("data").length();
//
//                        if (alertCount < 0) {
//                            alertCount = 0;
//                        }
//                        Preference.getInstance().savePreferenceData("total_count", alertCount);
//
////                        getActivity().runOnUiThread(new AlertListDataHandler(jsonRecieved));
//                    } else {
//                        isDataAvailable = false;
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
////            getActivity().runOnUiThread(new AlertListDataHandler(null));
//        }
//    }
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
//                    textCount.setText(String.valueOf(Preference.getInstance().mSharedPreferences.getInt("total_count",0)));
//
//
//
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
