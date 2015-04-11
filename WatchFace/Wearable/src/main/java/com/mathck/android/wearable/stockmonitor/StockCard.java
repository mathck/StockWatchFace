package com.mathck.android.wearable.stockmonitor;

import android.content.Context;
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
        mStockNamePaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));

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
                (name.length() >= 10 ? Character.toString ((char) 8230) : "");
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

    public void setPositions(DisplayMetrics metrics, boolean isRound) {
        float sWidth = metrics.widthPixels;
        float sHeight = metrics.heightPixels;

        int centerX = (int) ((sWidth / 2.0f));
        int centerY = (int) ((sHeight / 2.0f) * 1.1f);

        if(!isRound) // isSquare =
            centerY = (int) ((sHeight / 2.0f));

        mCardPositionX = 0;
        mCardPositionY = centerY;
        mCardWidth = sWidth;
        mCardHeight = sHeight;

        centerY *= 1.08f;

        // TREND ICON
        mTrendIconPositionX = centerX + (sWidth * 0.11f);
        if(!isRound)
            mTrendIconPositionX = centerX * 1.1f;
        mTrendIconPositionY = (centerY * 1.075f);

        // 501,30 €
        mStockPricePositionX = (sWidth * 0.115f);
        mStockPricePositionY = (centerY *  1.2f);

        if(!isRound)
            mStockPricePositionY += (sWidth * 0.03f);

        // ARROW
        mTrianglePositionX = (sWidth * 0.245f) + mTriangleUp.getWidth();
        mTrianglePositionY = mStockPricePositionY + (sHeight * 0.133f) - mTriangleUp.getHeight();

        // 1,23 %
        mStockPerformancePositionX = (sWidth * 0.29f);
        mStockPerformancePositionY = mStockPricePositionY + (sHeight * 0.13f);

        // GOOGLE-C
        mStockNamePositionX = (sWidth * 0.32f);
        mStockNamePositionY = mStockPerformancePositionY + (sHeight * 0.1f);

        if(!isRound) { // isSquare =
            mStockPricePositionX = sWidth * 0.075f;
            mStockPerformancePositionX = mStockNamePositionX = mStockPricePositionX;
            mTrianglePositionX = mStockPerformancePositionX + mTriangleUp.getWidth() + (sWidth * 0.055f);
            mStockPerformancePositionX += mTriangleUp.getWidth() + (sWidth * 0.045f);
            mTrendIconPositionX += (sWidth * 0.08f);
            mTrendIconPositionY += (sHeight * 0.03f);
        }
    }

    public void setTextSize(float size, DisplayMetrics metrics, boolean isRound) {
        if(!isRound) { // isSquare =>
            mStockPricePaint.setTextSize(size * 0.375f);
            mStockPerformancePaint.setTextSize(size * 0.30f);
            mStockNamePaint.setTextSize(size * 0.17f);
        }
        else { // isRound =>
            float sWidth = metrics.widthPixels;

            mStockPricePaint.setTextSize(convertPixelsToDp(size * 0.55f, metrics));
            mStockPerformancePaint.setTextSize(convertPixelsToDp(size * 0.45f, metrics));
            mStockNamePaint.setTextSize(convertPixelsToDp(size * 0.26f, metrics));
        }
    }

    public void drawBackground(Canvas canvas, boolean isDarkTheme) {
        canvas.drawColor(isDarkTheme ? mDarkThemeColor : mLightThemeColor);
    }

    public static int convertDpToPixel(float dp, DisplayMetrics metrics) {
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public static float convertPixelsToDp(float px, DisplayMetrics metrics) {
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
}
