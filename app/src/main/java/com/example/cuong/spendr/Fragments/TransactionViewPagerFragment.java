package com.example.cuong.spendr.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cuong.spendr.R;
import com.example.cuong.spendr.Models.Transaction;
import com.example.cuong.spendr.Database.TransactionVault;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This fragment is where the user views a single transaction they clicked on.
 * They can scroll left or right to look through their transactions.
 *
 * Transactions can be edited or deleted.
 */

public class TransactionViewPagerFragment extends Fragment {
    public static final String TAG_TRANSACTION_VIEW_PAGER = "tranaction viewing state";
    private static final String ARG_TRANSACTION = "transaction";
    private ViewPager mTransactionViewPager;
    private List<Transaction> mTransactions;

    private Transaction mTransaction;

    public TransactionViewPagerFragment() {
        // Required empty public constructor
    }

    public static TransactionViewPagerFragment newInstance(Transaction transaction) {
        System.out.println("newInstance VP: " + transaction.toString());
        TransactionViewPagerFragment fragment = new TransactionViewPagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransaction = (Transaction) getArguments().getSerializable(ARG_TRANSACTION);
        System.out.println("onCreate VP: " + mTransaction.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction_view_pager, container, false);

        mTransactions = new ArrayList<>(TransactionVault.getInstance().getSortedTransactionsList());

        mTransactionViewPager = view.findViewById(R.id.fragment_transaction_view_pager);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        mTransactionViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Transaction transaction = mTransactions.get(position);
                return TransactionFragment.newInstance(transaction);
            }

            @Override
            public int getCount() {
                return mTransactions.size();
            }
        });

        mTransactionViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Transaction transaction = mTransactions.get(position);
                getActivity().setTitle(transaction.getDate(Transaction.DATE_FORMAT_HORIZONTAL));
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        for (int i = 0; i < mTransactions.size(); i++) {
            if (mTransactions.get(i).equals(mTransaction)) {
                System.out.println("onCreateView VP: " + mTransactions.get(i).toString());
                mTransactionViewPager.setCurrentItem(i);
                getActivity().setTitle(mTransaction.getDate(Transaction.DATE_FORMAT_HORIZONTAL));
                break;
            }
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

    public Transaction getTransaction() {
        return mTransaction;
    }
}
