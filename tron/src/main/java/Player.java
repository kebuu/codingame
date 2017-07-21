import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            int N = in.nextInt(); // total number of players (2 to 4).
            int P = in.nextInt(); // your player number (0 to 3).
            for (int i = 0; i < N; i++) {
                int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
                int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("LEFT"); // A single line with UP, DOWN, LEFT or RIGHT
        }
    }

    static class TronGameState {
        final int maxX = 29;
        final int maxY = 19;
        final Lumicycle myLumiCycle;
        final List<Lumicycle> othersLumiCycle;

        TronGameState(Lumicycle myLumiCycle, List<Lumicycle> othersLumiCycle) {
            this.myLumiCycle = myLumiCycle;
            this.othersLumiCycle = othersLumiCycle;
        }
    }

    static class TronGameAction {
        final LumicycleDirection direction;

        TronGameAction(LumicycleDirection direction) {
            this.direction = direction;
        }

        String asString() {
            return direction.name();
        }
    }

    static class Lumicycle {
        final int id;
        final Coordinate currentPosition;
        final LumicycleDirection currentDirection;
        final List<Coordinate> oldPositions;

        Lumicycle(int id, Coordinate currentPosition, LumicycleDirection currentDirection) {
            this(id,currentPosition,currentDirection, new ArrayList<>());
        }

        Lumicycle(int id, Coordinate currentPosition, LumicycleDirection currentDirection, List<Coordinate> oldPositions) {
            this.id = id;
            this.currentPosition = currentPosition;
            this.currentDirection = currentDirection;
            this.oldPositions = oldPositions;
        }

        Lumicycle setNewPosition(Coordinate newPosition) {
            ArrayList<Coordinate> oldPositions = new ArrayList<>();
            oldPositions.addAll(this.oldPositions);
            oldPositions.add(currentPosition);
            return new Lumicycle(id, newPosition, currentPosition.directionTo(newPosition), oldPositions);
        }
    }

    enum LumicycleDirection {
        UP, DOWN, LEFT, RIGHT;
    }

    static class Coordinate {
        final int x;
        final int y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        LumicycleDirection directionTo(Coordinate toCoordinate) {
            if (x < toCoordinate.x) {
                return LumicycleDirection.RIGHT;
            } else if (x > toCoordinate.x) {
                return LumicycleDirection.LEFT;
            } else if (y < toCoordinate.y) {
                return LumicycleDirection.UP;
            } else {
                return LumicycleDirection.DOWN;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coordinate that = (Coordinate) o;

            if (x != that.x) return false;
            return y == that.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public String toString() {
            return '[' + x + " " + y + ']';
        }
    }

    Coordinate xy(int x, int y) {
        return new Coordinate(x, y);
    }
}