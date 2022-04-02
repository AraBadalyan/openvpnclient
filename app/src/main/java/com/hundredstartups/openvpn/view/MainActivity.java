package com.hundredstartups.openvpn.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.hundredstartups.openvpn.R;
import com.hundredstartups.openvpn.adapter.ServerListRVAdapter;
import com.hundredstartups.openvpn.cache.UserAccountManager;
import com.hundredstartups.openvpn.interfaces.ChangeServer;
import com.hundredstartups.openvpn.interfaces.NavItemClickListener;
import com.hundredstartups.openvpn.model.Server;
import com.hundredstartups.openvpn.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements NavItemClickListener {
    //    private FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
    private MainFragment mMainFragment;
    private LoginFragment mLoginFragment;
    private RecyclerView mServerListRecyclerView;
    private ArrayList<Server> mServerLists;
    private ServerListRVAdapter mServerListRVAdapter;
    private DrawerLayout mDrawer;
    private ChangeServer mChangeServer;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (TextUtils.isEmpty(UserAccountManager.INSTANCE.getToken())) {
            openSignIn();
        } else {
            init();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
    }

    public void openSignIn() {
        mLoginFragment = new LoginFragment();
        openFragment(R.id.container, mLoginFragment);
    }

    public void init() {
        // Initialize all variable
        initializeMain();

        ImageButton menuRight = findViewById(R.id.navbar_right);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);

        menuRight.setOnClickListener(v -> closeDrawer());

        openFragment(R.id.container, mMainFragment);
//        mTransaction.add(R.id.container, mMainFragment);
//        mTransaction.commit();

        // Server List recycler view initialize
        if (mServerLists != null) {
            mServerListRVAdapter = new ServerListRVAdapter(mServerLists, this);
            mServerListRecyclerView.setAdapter(mServerListRVAdapter);
        }
    }

    /**
     * Initialize all object, listener etc
     */
    private void initializeMain() {
        mDrawer = findViewById(R.id.drawer_layout);

        mMainFragment = new MainFragment();
        mServerListRecyclerView = findViewById(R.id.serverListRv);
        mServerListRecyclerView.setHasFixedSize(true);

        mServerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mServerLists = getServerList();
        mChangeServer = (ChangeServer) mMainFragment;
    }

    /**
     * Close navigation drawer
     */
    public void closeDrawer() {
        if (mDrawer.isDrawerOpen(GravityCompat.END)) {
            mDrawer.closeDrawer(GravityCompat.END);
        } else {
            mDrawer.openDrawer(GravityCompat.END);
        }
    }

    /**
     * Generate server array list
     */
    private ArrayList getServerList() {
        ArrayList<Server> servers = new ArrayList<>();
        servers.add(new Server("Client",
                Utils.getImgURL(R.drawable.usa_flag),
                "client.ovpn",
                "vpn",
                "vpn"
        ));


        servers.add(new Server("United States",
                Utils.getImgURL(R.drawable.usa_flag),
                "us.ovpn",
                "freeopenvpn",
                "416248023"
        ));

        servers.add(new Server("Japan",
                Utils.getImgURL(R.drawable.japan),
                "japan.ovpn",
                "vpn",
                "vpn"
        ));
        servers.add(new Server("Sweden",
                Utils.getImgURL(R.drawable.sweden),
                "sweden.ovpn",
                "vpn",
                "vpn"
        ));
        servers.add(new Server("Korea",
                Utils.getImgURL(R.drawable.korea),
                "korea.ovpn",
                "vpn",
                "vpn"
        ));

        return servers;
    }

    /**
     * On navigation item click, close drawer and change server
     *
     * @param index: server index
     */
    @Override
    public void clickedItem(int index) {
        closeDrawer();
        mChangeServer.newServer(mServerLists.get(index));
    }
}
