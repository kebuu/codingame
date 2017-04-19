import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.abs;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    private static final double MAX_SHOOTABLE_DISTANCE = 10;
    public static final int FIRE_COOLDOWN = 1;

    static GameState gameState;
    static Set<Mine> knownMines = new HashSet<>();
    static int currentTurn;
    static Ship currentShip;
    static Map<Integer, Integer> lastFireTurns = new HashMap<>();
    static Map<Integer, Coordinate> moveAwayDestination = new HashMap<Integer, Coordinate>() {{
        put(0, new Coordinate(0, 0));
        put(1, new Coordinate(0, 20));
        put(2, new Coordinate(20, 20));
    }};
    private static boolean debugEnabled = true;

    public static void main(String args[]) {
        CompositeStrategy strategy = new CompositeStrategy();
        //strategy.add(new MoveAwayStrategy());
        strategy.add(new EscapeCanonBallStrategy());
        strategy.add(new FireStaticStrategy());
        strategy.add(new ClosestBarrelStrategy());
        strategy.add(new SimpleFireStrategy());
       // strategy.add(new BarrelFireStrategy());
        //strategy.add(new StopStrategy());

        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            currentTurn++;
            gameState = new GameState();

            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int orientationOrBarrelRhumQuantity = in.nextInt();
                int shipSpeedOrCannonBallImpactDelay = in.nextInt();
                int rhumInShip = in.nextInt();
                int isMine = in.nextInt();

                if (entityType.equals("BARREL")) {
                    Barrel barrel = new Barrel(entityId, x, y, rhumInShip);
                    gameState.addBarrel(barrel);
                } else if (entityType.equals("SHIP")) {
                    Ship ship = new Ship(entityId, x, y, shipSpeedOrCannonBallImpactDelay, orientationOrBarrelRhumQuantity);
                    gameState.addShip(ship, isMine);
                } else if (entityType.equals("MINE")) {
                    Mine mine = new Mine(entityId, x, y);
                    knownMines.add(mine);
                } else if (entityType.equals("CANNONBALL")) {
                    CannonBall cannonBall = new CannonBall(entityId, x, y, shipSpeedOrCannonBallImpactDelay);
                    gameState.addCannonBall(cannonBall);
                } else {
                    throw new RuntimeException("Y a un truc que j'ai pas du piger...");
                }
            }

            knownMines.removeIf(mine -> gameState.getShipAt(mine.coordinate).isPresent());

            gameState.addAllMines(knownMines);

            for (int i = 0; i < myShipCount; i++) {
                Ship ship = gameState.getShip(i);
                currentShip = ship;
                log(ship);

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                Action action = strategy.getAction(ship, gameState);
                System.out.println(action.execute());
            }
        }
    }

    static class GameState {
        List<Ship> myShips = new ArrayList<>();
        List<Ship> enemyShips = new ArrayList<>();
        List<Barrel> barrels = new ArrayList<>();
        Set<Mine> mines = new HashSet<>();
        List<CannonBall> cannonBalls = new ArrayList<>();

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

        Optional<Barrel> findClosestSafeBarrel(Ship ship, Collection<Barrel> excludedBarrels) {
             return barrels.stream()
                     .filter(barrel -> !excludedBarrels.contains(barrel))
                     .filter(barrel -> !gameState.hasMine(barrel.coordinate))
                     .min(Comparator.comparing(barrel -> distance(barrel, ship)));
        }

        private boolean hasMine(Coordinate coordinate) {
            return mines.stream().anyMatch(mine -> mine.coordinate.equals(coordinate));
        }

        private boolean receivesCannonBallNextTurn(Coordinate coordinate) {
            return cannonBalls.stream()
                    .filter(cannonBall -> cannonBall.impactDelay == 1)
                    .anyMatch(cannonBall -> cannonBall.target.equals(coordinate));
        }

        private int turnToCannonBallImpact(Ship source, Coordinate target) {
            return turnToCannonBallImpact(source.shipCoordinates().head, target);
        }

        private int turnToCannonBallImpact(Coordinate source, Coordinate target) {
            return (int) (1 + Math.round(source.distance(target) / 3.));
        }

        Double distance(Positioned positioned1, Positioned positioned2) {
            return positioned1.coordinate.distance(positioned2.coordinate);
        }

        void addCannonBall(CannonBall cannonBall) {
            cannonBalls.add(cannonBall);
        }

        void addAllMines(Set<Mine> mines) {
            this.mines.addAll(mines);
        }

        public Optional<Ship> getShipAt(Coordinate coordinate) {
            return Stream.concat(myShips.stream(), enemyShips.stream())
                .filter(ship -> ship.shipCoordinates().asList().contains(coordinate))
                .findFirst();
        }

        public Optional<Ship> findClosestShootableShipFrom(Ship ship) {
            Ship closestShip = null;
            double distanceFromClosestShip = -1;

            for (Ship enemyShip : enemyShips) {
                double distance = enemyShip.distance(ship.shipCoordinates().head);

                if ((closestShip == null || distance < distanceFromClosestShip) && distance <= MAX_SHOOTABLE_DISTANCE) {
                    closestShip = enemyShip;
                    distanceFromClosestShip = distance;
                }
            }
            return Optional.ofNullable(closestShip);
        }

        Optional<Barrel> findClosestBarrel(Ship ship) {
            return barrels.stream()
                    .min(Comparator.comparing(barrel -> distance(barrel, ship)));
        }
    }

    static abstract class Positioned extends Identifiable {
        final Coordinate coordinate;

        Positioned(int id, Coordinate coordinate) {
            super(id);
            this.coordinate = coordinate;
        }

        Positioned(int id, int x, int y) {
            this(id, new Coordinate(x, y));
        }

        Optional<Path> pathTo(Positioned destination, GameState gameState) {
            return coordinate.pathTo(destination.coordinate, gameState, PathSearchMode.SAFE);
        }

        Optional<Path> pathTo(Positioned destination, GameState gameState, PathSearchMode mode) {
            return coordinate.pathTo(destination.coordinate, gameState, mode);
        }

        double distance(Positioned positioned) {
            return coordinate.distance(positioned);
        }

        double distance(Coordinate coordinate) {
            return this.coordinate.distance(coordinate);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Identifiable that = (Identifiable) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
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

        double distance(Positioned positioned) {
            return distance(positioned.coordinate);
        }

        Optional<Path> pathTo(Coordinate destination, GameState gameState) {
            return pathTo(destination, gameState, PathSearchMode.SAFE);
        }

        boolean isRowPair() {
            return y % 2 == 0;
        }

        Optional<Path> pathTo(Coordinate destination, GameState gameState, PathSearchMode mode) {
            Queue<PrioritizedCoordinate> queue = new PriorityQueue();
            queue.add(new PrioritizedCoordinate(this, 0.));

            Map<Coordinate, PrioritizedCoordinate> cameFrom = new HashMap<>();
            cameFrom.put(this, null);

            Map<Coordinate, Double> costs = new HashMap<>();
            costs.put(this, 0.);

            boolean pathFound = false;
            while (!queue.isEmpty()) {
                PrioritizedCoordinate currentPrioritizedCoordinate = queue.poll();
                Coordinate currentCoordinate = currentPrioritizedCoordinate.coordinate;

                if (currentCoordinate.equals(destination)) {
                    pathFound = true;
                    break;
                }

                List<Coordinate> neighbors = currentCoordinate.neighbors();
                for (Coordinate neighbor : neighbors) {

                    if (mode.equals(PathSearchMode.UNSAFE) || isNeighborSafe(neighbor, currentCoordinate, gameState)) {
                        double newCost = costs.get(currentCoordinate) + costBetween(currentCoordinate, neighbor);

                        if (!costs.containsKey(neighbor) || newCost < costs.get(neighbor)) {
                            costs.put(neighbor, newCost);
                            double priority = newCost + heuristic(destination, neighbor);
                            queue.add(new PrioritizedCoordinate(neighbor, priority));
                            cameFrom.put(neighbor, currentPrioritizedCoordinate);
                        }
                    }
                }
            }

            Path path = new Path(this, destination);

            Coordinate pathStep = destination;
            while (pathFound && !pathStep.equals(this)) {
                path.addFirst(pathStep);
                PrioritizedCoordinate prioritizedCoordinate = cameFrom.get(pathStep);
                pathStep = prioritizedCoordinate.coordinate;
            }

            return path.isEmpty() ? Optional.empty() : Optional.of(path);
        }

        boolean isNeighborSafe(Coordinate neighbor, Coordinate baseCoordinate, GameState gameState) {
            boolean neighborHasMine = gameState.hasMine(neighbor);

            boolean shipWillReceiveCannonBallNextTurnAfterMoving = false;
            if (baseCoordinate.equals(this)) {
                shipWillReceiveCannonBallNextTurnAfterMoving = gameState.receivesCannonBallNextTurn(neighbor);
            }

            return !neighborHasMine && !shipWillReceiveCannonBallNextTurnAfterMoving;
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

            if (isRowPair()) {
                neighbors.add(this.plusXY(-1));
                neighbors.add(this.plusX(-1).plusY(1));
            } else {
                neighbors.add(this.plusXY(1));
                neighbors.add(this.plusX(1).plusY(-1));
            }

            // histoire que l'avant du bateau soit le premier voisin (afin d'essayer de garder le cap)
            if (currentShip != null) {
                Coordinate head = currentShip.shipCoordinates().head;
                neighbors.remove(head);
                neighbors.add(0, head);
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

        int sumXY() {
            return x + y;
        }

        int orientationTo(Coordinate destination) {
            CubeCoordinate fromCubeCoordinate = this.toCubeCoordinate();
            CubeCoordinate nextStepCubeCoordinate = destination.toCubeCoordinate();

            if (fromCubeCoordinate.x == nextStepCubeCoordinate.x) {
                if (this.sumXY() > destination.sumXY()) {
                    return 2;
                } else {
                    return 5;
                }
            } else if (fromCubeCoordinate.y == nextStepCubeCoordinate.y) {
                if (this.y > destination.y) {
                    return 1;
                } else {
                    return 4;
                }
            } else {
                if (this.sumXY() > destination.sumXY()) {
                    return 3;
                } else {
                    return 0;
                }
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
            return String.format("[%d,%d]", x, y);
        }

        public boolean isValid() {
            return x > 0 && y > 0 && x < 23 && y < 21;
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

        @Override
        public String toString() {
            return String.format("[%d,%d,%d]", x, y, z);
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

        ShipCoordinates shipCoordinates(){
            ShipCoordinates shipCoordinates = new ShipCoordinates(coordinate, orientation);

            if (orientation % 3 == 0) {
                shipCoordinates.addHeadAndTail(coordinate.plusX(1), coordinate.plusX(-1));
            } else if (orientation % 3 == 1) {
                if (coordinate.isRowPair()) {
                    shipCoordinates.addHeadAndTail(coordinate.plusY(-1), coordinate.plusY(1).plusX(-1));
                } else {
                    shipCoordinates.addHeadAndTail(coordinate.plusY(-1).plusX(1), coordinate.plusY(1));
                }
            } else {
                if (coordinate.isRowPair()) {
                    shipCoordinates.addHeadAndTail(coordinate.plusY(-1).plusX(-1), coordinate.plusY(1));
                } else {
                    shipCoordinates.addHeadAndTail(coordinate.plusY(-1), coordinate.plusY(1).plusX(1));
                }
            }

            return shipCoordinates;
        }

        @Override
        public String toString() {
            return super.toString() + String.format("(id:%d, speed : %d, orientation: %d, coordinates: %s)", id, speed, orientation, shipCoordinates().asList().toString());
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

        @Override
        public String toString() {
            return super.toString() + "(" + quantity + ")";
        }
    }

    static void log(Object object) {
        if (debugEnabled) {
            System.err.println(object);
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

        void addFirst(Coordinate coordinate) {
            steps.add(0, coordinate);
        }

        @Override
        public String toString() {
            return steps.stream().map(coordinate -> coordinate.toString()).collect(Collectors.joining());
        }

        Coordinate firstStep() {
            return steps.get(0);
        }
        boolean isNextStepStraightForward(Ship ship) {
            log("isNextStepStraightForward:" + ship.shipCoordinates().head + "-" + firstStep());
            return ship.shipCoordinates().head.equals(firstStep());
        }

        public List<Coordinate> first(int firstN) {
            return steps.stream().limit(firstN).collect(Collectors.toList());
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

    static class Mine extends Positioned {
        Mine(int id, int x, int y) {
            super(id, x, y);
        }

        public Mine(int size, Coordinate coordinate) {
            super(size, coordinate);
        }
    }

    static class CannonBall extends Identifiable{
        final Coordinate target;
        final int impactDelay;

        public CannonBall(int entityId, int x, int y, int shipSpeedOrCannonBallImpactDelay) {
            super(entityId);
            target = new Coordinate(x, y);
            impactDelay = shipSpeedOrCannonBallImpactDelay;
        }

        @Override
        public String toString() {
            return String.format("CannonBall - [%d, %d](%d)", target.x, target.y, impactDelay);
        }
    }

    static class ShipCoordinates {
        static final List<Integer> FORWARD_ORIENTATIONS = Arrays.asList(4, 5, 0);

        Coordinate head;
        Coordinate tail;
        Coordinate center;
        final int orientation;

        ShipCoordinates(Coordinate coordinate, int orientation) {
            center = coordinate;
            this.orientation = orientation;
        }

        void addHeadAndTail(Coordinate coordinate1, Coordinate coordinate2) {
            int coordinate1SumXY = coordinate1.sumXY();
            int coordinate2SumXY = coordinate2.sumXY();

            if ((FORWARD_ORIENTATIONS.contains(orientation) && coordinate1SumXY > coordinate2SumXY) ||
                    (!FORWARD_ORIENTATIONS.contains(orientation) && coordinate1SumXY < coordinate2SumXY)  ) {
                head = coordinate1;
                tail = coordinate2;
            } else {
                head = coordinate2;
                tail = coordinate1;
            }
        }

        List<Coordinate> asList() {
            return Arrays.asList(head, center, tail);
        }
    }

    enum PathSearchMode {
        SAFE, UNSAFE
    }

    interface Action {
        String execute();
    }

    static class WaitAction implements Action {
        public String execute() {
            return "WAIT";
        }
    }

    static class MoveAction implements Action {

        private Coordinate coordinate;

        MoveAction(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public String execute() {
            return String.format("MOVE %d %d", coordinate.x, coordinate.y);
        }
    }

    static class FireAction implements Action {

        Coordinate coordinate;

        FireAction(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public String execute() {
            return String.format("FIRE %d %d", coordinate.x, coordinate.y);
        }
    }

    static class MineAction implements Action {
        public String execute() {
            return "MINE";
        }
    }

    static class SlowerAction implements Action {
        public String execute() {
            return "SLOWER";
        }
    }

    static class FasterAction implements Action {
        public String execute() {
            return "FASTER";
        }
    }

    static class CompositeStrategy implements Strategy {
        List<Strategy> strategies = new ArrayList<>();

        void add(Strategy strategy) {
            strategies.add(strategy);
        }

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            for (Strategy strategy : strategies) {
                action = strategy.getAction(ship, gameState);
                if (action != null) {
                    break;
                }
            }
            return action == null ? new WaitAction() : action;
        }
    }

    interface Strategy {
        Action getAction(Ship ship, GameState gameState);
    }

    static class ClosestBarrelStrategy implements Strategy {

        private Map<Integer, Barrel> lastTargetedBarrels = new HashMap<>();

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;
            cleanLastTargetBarrelsMap(ship, gameState);

            Collection<Barrel> otherTargetedBarrels = getOtherTargetedBarrels(ship);
            Optional<Barrel> closestSafeBarrelOrNot = gameState.findClosestSafeBarrel(ship, otherTargetedBarrels);

            if (closestSafeBarrelOrNot.isPresent()) {
                Barrel closestSafeBarrel = closestSafeBarrelOrNot.get();

                Optional<Path> pathUnsafeOrNot = ship.pathTo(closestSafeBarrel, gameState, PathSearchMode.UNSAFE);

                if (pathUnsafeOrNot.isPresent()) {
//                    if (pathUnsafeOrNot.get().first(3).stream().anyMatch(coordinate -> gameState.hasMine(coordinate))) {
//                        Optional<Path> pathSafeOrNot = ship.pathTo(closestSafeBarrel, gameState, PathSearchMode.SAFE);
//                        if (pathSafeOrNot.isPresent()) {
//                            log("detour");
//                            action = new MoveAction(pathSafeOrNot.get().firstStep());
//                        }
//                    }

//                    if (action == null) {
                        boolean targetBarrelChanged = !closestSafeBarrel.equals(lastTargetedBarrels.get(ship.id));
                        boolean shipIsInNotGoodDirection = !pathUnsafeOrNot.get().isNextStepStraightForward(ship);
                    log("shipIsInNotGoodDirection:"+shipIsInNotGoodDirection);
                        if (targetBarrelChanged || shipIsInNotGoodDirection || ship.speed == 0) {
                            lastTargetedBarrels.put(ship.id, closestSafeBarrel);
                            action = new MoveAction(closestSafeBarrel.coordinate);
                        } else {
                            log("continuing");
                        }
//                    }
                } else {
                    lastTargetedBarrels.remove(ship.id);
                }
            } else {
                lastTargetedBarrels.remove(ship.id);
            }

            return action;
        }

        private void cleanLastTargetBarrelsMap(Ship ship, GameState gameState) {
            List<Integer> shipIds = gameState.myShips.stream().map(myShip -> myShip.id).collect(Collectors.toList());

            for (Integer shipId : lastTargetedBarrels.keySet()) {
                if (!shipIds.contains(shipId)) {

                    log(shipIds);
                    log("Clean " + shipId);
                    lastTargetedBarrels.remove(shipId);
                }
            }
        }

        private Collection<Barrel> getOtherTargetedBarrels(Ship ship) {
            HashMap<Integer, Barrel> lastOtherTargetedBarrels = new HashMap<>(lastTargetedBarrels);
            lastOtherTargetedBarrels.remove(ship.id);
            return lastOtherTargetedBarrels.values();
        }
    }

    static class SimpleFireStrategy implements Strategy {
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            Integer lastFireTurn = lastFireTurns.get(ship.id);
            if (lastFireTurn == null || lastFireTurn + FIRE_COOLDOWN < currentTurn) {
                Optional<Ship> targetOrNot = gameState.findClosestShootableShipFrom(ship);

                if (targetOrNot.isPresent()) {
                    Ship targetShip = targetOrNot.get();
                    ShipCoordinates shipCoordinates = targetShip.shipCoordinates();

                    Coordinate target;
                    if (targetShip.speed == 0 || !shipCoordinates.head.isValid()) {
                        target = shipCoordinates.center;
                    } else {
                        target = shipCoordinates.head;
                    }
                    lastFireTurns.put(ship.id, currentTurn);
                    log("SimpleFireStrategy");
                    action = new FireAction(target);
                }
            }
            return action;
        }
    }

    static class BarrelFireStrategy implements Strategy {
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            Integer lastFireTurn = lastFireTurns.get(ship.id);
            if ((lastFireTurn == null || lastFireTurn + 1 < currentTurn) && !gameState.barrels.isEmpty()) {
                Optional<ShipAndBarrel> shipAndBarrelOptional = gameState.enemyShips.stream()
                        .map(enemyShip -> new ShipAndBarrel(enemyShip, gameState.findClosestBarrel(enemyShip).get()))
                        .filter(shipAndBarrel -> {
                            Double shipDistanceToBarrel = gameState.distance(shipAndBarrel.barrel, ship);
                            int cannonBallDelayImpact = gameState.turnToCannonBallImpact(ship, shipAndBarrel.barrel.coordinate);
                            return shipDistanceToBarrel == cannonBallDelayImpact && shipDistanceToBarrel < MAX_SHOOTABLE_DISTANCE;
                        })
                        .findFirst();

                if (shipAndBarrelOptional.isPresent()) {
                    lastFireTurns.put(ship.id, currentTurn);
                    action = new FireAction(shipAndBarrelOptional.get().barrel.coordinate);
                }
            }
            return action;
        }
    }

    static class ShipAndBarrel {
        final Ship ship;
        final Barrel barrel;

        ShipAndBarrel(Ship ship, Barrel barrel) {
            this.ship = ship;
            this.barrel = barrel;
        }
    }

    static class EscapeCanonBallStrategy implements Strategy {

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            ShipCoordinates shipCoordinates = ship.shipCoordinates();
            List<Coordinate> coordinates = shipCoordinates.asList();
            boolean shipIsTargeted = gameState.cannonBalls.stream()
                    .map(cannonBall -> cannonBall.target)
                    .anyMatch(coordinates::contains);

            if (shipIsTargeted) {
                if (ship.speed == 0) {
                    log("Targeted !! Move on");

                    Coordinate escapeDestination;
                    if (shipCoordinates.head.isValid()) {
                        escapeDestination = shipCoordinates.head;
                    }  else {
                        escapeDestination = shipCoordinates.tail;
                    }
                    action = new MoveAction(escapeDestination);
                }
            }
            return action;
        }
    }

    static class StopStrategy implements Strategy {

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            return new SlowerAction();
        }
    }

    static class MoveAwayStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            boolean shouldMoveAway = ship.coordinate.neighbors().stream().anyMatch(coordinate -> {
                Optional<Ship> shipAt = gameState.getShipAt(coordinate);
                return shipAt.isPresent() && !shipAt.get().equals(ship);
            });

            if (shouldMoveAway) {
                log("Moving away");
                return new MoveAction(moveAwayDestination.get(ship.id));
            } else {
                return null;
            }
        }
    }

    static class FireStaticStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Optional<Ship> staticShootableEnemyOrNot = gameState.enemyShips.stream()
                    .filter(enemyShip -> {
                        return  enemyShip.speed == 0 &&
                                enemyShip.distance(ship.shipCoordinates().head) <= MAX_SHOOTABLE_DISTANCE;
                    })
                    .findFirst();

            if (staticShootableEnemyOrNot.isPresent()) {
                log("FireStaticStrategy");
                return new FireAction(staticShootableEnemyOrNot.get().coordinate);
            } else {
                return null;
            }
        }
    }
}