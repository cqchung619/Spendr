package com.example.cuong.spendr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cuong.spendr.Database.TransactionVault;
import com.example.cuong.spendr.Fragments.DashboardViewPagerFragment;
import com.example.cuong.spendr.Fragments.HomeFragment;
import com.example.cuong.spendr.Fragments.TransactionFragment;
import com.example.cuong.spendr.Fragments.TransactionListFragment;
import com.example.cuong.spendr.Fragments.TransactionViewPagerFragment;
import com.example.cuong.spendr.Models.Budget;
import com.example.cuong.spendr.Models.Transaction;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This is the Home Activity. It serves as the host for all the fragments and is the only Activity
 * besides the Login Activity.
 *
 * This activity should only be started with a userId passed in through the intent.
 *
 * Aside from managing the fragments, it manages the ActionBar and the BottomNavigation.
 * Based on user interaction, option menu items will be shown or hidden.
 *
 * Based on the selection in the bottom navigation, the user will be presented with the appropriate
 * fragment.
 *
 * Option menu item functionalities are handled here.
 */

public class HomeActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener {
    private static final int WRITE_EXTERNAL_REQUEST_CODE = 1;
    private static final String ARG_USER_ID = "com.example.cuong.spendr.userid";
    private FloatingActionButton mFloatingActionButton; // This will initiate the add transaction sequence.
    private BottomNavigationView mBottomNavigation; // Navigation between Home, Transaction List, and Dashboard fragments.

    private Menu mOptionsMenu;
    private String mUserID;

    // Bottom navigation item selection listener.
    // Replaces the current fragment with the selected fragment.
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (mFloatingActionButton.getVisibility() == View.INVISIBLE) {
                showFloatingButton();
            }

            if (!mOptionsMenu.findItem(R.id.home_menu_set_budget).isVisible()) {
                showSettingsOptionsMenu();
            }

            if (mOptionsMenu.findItem(R.id.home_menu_delete).isVisible()) {
                hideDeleteTransactionOptionsMenu();
            }

            if (mOptionsMenu.findItem(R.id.home_menu_change_year).isVisible()) {
                hideChangeYearOptionsMenu();
            }

