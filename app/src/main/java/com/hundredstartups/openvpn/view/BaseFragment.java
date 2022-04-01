package com.hundredstartups.openvpn.view;

import android.content.Context;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected BaseActivity mActivity;
//    public PreferencesManager mPreferencesManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
//        mPreferencesManager = PreferencesManager.getInstance(mActivity);
    }
}
