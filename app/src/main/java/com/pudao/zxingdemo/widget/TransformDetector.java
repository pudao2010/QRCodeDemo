package com.pudao.zxingdemo.widget;

import android.graphics.PointF;
import android.view.MotionEvent;

import com.pudao.zxingdemo.utils.PointUtils;


/**
 * 自定义多功能手势检测工具类
 *
 * NOTE: 只能由两个触点操控，支持变换过程中所有手势检测
 *
 */
public class TransformDetector {

    // 最小触发变换距离 in px
    private final static int TRIGGER_DIS = 5;

    // 开始主点，次点坐标
    private PointF startMain = new PointF(), startMinor = new PointF();
    // 历史主点，次点坐标
    private PointF lastMain = new PointF(), lastMinor = new PointF();
    // 当前主点，次点坐标
    private PointF currentMain = new PointF(), currentMinor = new PointF();
    // 当前锚点，上次锚点坐标
    private PointF anchor = new PointF(), lastAnchor = new PointF();
    // 是否是触发变换
    private boolean transforming;
    // 是否触发多点模式
    private boolean multiTouched;
    // 主点索引，次点索引
    private int indexMain = -1, indexMinor = -1;
    // 监听器
    private final InternalTransformListener listener;

    TransformDetector(InternalTransformListener listener) {
        this.listener = listener;
    }

    /**
     * 主处理方法
     * @param event     事件
     * @param translate 是否返回平移信息
     * @return
     */
    boolean onTouchEvent(MotionEvent event, boolean translate) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // TODO: 2017/4/2 主手指
                this.indexMain = event.getActionIndex();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (this.indexMain < 0) {
                    // TODO: 2017/4/2 主次手指调换 交换索引
                    this.indexMain = this.indexMinor;
                    this.indexMinor = -1;
                }
                if (this.indexMinor < 0) {
                    // TODO: 2017/4/2 正常指续
                    this.indexMinor = event.getActionIndex();
                }
                if (!this.multiTouched && 0 <= indexMain && 0 <= indexMinor) {
                    startMain.set(event.getX(indexMain), event.getY(indexMain));
                    startMinor.set(event.getX(indexMinor), event.getY(indexMinor));
                    currentMain.set(startMain);
                    currentMinor.set(startMinor);
                    PointUtils.centerPoint(lastMain, lastMinor, this.lastAnchor);
                    this.multiTouched = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (this.multiTouched) {
                    try {
                        currentMain.set(event.getX(indexMain), event.getY(indexMain));
                        currentMinor.set(event.getX(indexMinor), event.getY(indexMinor));
                    } catch (Exception e) {
                        indexMain = indexMinor = -1;
                        this.multiTouched = this.transforming = false;
                        return true;
                    }
                    if (!transforming && (TRIGGER_DIS <
                            PointUtils.pointDistance(startMain.x, startMain.y,
                                    currentMain.x, currentMain.y) || TRIGGER_DIS <
                            PointUtils.pointDistance(startMinor.x, startMinor.y,
                                    currentMinor.x, currentMinor.y))) {
                        // 计算中点即锚点
                        PointUtils.centerPoint(currentMain, currentMinor, this.anchor);
                        this.lastAnchor.set(this.anchor);
                        listener.onStart(this.anchor);
                        transforming = true;
                    }
                    if (this.transforming) {
                        // 平移、旋转、缩放
                        PointUtils.centerPoint(currentMain, currentMinor, this.anchor);
                        // TODO: 2017/3/31 平移
                        if (translate && Math.abs(anchor.x - lastAnchor.x) > 0
                                && Math.abs(anchor.y - lastAnchor.y) > 0)
                            listener.onTranslate(anchor.x - lastAnchor.x, anchor.y - lastAnchor.y);

                        // TODO: 2017/3/31 缩放
                        double lastDis = PointUtils.pointDistance(
                                lastMain.x, lastMain.y, lastMinor.x, lastMinor.y);
                        double currentDis = PointUtils.pointDistance(
                                currentMain.x, currentMain.y, currentMinor.x, currentMinor.y);
                        double scale = currentDis / lastDis;
                        if (Math.abs(scale) > 0)
                            listener.onScale(scale, scale, this.anchor);

                        // TODO: 2017/3/31 旋转计算
                        if (0 != lastDis && 0 != currentDis) {
                            double degree1 = PointUtils.deltaDegree(
                                    lastMain.x, lastMain.y,
                                    anchor.x, anchor.y,
                                    currentMain.x, currentMain.y) * (180 / Math.PI);
                            double degree2 = PointUtils.deltaDegree(
                                    lastMinor.x, lastMinor.y,
                                    anchor.x, anchor.y,
                                    currentMinor.x, currentMinor.y) * (180 / Math.PI);
                            // 修正临界角度
                            double degree = ((Math.abs(degree1) < 350 ? degree1 : Math.signum(degree1) * (360 - Math.abs(degree1))) +
                                    (Math.abs(degree2) < 350 ? degree2 : Math.signum(degree2) * (360 - Math.abs(degree2)))) / 2;
                            if (Math.abs(degree) > 0)
                                listener.onRotate(degree, anchor);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getActionIndex() == this.indexMinor) {
                    startMinor.set(0, 0);
                    currentMinor.set(0, 0);
                    indexMinor = -1;
                    multiTouched = false;
                    transforming = false;
                } else if (event.getActionIndex() == this.indexMain) {
                    startMain.set(0, 0);
                    currentMain.set(0, 0);
                    anchor.set(0, 0);
                    indexMain = -1;
                    multiTouched = false;
                    transforming = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startMain.set(0, 0);
                currentMain.set(0, 0);
                anchor.set(0, 0);
                indexMain = -1;
                transforming = false;
                listener.onFinish();
        }

        lastMain.set(currentMain);
        lastMinor.set(currentMinor);
        lastAnchor.set(anchor);

        return true;
    }

    interface InternalTransformListener {

        /**
         * 变换开始
         * @param anchor    锚点
         */
        void onStart(PointF anchor);

        /**
         * 平移变换
         * @param deltaX    x方向增量
         * @param deltaY    y方向增量
         */
        void onTranslate(double deltaX, double deltaY);

        /**
         * 缩放变换
         * @param scaleX    x方向缩放
         * @param scaleY    y方向缩放
         * @param anchor    锚点
         */
        void onScale(double scaleX, double scaleY, PointF anchor);

        /**
         * 旋转变换
         * @param degree    旋转角度
         * @param anchor    锚点
         */
        void onRotate(double degree, PointF anchor);

        /**
         * 双指全部离开，变换结束
         */
        void onFinish();

    }

}
