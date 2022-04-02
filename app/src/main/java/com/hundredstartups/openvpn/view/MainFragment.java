package com.hundredstartups.openvpn.view;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.hundredstartups.openvpn.AlertManager;
import com.hundredstartups.openvpn.CheckInternetConnection;
import com.hundredstartups.openvpn.R;
import com.hundredstartups.openvpn.SharedPreference;
import com.hundredstartups.openvpn.api.APIClient;
import com.hundredstartups.openvpn.api.APIInterface;
import com.hundredstartups.openvpn.cache.UserAccountManager;
import com.hundredstartups.openvpn.databinding.FragmentMainBinding;
import com.hundredstartups.openvpn.interfaces.ChangeServer;
import com.hundredstartups.openvpn.model.Empty;
import com.hundredstartups.openvpn.model.Server;
import com.hundredstartups.openvpn.model.VpnData;
import com.hundredstartups.openvpn.model.VpnEntity;
import com.hundredstartups.openvpn.utils.CountryFlags;
import com.hundredstartups.openvpn.utils.FormatterUtils;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import retrofit2.Call;
import retrofit2.Callback;

public class MainFragment extends BaseFragment implements ChangeServer {

    private Server mServer;
    private CheckInternetConnection mConnection;
    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    boolean mVpnStart = false;
    private SharedPreference mPreference;
    private FragmentMainBinding mBinding;

    private APIInterface mApiInterface;
    private List<VpnEntity> mVpnEntityList;
    private RewardedAd mRewardedAd;

