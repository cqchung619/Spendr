package com.example.cuong.spendr.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.annimon.stream.function.ToDoubleFunction;
import com.example.cuong.spendr.Models.Budget;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.R;
import com.example.cuong.spendr.Database.TransactionVault;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This is the HomeFragment.
 *
 * The remaining budget balance for the current month is displayed with a circular progress bar.
 * Users can add a transaction by pressing the floating button.
 *
 */

public class HomeFragment extends Fragment implements TransactionVault.BudgetDataListener {
    private ProgressBar mProgressBar;
    private TextView mMonthTextView;
    private TextView mBudgetTextView;
    private Budget mBudget;

    private List<HomeFragmentListener> mListeners;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required default public constructor
        mListeners = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mProgressBar = view.findViewById(R.id.circle_progress_bar);
        mMonthTextView = view.findViewById(R.id.fragment_home_month_text_view);
        mBudgetTextView = view.findViewById(R.id.fragment_home_budget_text_view);

        mBudgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (HomeFragmentListener listener : mListeners) {
                    listener.onFragmentInteraction();
                }
            }
        });

        TransactionVault.getInstance().addBudgetDataListener(this);
        updateUI();

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

    @Override
    public void onDataChanged() {
        updateUI();
    }

    public void addHomeFragmentListener(HomeFragmentListener listener) {
        mListeners.add(listener);
    }

    public interface HomeFragmentListener {
        void onFragmentInteraction();
    }

    private void updateUI() {
        if (TransactionVault.getInstance().isSetBudget()) {
            mBudget = TransactionVault.getInstance().getBudget();
            Calendar calendar = Calendar.getInstance();
            final int month = calendar.get(Calendar.MONTH);

            Double totalExpense = Stream.of(TransactionVault.getInstance().getSortedTransactionsList())
                    .filter(new com.annimon.stream.function.Predicate<Transaction>() {
                        @Override
                        public boolean test(Transaction value) {
                            return value.getMonth() == month;
                        }
                    })
                    .mapToDouble(new ToDoubleFunction<Transaction>() {
                        @Override
                        public double applyAsDouble(Transaction transaction) {
                            return transaction.getAmount();
                        }
                    })
                    .sum();

            String monthString = DateFormatSymbols.getInstance().getMonths()[month];
            Double remainingBalance = mBudget.getStartBalance() - totalExpense;

            mMonthTextView.setText(String.format(Locale.US, "%s's remaining balance:", monthString));
            mBudgetTextView.setText(String.format(Locale.US, "$%.2f", remainingBalance));
            if (remainingBalance < 0) {
                mBudgetTextView.setTextColor(Color.RED);
            } else {
                mBudgetTextView.setTextColor(Color.BLACK);
            }

            Double progress = remainingBalance / mBudget.getStartBalance() * 100;
            mProgressBar.setProgress(progress.intValue());
        }
    }
}
