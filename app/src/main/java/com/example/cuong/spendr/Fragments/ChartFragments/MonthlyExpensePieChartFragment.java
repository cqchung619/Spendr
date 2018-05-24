package com.example.cuong.spendr.Fragments.ChartFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.ToDoubleFunction;
import com.example.cuong.spendr.Database.TransactionVault;
import com.example.cuong.spendr.Models.Budget;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment contains a Pie Chart displaying the monthly expenses for the current year.
 *
 * The charts are created using the external library MPAndroidChart
 * Source: https://github.com/PhilJay/MPAndroidChart
 *
 * The chart setups were adapted from the examples provided by the creator of MPAndroidChart.
 */

public class MonthlyExpensePieChartFragment extends Fragment {
    private PieChart mMonthlyExpensePieChart;

    public MonthlyExpensePieChartFragment() {
        // Required empty public constructor
    }

    public static MonthlyExpensePieChartFragment newInstance() {
        MonthlyExpensePieChartFragment fragment = new MonthlyExpensePieChartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monthly_expense_pie_chart, container, false);

        mMonthlyExpensePieChart = view.findViewById(R.id.dashboard_pie_chart);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        setupPieChart(year);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.home_menu_set_budget).setVisible(false);
        menu.findItem(R.id.home_menu_change_year).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu_change_year:
                showYearInputAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setupPieChart(int year) {
        mMonthlyExpensePieChart.getDescription().setEnabled(false);
        mMonthlyExpensePieChart.setExtraOffsets(5, 10, 5, 5);
        mMonthlyExpensePieChart.setDragDecelerationFrictionCoef(0.95f);
        mMonthlyExpensePieChart.setRotationEnabled(false);
        mMonthlyExpensePieChart.setCenterText(String.format(Locale.US,"Monthly Expenses\nfor %d", year));
        mMonthlyExpensePieChart.setCenterTextColor(Color.BLUE);
        mMonthlyExpensePieChart.setDrawHoleEnabled(true);
        mMonthlyExpensePieChart.setEntryLabelColor(Color.BLACK);
        mMonthlyExpensePieChart.setHoleColor(android.R.color.primary_text_light);
        mMonthlyExpensePieChart.setHoleRadius(58f);
        mMonthlyExpensePieChart.setTransparentCircleRadius(61f);
        mMonthlyExpensePieChart.setDrawCenterText(true);
        mMonthlyExpensePieChart.animateY(2500);

        Legend legend = mMonthlyExpensePieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);

        Map<Pair<Integer, String>, Double> monthlyExpensesByYear = getMonthlyExpensesByYear();

        List<PieEntry> monthlyExpensePieEntries = new ArrayList<>();
        for (Map.Entry<Pair<Integer, String>, Double> pair : monthlyExpensesByYear.entrySet()) {
            if (pair.getKey().first == year) {
                monthlyExpensePieEntries.add(new PieEntry(pair.getValue().floatValue(), pair.getKey().second));
            }
        }

        if (monthlyExpensePieEntries.isEmpty()) {
            mMonthlyExpensePieChart.setCenterText(String.format(Locale.US,"No Expenses\nin %d", year));
        }

        PieDataSet monthlyExpensePieDataSet = new PieDataSet(monthlyExpensePieEntries, "Months");
        monthlyExpensePieDataSet.setSliceSpace(2f);
        monthlyExpensePieDataSet.setSelectionShift(5f);
        monthlyExpensePieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        PieData monthlyExpensePieData = new PieData(monthlyExpensePieDataSet);
        monthlyExpensePieData.setValueTextSize(10f);
        monthlyExpensePieData.setValueTextColor(Color.BLUE);

        mMonthlyExpensePieChart.setData(monthlyExpensePieData);
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

    public void showYearInputAlertDialog() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_year)
                .setView(input)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText())) {
                            dialog.cancel();
                        } else {
                            int year = Integer.valueOf(input.getText().toString());
                            setupPieChart(year);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
