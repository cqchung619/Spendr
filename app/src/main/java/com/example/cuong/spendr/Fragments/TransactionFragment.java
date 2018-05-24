package com.example.cuong.spendr.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.cuong.spendr.HomeActivity;
import com.example.cuong.spendr.R;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.Database.TransactionVault;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment is where the user:
 * - Enters transaction data to create a new transaction or
 * - Views a transaction or
 * - Edits a transaction or
 * - Delete a transaction
 *
 */

public class TransactionFragment extends Fragment {
    private static final String ARG_TRANSACTION = "transaction";
    private EditText mDateEditText;
    private EditText mCategoryEditText;
    private EditText mItemDescriptionEditText;
    private EditText mAmountEditText;
    private FloatingActionButton mFloatingActionButtonSave;

    private Transaction mTransaction;

    public static TransactionFragment newInstance(Transaction transaction) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);

        return fragment;
    }

    public TransactionFragment() {
        // Required default constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransaction = (Transaction) getArguments().getSerializable(ARG_TRANSACTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        mDateEditText= view.findViewById(R.id.edit_text_date);
        mCategoryEditText = view.findViewById(R.id.edit_text_category);
        mItemDescriptionEditText = view.findViewById(R.id.edit_text_item_description);
        mAmountEditText = view.findViewById(R.id.edit_text_amount);
        mFloatingActionButtonSave = view.findViewById(R.id.floating_button_transaction_add_confirm);

        mDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mFloatingActionButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    String newDate = mDateEditText.getText().toString();
                    String newCategory = mCategoryEditText.getText().toString();
                    String newItemDescription = mItemDescriptionEditText.getText().toString();
                    double newAmount = Double.valueOf(mAmountEditText.getText().toString());

                    Transaction newTransaction = new Transaction(newCategory, newItemDescription, newAmount, newDate);
                    TransactionVault.getInstance().add(TransactionVault.TRANSACTIONS, newTransaction);

                    // Reset host activity's floating button and options menu
                    ((HomeActivity) getActivity()).showFloatingButton();
                    ((HomeActivity) getActivity()).showSettingsOptionsMenu();
                    mFloatingActionButtonSave.setVisibility(View.INVISIBLE);

                    getActivity().setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment.newInstance())
                            .commit();
                } else {
                    TransactionVault.getInstance();
                }
            }
        });

        // This fragment was created from TransactionViewPagerFragment.
        // We update the view with the transactions' data and set the floating button listener to update
        // the data in Firebase instead of adding a new entry.
        if (mTransaction != null) {
            ((HomeActivity) getActivity()).showDeleteTransactionOptionsMenu();
            mDateEditText.setText(mTransaction.getDate());
            mCategoryEditText.setText(mTransaction.getCategory());
            mItemDescriptionEditText.setText(mTransaction.getItemDescription());
            mAmountEditText.setText(String.format(Locale.US, "%.2f", mTransaction.getAmount()));

            mFloatingActionButtonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTransaction.setDate(mDateEditText.getText().toString());
                    mTransaction.setCategory(mCategoryEditText.getText().toString());
                    mTransaction.setItemDescription(mItemDescriptionEditText.getText().toString());
                    mTransaction.setAmount(Double.valueOf(mAmountEditText.getText().toString()));

                    TransactionVault.getInstance().update(TransactionVault.TRANSACTIONS, mTransaction);

                    mTransaction = null;
                    ((HomeActivity) getActivity()).hideDeleteTransactionOptionsMenu();
                    getActivity().setTitle(R.string.tab_transaction_list);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, TransactionListFragment.newInstance())
                            .commit();
                }
            });
        }

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

    // Displays a date picker dialog for the user to enter a date.
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (mTransaction != null) {
            String date = mTransaction.getDate();
            year = Integer.valueOf(date.substring(date.lastIndexOf('/') + 1, date.length()));
            month = Integer.valueOf(date.substring(0, date.indexOf('/'))) - 1;
            day = Integer.valueOf(date.substring(date.indexOf('/') + 1, date.lastIndexOf('/')));
        }

        // Create a DatePickerDialog and display it.
        DatePickerDialog datePickerDialog =
                // args to DatePickerDialog: context, OnDateSetListener, YEAR, MONTH, DAY_OF_MONTH.
                new DatePickerDialog(
                        getActivity(), // Context
                        new DatePickerDialog.OnDateSetListener() { // Listener
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                                mDateEditText.setText(date);

                                View currentView = getActivity().getCurrentFocus();
                                if (currentView != null) {
                                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }
                        },
                        year, // YEAR
                        month, // MONTH
                        day // DAY_OF_MONTH
                );
        datePickerDialog.setTitle(getString(R.string.fragment_transaction_date_dialog_title));
        datePickerDialog.show();
    }

    private boolean validateInputs() {
        boolean valid = true;

        if (TextUtils.isEmpty(mDateEditText.getText())) { // Validate date
            mDateEditText.setError("Required");
            valid = false;
        }
        if (TextUtils.isEmpty(mCategoryEditText.getText())) { // Validate category
            mCategoryEditText.setError("Required");
            valid = false;
        }
        if (TextUtils.isEmpty(mItemDescriptionEditText.getText())) { // Validate item description
            mItemDescriptionEditText.setError("Required");
            valid = false;
        }

        if (TextUtils.isEmpty(mAmountEditText.getText())) { // Validate amount
            mAmountEditText.setError("Required");
            valid = false;
        }

        return valid;
    }
}
