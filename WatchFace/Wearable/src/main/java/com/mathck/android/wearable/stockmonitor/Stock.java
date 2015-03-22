package com.mathck.android.wearable.stockmonitor;

public class Stock {

    private float stockPrice;
    private String stockCurrency;
    private float stockPerformance;
    private String stockName;
    private boolean isPositive;

    public Stock() {

        stockPrice = 416.24f;
        stockCurrency = "€";
        stockPerformance = 2.34f;
        stockName = "Google Inc.";
        isPositive = stockPerformance >= 0;
    }

    // Microsoft Corporation,42.28,USD,+1.41%
    public void setStock(String name, float price, String currency, float performance) {
        stockName = name;
        stockPrice = price;
        stockCurrency = currency;
        stockPerformance = performance;

        isPositive = stockPerformance >= 0;
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
