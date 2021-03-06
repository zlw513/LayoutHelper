package com.zhlw.layouthelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class DecsriptionView extends View {

    private Paint descTextPaint;
    private Paint borderPaint;
    private int centerX,centerY;
    private final String tempWidth = "width %spx : %sdp";
    private final String tempHeight = "height %spx : %sdp";
    private final String tempWidAndHei = "width %spx height %spx";
    private int minWidth = 80;// dp
    private int minHeight = 20;// dp

    public DecsriptionView(Context context) {
        super(context);
        initView(context, null);
    }

    public DecsriptionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public DecsriptionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context,AttributeSet attrs){

        descTextPaint = new Paint();
        descTextPaint.setAntiAlias(true);
        descTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        descTextPaint.setStrokeWidth(1);
        descTextPaint.setTextSize(18);
        descTextPaint.setColor(getResources().getColor(R.color.white, null));
        descTextPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(Color.RED);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        autoFitDrawInfo(getWidth(), getHeight(), canvas);
    }

    private void autoFitDrawInfo(int width,int height,Canvas canvas){
        if (width <= dp2px(getContext(), minWidth) && height >= dp2px(getContext(), minWidth)) {//???????????? minwidth ????????????
            // height is fine but width too small
            Path path = new Path();
            path.moveTo(centerX/1.5f, 0);
            path.lineTo(centerX/1.5f, height);
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            canvas.drawTextOnPath(String.format(tempWidAndHei, getWidth(),getHeight()), path,0 ,0, descTextPaint);
        } else if (width >= dp2px(getContext(), minWidth) && height <= dp2px(getContext(), minHeight)){
            // width is fine but height too small
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            descTextPaint.setTextSize(14);
            canvas.drawText(String.format(tempWidAndHei, getWidth(),getHeight()), centerX, getHeight()-2, descTextPaint);
        } else if (width <= dp2px(getContext(), minWidth) || height <= dp2px(getContext(), minHeight)) {//?????????????????? minwidth ,??????????????????????????????
            //bad
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);

            //??????????????????????????????
            if (width >= dp2px(getContext(), minWidth/2) && height >= dp2px(getContext(), minHeight)){//???????????????????????????
                String widStr = "w: %spx";
                String heighStr = "h: %spx";
                canvas.drawText(String.format(widStr, getWidth()), centerX, centerY, descTextPaint);
                canvas.drawText(String.format(heighStr, getHeight()), centerX, centerY+descTextPaint.getTextSize(), descTextPaint);
            }

        }else {
            // is ok
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            canvas.drawText(String.format(tempWidth, getWidth(),px2dp(getContext(), getWidth())), centerX, centerY, descTextPaint);
            canvas.drawText(String.format(tempHeight, getHeight(),px2dp(getContext(), getHeight())), centerX, centerY+descTextPaint.getTextSize(), descTextPaint);
        }
    }


    /**
     * dip?????????px??????
     * @param context    ?????????????????????
     * @param dpValue    dp???
     * @return    ????????????px???
     */
    public static int dp2px(Context context, int dpValue){
        return (int) (dpValue * getDensity(context) + 0.5f);
    }


    /**
     * px?????????dp???
     * @param context    ?????????????????????
     * @param pxValue    px???
     * @return    ????????????dp???
     */
    public static int px2dp(Context context, int pxValue){
        return (int) (pxValue / getDensity(context) + 0.5f);
    }

    /**
     * ????????????dp???????????????
     * @param context    ?????????????????????
     * @return
     */
    public static float getDensity(Context context){
        return getDisplayMetrics(context).density;
    }


    /**
     * ??????DisplayMetrics??????
     * @param context    ?????????????????????
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

}
