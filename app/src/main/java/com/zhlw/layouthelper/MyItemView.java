package com.zhlw.layouthelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MyItemView extends LinearLayout {

    private final String TAG = "zlww";
    private Paint bgRightPaint,bgLeftPaint;

    public MyItemView(Context context) {
        super(context);
    }

    public MyItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context,AttributeSet attrs){
        bgLeftPaint = new Paint();
        bgLeftPaint.setAntiAlias(true);
        bgLeftPaint.setColor(context.getResources().getColor(R.color.trans_black, null));
        bgLeftPaint.setStyle(Paint.Style.FILL);
        
        bgRightPaint = new Paint();
        bgRightPaint.setAntiAlias(true);
        bgRightPaint.setColor(context.getResources().getColor(R.color.cool_red, null));
        bgRightPaint.setStyle(Paint.Style.FILL);

        int padding = 50;
        setPadding(padding,10,padding,10);
        setClipToPadding(false);//为了不让所设的padding影响到我的视图
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);//我这里就让他自动布局就行了

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            if (child instanceof EditText){
                ((EditText) child).setMaxWidth(child.getWidth());
                ((EditText) child).setTextSize(16);
                ((EditText) child).setSingleLine();
            } else if (child instanceof ImageButton){
                int offset = getPaddingLeft() / 2 + 10;
                child.layout(child.getLeft()+offset, child.getTop(),child.getLeft()+child.getWidth()+offset, child.getBottom());
            } else if (child instanceof TextView){
                ((TextView) child).setTextSize(16);//固定为 16sp 为了效果好一点
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //画背景
        drawBackground(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawBackground(Canvas canvas){
        Path p1 = new Path();
        float factor = (getHeight()/2f +getPaddingLeft()) / getWidth();
        float pointer1 = getWidth() * factor;
        float pointer2 = getWidth() * (1 - factor);
        p1.moveTo(pointer1, 0);
        p1.arcTo(0,0,pointer1,getHeight(), -90, -180, false);
        p1.lineTo(pointer2,getHeight());
        p1.lineTo(pointer2, 0);
        p1.close();

        Path p2 = new Path();
        p2.moveTo(pointer2, 0);
        p2.arcTo(pointer2,0,getWidth(),getHeight(),-90, 180, false);
        p2.lineTo(pointer2, getHeight());
        p2.close();

        canvas.drawPath(p1, bgLeftPaint);
        canvas.drawPath(p2, bgRightPaint);
    }

}
