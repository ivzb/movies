/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.udacity.popularMovies.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import android.support.annotation.NonNull;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public final class ArcUtils {
    private static final double FULL_CIRCLE_RADIANS = toRadians(360d);

    private ArcUtils() {
    }

    /**
     * Draws a circular arc on the given {@code Canvas}.
     *
     * @param canvas       The canvas to draw into.
     * @param circleCenter The center of the circle on which to draw the arc.
     * @param circleRadius The radius of the circle on which to draw the arc.
     * @param startAngle   Starting angle (in degrees) where the arc begins.
     * @param sweepAngle   Sweep angle (in degrees) measured clockwise.
     * @param paint        The paint to use then drawing the arc.
     * @see #drawArc(android.graphics.Canvas, android.graphics.PointF, float, float, float, android.graphics.Paint, int, boolean)
     */
    public static void drawArc(@NonNull Canvas canvas, PointF circleCenter, float circleRadius,
                               float startAngle, float sweepAngle, @NonNull Paint paint) {
        drawArc(canvas, circleCenter, circleRadius, startAngle, sweepAngle, paint, 8, false);
    }

    /**
     * Draws a circular arc on the given {@code Canvas}.
     *
     * @param canvas             The canvas to draw into.
     * @param circleCenter       The center of the circle on which to draw the arc.
     * @param circleRadius       The radius of the circle on which to draw the arc.
     * @param startAngle         Starting angle (in degrees) where the arc begins.
     * @param sweepAngle         Sweep angle (in degrees) measured clockwise.
     * @param paint              The paint to use then drawing the arc.
     * @param arcsPointsOnCircle See {@link #createBezierArcDegrees(android.graphics.PointF, float, float, float, int, boolean, android.graphics.Path)}.
     * @param arcsOverlayPoints  See {@link #createBezierArcDegrees(android.graphics.PointF, float, float, float, int, boolean, android.graphics.Path)}.
     * @see #drawArc(android.graphics.Canvas, android.graphics.PointF, float, float, float, android.graphics.Paint)
     */
    public static void drawArc(@NonNull Canvas canvas, PointF circleCenter, float circleRadius,
                               float startAngle, float sweepAngle, @NonNull Paint paint,
                               int arcsPointsOnCircle, boolean arcsOverlayPoints) {
        if (sweepAngle == 0f) {
            final PointF p = pointFromAngleDegrees(circleCenter, circleRadius, startAngle);
            canvas.drawPoint(p.x, p.y, paint);
        } else {
            canvas.drawPath(createBezierArcDegrees(
                    circleCenter, circleRadius, startAngle, sweepAngle,
                    arcsPointsOnCircle, arcsOverlayPoints, null), paint);
        }
    }

    /**
     * Normalize the input radians in the range 360° > x >= 0°.
     *
     * @param radians The angle to normalize (in radians).
     * @return The angle normalized in the range 360° > x >= 0°.
     */
    public static double normalizeRadians(double radians) {
        radians %= FULL_CIRCLE_RADIANS;
        if (radians < 0d) {
            radians += FULL_CIRCLE_RADIANS;
        }
        if (radians == FULL_CIRCLE_RADIANS) {
            radians = 0d;
        }
        return radians;
    }


    /**
     * Returns the point of a given angle (in radians) on a circle.
     *
     * @param center       The center of the circle.
     * @param radius       The radius of the circle.
     * @param angleRadians The angle (in radians).
     * @return The point of the given angle on the specified circle.
     * @see #pointFromAngleDegrees(android.graphics.PointF, float, float)
     */
    @NonNull
    public static PointF pointFromAngleRadians(@NonNull PointF center, float radius, double angleRadians) {
        return new PointF((float) (center.x + radius * cos(angleRadians)),
                (float) (center.y + radius * sin(angleRadians)));
    }

    /**
     * Returns the point of a given angle (in degrees) on a circle.
     *
     * @param center       The center of the circle.
     * @param radius       The radius of the circle.
     * @param angleDegrees The angle (in degrees).
     * @return The point of the given angle on the specified circle.
     * @see #pointFromAngleRadians(android.graphics.PointF, float, double)
     */
    @NonNull
    public static PointF pointFromAngleDegrees(@NonNull PointF center, float radius, float angleDegrees) {
        return pointFromAngleRadians(center, radius, toRadians(angleDegrees));
    }

    /**
     * Adds a circular arc to the given path by approximating it through a cubic Bézier curve.
     * <p/>
     * <p>
     * Note that this <strong>does not</strong> split the arc to better approximate it, for that see either:
     * <ul>
     * <li>{@link #createBezierArcDegrees(android.graphics.PointF, float, float, float, int, boolean,
     * android.graphics.Path)}</li>
     * <li>{@link #createBezierArcRadians(android.graphics.PointF, float, double, double, int, boolean,
     * android.graphics.Path)}</li>
     * </ul>
     * </p>
     * <p/>
     * For a technical explanation:
     * <a href="http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html">
     * http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html
     * </a>
     *
     * @param path        The path to add the arc to.
     * @param center      The center of the circle.
     * @param start       The starting point of the arc on the circle.
     * @param end         The ending point of the arc on the circle.
     * @param moveToStart If {@code true}, move to the starting point of the arc
     *                    (see: {@link android.graphics.Path#moveTo(float, float)}).
     * @see #createBezierArcDegrees(android.graphics.PointF, float, float, float, int, boolean, android.graphics.Path)
     * @see #createBezierArcRadians(android.graphics.PointF, float, double, double, int, boolean, android.graphics.Path)
     */
    public static void addBezierArcToPath(@NonNull Path path, @NonNull PointF center,
                                          @NonNull PointF start, @NonNull PointF end, boolean moveToStart) {
        if (moveToStart) {
            path.moveTo(start.x, start.y);
        }
        if (start.equals(end)) {
            return;
        }

        final double ax = start.x - center.x;
        final double ay = start.y - center.y;
        final double bx = end.x - center.x;
        final double by = end.y - center.y;
        final double q1 = ax * ax + ay * ay;
        final double q2 = q1 + ax * bx + ay * by;
        final double k2 = 4d / 3d * (sqrt(2d * q1 * q2) - q2) / (ax * by - ay * bx);
        final float x2 = (float) (center.x + ax - k2 * ay);
        final float y2 = (float) (center.y + ay + k2 * ax);
        final float x3 = (float) (center.x + bx + k2 * by);
        final float y3 = (float) (center.y + by - k2 * bx);

        path.cubicTo(x2, y2, x3, y3, end.x, end.y);
    }

    /**
     * Adds a circular arc to the given path by approximating it through a cubic Bézier curve, splitting it if
     * necessary. The precision of the approximation can be adjusted through {@code pointsOnCircle} and
     * {@code overlapPoints} parameters.
     * <p>
     * <strong>Example:</strong> imagine an arc starting from 0° and sweeping 100° with a value of
     * {@code pointsOnCircle} equal to 12 (threshold -> 360° / 12 = 30°):
     * <ul>
     * <li>if {@code overlapPoints} is {@code true}, it will be split as following:
     * <ul>
     * <li>from 0° to 30° (sweep 30°)</li>
     * <li>from 30° to 60° (sweep 30°)</li>
     * <li>from 60° to 90° (sweep 30°)</li>
     * <li>from 90° to 100° (sweep 10°)</li>
     * </ul>
     * </li>
     * <li>if {@code overlapPoints} is {@code false}, it will be split into 4 equal arcs:
     * <ul>
     * <li>from 0° to 25° (sweep 25°)</li>
     * <li>from 25° to 50° (sweep 25°)</li>
     * <li>from 50° to 75° (sweep 25°)</li>
     * <li>from 75° to 100° (sweep 25°)</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     * <p/>
     * For a technical explanation:
     * <a href="http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html">
     * http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html
     * </a>
     *
     * @param center            The center of the circle.
     * @param radius            The radius of the circle.
     * @param startAngleRadians The starting angle on the circle (in radians).
     * @param sweepAngleRadians How long to make the total arc (in radians).
     * @param pointsOnCircle    Defines a <i>threshold</i> (360° /{@code pointsOnCircle}) to split the Bézier arc to
     *                          better approximate a circular arc, depending also on the value of {@code overlapPoints}.
     *                          The suggested number to have a reasonable approximation of a circle is at least 4 (90°).
     *                          Less than 1 will be ignored (the arc will not be split).
     * @param overlapPoints     Given the <i>threshold</i> defined through {@code pointsOnCircle}:
     *                          <ul>
     *                          <li>if {@code true}, split the arc on every angle which is a multiple of the
     *                          <i>threshold</i> (yields better results if drawing precision is required,
     *                          especially when stacking multiple arcs, but can potentially use more points)</li>
     *                          <li>if {@code false}, split the arc equally so that each part is shorter than
     *                          the <i>threshold</i></li>
     *                          </ul>
     * @param addToPath         An existing path where to add the arc to, or {@code null} to create a new path.
     * @return {@code addToPath} if it's not {@code null}, otherwise a new path.
     * @see #createBezierArcDegrees(android.graphics.PointF, float, float, float, int, boolean, android.graphics.Path)
     */
    @NonNull
    private static Path createBezierArcRadians(@NonNull PointF center, float radius, double startAngleRadians,
                                              double sweepAngleRadians, int pointsOnCircle, boolean overlapPoints,
                                              Path addToPath) {
        final Path path = addToPath != null ? addToPath : new Path();
        if (sweepAngleRadians == 0d) {
            return path;
        }

        if (pointsOnCircle >= 1) {
            final double threshold = FULL_CIRCLE_RADIANS / pointsOnCircle;
            if (abs(sweepAngleRadians) > threshold) {
                double angle = normalizeRadians(startAngleRadians);
                PointF end, start = pointFromAngleRadians(center, radius, angle);
                path.moveTo(start.x, start.y);
                if (overlapPoints) {
                    final boolean cw = sweepAngleRadians > 0; // clockwise?
                    final double angleEnd = angle + sweepAngleRadians;
                    while (true) {
                        double next = (cw ? ceil(angle / threshold) : floor(angle / threshold)) * threshold;
                        if (angle == next) {
                            next += threshold * (cw ? 1d : -1d);
                        }
                        final boolean isEnd = cw ? angleEnd <= next : angleEnd >= next;
                        end = pointFromAngleRadians(center, radius, isEnd ? angleEnd : next);
                        addBezierArcToPath(path, center, start, end, false);
                        if (isEnd) {
                            break;
                        }
                        angle = next;
                        start = end;
                    }
                } else {
                    final int n = abs((int) ceil(sweepAngleRadians / threshold));
                    final double sweep = sweepAngleRadians / n;
                    for (int i = 0;
                         i < n;
                         i++, start = end) {
                        angle += sweep;
                        end = pointFromAngleRadians(center, radius, angle);
                        addBezierArcToPath(path, center, start, end, false);
                    }
                }
                return path;
            }
        }

        final PointF start = pointFromAngleRadians(center, radius, startAngleRadians);
        final PointF end = pointFromAngleRadians(center, radius, startAngleRadians + sweepAngleRadians);
        addBezierArcToPath(path, center, start, end, true);
        return path;
    }

    /**
     * Adds a circular arc to the given path by approximating it through a cubic Bézier curve, splitting it if
     * necessary. The precision of the approximation can be adjusted through {@code pointsOnCircle} and
     * {@code overlapPoints} parameters.
     * <p>
     * <strong>Example:</strong> imagine an arc starting from 0° and sweeping 100° with a value of
     * {@code pointsOnCircle} equal to 12 (threshold -> 360° / 12 = 30°):
     * <ul>
     * <li>if {@code overlapPoints} is {@code true}, it will be split as following:
     * <ul>
     * <li>from 0° to 30° (sweep 30°)</li>
     * <li>from 30° to 60° (sweep 30°)</li>
     * <li>from 60° to 90° (sweep 30°)</li>
     * <li>from 90° to 100° (sweep 10°)</li>
     * </ul>
     * </li>
     * <li>if {@code overlapPoints} is {@code false}, it will be split into 4 equal arcs:
     * <ul>
     * <li>from 0° to 25° (sweep 25°)</li>
     * <li>from 25° to 50° (sweep 25°)</li>
     * <li>from 50° to 75° (sweep 25°)</li>
     * <li>from 75° to 100° (sweep 25°)</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     * <p/>
     * For a technical explanation:
     * <a href="http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html">
     * http://hansmuller-flex.blogspot.de/2011/10/more-about-approximating-circular-arcs.html
     * </a>
     *
     * @param center            The center of the circle.
     * @param radius            The radius of the circle.
     * @param startAngleDegrees The starting angle on the circle (in degrees).
     * @param sweepAngleDegrees How long to make the total arc (in degrees).
     * @param pointsOnCircle    Defines a <i>threshold</i> (360° /{@code pointsOnCircle}) to split the Bézier arc to
     *                          better approximate a circular arc, depending also on the value of {@code overlapPoints}.
     *                          The suggested number to have a reasonable approximation of a circle is at least 4 (90°).
     *                          Less than 1 will ignored (the arc will not be split).
     * @param overlapPoints     Given the <i>threshold</i> defined through {@code pointsOnCircle}:
     *                          <ul>
     *                          <li>if {@code true}, split the arc on every angle which is a multiple of the
     *                          <i>threshold</i> (yields better results if drawing precision is required,
     *                          especially when stacking multiple arcs, but can potentially use more points)</li>
     *                          <li>if {@code false}, split the arc equally so that each part is shorter than
     *                          the <i>threshold</i></li>
     *                          </ul>
     * @param addToPath         An existing path where to add the arc to, or {@code null} to create a new path.
     * @return {@code addToPath} if it's not {@code null}, otherwise a new path.
     * @see #createBezierArcRadians(android.graphics.PointF, float, double, double, int, boolean, android.graphics.Path)
     */
    @NonNull
    private static Path createBezierArcDegrees(@NonNull PointF center, float radius, float startAngleDegrees,
                                              float sweepAngleDegrees, int pointsOnCircle, boolean overlapPoints,
                                              Path addToPath) {
        return createBezierArcRadians(center, radius, toRadians(startAngleDegrees), toRadians(sweepAngleDegrees),
                pointsOnCircle, overlapPoints, addToPath);
    }
}