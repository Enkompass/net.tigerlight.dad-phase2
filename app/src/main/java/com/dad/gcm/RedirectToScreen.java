package com.dad.gcm;

import android.content.Context;
import android.os.Bundle;

/**
 *
 */

public class RedirectToScreen {

    private Context mContext;

    public void openScreen(Context mContext, Bundle data) {

        this.mContext = mContext;
        String type = "";
        if (data != null) {
            if (data.containsKey("redirect_type")) {
                type = data.getString("redirect_type");
            }
        }
        if (type != null) {
            if (type.equalsIgnoreCase("job_detail")) {
                openJSJobDetail(data.getString("job_id"), 1);
            } else if (type.equalsIgnoreCase("job_invitation_detail")) {
                openJSJobDetail(data.getString("job_id"), 0);
            }
        }
        if (data != null) {
            data.clear();
            data = null;
        }
    }

    private void openEmpJobDetail(String job_id) {
//        final EmpJobDetailFragment empJobDetailFragment = new EmpJobDetailFragment();
//        final Bundle bundle = new Bundle();
//        bundle.putString(mContext.getString(R.string.BUNDLE_JOB_ID), job_id);
//        empJobDetailFragment.setArguments(bundle);
//        ((EmployerActivity) mContext).addFragment(empJobDetailFragment, ((EmployerActivity) mContext).getLocalFragmentManager().findFragmentById(R.id.content_main_container));
    }

    private void openJSJobDetail(String job_id, int type) {
//        JSJobDetailFragment jsJobDetailFragment = new JSJobDetailFragment();
        //        Bundle bundle = new Bundle();
        //        bundle.putString(mContext.getString(R.string.BUNDLE_JOB_ID), job_id);
        //        if (type == 0) {
        //            bundle.putString(mContext.getString(R.string.BUNDLE_FARGMENT_NAME), JSJobInvitationsFragment.class.getSimpleName());
        //        } else if (type == 1) {
        //            bundle.putString(mContext.getString(R.string.BUNDLE_FARGMENT_NAME), JSMyJobsFragment.class.getSimpleName());
        //        }
        //        jsJobDetailFragment.setArguments(bundle);
        //        ((JobSeekerActivity) mContext).addFragment(jsJobDetailFragment, ((JobSeekerActivity) mContext).getLocalFragmentManager().findFragmentById(R.id.content_main_container));
    }
}

/**
 * 0 for Job Invitation fragment
 * 1 for Active or Over Job Fragment
 **/