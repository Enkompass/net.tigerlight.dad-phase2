package com.dad.contacts.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dad.R;
import com.dad.home.BaseFragment;

/**
 * A simple {@link com.dad.home.BaseFragment} subclass.
 */
public class ContactListFragment extends BaseFragment {


    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {

    }

}
