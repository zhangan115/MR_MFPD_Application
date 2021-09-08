package com.mr.mf_pd.application.view.opengl.utils;

public class Geometry {

    /**
     * 三维场景中的一个点
     */
    public static class Point {

        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * 沿着Y轴平移
         *
         * @param distance 平移距离
         * @return 点
         */
        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }
    }

    /**
     * 圆
     */
    public static class Circle {

        public final Point center;
        public final float radius;

        public Circle(Point point, float radius) {
            this.center = point;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    /**
     * 圆柱
     */
    public static class Cylinder {

        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }


}
