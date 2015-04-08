package com.mathck.android.wearable.stockmonitor;

public class Stock {

    private float stockPrice;
    private String stockCurrency;
    private float stockPerformance;
    private String stockName;
    private boolean isPositive;

    public Stock() {

        stockPrice = 0.0f;
        stockCurrency = "€";
        stockPerformance = 0.0f;
        stockName = "Google Inc.";
        isPositive = stockPerformance >= 0;
    }

    // Microsoft Corporation,42.28,USD,+1.41%
    public boolean setStock(String name, float price, String currency, float performance) {
        boolean result = false;

        if(price != stockPrice || performance != stockPerformance)
            result = true;

        stockName = name;
        stockPrice = price;
        stockCurrency = currency;
        stockPerformance = performance;

        isPositive = stockPerformance >= 0;

        return result;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockCurrency() {
        return stockCurrency;
    }

    public float getStockPrice() {
        return stockPrice;
    }

    public float getStockPerformance() {
        return stockPerformance;
    }
}
