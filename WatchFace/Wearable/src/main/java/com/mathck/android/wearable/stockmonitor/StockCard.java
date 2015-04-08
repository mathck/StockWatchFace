package com.mathck.android.wearable.stockmonitor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

public class StockCard {

    public Stock mStock;

    // todo resize bitmaps
    private Bitmap mTriangleUp, mTriangleDown, mStockUp, mStockDown;

    private Paint mStockPricePaint;
    private Paint mStockPerformancePaint;
    private Paint mStockNamePaint;
    private Paint mCardColorPaintGreen;
    private Paint mCardColorPaintRed;

    private float mStockPricePositionX, mStockPricePositionY;
    private float mStockPerformancePositionX, mStockPerformancePositionY;
    private float mStockNamePositionX, mStockNamePositionY;
    private float mTrendIconPositionX, mTrendIconPositionY;
    private float mTrianglePositionX, mTrianglePositionY;
    private float mCardPositionX, mCardPositionY;
    private float mCardWidth, mCardHeight;

    private int mDarkThemeColor;
    private int mLightThemeColor;

    public StockCard(Resources resources) {
        mStockPricePaint = createTextPaint();

        mStockPerformancePaint = createTextPaint();
                        mStockPerformancePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        mStockNamePaint = createTextPaint();
        mStockNamePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        mCardColorPaintGreen = new Paint();
        mCardColorPaintGreen.setColor(resources.getColor(R.color.green));

        mCardColorPaintRed = new Paint();
        mCardColorPaintRed.setColor(resources.getColor(R.color.red));

        mTriangleUp = BitmapFactory.decodeResource(resources, R.drawable.arrow);
        mTriangleDown = RotateBitmap(mTriangleUp, 180);
        mStockUp = BitmapFactory.decodeResource(resources, R.drawable.up);
        mStockDown = BitmapFactory.decodeResource(resources, R.drawable.down);

        mDarkThemeColor = resources.getColor(R.color.bg_darkgrey);
        mLightThemeColor = resources.getColor(R.color.bg_bright);

        mStock = new Stock();
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(mCardPositionX, mCardPositionY, mCardWidth, mCardHeight, mStock.isPositive() ? mCardColorPaintGreen : mCardColorPaintRed);

        canvas.drawBitmap(  mStock.isPositive() ? mTriangleUp : mTriangleDown, mTrianglePositionX - ((mStockPerformancePaint.measureText(mStock.getStockPerformance() + "%")) / 2),
                            mTrianglePositionY, null);

        canvas.drawBitmap(mStock.isPositive() ? mStockUp : mStockDown, mTrendIconPositionX, mTrendIconPositionY, null);

        canvas.drawText(twoDecimal(mStock.getStockPrice()) + " " + mStock.getStockCurrency(), mStockPricePositionX, mStockPricePositionY, mStockPricePaint);
        canvas.drawText(twoDecimal(Math.abs(mStock.getStockPerformance())) + "%", mStockPerformancePositionX, mStockPerformancePositionY, mStockPerformancePaint);
        canvas.drawText(getShortName(mStock.getStockName()), mStockNamePositionX, mStockNamePositionY, mStockNamePaint);
    }

    private String twoDecimal(float value) {
        return String.format("%.02f", value);
    }

    private String getShortName(String name) {
        return name.substring(0, Math.min(name.length(), 10)) +
                (name.length() >= 10 ? "..." : "");
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Paint createTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);
        return paint;
    }

    public void setPositions(DisplayMetrics metrics) {
        // todo position everything using screenPercentage

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int centerX = (int) ((screenWidth / 2.0f));
        int centerY = (int) ((screenHeight / 2.0f) * 1.2f);

        mCardPositionX = 0;
        mCardPositionY = centerY;
        mCardWidth = screenWidth;
        mCardHeight = screenHeight;

        centerY += 5;

        mTrendIconPositionX = centerX + 30;
        mTrendIconPositionY = centerY + 22;

        mStockPricePositionX = centerX - 130;
        mStockPricePositionY = centerY *  1.2f;

        mStockPerformancePositionX = centerX - 78;
        mStockPerformancePositionY = mStockPricePositionY + 33;

        mStockNamePositionX = centerX - 83;
        mStockNamePositionY = mStockPerformancePositionY + 25;

        mTrianglePositionX = centerX - 52 - mTriangleUp.getWidth();
        mTrianglePositionY = mStockPerformancePositionY - mTriangleUp.getHeight() + 2;
    }

    public void setTextSize(float size) {
        mStockPricePaint.setTextSize(size * 0.375f);
        mStockPerformancePaint.setTextSize(size * 0.30f);
        mStockNamePaint.setTextSize(size * 0.20f);
    }

    public void drawBackground(Canvas canvas, boolean isDarkTheme) {
        canvas.drawColor(isDarkTheme ? mDarkThemeColor : mLightThemeColor);
    }
}
