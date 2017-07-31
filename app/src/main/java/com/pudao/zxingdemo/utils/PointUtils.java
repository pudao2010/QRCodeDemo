package com.pudao.zxingdemo.utils;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Created by pucheng on 2017/7/6.
 */

public class PointUtils {

    /**
     * 计算旋转角差，以y轴为起点利用{@link Math#atan2(double, double)} 计算角度差
     * note: 此函数在x轴上方为[0, PI]，在x轴下方取值为[0, -PI]，注意转换
     *
     * @param lx 上次坐标x
     * @param ly 上次坐标y
     * @param ax 锚点在屏幕上坐标x
     * @param ay 锚点在屏幕上坐标y
     * @param cx 当前坐标x
     * @param cy 当前坐标y
     * @return 两次角度差
     */
    public static double deltaDegree(float lx, float ly,
                                     float ax, float ay,
                                     float cx, float cy) {

        double degree;

        // 计算上次点的角度
        double lastDegree = Math.atan2(ly - ay, lx - ax);

        // 计算这次点的角度
        double currentDegree = Math.atan2(cy - ay, cx - ax);

        // 计算差值
        degree = currentDegree - lastDegree;

        return degree;
    }

    /**
     * 计算两点距离
     *
     * @return
     */
    public static double pointDistance(float x1, float y1, float x2, float y2) {
        return Math.abs(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    /**
     * 计算两点间中点坐标
     * @param start 起始点
     * @param end   终点
     * @return
     */
    public static PointF centerPoint(@NonNull PointF start, @NonNull PointF end, PointF ret) {
        if (null == ret) {
            ret = new PointF();
        }
        ret.set((start.x + end.x) / 2, (start.y + end.y) / 2);

        return ret;
    }

    /**
     * 计算交叉矩形区域
     * @param src   矩形1
     * @param dst   矩形2
     * @param ret   保存结果
     */
    public static RectF crossRect(@NonNull RectF src, @NonNull RectF dst, RectF ret) {
        if (null == ret) {
            ret = new RectF();
        }
        ret.set(Math.max(src.left, dst.left),
                Math.max(src.top, dst.top),
                Math.min(src.right, dst.right),
                Math.min(src.bottom, dst.bottom));

        return ret;
    }
}
