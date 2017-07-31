package com.pudao.zxingdemo.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.pudao.zxingdemo.R;
import com.pudao.zxingdemo.utils.DisplayUtils;


/**
 * Created by pucheng on 2017/7/5.
 * 贴纸控制框
 */

public class ControllFrameView extends View {

    private Context context;

    //缩放旋转控制点
    private Bitmap zoomRotateBitmap;
    //删除控制点
    private Bitmap deleteBitmap;
    //镜像控制点
    private Bitmap mirrorBitmap;

    //源图, 具体的水印,贴纸,二维码等, 包裹在控制框内部
    private Bitmap srcBitmap;

    //主画笔
    private Paint mainPaint;

    //默认不缩放
    private float defaultScale = 1f;

    //主矩阵
    private Matrix mainMatrix;

    //初始点, 初次加载水印,贴纸,二维码的图形尺寸
    private float[] originPoints;
    private float[] dstPoints;
    //初始矩形
    private RectF originRect;

    private RectF dstRect;

    private Rect iconRect;

    // 边框线, 控制的边框
    private Path path = new Path();

    // 当前控件的矩形
    private RectF viewRect;

    private float lastPointX, lastPointY; //上一次记录的x, y坐标
    private boolean isIndelete, isInControl, isInMirror;
    private boolean isMove;

    private float MIN_SCALE_SIZE = 0.5f;
    private float MAX_SCALE_SIZE = 3.5f;

    public ControllFrameView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        mainPaint = new Paint();
        //抗边缘锯齿
        mainPaint.setAntiAlias(true);
        //绘制位图抗锯齿
        mainPaint.setFilterBitmap(true);
        //画笔描边
        mainPaint.setStyle(Paint.Style.STROKE);
        //线宽
        mainPaint.setStrokeWidth(4.0f);
        //画笔颜色
        mainPaint.setColor(0xffaadaf5);

