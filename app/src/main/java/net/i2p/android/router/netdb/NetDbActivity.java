package net.i2p.android.router.netdb;

import net.i2p.android.router.I2PActivityBase;
import net.i2p.android.router.R;
import net.i2p.data.Hash;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

public class NetDbActivity extends I2PActivityBase implements
        NetDbListFragment.OnEntrySelectedListener {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static final String SELECTED_TAB = "selected_tab";

    @Override
    protected boolean canUseTwoPanes() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up action bar for tabs
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Statistics tab
        NetDbSummaryPagerFragment sf = new NetDbSummaryPagerFragment();
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Statistics")
                        .setTabListener(new NetDbSummaryPagerTabListener(sf)));

        // Routers tab
        NetDbListFragment rf = new NetDbListFragment();
        Bundle args = new Bundle();
        args.putBoolean(NetDbListFragment.SHOW_ROUTERS, true);
        rf.setArguments(args);
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Routers")
                        .setTabListener(new TabListener(rf)));

        // LeaseSets tab
        NetDbListFragment lf = new NetDbListFragment();
        args = new Bundle();
        args.putBoolean(NetDbListFragment.SHOW_ROUTERS, false);
        lf.setArguments(args);
        actionBar.addTab(
                actionBar.newTab()
                        .setText("LeaseSets")
                        .setTabListener(new TabListener(lf)));

        if (savedInstanceState != null) {
            int selected = savedInstanceState.getInt(SELECTED_TAB);
            actionBar.setSelectedNavigationItem(selected);
        }

        if (findViewById(R.id.detail_fragment) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            rf.setActivateOnItemClick(true);
            lf.setActivateOnItemClick(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    public static class NetDbSummaryPagerTabListener extends TabListener {
        public NetDbSummaryPagerTabListener(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            /**
             * This is a work-around for Issue 42601
             * https://code.google.com/p/android/issues/detail?id=42601
             * 
             * The method getChildFragmentManager() does not clear up
             * when the Fragment is detached.
             */
            mFragment = new NetDbSummaryPagerFragment();
            super.onTabSelected(tab, ft);
        }
    }

    // NetDbListFragment.OnEntrySelectedListener

    public void onEntrySelected(boolean isRouterInfo, Hash entryHash) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            NetDbDetailFragment detailFrag = NetDbDetailFragment.newInstance(
                    isRouterInfo, entryHash);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment, detailFrag).commit();

            // If we are coming from a LS to a RI, change the tab
            int currentTab = getSupportActionBar().getSelectedNavigationIndex();
            if (isRouterInfo && currentTab !=1)
                getSupportActionBar().setSelectedNavigationItem(1);
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, NetDbDetailActivity.class);
            detailIntent.putExtra(NetDbDetailFragment.IS_RI, isRouterInfo);
            detailIntent.putExtra(NetDbDetailFragment.ENTRY_HASH,
                    entryHash.toBase64());
            startActivity(detailIntent);
        }
    }
}