    private static final String TAG = "Open VPN TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeAll();
        getData();
        initListeners(view);
        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getActivity().getCacheDir());
    }

    /**
     * Initialize all variable and object
     */
    private void initializeAll() {
        mApiInterface = new APIClient().getClient().create(APIInterface.class);
        mPreference = new SharedPreference(getContext());
        mServer = mPreference.getServer();
        // Update current selected server icon
//        updateCurrentServerIcon(mServer.getFlagUrl());
        mConnection = new CheckInternetConnection();
    }

    private void getData() {
        Call<VpnData> call = mApiInterface.getVpnData(new Empty());
        call.enqueue(new Callback<VpnData>() {
            @Override
            public void onResponse(Call<VpnData> call, retrofit2.Response<VpnData> response) {
                if (response.headers().get("Token") != null) {
                    mVpnEntityList = response.body().getIpList();
                    if (mVpnEntityList != null && mVpnEntityList.size() > 0) {
                        initStatusTexts();
                    }
                    initRewardedAdd();
                } else {
                    UserAccountManager.INSTANCE.saveUserData(null, null, null);
                    ((MainActivity) getActivity()).openSignIn();
                }
            }

            @Override
            public void onFailure(Call<VpnData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void initListeners(View view) {
        view.findViewById(R.id.vpnImageView).setOnClickListener(v -> {
            if (mVpnEntityList != null && mVpnEntityList.size() > 0) {
                if (isVpnExpired()) {
                    confirmShowAdd();
                } else {
                    if (mVpnStart) {
                        confirmDisconnect();
                    } else {
                        prepareVpn();
                    }
                }
            } else {
                Toast.makeText(mActivity, getString(R.string.error_something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.layout_logout).setOnClickListener(v -> confirmLogout());
        view.findViewById(R.id.showAddLayout).setOnClickListener(v -> showRewardedAdd());
    }

    private boolean isVpnExpired() {
        return FormatterUtils.INSTANCE.calculateDayFromTo(mVpnEntityList.get(0).getTime().getEndTime()) < 0;
    }

    private void confirmLogout() {
        AlertManager.INSTANCE.showTwoButtonsDialog(
                mActivity,
                getActivity().getString(R.string.logout_confirm),
                true,
                getActivity().getString(R.string.yes),
                getActivity().getString(R.string.no),
                new AlertManager.OnTwoButtonDialogButtonClick() {
                    @Override
                    public void onPositiveClick() {
                        logout();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                }
        );
    }

    private void confirmShowAdd() {
        AlertManager.INSTANCE.showTwoButtonsDialog(
                mActivity,
                getActivity().getString(R.string.show_add_confirm),
                true,
                getActivity().getString(R.string.yes),
                getActivity().getString(R.string.no),
                new AlertManager.OnTwoButtonDialogButtonClick() {
                    @Override
                    public void onPositiveClick() {
                        showRewardedAdd();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                }
        );
    }

    private void logout() {
        Call<Void> call = mApiInterface.logout(new Empty());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                UserAccountManager.INSTANCE.clearCache();
                if (mVpnStart) {
                    stopVpn();
                }
                ((MainActivity) mActivity).openSignIn();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void getReward() {
        Call<VpnData> call = mApiInterface.getReward(new Empty());
        call.enqueue(new Callback<VpnData>() {
            @Override
            public void onResponse(Call<VpnData> call, retrofit2.Response<VpnData> response) {
                mVpnEntityList = response.body().getIpList();
            }

            @Override
            public void onFailure(Call<VpnData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Show show disconnect confirm dialog
     */
    public void confirmDisconnect() {
        AlertManager.INSTANCE.showTwoButtonsDialog(
                mActivity,
                getActivity().getString(R.string.connection_close_confirm),
                true,
                getActivity().getString(R.string.yes),
                getActivity().getString(R.string.no),
                new AlertManager.OnTwoButtonDialogButtonClick() {
                    @Override
                    public void onPositiveClick() {
                        stopVpn();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                }
        );
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private void prepareVpn() {
        if (!mVpnStart) {
            if (getInternetStatus()) {
                // Checking permission for network monitor
                Intent intent = VpnService.prepare(getContext());
                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else {
                    startVpn();//have already permission
                }
                // Update confection status
                status("connecting");
            } else {
                // No internet connection available
                showToast("you have no internet connection !!");
            }
        } else if (stopVpn()) {
            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    public boolean stopVpn() {
        try {
            vpnThread.stop();
            status("connect");
            mVpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Taking permission for network access
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("Permission Deny !! ");
        }
    }

    /**
     * Internet connection status.
     */
    public boolean getInternetStatus() {
        return mConnection.netCheck(getContext());
    }

    /**
     * Get service status
     */
    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    /**
     * Start the VPN
     */
    private void startVpn() {
        if (mVpnEntityList != null && mVpnEntityList.size() > 0) {
            try {
                // .ovpn file
                InputStream conf = getActivity().getAssets().open(mServer.getOvpn());
                InputStreamReader isr = new InputStreamReader(conf);
                BufferedReader br = new BufferedReader(isr);
                String config = "";
                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    config += line + "\n";
                }
                br.readLine();
//            OpenVpnApi.startVpn(getContext(), config, mServer.getCountry(), mServer.getOvpnUserName(), mServer.getOvpnUserPassword());
                VpnEntity entity = mVpnEntityList.get(0);
                OpenVpnApi.startVpn(getContext(), entity.getConf(), entity.getCountryCode(), "", "");
                // Update log
                mBinding.logTv.setText("Connecting...");
                mVpnStart = true;
            } catch (IOException | RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.error_something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Status change with corresponding vpn connection status
     *
     * @param connectionState
     */
    public void setStatus(String connectionState) {
        if (connectionState != null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    mVpnStart = false;
                    vpnService.setDefaultStatus();
                    mBinding.logTv.setText("");
                    break;
                case "CONNECTED":
                    mVpnStart = true;// it will use after restart this activity
                    status("connected");
                    mBinding.logTv.setText("");
                    break;
                case "WAIT":
                    mBinding.logTv.setText("waiting for server connection!!");
                    break;
                case "AUTH":
                    mBinding.logTv.setText("server authenticating!!");
                    break;
                case "RECONNECTING":
                    status("connecting");
                    mBinding.logTv.setText("Reconnecting...");
                    break;
                case "NONETWORK":
                    mBinding.logTv.setText("No network connection");
                    break;
            }

    }

    /**
     * Change button background color and text
     *
     * @param status: VPN current status
     */
    public void status(String status) {
        if (status.equals("connect")) {
            mBinding.vpnTextView.setText(getContext().getString(R.string.disconnect));
            mBinding.vpnImageView.setImageResource(R.drawable.img_disconnected);
        } else if (status.equals("connecting")) {
            mBinding.vpnTextView.setText(getContext().getString(R.string.connecting));
        } else if (status.equals("connected")) {
            mBinding.vpnTextView.setText(getContext().getString(R.string.connect));
            mBinding.vpnImageView.setImageResource(R.drawable.img_connected);
        } else if (status.equals("tryDifferentServer")) {
            mBinding.vpnTextView.setText("Try Different\nServer");
        } else if (status.equals("loading")) {
            mBinding.vpnTextView.setText("Loading Server..");
        } else if (status.equals("invalidDevice")) {
            mBinding.vpnTextView.setText("Invalid Device");
        } else if (status.equals("authenticationCheck")) {
            mBinding.vpnTextView.setText("Authentication \n Checking...");
        }
    }

    /**
     * Receive broadcast message
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Update status UI
     *
     * @param duration:          running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn:            incoming data
     * @param byteOut:           outgoing data
     */
    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        mBinding.durationTv.setText("Duration: " + duration);
        mBinding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        mBinding.byteInTv.setText("Bytes In: " + byteIn);
        mBinding.byteOutTv.setText("Bytes Out: " + byteOut);

        if (mVpnEntityList != null && mVpnEntityList.size() > 0) {
            mBinding.ipTv.setText("IP " + mVpnEntityList.get(0).getIp());
        }
    }

    /**
     * Show toast message
     *
     * @param message: toast message
     */
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * VPN server country icon change
     *
     * @param serverIcon: icon URL
     */
//    public void updateCurrentServerIcon(String serverIcon) {
//        Glide.with(getContext())
//                .load(serverIcon)
//                .into(mBinding.selectedServerIcon);
//    }

    /**
     * Change server when user select new server
     *
     * @param server ovpn server details
     */
    @Override
    public void newServer(Server server) {
        this.mServer = server;
//        updateCurrentServerIcon(server.getFlagUrl());

        // Stop previous connection
        if (mVpnStart) {
            stopVpn();
        }
        prepareVpn();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        if (mServer == null) {
            mServer = mPreference.getServer();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    /**
     * Save current selected server on local shared preference
     */
    @Override
    public void onStop() {
        if (mServer != null) {
            mPreference.saveServer(mServer);
        }
        super.onStop();
    }

    private void initStatusTexts() {
        Locale loc = new Locale("", mVpnEntityList.get(0).getCountryCode());
        String countryName = loc.getDisplayCountry();
        String cityName = mVpnEntityList.get(0).getCityName();
        String countryText = CountryFlags.getCountryFlagByCountryCode(mVpnEntityList.get(0).getCountryCode())
                + "  " + countryName + "/" + cityName;
        mBinding.countryTv.setText(countryText);
        mBinding.untilTv.setText("Expires: " + FormatterUtils.INSTANCE.calculateExpiresIn(mVpnEntityList.get(0).getTime().getEndTime()));
    }

    public void initButtonText(int extraTime) {
        long endTime = mVpnEntityList.get(0).getTime().getEndTime() + extraTime * 60 * 60 * 1000;
        mVpnEntityList.get(0).getTime().setEndTime(endTime);

        String endTimeText = FormatterUtils.INSTANCE.calculateExpiresIn(System.currentTimeMillis() + extraTime * 60 * 60 * 1000);
        mBinding.btnText.setText("Extend " + endTimeText);
    }

    private void initRewardedAdd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(mActivity, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;

                        String userID = UserAccountManager.INSTANCE.getUserId();
                        ServerSideVerificationOptions serverSideVerificationOptions = new ServerSideVerificationOptions.Builder().setUserId(userID).build();
                        mRewardedAd.setServerSideVerificationOptions(serverSideVerificationOptions);

                        Log.d(TAG, "Ad was loaded.");
                        initRewardedListener();
                        initButtonText(rewardedAd.getRewardItem().getAmount());
                    }
                });
    }

    private void initRewardedListener() {
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad was shown.");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.d(TAG, "Ad failed to show.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad was dismissed.");
                mRewardedAd = null;
            }
        });
    }

    public void showRewardedAdd() {
        if (mRewardedAd != null) {
            mRewardedAd.show(mActivity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    if (mVpnEntityList.get(0).getTime().getEndTime() <= System.currentTimeMillis()) {
                        getData();
                    }
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }
}