            if (mOptionsMenu.findItem(R.id.home_menu_filter_year).isVisible()) {
                mOptionsMenu.findItem(R.id.home_menu_filter_year).setVisible(false);
            }

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportFragmentManager().popBackStack();
            }

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    HomeFragment fragment = HomeFragment.newInstance();
                    fragment.addHomeFragmentListener(HomeActivity.this);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                    setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    return true;
                case R.id.navigation_transaction_list:
                    hideFloatingButton();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, TransactionListFragment.newInstance()).commit();
                    setTitle(R.string.tab_transaction_list);
                    return true;
                case R.id.navigation_dashboard:
                    hideSettingsOptionsMenu();
                    hideFloatingButton();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, DashboardViewPagerFragment.newInstance()).commit();
                    return true;
            }
            return false;
        }
    };

    public static Intent newIntent(Context packageContext, String userID) {
        Intent intent = new Intent(packageContext, HomeActivity.class);
        intent.putExtra(ARG_USER_ID, userID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mUserID = getIntent().getStringExtra(ARG_USER_ID);
        TransactionVault.getInstance().setContext(this).connect(mUserID);
        setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFloatingActionButton = findViewById(R.id.floating_button_add_transaction);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, TransactionFragment.newInstance(null))
                        .commit();
                setTitle(R.string.fragment_transaction_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                hideFloatingButton();
                hideSettingsOptionsMenu();
            }
        });

        HomeFragment fragment = HomeFragment.newInstance();
        fragment.addHomeFragmentListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home_activity_menu, menu);
        mOptionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStackImmediate();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                handleBackPress();
                return true;
            case R.id.home_menu_set_budget:
                showInputAlertDialog();
                return true;
            case R.id.home_menu_change_year:
                return false;
            case R.id.home_menu_backup:
                startExportSequence();
                return true;
            case R.id.home_menu_delete:
                TransactionViewPagerFragment transactionViewPagerFragment = (TransactionViewPagerFragment) getSupportFragmentManager().findFragmentByTag(TransactionViewPagerFragment.TAG_TRANSACTION_VIEW_PAGER);
                Transaction transaction = transactionViewPagerFragment.getTransaction();

                if (transaction != null) {
                    TransactionVault.getInstance().delete(TransactionVault.TRANSACTIONS, transaction);
                    Toast.makeText(this, R.string.toast_transaction_deleted, Toast.LENGTH_SHORT).show();

                    getSupportFragmentManager().popBackStackImmediate();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }

                hideDeleteTransactionOptionsMenu();
                return true;
            case R.id.home_menu_sign_out:
                LoginActivity.signOut();
                Intent intentToStartLoginActivity = new Intent(this, LoginActivity.class);
                startActivity(intentToStartLoginActivity);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportActionBar().getDisplayOptions() != 0 && ActionBar.DISPLAY_HOME_AS_UP != 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        handleBackPress();
    }

    @Override
    public void onFragmentInteraction() {
        showInputAlertDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportToLocalExternalStorage();
                }
                return;
        }
    }

    public void showChangeYearOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_change_year).setVisible(true);
    }

    public void hideChangeYearOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_change_year).setVisible(false);
    }

    public void showDeleteTransactionOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_delete).setVisible(true);
    }

    public void hideDeleteTransactionOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_delete).setVisible(false);
    }

    public void showSettingsOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_set_budget).setVisible(true);
    }

    public void hideSettingsOptionsMenu() {
        mOptionsMenu.findItem(R.id.home_menu_set_budget).setVisible(false);
    }

    public void showFloatingButton() {
        mFloatingActionButton.setVisibility(View.VISIBLE);
    }

    public void hideFloatingButton() {
        mFloatingActionButton.setVisibility(View.INVISIBLE);
    }

    // Alert Dialog letting the user set a new budget.
    public void showInputAlertDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_budget)
                .setView(input)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText())) {
                            dialog.cancel();
                        } else {
                            Budget budget = new Budget(Double.valueOf(input.getText().toString()));
                            saveBudgetToDatabase(budget);
                            Toast.makeText(HomeActivity.this, "Budget Saved.", Toast.LENGTH_SHORT).show();
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

    private void handleBackPress() {
        if (mOptionsMenu.findItem(R.id.home_menu_delete).isVisible()) {
            setTitle(R.string.tab_transaction_list);
            hideDeleteTransactionOptionsMenu();
        } else {
            setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            mOptionsMenu.findItem(R.id.home_menu_set_budget).setVisible(true);
            showFloatingButton();
        }
    }

    // Helper function to save budget to Firebase.
    private void saveBudgetToDatabase(Budget budget) {
        TransactionVault.getInstance().add(TransactionVault.BUDGET, budget);
    }

    // Shows Alert Dialog letting user select an export location.
    // Checks for Storage permission.
    private void startExportSequence() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_export_location)
                .setItems(R.array.dialog_array_export_locations, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isExternalStorageWritable()) {
                            switch (which) {
                                case 0:
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkForStoragePermission();
                                    } else {
                                        exportToLocalExternalStorage();
                                    }
                                    break;
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "External storage not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();

    }

    // Checks if external storage is mounted.
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Checks if Storage permission granted. If not, request it.
    private void checkForStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    // Retrieves transaction list from TransactionVault and writes the data into a csv file to the
    // local external storage.
    private void exportToLocalExternalStorage() {
        File spendrAppDirectory = new File(Environment.getExternalStorageDirectory(), "Spendr");
        if (!spendrAppDirectory.exists()) {
            System.out.println(spendrAppDirectory.mkdir());
        }

        System.out.println(spendrAppDirectory.toString());

        StringBuilder dateTime = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        dateTime.append(calendar.get(Calendar.MONTH) + 1);
        dateTime.append("-");
        dateTime.append(calendar.get(Calendar.DAY_OF_MONTH));
        dateTime.append("-");
        dateTime.append(calendar.get(Calendar.YEAR));
        dateTime.append("_");
        dateTime.append(calendar.getTimeInMillis());

        String exportDataFilename = dateTime.toString() + ".csv";
        File exportFile = new File(spendrAppDirectory.getPath(), exportDataFilename);
        try {
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(exportFile, false);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("date,category,item_description,amount");
            for (Transaction transaction : TransactionVault.getInstance().getSortedTransactionsList()) {
                printWriter.println(transaction.toString());
            }
            printWriter.flush();
            printWriter.close();
            outputStream.close();

            Toast.makeText(this, "Exported data to local storage", Toast.LENGTH_SHORT).show();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
