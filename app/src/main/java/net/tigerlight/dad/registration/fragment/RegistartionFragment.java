package net.tigerlight.dad.registration.fragment;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.MainActivity;

import android.app.FragmentManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

/**
 * RegistartionFragment : user can register or login
 */
public class RegistartionFragment extends BaseFragment {


    private static final String TAG = RegistartionFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_new, container, false);
    }

    @Override
    public void initView(View view) {
        final TextView tvCreateAccount = (TextView) view.findViewById(R.id.fragment_registration_tv_create_account);
        final TextView tvLoginToYourAccount = (TextView) view.findViewById(R.id.fragment_registration_tv_login_to_your_account);
        final TextView tvShowEula = (TextView) view.findViewById(R.id.fragment_registration_tv_show_eula);
        tvCreateAccount.setOnClickListener(this);
        tvLoginToYourAccount.setOnClickListener(this);
        tvShowEula.setOnClickListener(this);

        TextView tvBuildVersion = (TextView) view.findViewById(R.id.fragment_registration_tv_build_version);
        try
        {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);

            tvBuildVersion.setText(String.format(Locale.US, getString(R.string.build_no), packageInfo.versionCode, packageInfo.versionName));
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e(TAG, "Package Not found." + e.getMessage());
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
        final int fragmentId = v.getId();
        if (fragmentId == R.id.fragment_registration_tv_create_account) {
            ((MainActivity) getActivity()).addFragment(new CreateAccountFragment(), RegistartionFragment.this);
        } else if (fragmentId == R.id.fragment_registration_tv_login_to_your_account) {
            ((MainActivity) getActivity()).addFragment(new LoginToYourAccountFragment(), RegistartionFragment.this);
        } else if (fragmentId == R.id.fragment_registration_tv_show_eula) {
            final FragmentManager fm = getFragmentManager();
            final TermAndConditionFragment termAndConditionFragment = new TermAndConditionFragment();
            termAndConditionFragment.show(fm, RegistartionFragment.class.getSimpleName());
        }
    }


}
