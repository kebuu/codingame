import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    static GameState gameState;
    private static boolean debugEnabled;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            gameState = new GameState();

            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int orientation = in.nextInt();
                int speed = in.nextInt();
                int rhum = in.nextInt();
                int mine = in.nextInt();

                if (entityType.equals("BARREL")) {
                    Barrel barrel = new Barrel(entityId, x, y, rhum);
                    gameState.addBarrel(barrel);
                } else if (entityType.equals("SHIP")) {
                    Ship ship = new Ship(entityId, x, y, speed, orientation);
                    gameState.addShip(ship, mine);
                } else {
                    System.err.println("Y a un truc que j'ai pas du piger...");
                }
            }

            for (int i = 0; i < myShipCount; i++) {
                Ship ship = gameState.getShip(i);

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                Optional<Barrel> closestBarrel = gameState.findClosestBarrel(ship);

                if (closestBarrel.isPresent()) {
                    Coordinate barrelCoordinate = closestBarrel.get().coordinate;
                    System.out.println(String.format("MOVE %d %d", barrelCoordinate.x, barrelCoordinate.y));
                } else {
                    System.out.println("WAIT");
                }

            }
        }
    }

    static class GameState {
        List<Ship> myShips = new ArrayList<>();
        List<Ship> enemyShips = new ArrayList<>();
        List<Barrel> barrels = new ArrayList<>();

        void addBarrel(Barrel barrel) {
            barrels.add(barrel);
        }

        Ship getShip(int index) {
            return myShips.get(index);
        }

        void addShip(Ship ship, int mine) {
            if (mine == 1) {
                myShips.add(ship);
            } else {
                enemyShips.add(ship);
            }
        }

        Optional<Barrel> findClosestBarrel(Ship ship) {
             return barrels.stream().min(Comparator.comparing(barrel -> distance(barrel, ship)));
        }

        Double distance(Positioned positioned1, Positioned positioned2) {
            double distance = positioned1.coordinate.distance(positioned2.coordinate);
            log(String.format("Distance : " + distance + " (%s -> %s)",
                    positioned1.coordinate.toString(), positioned2.coordinate.toString()));
            return distance;
        }
    }

    static abstract class Positioned extends Identifiable {
        final Coordinate coordinate;

        Positioned(int id, Coordinate coordinate) {
            super(id);
            this.coordinate = coordinate;
        }

        @Override
        public String toString() {
            return coordinate.toString();
        }
    }

    static abstract class Identifiable {
        final int id;

        Identifiable(int id) {
            this.id = id;
        }
    }

    static class Coordinate {
        final int x;
        final int y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        CubeCoordinate toCubeCoordinate() {
            return new CubeCoordinate(this);
        }

        double distance(Coordinate other) {
            return new CubeCoordinate(this).distance(other.toCubeCoordinate());
        }

        Path pathTo(Coordinate destination, GameState gameState) {
            Queue<PrioritizedCoordinate> queue = new PriorityQueue();
            queue.add(new PrioritizedCoordinate(this, 0.));

            Map<Coordinate, PrioritizedCoordinate> cameFrom = new HashMap<>();
            cameFrom.put(this, null);

            Map<Coordinate, Double> costs = new HashMap<>();
            costs.put(this, 0.);

            while (!queue.isEmpty()) {
                PrioritizedCoordinate currentPrioritizedCoordinate = queue.poll();
                Coordinate currentCoordinate = currentPrioritizedCoordinate.coordinate;

                if (currentCoordinate.equals(destination)) {
                    break;
                }

                List<Coordinate> neighbors = currentCoordinate.neighbors();
                for (Coordinate neighbor : neighbors) {
                    double newCost = costs.get(currentCoordinate) + costBetween(currentCoordinate, neighbor);

                    if (!costs.containsKey(neighbor) || newCost < costs.get(neighbor)) {
                        costs.put(neighbor, newCost);
                        double priority = newCost + heuristic(destination, neighbor);
                        queue.add(new PrioritizedCoordinate(neighbor, priority));
                        cameFrom.put(neighbor, currentPrioritizedCoordinate);
                    }
                }
            }

            Path path = new Path(this, destination);

            Coordinate pathStep = destination;
            while (!pathStep.equals(this)) {
                path.addLast(pathStep);
                PrioritizedCoordinate prioritizedCoordinate = cameFrom.get(pathStep);
                pathStep = prioritizedCoordinate.coordinate;
            }

            return path;
        }

        double heuristic(Coordinate coordinate1, Coordinate coordinate2) {
            return abs(coordinate1.x - coordinate2.x) + abs(coordinate1.y - coordinate2.y);
        }

        double costBetween(Coordinate coordinate1, Coordinate coordinate2) {
            return coordinate1.distance(coordinate2);
        }

        List<Coordinate> neighbors() {
            List<Coordinate> neighbors = new ArrayList<>();

            neighbors.add(this.plusX(1));
            neighbors.add(this.plusX(-1));
            neighbors.add(this.plusY(1));
            neighbors.add(this.plusY(-1));

            if (x % 2 == 0) {
                neighbors.add(this.plusXY(-1));
                neighbors.add(this.plusX(1).plusY(-1));
            } else {
                neighbors.add(this.plusXY(1));
                neighbors.add(this.plusX(-1).plusY(1));
            }

            return neighbors.stream().filter(coordinate -> coordinate.x >= 0 && coordinate.x <= 22 && coordinate.y <= 20 && coordinate.y >= 0).collect(Collectors.toList());
        }

        Coordinate plusX(int value) {
            return new Coordinate(this.x + value, this.y);
        }

        Coordinate plusY(int value) {
            return new Coordinate(this.x, this.y + value);
        }

        Coordinate plusXY(int value) {
            return plusX(value).plusY(value);
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
            return String.format("[%d,%d]", x, y);
        }
    }

    static class CubeCoordinate {
        final int x;
        final int y;
        final int z;

        CubeCoordinate(Coordinate coordinate) {
            x = coordinate.y - (coordinate.x - (coordinate.x % 2)) / 2;
            z = coordinate.x;
            y = -x -z;
        }

        int distance(CubeCoordinate other){
            return IntStream.of(abs(x - other.x), abs(y - other.y), abs(z - other.z)).max().getAsInt();
        }
    }

    static class Ship extends Positioned {
        final int speed;
        final int orientation;

        Ship(int id, Coordinate coordinate, int speed, int orientation) {
            super(id, coordinate);
            this.speed = speed;
            this.orientation = orientation;
        }

        Ship(int id, int x, int y, int speed, int orientation) {
            this(id, new Coordinate(x, y), speed, orientation);
        }
    }

    static class Barrel extends Positioned {
        final int quantity;

        Barrel(int id, Coordinate coordinate, int quantity) {
            super(id, coordinate);
            this.quantity = quantity;
        }

        Barrel(int id, int x, int y, int quantity) {
            this(id, new Coordinate(x, y), quantity);
        }
    }

    static void log(String log) {
        if (debugEnabled) {
            System.err.println(log);
        }
    }

    static class Path {
        final Coordinate from;
        final Coordinate to;

        List<Coordinate> steps = new ArrayList<>();

        Path(Coordinate from, Coordinate to) {
            this.from = from;
            this.to = to;
        }

        boolean isEmpty() {
            return steps.isEmpty();
        }

        int size() {
            return steps.size();
        }

        void addLast(Coordinate coordinate) {
            steps.add(0, coordinate);
        }

        @Override
        public String toString() {
            return steps.stream().map(coordinate -> coordinate.toString()).collect(Collectors.joining());
        }
    }

    static class PrioritizedCoordinate implements Comparable<PrioritizedCoordinate> {
        final Coordinate coordinate;
        final double priority;

        PrioritizedCoordinate(Coordinate coordinate, double priority) {
            this.coordinate = coordinate;
            this.priority = priority;
        }

        @Override
        public int compareTo(PrioritizedCoordinate prioritizedCoordinate) {
            return priority >= prioritizedCoordinate.priority ? 1 : -1;
        }

        @Override
        public String toString() {
            return coordinate.toString() + "(" + priority + ")";
        }
    }
}