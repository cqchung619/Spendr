package com.example.cuong.spendr.Fragments;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuong.spendr.HomeActivity;
import com.example.cuong.spendr.R;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.Database.TransactionVault;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment is where users see all their transactions in list form. The transactions are sorted
 * by date and amount.
 *
 * We use a RecyclerView to provide a more efficient use of the list ViewHolder items.
 *
 * We define the custom adapter and view holder as inner classes.
 */

public class TransactionListFragment extends Fragment implements TransactionVault.BudgetDataListener {
    private RecyclerView mRecyclerView;
    private TransactionAdapter mAdapter;
    private Button mNoTransactionsButton;

    public TransactionListFragment() {
        // Required empty public constructor
    }

    public static TransactionListFragment newInstance() {
        TransactionListFragment fragment = new TransactionListFragment();
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
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        TransactionVault.getInstance().addBudgetDataListener(this);

        mRecyclerView = view.findViewById(R.id.fragment_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNoTransactionsButton = view.findViewById(R.id.fragment_transaction_list_button);
        mNoTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, TransactionFragment.newInstance(null))
                        .commit();
                getActivity().setTitle(R.string.tab_transaction_list);
                return;
            }
        });

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.home_menu_filter_year).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu_filter_year:
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

    private void updateUI() {
        List<Transaction> transactions = new ArrayList<>(TransactionVault.getInstance().getSortedTransactionsList());
        updateViews(transactions);
    }

    private void updateUI(int year) {
        List<Transaction> transactions = new ArrayList<>(TransactionVault.getInstance().getSortedTransactionsList());
        List<Transaction> toRemove = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getYear() != year) {
                toRemove.add(transaction);
            }
        }
        transactions.removeAll(toRemove);

        updateViews(transactions);
    }

    private void updateViews(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mNoTransactionsButton.setVisibility(View.VISIBLE);
        } else {
            mNoTransactionsButton.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mAdapter = new TransactionAdapter(transactions);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public void showYearInputAlertDialog() {
        final LinearLayout layout = new LinearLayout(getActivity());
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        final CheckBox reset = new CheckBox(getActivity());
        reset.setText("Reset");

        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        layout.addView(reset);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_year)
                .setView(layout)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText())) {
                            if (reset.isChecked()) {
                                updateUI();
                            } else {
                                dialog.cancel();
                            }
                        } else {
                            int year = Integer.valueOf(input.getText().toString());
                            if (reset.isChecked()) {
                                updateUI();
                            } else {
                                updateUI(year);
                            }
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

    @Override
    public void onDataChanged() {
        updateUI();
    }

    /***********************************************************************************************
     * Custom RecyclerView Adapter
     ***********************************************************************************************/
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {
        private List<Transaction> mTransactions;

        public TransactionAdapter(List<Transaction> transactions) {
            mTransactions = transactions;
        }

        @Override
        public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_transaction_list, parent, false);

            return new TransactionHolder(view);
        }

        @Override
        public void onBindViewHolder(TransactionHolder holder, int position) {
            Transaction transaction = mTransactions.get(position);
            holder.bindTransaction(transaction);
        }

        @Override
        public int getItemCount() {
            return mTransactions.size();
        }

        public void setTransactions(List<Transaction> transactions) {
            mTransactions = transactions;
        }
    }

    /***********************************************************************************************
     * Custom RecyclerView ViewHolder
     ***********************************************************************************************/
    private class TransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Transaction mTransaction;
        private TextView mDateTextView;
        private TextView mCategoryTextView;
        private TextView mItemDescriptionTextView;
        private TextView mAmountTextView;

        public TransactionHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mDateTextView = itemView.findViewById(R.id.list_item_date_text_view);
            mCategoryTextView = itemView.findViewById(R.id.list_item_category_text_view);
            mItemDescriptionTextView = itemView.findViewById(R.id.list_item_item_description_text_view);
            mAmountTextView = itemView.findViewById(R.id.list_item_amount_text_view);
        }

        // Update the view based on the transaction data.
        public void bindTransaction(Transaction transaction) {
            mTransaction = transaction;

            String date = mTransaction.getDate(Transaction.DATE_FORMAT_VERTICAL);
            mDateTextView.setText(date);
            mCategoryTextView.setText(mTransaction.getCategory());
            mItemDescriptionTextView.setText(mTransaction.getItemDescription());

            Double amount = mTransaction.getAmount();
            if (amount < 0) {
                mAmountTextView.setTextColor(Color.GREEN);
            } else {
                mAmountTextView.setTextColor(Color.RED);
            }
            mAmountTextView.setText(String.format(Locale.US, "%.2f", amount));
        }

        // Switches in the TransactionViewPagerFragment. This FragmentTransaction will be placed on the
        // backstack so we can return back here upon back button pressed.
        @Override
        public void onClick(View v) {
            HomeActivity host = (HomeActivity) getActivity();
            host.hideFloatingButton();
            host.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            host.getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, TransactionViewPagerFragment.newInstance(mTransaction), TransactionViewPagerFragment.TAG_TRANSACTION_VIEW_PAGER)
                    .commit();
        }
    }
}
