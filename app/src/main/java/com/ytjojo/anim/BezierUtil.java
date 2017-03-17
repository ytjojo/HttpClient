package com.ytjojo.anim;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算贝塞尔曲线上的点坐标
 * <p>
 * Created by xuyisheng on 16/7/13.
 */
public class BezierUtil {

    private void metaBallVersion2(Canvas canvas, Paint mPaint, Path mPath, float mCircleTwoX, float mCircleTwoY, float mCircleOneX, float mCircleOneY, float mRadiusNormal) {
        float x = mCircleTwoX;
        float y = mCircleTwoY;
        float startX = mCircleOneX;
        float startY = mCircleOneY;
        float controlX = (startX + x) / 2;
        float controlY = (startY + y) / 2;

        float distance = (float) Math.sqrt((controlX - startX) * (controlX - startX) + (controlY - startY) * (controlY - startY));
        double a = Math.acos(mRadiusNormal / distance);

        double b = Math.acos((controlX - startX) / distance);
        float offsetX1 = (float) (mRadiusNormal * Math.cos(a - b));
        float offsetY1 = (float) (mRadiusNormal * Math.sin(a - b));
        float tanX1 = startX + offsetX1;
        float tanY1 = startY - offsetY1;

        double c = Math.acos((controlY - startY) / distance);
        float offsetX2 = (float) (mRadiusNormal * Math.sin(a - c));
        float offsetY2 = (float) (mRadiusNormal * Math.cos(a - c));
        float tanX2 = startX - offsetX2;
        float tanY2 = startY + offsetY2;

        double d = Math.acos((y - controlY) / distance);
        float offsetX3 = (float) (mRadiusNormal * Math.sin(a - d));
        float offsetY3 = (float) (mRadiusNormal * Math.cos(a - d));
        float tanX3 = x + offsetX3;
        float tanY3 = y - offsetY3;

        double e = Math.acos((x - controlX) / distance);
        float offsetX4 = (float) (mRadiusNormal * Math.cos(a - e));
        float offsetY4 = (float) (mRadiusNormal * Math.sin(a - e));
        float tanX4 = x - offsetX4;
        float tanY4 = y + offsetY4;

        mPath.reset();
        mPath.moveTo(tanX1, tanY1);
        mPath.quadTo(controlX, controlY, tanX3, tanY3);
        mPath.lineTo(tanX4, tanY4);
        mPath.quadTo(controlX, controlY, tanX2, tanY2);
        canvas.drawPath(mPath, mPaint);

        // 辅助线
        canvas.drawCircle(tanX1, tanY1, 5, mPaint);
        canvas.drawCircle(tanX2, tanY2, 5, mPaint);
        canvas.drawCircle(tanX3, tanY3, 5, mPaint);
        canvas.drawCircle(tanX4, tanY4, 5, mPaint);
        canvas.drawLine(mCircleOneX, mCircleOneY, mCircleTwoX, mCircleTwoY, mPaint);
        canvas.drawLine(0, mCircleOneY, mCircleOneX + mRadiusNormal + 400, mCircleOneY, mPaint);
        canvas.drawLine(mCircleOneX, 0, mCircleOneX, mCircleOneY + mRadiusNormal + 50, mPaint);
        canvas.drawLine(mCircleTwoX, mCircleTwoY, mCircleTwoX, 0, mPaint);
        canvas.drawCircle(controlX, controlY, 5, mPaint);
        canvas.drawLine(startX, startY, tanX1, tanY1, mPaint);
        canvas.drawLine(tanX1, tanY1, controlX, controlY, mPaint);
    }
    public static final float CIRCLE_P = 0.551915024494f;
    /**
     * B(t) = (1 - t)^2 * P0 + 2t * (1 - t) * P1 + t^2 * P2, t ∈ [0,1]
     *
     * @param t  曲线长度比例
     * @param p0 起始点
     * @param p1 控制点
     * @param p2 终止点
     * @return t对应的点
     */
    public static PointF CalculateBezierPointForQuadratic(float t, PointF p0, PointF p1, PointF p2) {
        PointF point = new PointF();
        float temp = 1 - t;
        point.x = temp * temp * p0.x + 2 * t * temp * p1.x + t * t * p2.x;
        point.y = temp * temp * p0.y + 2 * t * temp * p1.y + t * t * p2.y;
        return point;
    }

