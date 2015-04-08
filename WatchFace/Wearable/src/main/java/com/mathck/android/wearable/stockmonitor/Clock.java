package com.mathck.android.wearable.stockmonitor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.DisplayMetrics;

public class Clock {
    Time mTime;
    public float mHourPositionX, mHourPositionY;
    public float mMinutePositionX, mMinutePositionY;
    Paint mHourPaintDark;
    Paint mMinutePaintDark;

    Paint mHourPaintLight;
    Paint mMinutePaintLight;

    public Clock() {
        mTime = new Time();
        refresh();

        mHourPaintDark = createTextPaint(Color.WHITE, Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        mHourPaintLight = createTextPaint(Color.rgb(54, 54, 54), Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));

        mMinutePaintDark = createTextPaint(Color.rgb(151, 151, 151), Typeface.create("sans-serif-light", Typeface.NORMAL));
        mMinutePaintLight = createTextPaint(Color.rgb(81, 81, 81), Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    public void clear(String timeZone) {
        mTime.clear(timeZone);
    }

    public void refresh() {
        mTime.setToNow();
    }

    public void setPositions(DisplayMetrics metrics, boolean isRound) {
        mHourPositionX = (metrics.widthPixels / 2) - 6; // 6 is for the empty room between hours and minutes
        mHourPositionY = (metrics.heightPixels / 2);

        mMinutePositionY = (metrics.heightPixels / 2);

        if(!isRound) {
            mHourPositionY -= mHourPositionY * 0.15f;
            mMinutePositionY -= mMinutePositionY * 0.15f;
        }
        else {
            mHourPositionY -= mHourPositionY * 0.075f;
            mMinutePositionY -= mMinutePositionY * 0.075f;
        }
    }

    public void draw(Canvas canvas, boolean isDarkTheme) {
        canvas.drawText(getHour(), mHourPositionX, mHourPositionY, isDarkTheme ? mHourPaintDark : mHourPaintLight);
        updateMinutePosition(getHour());
        canvas.drawText(getMinutes(), mMinutePositionX, mMinutePositionY, isDarkTheme ? mMinutePaintDark : mMinutePaintLight);
    }

    private void updateMinutePosition(String hourText) {
        mMinutePositionX = mHourPositionX + mHourPaintDark.measureText(hourText) + 6;
        // 6 is for the empty room between hours and minutes
    }

    public void setTextSize(float size) {
        mHourPaintDark.setTextSize(size);
        mMinutePaintDark.setTextSize(size);
        mHourPaintLight.setTextSize(size);
        mMinutePaintLight.setTextSize(size);
    }

    public String getHour() {
        return formatTwoDigitNumber(mTime.hour);
    }

    public String getMinutes() {
        return formatTwoDigitNumber(mTime.minute);
    }

    public String getSeconds() {
        return formatTwoDigitNumber(mTime.second);
    }

    /*
        HELPER METHODS
     */
    private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(defaultInteractiveColor);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        return paint;
    }

    private String formatTwoDigitNumber(int hour) {
        return String.format("%02d", hour);
    }
}
