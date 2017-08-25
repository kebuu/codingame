import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Ground ground = new Ground();

        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.
        for (int i = 0; i < surfaceN; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.

            ground.addPoint(landX, landY);
        }

        Flat flat = ground.getFlat();
        System.err.println(flat);

        // game loop
        while (true) {
            int X = in.nextInt();
            int Y = in.nextInt();
            int hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
            int fuel = in.nextInt(); // the quantity of remaining fuel in liters.
            int rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            int power = in.nextInt(); // the thrust power (0 to 4).

            Lander lander = new Lander(X, Y, hSpeed, vSpeed, fuel, rotate, power);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            LanderPosition landerPosition = lander.positionFromFlat(flat);
            System.err.println(landerPosition);

            LanderResponse response = new LanderResponse(0, 4);

            if (landerPosition == LanderPosition.ABOVE) {
                if (Math.abs(hSpeed) > 20) {
                    int rotation = 45 * (int) Math.signum((double)hSpeed);
                    response = new LanderResponse(rotation, 4);
                } else {
                    if (Math.abs(vSpeed) > 35) {
                        response = new LanderResponse(0, 4);
                    } else {
                        response = new LanderResponse(0, 2);
                    }
                }
            } else if (landerPosition == LanderPosition.LEFT) {
                response = new LanderResponse(-30, 4);
            } else if (landerPosition == LanderPosition.RIGHT) {
                response = new LanderResponse(30, 4);
            }

            if (hSpeed > 30) {
                response = new LanderResponse(30, 4);
            } else if (hSpeed < -30) {
                response = new LanderResponse(-30, 4);
            }

            if (Y < flat.rightPoint.y + 150) {
                response = response.withoutRotation();
            }

            System.out.println(response);
        }
    }

    static class Lander {
        final int x;
        final int y;
        final int hSpeed;
        final int vSpeed;
        final int fuel;
        final int rotate;
        final int power;

        Lander(int x, int y, int hSpeed, int vSpeed, int fuel, int rotate, int power) {
            this.x = x;
            this.y = y;
            this.hSpeed = hSpeed;
            this.vSpeed = vSpeed;
            this.fuel = fuel;
            this.rotate = rotate;
            this.power = power;
        }

        LanderPosition positionFromFlat(Flat flat) {
            if (flat.leftPoint.x > x - 400) {
                return LanderPosition.LEFT;
            } else if (flat.rightPoint.x < x + 400) {
                return LanderPosition.RIGHT;
            } else {
                return LanderPosition.ABOVE;
            }
        }
    }

    enum LanderPosition {
        LEFT, RIGHT, ABOVE
    }

    static class LanderResponse {
        final int rotate;
        final int power;

        LanderResponse(int rotate, int power) {
            this.rotate = rotate;
            this.power = power;
        }

        @Override
        public String toString() {
            return rotate + " " + power;
        }

        public LanderResponse withoutRotation() {
            return new LanderResponse(0, power);
        }
    }

    static class Ground {
        List<GroundPoint> points = new ArrayList<>();

        void addPoint(GroundPoint groundPoint) {
            points.add(groundPoint);
        }

        void addPoint(int x, int y) {
            addPoint(new GroundPoint(x, y));
        }

        Flat getFlat() {
            for(int i = 1; i < points.size(); i++) {
                GroundPoint pointiMinus1 = points.get(i - 1);
                GroundPoint pointi = points.get(i);

                if (pointiMinus1.y == pointi.y) {
                    return new Flat(pointiMinus1, pointi);
                }
            }

            throw new IllegalStateException();
        }
    }

    static class GroundPoint {
        final int x;
        final int y;

        GroundPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }

    static class Flat {
        final GroundPoint leftPoint;
        final GroundPoint rightPoint;

        Flat(GroundPoint leftPoint, GroundPoint rightPoint) {
            this.leftPoint = leftPoint;
            this.rightPoint = rightPoint;
        }

        @Override
        public String toString() {
            return "[" + leftPoint + "-" + rightPoint + "]";
        }
    }
}