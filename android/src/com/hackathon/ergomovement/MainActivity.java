package com.hackathon.ergomovement;

import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    private ServiceManager mService;
    private static TextView mStatus;

    private Handler mResponseHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            Log.d(TAG, "Handling messages");

            switch (msg.what) {
                case Magic.STATUS_SITTING:
                    mStatus.setText("SITTING");
                    Log.d(TAG, "SITTING");
                    break;
                case Magic.STATUS_FUCKEDUP:
                    mStatus.setText("FUCKEDUP");
                    Log.d(TAG, "FUCKEDUP");
                    break;
                case Magic.STATUS_JUMPING:
                    mStatus.setText("JUMPING");
                    Log.d(TAG, "JUMPING");
                    break;
                default:
                    break;
            }

        }
    };

    public void startServiceManager() {
        mService.start();
    }

    public void stopServiceManager() {
        mService.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mService.unbind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        mService = new ServiceManager(this, AnalyzerService.class, mResponseHandler);
//        startServiceManager();

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            switch (position) {
                case 0:
                    fragment = new MainFragment();
                    //                    Bundle args = new Bundle();
                    //                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                    //                    fragment.setArguments(args);
                    break;
                case 1:
                    fragment = new TimerFragment();
                    break;
                case 2:
                    fragment = new ModeFragment();
                    break;
                default:
                    throw new IllegalArgumentException("Wrong tab specified");
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class MainFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_tab, container, false);
            Switch work = (Switch) rootView.findViewById(R.id.switch1);
            mStatus = (TextView) rootView.findViewById(R.id.status);

            work.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Intent service = new Intent(buttonView.getContext(), AnalyzerService.class);
//                        buttonView.getContext().startService(service);
                        ((MainActivity) getActivity()).startServiceManager();
                    } else {
                        Intent service = new Intent(buttonView.getContext(), AnalyzerService.class);
//                        buttonView.getContext().stopService(service);
                        ((MainActivity) getActivity()).stopServiceManager();
                    }
                    Log.d("TEST", Boolean.toString(isChecked));

                }
            });
            return rootView;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class TimerFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public TimerFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.timer_tab, container, false);
            final CheckBox remindMe = (CheckBox) rootView.findViewById(R.id.checkBox1);
            final Spinner interval = (Spinner) rootView.findViewById(R.id.spinner1);
            remindMe.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (remindMe.isChecked()) {
                        interval.setVisibility(View.VISIBLE);
                    } else {
                        interval.setVisibility(View.GONE);
                    }
                }
            });
            return rootView;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class ModeFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public ModeFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mode_tab, container, false);
            SeekBar mode = (SeekBar) rootView.findViewById(R.id.seekBar1);
            mode.setMax(2);
            mode.incrementProgressBy(1);

            return rootView;
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

}
