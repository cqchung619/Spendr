package com.example.cuong.spendr.Fragments.ChartFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.ToDoubleFunction;
import com.example.cuong.spendr.Database.TransactionVault;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment contains a Bar Chart displaying yearly expenses.
 *
 * The charts are created using the external library MPAndroidChart
 * Source: https://github.com/PhilJay/MPAndroidChart
 *
 * The chart setups were adapted from the examples provided by the creator of MPAndroidChart.
 */

public class YearlyExpenseBarChartFragment extends Fragment {
    private BarChart mYearlyExpenseBarChart;

    public YearlyExpenseBarChartFragment() {
        // Required empty public constructor
    }

    public static YearlyExpenseBarChartFragment newInstance() {
        return new YearlyExpenseBarChartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_yearly_expense_bar_chart, container, false);

        mYearlyExpenseBarChart = view.findViewById(R.id.dashboard_bar_chart);
        setupHorizontalBarChart();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.home_menu_set_budget).setVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setupHorizontalBarChart() {
        mYearlyExpenseBarChart.getDescription().setEnabled(false);
        mYearlyExpenseBarChart.setDrawGridBackground(false);
        mYearlyExpenseBarChart.setDrawBarShadow(false);

        Map<Pair<Integer, String>, Double> yearlyExpenses = getMonthlyExpensesByYear();
        Map<Integer, Double> yearlyExpensesMap = new HashMap<>();
        for (Map.Entry<Pair<Integer, String>, Double> entry : yearlyExpenses.entrySet()) {
            if (!yearlyExpensesMap.containsKey(entry.getKey().first)) {
                yearlyExpensesMap.put(entry.getKey().first, entry.getValue());
            } else {
                yearlyExpensesMap.put(entry.getKey().first, yearlyExpensesMap.get(entry.getKey().first) + entry.getValue());
            }
        }

        XAxis xAxis = mYearlyExpenseBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(yearlyExpensesMap.size());
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format(Locale.US, "%.0f", value);
            }
        });

        Map.Entry<Integer, Double> maxExpense = Stream.of(yearlyExpensesMap).max(new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        }).get();

        YAxis yAxis = mYearlyExpenseBarChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMaximum(maxExpense.getValue().floatValue() + 200);
        yAxis.setLabelCount(10);
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format(Locale.US, "$ %.0f", value);
            }
        });

        mYearlyExpenseBarChart.getAxisRight().setEnabled(false);
        mYearlyExpenseBarChart.getLegend().setEnabled(false);

        List < BarEntry > yearlyExpenseBarEntries = new ArrayList<>();
        for (Map.Entry<Integer, Double> pair : yearlyExpensesMap.entrySet()) {
            yearlyExpenseBarEntries.add(new BarEntry(pair.getKey(), pair.getValue().floatValue()));
        }

        BarDataSet yearlyExpenseBarDataSet = new BarDataSet(yearlyExpenseBarEntries, "Budget");
        yearlyExpenseBarDataSet.setColors(ColorTemplate.PASTEL_COLORS);

        List<IBarDataSet> yearlyExpenseIBarDataSet = new ArrayList<>();
        yearlyExpenseIBarDataSet.add(yearlyExpenseBarDataSet);

        BarData yearlyExpenseBarData = new BarData(yearlyExpenseIBarDataSet);
        yearlyExpenseBarData.setValueTextSize(10f);
        //yearlyExpenseBarData.setBarWidth(4f);

        mYearlyExpenseBarChart.setData(yearlyExpenseBarData);
        mYearlyExpenseBarChart.setFitBars(true);
        mYearlyExpenseBarChart.animateY(2500);
    }

    private Map<Pair<Integer, String>, Double> getMonthlyExpensesByYear() {
        List<Transaction> transactions = TransactionVault.getInstance().getSortedTransactionsList();
        return Stream.of(transactions).collect(
                Collectors.groupingBy(
                        new Function<Transaction, Pair<Integer, String>>() {
                            @Override
                            public Pair<Integer, String> apply(Transaction transaction) {
                                int year = transaction.getYear();
                                String month = DateFormatSymbols.getInstance().getMonths()[transaction.getMonth()];

                                return new Pair<>(year, month);
                            }
                        }, Collectors.summingDouble(
                                new ToDoubleFunction<Transaction>() {
                                    @Override
                                    public double applyAsDouble(Transaction transaction) {
                                        return transaction.getAmount();
                                    }
                                }
                        )
                )
        );
    }
}
