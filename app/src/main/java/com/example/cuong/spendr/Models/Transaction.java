package com.example.cuong.spendr.Models;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.Objects;

/**
 * Author: Cuong Chung
 * Class: CSCI 39585 Android Application Development
 * Professor: Anna Wisniewska
 *
 * This is class encapsulates the data for a single Transaction.
 *
 * A transaction has the following:
 * Date
 * Category
 * Item Description
 * Amount
 *
 */

public class Transaction implements Serializable {
    public static final String DATE_FORMAT_VERTICAL = "VERTICAL";
    public static final String DATE_FORMAT_HORIZONTAL = "HORIZONTAL";
    private String date;
    private String category;
    private String itemDescription;
    private double amount;

    private static final String mDateFormat = "MM/dd/yyyy";

    public Transaction() {
        // Required no arg constructor for Firebase.
    }

    public Transaction(String category, String itemDescription, double amount, String date) {
        this.date = date;
        this.category = category;
        this.itemDescription = itemDescription;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getDate(String format) {
        int monthNumber = Integer.valueOf(date.substring(0, date.indexOf('/'))) - 1;
        String month = DateFormatSymbols.getInstance().getMonths()[monthNumber];
        String day = date.substring(date.indexOf('/') + 1, date.lastIndexOf('/'));
        String year = date.substring(date.lastIndexOf('/') + 1, date.length());

        if (format == DATE_FORMAT_HORIZONTAL) {
            return month + " " + day + ", " + year;
        } else if (format == DATE_FORMAT_VERTICAL) {
            return month + "\n" + day + "\n" + year;
        } else {
            return date;
        }
    }

    public int getMonth() {
        return Integer.valueOf(date.substring(0, date.indexOf('/'))) - 1;
    }

    public int getDay() {
        return Integer.valueOf(date.substring(date.indexOf('/') + 1, date.lastIndexOf('/')));
    }

    public int getYear() {
        return Integer.valueOf(date.substring(date.lastIndexOf('/') + 1, date.length()));
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getItemDescription() {
        return itemDescription;
    }
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(date, that.date) &&
                Objects.equals(category, that.category) &&
                Objects.equals(itemDescription, that.itemDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, category, itemDescription, amount);
    }

    @Override
    public String toString() {
        StringBuilder transaction = new StringBuilder();
        transaction.append(this.date);
        transaction.append(",");
        transaction.append(this.category);
        transaction.append(",");
        transaction.append(this.itemDescription);
        transaction.append(",");
        transaction.append(this.amount);

        return transaction.toString();
    }
}
