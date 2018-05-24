package com.example.cuong.spendr.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.ToDoubleFunction;
import com.example.cuong.spendr.Database.TransactionVault;
import com.example.cuong.spendr.Fragments.ChartFragments.MonthlyExpensePieChartFragment;
import com.example.cuong.spendr.Fragments.ChartFragments.YearlyExpenseBarChartFragment;
import com.example.cuong.spendr.HomeActivity;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.text.DateFormatSymbols;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment is where we display our charts and summary data.
 * This is a ViewPager so users can scroll through the various charts.
 *
 * The charts are created using the external library MPAndroidChart
 * Source: https://github.com/PhilJay/MPAndroidChart *
 */

public class DashboardViewPagerFragment extends Fragment {
    private ViewPager mDashboardViewPager;

    public DashboardViewPagerFragment() {
        // Required empty public constructor
    }

    public static DashboardViewPagerFragment newInstance() {
        DashboardViewPagerFragment fragment = new DashboardViewPagerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_view_pager, container, false);

        mDashboardViewPager = view.findViewById(R.id.dashboard_view_pager);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        mDashboardViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        ((HomeActivity) getActivity()).showChangeYearOptionsMenu();
                        fragment = MonthlyExpensePieChartFragment.newInstance();
                        break;
                    case 1:
                        fragment = YearlyExpenseBarChartFragment.newInstance();
                        break;
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        mDashboardViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        ((HomeActivity) getActivity()).showChangeYearOptionsMenu();
                        getActivity().setTitle(R.string.dashboard_title_monthly_expense);
                        break;
                    case 1:
                        ((HomeActivity) getActivity()).hideChangeYearOptionsMenu();
                        getActivity().setTitle(R.string.dashboard_title_yearly_expense);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
