package com.hundredstartups.openvpn.view;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BaseActivity extends AppCompatActivity {

    public FragmentManager mFragmentManager;
    public BaseFragment mLastOpenedFragment;
    private Dialog mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void openFragment(int resId, BaseFragment pFragment) {
        mFragmentManager = getSupportFragmentManager();
        mLastOpenedFragment = pFragment;
//        Fragment f = mFragmentManager.findFragmentByTag(pFragment.getClass().getSimpleName());
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(resId, pFragment, pFragment.getClass().getSimpleName()).addToBackStack(pFragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    public void showProgressDialog() {
        if (mProgressBar != null && !mProgressBar.isShowing()) {
            try {
                mProgressBar.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hideProgressDialog() {
        if (mProgressBar != null && mProgressBar.isShowing()) {
            try {
                new Handler().postDelayed(() -> {
                    if (!isDestroyed() && !isFinishing() && mProgressBar != null )
                        mProgressBar.dismiss();
                },500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
