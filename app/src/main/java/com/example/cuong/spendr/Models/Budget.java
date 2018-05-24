package com.example.cuong.spendr.Models;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This is class encapsulates the data for a Budget.
 *
 * A transaction has the following:
 * Start Date
 * Start Balance
 * Current Balance
 *
 */

public class Budget implements Serializable {
    String startDate;
    Double startBalance;
    Double currentBalance;

    public Budget() {
        // Required no arg constructor for Firebase.
    }

    public Budget (Double balance) {
        Calendar calendar = Calendar.getInstance();
        this.startDate = calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);
        this.startBalance = balance;
        this.currentBalance = balance;
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public Double getStartBalance() { return startBalance; }
    public void setStartBalance(Double startBalance) { this.startBalance = startBalance; }

    public Double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Double currentBalance) { this.currentBalance = currentBalance; }
}
