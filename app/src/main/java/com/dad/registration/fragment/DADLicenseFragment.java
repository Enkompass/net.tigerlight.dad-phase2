package com.dad.registration.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.dad.R;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.util.Preference;

import java.util.Locale;

/**
 * Created on 25/11/16.
 */

public class DADLicenseFragment extends BaseFragment {

    private TextView tvDoNotAccept;
    private TextView tvAccept;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dad_licence, container, false);
    }

    @Override
    public void initView(View view) {
        tvDoNotAccept = (TextView) view.findViewById(R.id.fragment_dad_license_tvDoNotAccept);
        tvAccept = (TextView) view.findViewById(R.id.fragment_dad_license_tvAccept);
        final WebView webView = (WebView) view.findViewById(R.id.fragment_dad_license_wvTerms);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        final Locale locale = getResources().getConfiguration().locale;
        if (locale.getLanguage().equals("ar")) {
            webView.loadUrl("file:///android_asset/termsandconditionsofusearbic.html");
        } else {
            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
        }

        tvAccept.setOnClickListener(this);
        tvDoNotAccept.setOnClickListener(this);
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
            case R.id.fragment_dad_license_tvDoNotAccept:
                Utills.displayDialog(getActivity(), getString(R.string.dialog_eula_title), getString(R.string.dialog_eula_msg), getString(R.string.ok), "", false, false);
                break;
            case R.id.fragment_dad_license_tvAccept:
                Preference.getInstance().savePreferenceData(Constant.IS_ACCEPT, true);
                ((MainActivity) getActivity()).replaceFragment(new RegistartionFragment());
                break;
        }
    }
}
