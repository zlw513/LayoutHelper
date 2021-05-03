package com.zhlw.layouthelper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomSwitchButton extends View {

    private final String TAG = "zlww";
    private Paint bgPaint;
    private Paint cyclePaint;
    private Paint linePaint;
    private Path path;
    private RectF rectF;
    private static final int BGCOLOR = Color.parseColor("#46AADC");
    private static final int BGCOLOR_GRAY = Color.GRAY;//阴影的颜色
    private static final int BGCOLOR_DEFAULT = 0xdde0e0e0;//阴影的颜色

    private int width;
    private int height;

    private int cyclerRadius = 30;
    private final double factor = 0.85;
    private float mpostion = 0;

    private boolean isAutoUpdateOpen = false;

    private ObjectAnimator animator;

    public CustomSwitchButton(Context context) {
        super(context);
        init();
    }

    public CustomSwitchButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);

        cyclePaint = new Paint();
        cyclePaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(3);

        animator = new ObjectAnimator();
    }

    private void drawBackground(Canvas canvas){
        if (rectF == null){
            rectF = new RectF(0, 0, width, height);//默认的背景
        }

        if (path == null){
            path = new Path();
            path.addRoundRect(rectF, height / 2, height / 2, Path.Direction.CW);//将默认背景改为圆角矩形
        }

        if (isAutoUpdateOpen){
            bgPaint.setColor(BGCOLOR);
        } else {
            bgPaint.setColor(BGCOLOR_DEFAULT);
        }

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.clearShadowLayer();
        canvas.drawPath(path,bgPaint);//画背景

        canvas.save();

        bgPaint.setShadowLayer(3,0,0,BGCOLOR_GRAY);
        bgPaint.setStyle(Paint.Style.STROKE);
        canvas.clipPath(path);//剪除多余的部分，比如外阴影就被剪掉了，只留下内阴影

        canvas.drawPath(path,bgPaint);
        canvas.restore();
    }

    private void drawCircle(Canvas canvas){
        cyclePaint.setColor(Color.WHITE);
        cyclePaint.setStyle(Paint.Style.FILL);
        cyclePaint.setShadowLayer(3, 1, 3, BGCOLOR_GRAY);
        if (isAutoUpdateOpen){
            if (animator.isRunning()){
                canvas.drawCircle(cyclerRadius + mpostion,cyclerRadius,(float) (cyclerRadius * factor), cyclePaint);
            } else {
                canvas.drawCircle(width - cyclerRadius, cyclerRadius, (float) (cyclerRadius * factor), cyclePaint);
            }
        } else {
            if (animator.isRunning()){
                canvas.drawCircle(mpostion + cyclerRadius, cyclerRadius, (float) (cyclerRadius * factor), cyclePaint);
            } else {
                canvas.drawCircle(cyclerRadius, cyclerRadius, (float) (cyclerRadius * factor), cyclePaint);
            }
        }
    }

    private void drawLine(Canvas canvas){
        if (isAutoUpdateOpen){
            canvas.drawLine((float) (cyclerRadius*1.5),cyclerRadius >> 1,(float) (cyclerRadius*1.5),height-(cyclerRadius >> 1),linePaint);
        } else {
            canvas.drawLine(width - (float) (cyclerRadius*1.5),cyclerRadius >> 1,width - (float) (cyclerRadius*1.5),height-(cyclerRadius >> 1),linePaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cyclerRadius = height / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawstatus(canvas);
    }

    private void drawstatus(Canvas canvas){
        drawBackground(canvas);
        drawLine(canvas);
        drawCircle(canvas);
    }

    public void setAutoUpdateOpenOnOff(boolean flag){
        isAutoUpdateOpen = flag;
        if (animator.isRunning()) return;
        startAnimation();
    }

    public boolean isAutoUpdateOpen() {
        return isAutoUpdateOpen;
    }

    public void setMpostion(float mpostion) {
        this.mpostion = mpostion;
        invalidate();
    }

    //主要是移动圆
    private void startAnimation(){
        if (isAutoUpdateOpen){
            animator = ObjectAnimator.ofFloat(this,"mpostion",width -(cyclerRadius * 2));
        } else {
            animator = ObjectAnimator.ofFloat(this,"mpostion",width -(cyclerRadius * 2),0);
        }
        animator.setDuration(300);
        animator.start();
    }

}