    /**
     * B(t) = P0 * (1-t)^3 + 3 * P1 * t * (1-t)^2 + 3 * P2 * t^2 * (1-t) + P3 * t^3, t ∈ [0,1]
     *
     * @param t  曲线长度比例
     * @param p0 起始点
     * @param p1 控制点1
     * @param p2 控制点2
     * @param p3 终止点
     * @return t对应的点
     */
    public static PointF CalculateBezierPointForCubic(float t, PointF p0, PointF p1, PointF p2, PointF p3) {
        PointF point = new PointF();
        float temp = 1 - t;
        point.x = p0.x * temp * temp * temp + 3 * p1.x * t * temp * temp + 3 * p2.x * t * t * temp + p3.x * t * t * t;
        point.y = p0.y * temp * temp * temp + 3 * p1.y * t * temp * temp + 3 * p2.y * t * t * temp + p3.y * t * t * t;
        return point;
    }

    public static ArrayList<PointF> getPathPoint(Path path) {
        PathMeasure measure = new PathMeasure(path, false);
        float step = 0.0001f;
        float len = measure.getLength();
        final int numPoints = (int) (len / step) + 1;
        ArrayList<PointF> pointFs = new ArrayList();
        float[] point = new float[2];
        for (float t = 0; t <= 1; t += step) {
            float dis = t * len;
            measure.getPosTan(dis, point, null);
            PointF paint = new PointF(point[0], point[1]);
            pointFs.add(paint);
        }
        return pointFs;
    }

    public static class Lasso {
        // polygon coordinates
        private float[] mPolyX, mPolyY;

        // number of size in polygon
        private int mPolySize;

        /**
         * 　　* default constructor
         * 　　*
         * 　　* @param px
         * 　　*            polygon coordinates X
         * 　　* @param py
         * 　　*            polygon coordinates Y
         * 　　* @param ps
         * 　　*            polygon sides count
         */
        public Lasso(float[] px, float[] py, int ps) {
            this.mPolyX = px;
            this.mPolyY = py;
            this.mPolySize = ps;
        }

        /**
         * 　　* constructor
         * 　　*
         * 　　* @param pointFs
         * 　　*            points list of the lasso
         */
        public Lasso(List<PointF> pointFs) {
            this.mPolySize = pointFs.size();
            this.mPolyX = new float[this.mPolySize];
            this.mPolyY = new float[this.mPolySize];

            for (int i = 0; i < this.mPolySize; i++) {
                this.mPolyX[i] = pointFs.get(i).x;
                this.mPolyY[i] = pointFs.get(i).y;
            }
            Log.d("lasso", "lasso size:" + mPolySize);
        }

        /**
         * 　* check if this polygon contains the point.
         * 　*
         * 　* @param x
         * 　*            point coordinate X
         * 　* @param y
         * 　*            point coordinate Y
         * 　* @return point is in polygon flag
         *
         */

        public boolean contains(float x, float y) {
            boolean result = false;
            for (int i = 0, j = mPolySize - 1; i < mPolySize; j = i++) {
                if ((mPolyY[i] < y && mPolyY[j] >= y)
                        || (mPolyY[j] < y && mPolyY[i] >= y)) {
                    if (mPolyX[i] + (y - mPolyY[i]) / (mPolyY[j] - mPolyY[i]) * (mPolyX[j] - mPolyX[i]) < x) {
                        result = !result;
                    }
                }
            }
            return result;
        }
    }
}