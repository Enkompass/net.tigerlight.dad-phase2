package com.dad.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dad.util.CheckForeground;
import com.dad.util.Constants;
import com.dad.util.Util;

/**
 * Created by B.S on 19/04/16.
 * Base Fragment of all fragment that is used in application
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener {
    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public abstract void initView(View view);

    public abstract void trackScreen();

    public abstract void initActionBar();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        trackScreen();
        initActionBar();
    }

    /**
     * Gets the fragment manager object of activity required for fragment transaction
     * <p>This method can be customised on the need of application,in which it returns {@link FragmentManager} or {@link android.support.v4.app.FragmentManager}</p>
     *
     * @return object of {@link FragmentManager} or {@link android.support.v4.app.FragmentManager}
     */
    public FragmentManager getLocalFragmentManager() {
        return this.getActivity().getFragmentManager();
    }

    /**
     * Gets the child fragment manager object of fragment required for fragment transaction
     * <p>This method can be customised on the need of application,in which it returns {@link FragmentManager} or {@link android.support.v4.app.FragmentManager}</p>
     *
     * @return object of {@link FragmentManager} or {@link android.support.v4.app.FragmentManager}
     */
    public FragmentManager getLocalChildFragmentManager() {
        return this.getChildFragmentManager();
    }

    @Override
    public void onClick(View v) {
        Util.getInstance().hideSoftKeyboard(getActivity());
        /**
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.MAX_CLICK_INTERVAL) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    /**
     * removes current fragment from container and replace with the new Fragment recieves in parameter
     *
     * @param newFragment  a fragment object that replaces current fragment
     * @param container_id id of container in which you want to replace fragment
     */
    public void replaceChildFragment(final Fragment newFragment, final int container_id) {
        getLocalChildFragmentManager().beginTransaction().replace(container_id, newFragment, newFragment.getClass().getSimpleName()).commit();
    }


    @Override
    public void onPause() {
        super.onPause();
        CheckForeground.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        CheckForeground.onResume(getActivity());
    }

    //    @Override
    //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //        long currentTime = AnimationUtils.currentAnimationTimeMillis();
    //        if (currentTime - mLastClickTime > DEFAULT_MIN_INTERVAL) {
    //            mListener.onItemClick(parent, view, position, id);
    //            mLastClickTime = currentTime;
    //        }
    //    }
    //}


}