        Resources resources = this.context.getResources();
        zoomRotateBitmap = BitmapFactory.decodeResource(resources, R.mipmap.media_edit_btn_rotation_scale);
        deleteBitmap = BitmapFactory.decodeResource(resources, R.mipmap.media_edit_btn_delete);
        mirrorBitmap = BitmapFactory.decodeResource(resources, R.mipmap.media_edit_btn_mirror);

//        this.iconRect.set(0, 0, deleteBitmap.getWidth(), deleteBitmap.getHeight());

    }


    public void setOriginBitmap(@NonNull Bitmap bitmap) {
        srcBitmap = bitmap;
        setFocusable(true);
        try {
            float width = srcBitmap.getWidth();
            float height = srcBitmap.getHeight();
            // 记录了5个点坐标, 4个角和中心点,依次为左上, 右上, 右下, 左下,中心点
            originPoints = new float[]{
                    0, 0,
                    width, 0,
                    width, height,
                    0, height,
                    width / 2, height / 2};
            //记录左上右下
            originRect = new RectF(0, 0, width, height);

            dstPoints = new float[10];
            dstRect = new RectF();

            mainMatrix = new Matrix();

            float transtLeft = ((float) DisplayUtils.getDisplayWidthPixels(getContext()) - srcBitmap.getWidth()) / 2;
            float transtTop = ((float)DisplayUtils.getDisplayWidthPixels(getContext()) - srcBitmap.getHeight()) / 2;

            mainMatrix.postTranslate(transtLeft, transtTop);

        } catch (Exception e) {
            e.printStackTrace();
        }
        postInvalidate();
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (srcBitmap == null || mainMatrix == null) {
            return;
        }
        //映射点的值到指定的数组中，这个方法可以在矩阵变换以后，给出指定点的值
        mainMatrix.mapPoints(dstPoints, originPoints);

        //把src中指定的矩形的左上角和右下角的两个点的坐标，写入dst中。
        mainMatrix.mapRect(dstRect, originRect);

        canvas.drawBitmap(srcBitmap, mainMatrix, mainPaint);
        path.reset();
        path.moveTo(originPoints[0], originPoints[1]);
        path.lineTo(originPoints[2], originPoints[3]);
        path.lineTo(originPoints[4], originPoints[5]);
        path.lineTo(originPoints[6], originPoints[7]);
        path.close();
        canvas.drawPath(path, mainPaint);
        //绘制操作图标
        // 绘制删除操作图标
        canvas.drawBitmap(deleteBitmap, dstPoints[0] - deleteBitmap.getWidth() / 2, dstPoints[1] - deleteBitmap.getHeight() / 2, mainPaint);
        // 绘制缩放控制操作图标
        canvas.drawBitmap(zoomRotateBitmap, dstPoints[4] - zoomRotateBitmap.getHeight() / 2, dstPoints[5] - zoomRotateBitmap.getHeight() / 2, mainPaint);
        // 绘制镜像操作图标
        canvas.drawBitmap(mirrorBitmap, dstPoints[2] - mirrorBitmap.getWidth() / 2, dstPoints[3] - mirrorBitmap.getHeight() / 2, mainPaint);
    }


    /**
     * 判断当前操作点是否是控制器区域内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInController(float x, float y) {
        // 控制器对应的坐标点位
        int position = 4;
        float rx = dstPoints[position];
        float ry = dstPoints[position + 1];
        int zoomRotateBitmapWidth = zoomRotateBitmap.getWidth();
        int zoomRotateBitmapHeight = zoomRotateBitmap.getHeight();
        RectF rectF = new RectF(rx - zoomRotateBitmapWidth / 2,
                ry - zoomRotateBitmapHeight / 2,
                rx + zoomRotateBitmapWidth / 2,
                ry + zoomRotateBitmapHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;
    }


    /**
     * 判断当前操作点是否在删除区域内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInDelete(float x, float y) {
        int position = 0;
        float rx = dstPoints[position];
        float ry = dstPoints[position + 1];
        int deleteBitmapWidth = deleteBitmap.getWidth();
        int deleteBitmapHeight = deleteBitmap.getHeight();
        RectF rectF = new RectF(rx - deleteBitmapWidth / 2,
                ry - deleteBitmapHeight / 2,
                rx + deleteBitmapWidth / 2,
                ry + deleteBitmapHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前点是否在镜像区域内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInMirror(float x, float y) {
        int position = 2;
        float rx = dstPoints[position];
        float ry = dstPoints[position + 1];
        int mirrorBitmapWidth = mirrorBitmap.getWidth();
        int mirrorBitmapHeight = mirrorBitmap.getHeight();
        RectF rectF = new RectF(rx - mirrorBitmapWidth / 2,
                ry - mirrorBitmapHeight / 2,
                rx + mirrorBitmapWidth / 2,
                ry + mirrorBitmapHeight / 2);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isFocusable()) {
            return super.dispatchTouchEvent(event);
        }
        if (viewRect == null) {
            viewRect = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInController(x, y)) {
                    isIndelete = true;
                    lastPointX = x;
                    lastPointY = y;
                    break;
                }

                if (isInDelete(x, y)) {
                    isIndelete = true;
                    break;
                }

                if (isInMirror(x, y)) {
                    isInMirror = true;
                    break;
                }

                if (dstRect.contains(x, y)) {
                    lastPointY = y;
                    lastPointX = x;
                    isMove = true;
                    break;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isInDelete(x, y) && isIndelete) {
                    doDeleteAction();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                lastPointX = 0;
                lastPointY = 0;
                isIndelete = false;
                isInControl = false;
                isInMirror = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isInControl) {
                    // 围绕中心点旋转
                    mainMatrix.postRotate(rotation(event), dstPoints[8], dstPoints[9]);

                    // 对角线一半的长度
                    float nowLength = caculateLength(dstPoints[0], dstPoints[1]);
                    // 当前触摸点与中心点的距离
                    float touchLength = caculateLength(event.getX(), event.getY());

                    if (Math.sqrt((nowLength - touchLength) * (nowLength - touchLength)) > 0.0f) {
                        float scale = touchLength / nowLength;
                        float nowsc = defaultScale * scale;
                        if (nowsc >= MIN_SCALE_SIZE && nowsc <= MAX_SCALE_SIZE) {
                            mainMatrix.postScale(scale, scale, dstPoints[8], dstPoints[9]);
                            defaultScale = nowsc;
                        }
                    }

                    invalidate();
                    lastPointX = x;
                    lastPointY = y;
                    break;
                }
                if (isMove) { //拖动的操作
                    float cX = x - lastPointX;
                    float cY = y - lastPointY;
                    isInControl = false;
                    //Log.i("MATRIX_OK", "ma_jiaodu:" + a(cX, cY));

                    if (Math.sqrt(cX * cX + cY * cY) > 2.0f  && canStickerMove(cX, cY)) {
                        //Log.i("MATRIX_OK", "is true to move");
                        mainMatrix.postTranslate(cX, cY);
                        postInvalidate();
                        lastPointX = x;
                        lastPointY = y;
                    }
                    break;
                }
                break;
        }
        return true;
    }

    /**
        删除操作
     */
    private void doDeleteAction() {
        setOriginBitmap(null);
        if (mOnStickerDeleteListener != null) {
            mOnStickerDeleteListener.onDelete();
        }
    }

    /**
     * 贴纸能否继续移动
     * @param cx
     * @param cy
     * @return
     */
    private boolean canStickerMove(float cx, float cy) {
        float px = cx + dstPoints[8];
        float py = cy + dstPoints[9];
        if (viewRect.contains(px, py)) {
            return true;
        } else {
            return false;
        }
    }

    private float rotation(MotionEvent event) {
        float originDegree = calculateDegree(lastPointX, lastPointY);
        float nowDegree = calculateDegree(event.getX(), event.getY());
        return nowDegree - originDegree;
    }

    private float calculateDegree(float x, float y) {
        double delta_x = x - dstPoints[8];
        double delta_y = y - dstPoints[9];
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float caculateLength(float x, float y) {
        float ex = x - dstPoints[8];
        float ey = y - dstPoints[9];
        return (float) Math.sqrt(ex * ex + ey * ey);
    }

    private OnStickerDeleteListener mOnStickerDeleteListener;

    public interface OnStickerDeleteListener {
        void onDelete();
    }

    public void setOnStickerDeleteListener(OnStickerDeleteListener listener) {
        mOnStickerDeleteListener = listener;
    }
}
