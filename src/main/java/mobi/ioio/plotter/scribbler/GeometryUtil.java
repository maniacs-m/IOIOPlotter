package mobi.ioio.plotter.scribbler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeometryUtil {
    public static float normalizeAngle(float angle) {
        double cycles = angle / (2 * Math.PI);
        double remainder = cycles - Math.floor(cycles);
        return (float) (remainder * 2 * Math.PI);
    }

    /**
     * Calculate intersections of a line with the borders.
     * @param width Image width.
     * @param height Image height.
     * @param x X coordinate of starting point of the line.
     * @param y Y coordinate of starting point of the line.
     * @param angle Angle above horizon of the line.
     * @return An array with 6 elements. Elements [0,1] are the coordinates of the intersection of
     *         the "front" of the line with the borders. Elements [2,3] are the same for the "back"
     *         of the line. Element 4 is the distance to the front intersection and 5 to the back.
     */
    public static float[] intersectLineWithBorders(float width, float height, float x, float y, float angle) {
        angle = normalizeAngle(angle);
        float tana = (float) Math.tan(angle);

        // Intersect with right border.
        float yright = y - (width - x) * tana;
        // Intersect with left border.
        float yleft = y + x * tana;
        // Intersect with bottom border.
        float xbottom = x + (y - height) / tana;
        // Intersect with top border.
        float xtop = x + y / tana;

        float distRight = (float) Math.hypot(y - yright, x - width);
        float distLeft = (float) Math.hypot(y - yleft, x);
        float distBottom = (float) Math.hypot(x - xbottom, y - height);
        float distTop = (float) Math.hypot(x - xtop, y);

        float[] result = new float[6];
        int quarternion = (int) (angle / (Math.PI / 2));
        switch (quarternion) {
            case 0:
                // Intersects with top/right on the front and bottom/left on the back.
                if (distTop < distRight) {
                    result[0] = xtop;
                    result[1] = 0;
                    result[4] = distTop;
                } else {
                    result[0] = width;
                    result[1] = yright;
                    result[4] = distRight;
                }
                if (distBottom < distLeft) {
                    result[2] = xbottom;
                    result[3] = height;
                    result[5] = distBottom;
                } else {
                    result[2] = 0;
                    result[3] = yleft;
                    result[5] = distLeft;
                }
                break;

            case 1:
                // Intersects with top/left on the front and bottom/right on the back.
                if (distTop < distLeft) {
                    result[0] = xtop;
                    result[1] = 0;
                    result[4] = distTop;
                } else {
                    result[0] = 0;
                    result[1] = yleft;
                    result[4] = distLeft;
                }
                if (distBottom < distRight) {
                    result[2] = xbottom;
                    result[3] = height;
                    result[5] = distBottom;
                } else {
                    result[2] = width;
                    result[3] = yright;
                    result[5] = distRight;
                }
                break;

            case 2:
                // Intersects with top/right on the back and bottom/left on the front.
                if (distTop < distRight) {
                    result[2] = xtop;
                    result[3] = 0;
                    result[5] = distTop;
                } else {
                    result[2] = width;
                    result[3] = yright;
                    result[5] = distRight;
                }
                if (distBottom < distLeft) {
                    result[0] = xbottom;
                    result[1] = height;
                    result[4] = distBottom;
                } else {
                    result[0] = 0;
                    result[1] = yleft;
                    result[4] = distLeft;
                }
                break;

            case 3:
                // Intersects with top/left on the back and bottom/right on the front.
                if (distTop < distLeft) {
                    result[2] = xtop;
                    result[3] = 0;
                    result[5] = distTop;
                } else {
                    result[2] = 0;
                    result[3] = yleft;
                    result[5] = distLeft;
                }
                if (distBottom < distRight) {
                    result[0] = xbottom;
                    result[1] = height;
                    result[4] = distBottom;
                } else {
                    result[0] = width;
                    result[1] = yright;
                    result[4] = distRight;
                }
                break;
        }

        return result;
    }

    public static float degToRad(float deg) {
        return (float) (deg * Math.PI / 180);
    }

    public static float[] intersectArcWithLine(float dist, float radius, float angle) {
        if (radius <= dist) return null;
        float a = (float) Math.acos(dist / radius);
        return new float[] { angle + a, angle - a };
    }

    public static float[] intersectArcWithBorders(float width, float height, float centerx, float centery, float radius, float startAngle) {
        ArrayList<Float> intersectionAngles = new ArrayList<Float>(8);

        // Intersect with all borders.
        float[] intersectTop = intersectArcWithLine(Math.abs(centery), radius, (float) Math.copySign(Math.PI / 2, centery));
        float[] intersectBottom = intersectArcWithLine(Math.abs(height - centery), radius, (float) Math.copySign(Math.PI / 2, centery - height));
        float[] intersectLeft = intersectArcWithLine(Math.abs(centerx), radius, centerx < 0 ? 0 : (float) Math.PI);
        float[] intersectRight = intersectArcWithLine(Math.abs(width - centerx), radius, centerx < width ? 0 : (float) Math.PI);

        // Add all intersections to list.
        for (float[] angles : new float[][] {intersectTop, intersectBottom, intersectLeft, intersectRight }) {
            if (angles != null) {
                for (float angle : angles) {
                    intersectionAngles.add(angle);
                }
            }
        }

        // If there are no intersections at all, we can go a full circle in either direction.
        if (intersectionAngles.isEmpty()) return new float[] {(float) (-2 * Math.PI),
                                                              (float) (2 * Math.PI)};

        // Make all angles relative to start angle.
        for (int i = 0; i < intersectionAngles.size(); ++i) {
            intersectionAngles.set(i, normalizeAngle(intersectionAngles.get(i) - startAngle));
        }

        // Sort angles.
        Collections.sort(intersectionAngles);

        // Now, the first entry is the first clockwise intersection and the last is the first counter-clockwise.
        return new float[] {
                intersectionAngles.get(0),
                (float) (intersectionAngles.get(intersectionAngles.size() - 1) - 2 * Math.PI)
        };
    }
}
