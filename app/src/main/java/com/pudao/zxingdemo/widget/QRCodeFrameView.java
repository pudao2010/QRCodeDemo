package com.pudao.zxingdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.pudao.zxingdemo.utils.DisplayUtils;


/**
 * Created by pucheng on 2017/7/6.
 */

public class QRCodeFrameView extends View {

    private final float mLineWidth = 5f;
    private Paint mPaint = new Paint();
    private final Path mPath = new Path();

    private float mWidth = 600f;
    private float mHeight = 600f;

    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float x3;
    private float y3;
    private float x4;
    private float y4;

    private Paint whitePaint = new Paint();

    public QRCodeFrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QRCodeFrameView(Context context) {
        super(context);
        init();
    }

    private void init() {

        whitePaint.setColor(Color.RED);
        whitePaint.setAntiAlias(true);
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setStrokeWidth(mLineWidth);

        //抗边缘锯齿
        mPaint.setAntiAlias(true);
        //绘制位图抗锯齿
        mPaint.setFilterBitmap(true);
        //画笔描边
        mPaint.setStyle(Paint.Style.STROKE);
        //线宽
        mPaint.setStrokeWidth(mLineWidth);
        //画笔颜色
        mPaint.setColor(0xffaadaf5);

        int displayWidthPixels = DisplayUtils.getDisplayWidthPixels(getContext());
        int displayheightPixels = DisplayUtils.getDisplayheightPixels(getContext());

        mWidth = displayWidthPixels / 2;
        mHeight = mWidth;

        x1 = (displayWidthPixels - mWidth) / 2;
        y1 = (displayheightPixels - mHeight) / 2;

        x2 = x1 + mWidth;
        y2 = y1;

        x3 = x2;
        y3 = y1 + mHeight;

        x4 = x1;
        y4 = y3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF frame = new RectF(x1, y1, x3, y3);
        // 绘制框线
        mPath.reset();
        mPath.moveTo(x1, y1);
        mPath.lineTo(x2, y2);
        mPath.lineTo(x3, y3);
        mPath.lineTo(x4, y4);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
//        canvas.drawRect(frame, mPaint);
        drawFrameCorner(canvas, frame);

        // 绘制4条直线
        canvas.drawLine(x1 + mWidth /3, y1, x1 + mWidth /3, y3, whitePaint);
        canvas.drawLine(x1 + mWidth*2 /3, y1, x1 + mWidth*2 /3, y3, whitePaint);
        canvas.drawLine(x1, y1 + mWidth /3, x3, y1 + mWidth /3, whitePaint);
        canvas.drawLine(x1, y1 + mWidth*2 /3, x3, y1 + mWidth*2 /3, whitePaint);
    }

    /**
     * 绘制扫描框4角
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameCorner(Canvas canvas, RectF frame) {
        mPaint.setStyle(Paint.Style.FILL);
        // 左上角
        canvas.drawRect(frame.left - 10, frame.top, frame.left, frame.top
                + 10, mPaint);
        canvas.drawRect(frame.left - 10, frame.top - 10, frame.left
                + 10, frame.top, mPaint);
        // 右上角
        canvas.drawRect(frame.right, frame.top, frame.right + 10,
                frame.top + 10, mPaint);
        canvas.drawRect(frame.right - 10, frame.top - 10,
                frame.right + 10, frame.top, mPaint);
        // 左下角
        canvas.drawRect(frame.left - 10, frame.bottom - 10,
                frame.left, frame.bottom, mPaint);
        canvas.drawRect(frame.left - 10, frame.bottom, frame.left
                + 10, frame.bottom + 10, mPaint);
        // 右下角
        canvas.drawRect(frame.right, frame.bottom - 10, frame.right
                + 10, frame.bottom, mPaint);
        canvas.drawRect(frame.right - 10, frame.bottom, frame.right
                + 10, frame.bottom + 10, mPaint);
    }
}
