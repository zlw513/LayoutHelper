package com.zhlw.layouthelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private final String tempWidth = "width %s px : %s dp";
    private final String tempHeight = "height %s px : %s dp";
    private final String tempWidAndHei = "width is %spx height is %spx";
    private int minWidth = 60;// dp
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
        descTextPaint.setTextSize(24);
        descTextPaint.setColor(getResources().getColor(R.color.white, null));
        descTextPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
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
        if (width < dp2px(getContext(),minWidth) && height < dp2px(getContext(), minHeight)){//写不了字了
            //both too small
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
        } else if (width < dp2px(getContext(), minHeight) && height > dp2px(getContext(), minWidth)) {//高度大于 minwidth 才好写字
            // height is fine but width too small
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            canvas.save();
            canvas.rotate(90, centerX, centerY);
            canvas.drawText(String.format(tempWidth, getWidth(),px2dp(getContext(), getWidth())), centerX, centerY, descTextPaint);
            canvas.drawText(String.format(tempHeight, getHeight(),px2dp(getContext(), getHeight())), centerX+descTextPaint.getTextSize(), centerY, descTextPaint);
            canvas.restore();
        } else if (width > dp2px(getContext(), (int) (minWidth*1.5f)) && height < dp2px(getContext(), minHeight)){
            // width is fine but height too small
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            canvas.drawText(String.format(tempWidAndHei, getWidth(),getHeight()), centerX, centerY, descTextPaint);
        } else {
            // is ok
            canvas.drawColor(getResources().getColor(R.color.trans_light_black, null));
            canvas.drawRect(0,0, getWidth(), getHeight(), borderPaint);
            canvas.drawText(String.format(tempWidth, getWidth(),px2dp(getContext(), getWidth())), centerX, centerY, descTextPaint);
            canvas.drawText(String.format(tempHeight, getHeight(),px2dp(getContext(), getHeight())), centerX, centerY+descTextPaint.getTextSize(), descTextPaint);
        }
    }


    /**
     * dip转换为px大小
     * @param context    应用程序上下文
     * @param dpValue    dp值
     * @return    转换后的px值
     */
    public static int dp2px(Context context, int dpValue){
        return (int) (dpValue * getDensity(context) + 0.5f);
    }


    /**
     * px转换为dp值
     * @param context    应用程序上下文
     * @param pxValue    px值
     * @return    转换后的dp值
     */
    public static int px2dp(Context context, int pxValue){
        return (int) (pxValue / getDensity(context) + 0.5f);
    }

    /**
     * 获取系统dp尺寸密度值
     * @param context    应用程序上下文
     * @return
     */
    public static float getDensity(Context context){
        return getDisplayMetrics(context).density;
    }


    /**
     * 获取DisplayMetrics对象
     * @param context    应用程序上下文
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

}
