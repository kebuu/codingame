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
    static Map<Ship, Coordinate> shipMoves = new HashMap<>();
    static Map<Integer, List<Coordinate>> shipDestroyedMines = new HashMap<>();

    private static boolean debugEnabled = true;

    public static void main(String args[]) {
        CompositeStrategy strategy = new CompositeStrategy();
        strategy.add(new FireStaticStrategy(true));
        strategy.add(new EscapeCanonBallStrategy());
        strategy.add(new EscapeCanonBallStrategyInCourse());
        strategy.add(new AvoidMineStrategy());
        strategy.add(new ClosestBarrelStrategy());
        //strategy.add(new DestroyMineStrategy());
        //strategy.add(new BarrelFireStrategy());
        strategy.add(new SimpleFireStrategy());
        strategy.add(new DestroyMineOnEnemyStrategy());
        strategy.add(new FireStaticStrategy(false));
        strategy.add(new PrepareMoveStrategy());
        strategy.add(new GoToFightStrategy());

        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            currentTurn++;
            //knownMines.clear();
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
                    Barrel barrel = new Barrel(entityId, x, y, orientationOrBarrelRhumQuantity);
                    gameState.addBarrel(barrel);
                } else if (entityType.equals("SHIP")) {
                    Ship ship = new Ship(entityId, x, y, shipSpeedOrCannonBallImpactDelay, orientationOrBarrelRhumQuantity);
                    ship.rhum = rhumInShip;
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
            knownMines.removeIf(mine -> gameState.cannonBalls.stream().anyMatch(cannonBall -> cannonBall.impactDelay == 1 && cannonBall.target.equals(mine.coordinate)));
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

            Stream<Barrel> sorted1 = barrels.stream()
                    .filter(barrel -> !excludedBarrels.contains(barrel))
                    .filter(barrel -> !gameState.hasMine(barrel.coordinate))
                    .sorted(Comparator.comparing(barrel -> distance(barrel, ship)));

            Stream<Barrel> sorted2 = barrels.stream()
                    .filter(barrel -> !excludedBarrels.contains(barrel))
                    .filter(barrel -> !gameState.hasMine(barrel.coordinate))
                    .sorted(Comparator.comparing(barrel -> distance(barrel, ship)));

            Optional<Barrel> firstCompletelySafe = sorted1
                    .filter(barrel -> {
                        Optional<Path> pathOrNot = ship.inNTurns(1).pathTo(barrel, gameState, PathSearchMode.UNSAFE);
                        if (pathOrNot.isPresent()) {
                            for (Coordinate step : pathOrNot.get().steps) {
                                for (Coordinate neighbor : step.neighbors()) {
                                    if (gameState.hasMine(neighbor)) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        } else {
                            return false;
                        }

                    })
                    .limit(1)
                    .findFirst();

            return firstCompletelySafe.isPresent() ? firstCompletelySafe : sorted2.limit(1).findFirst();
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

        Optional<Shoot> findBestShootFor(Ship ship, GameState gameState) {
            Shoot shoot = null;
            List<Ship> enemiesSortedByDistance = enemyShips.stream()
                    .sorted((ship1, ship2) -> ship1.rhum < ship2.rhum ? -1 : 1)
                    .collect(Collectors.toList());

            for (Ship enemyShip : enemiesSortedByDistance) {
                for (int i = 1; i <= 5; i++) {
                    Ship enemyShipInNTurns = enemyShip.inNTurns(i);
                    ShipCoordinates currentShipCoordinates = enemyShipInNTurns.shipCoordinates();

                    int turnToCannonBallImpactHead = gameState.turnToCannonBallImpact(ship, currentShipCoordinates.head);
                    int turnToCannonBallImpactCenter = gameState.turnToCannonBallImpact(ship, currentShipCoordinates.center);
                    int turnToCannonBallImpactTail = gameState.turnToCannonBallImpact(ship, currentShipCoordinates.tail);

                    if (turnToCannonBallImpactCenter == i && ship.distance(currentShipCoordinates.center) <= MAX_SHOOTABLE_DISTANCE) {
                        shoot = new Shoot(enemyShip, currentShipCoordinates.center);
                    } else if (turnToCannonBallImpactHead == i && ship.distance(currentShipCoordinates.head) <= MAX_SHOOTABLE_DISTANCE) {
                        shoot = new Shoot(enemyShip, currentShipCoordinates.head);
                    } else if (turnToCannonBallImpactTail == i && ship.distance(currentShipCoordinates.tail) <= MAX_SHOOTABLE_DISTANCE) {
                        shoot = new Shoot(enemyShip, currentShipCoordinates.tail);
                    }

                    if (shoot != null) {
                        break;
                    }
                }
                if (shoot != null) {
                    break;
                }
            }
            return Optional.ofNullable(shoot);
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
            return pathTo(destination, gameState, PathSearchMode.UNSAFE);
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
        int rhum;

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

        Ship inNTurns(int steps) {
            Ship ship = this;

            for (int i = 0; i < steps; i++) {
                for (int j = 0; j < ship.speed; j++) {
                    Coordinate shipHead = ship.shipCoordinates().head;
                    int speed = ship.speed;
                    if (!shipHead.isValid() || gameState.getShipAt(shipHead).isPresent()) {
                        speed = 0;
                    }
                    ship = new Ship(ship.id, shipHead, speed, ship.orientation);
                }
            }
            return ship;
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
            shipMoves.put(currentShip, coordinate);
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

    static class PortAction implements Action {
        public String execute() {
            return "PORT";
        }
    }

    static class StarboardAction implements Action {
        public String execute() {
            return "STARBOARD";
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
                    boolean targetBarrelChanged = !closestSafeBarrel.equals(lastTargetedBarrels.get(ship.id));
                    boolean shipIsInNotGoodDirection = !pathUnsafeOrNot.get().isNextStepStraightForward(ship);

                    if (targetBarrelChanged || shipIsInNotGoodDirection || ship.speed == 0) {
                        lastTargetedBarrels.put(ship.id, closestSafeBarrel);
                        action = new MoveAction(closestSafeBarrel.coordinate);
                    } else {
                        log("continuing");
                    }
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

            if (canShoot(ship)) {
                Optional<Shoot> shootOrNot = gameState.findBestShootFor(ship, gameState);

                if (shootOrNot.isPresent()) {
                    lastFireTurns.put(ship.id, currentTurn);
                    log("SimpleFireStrategy");
                    action = new FireAction(shootOrNot.get().coordinate);
                }
            }
            return action;
        }

    }

    static boolean canShoot(Ship ship) {
        Integer lastFireTurn = lastFireTurns.get(ship.id);
        return lastFireTurn == null || lastFireTurn + FIRE_COOLDOWN < currentTurn;
    }

    static class BarrelFireStrategy implements Strategy {
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            if (canShoot(ship) && !gameState.barrels.isEmpty()) {
                Optional<ShipAndBarrel> shipAndBarrelOptional = gameState.enemyShips.stream()
                        .map(enemyShip -> new ShipAndBarrel(enemyShip, gameState.findClosestBarrel(enemyShip).get()))
                        .filter(shipAndBarrel -> {
                            Double shipDistanceToBarrel = gameState.distance(shipAndBarrel.barrel, ship);
                            Double enemyShipDistanceToBarrel = gameState.distance(shipAndBarrel.barrel, shipAndBarrel.ship);
                            int cannonBallDelayImpact = gameState.turnToCannonBallImpact(ship, shipAndBarrel.barrel.coordinate);
                            return enemyShipDistanceToBarrel == cannonBallDelayImpact && shipDistanceToBarrel <= MAX_SHOOTABLE_DISTANCE;
                        })
                        .findFirst();

                if (shipAndBarrelOptional.isPresent()) {
                    lastFireTurns.put(ship.id, currentTurn);
                    log("BarrelFireStrategy");
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

                    if (shipCoordinates.head.isValid()) {
                        Optional<Ship> shipAtHead = gameState.getShipAt(shipCoordinates.head);
                        if (shipAtHead.isPresent() && !shipAtHead.get().equals(ship)) {
                            if (gameState.mines.contains(shipAtHead)) {
                                new MoveAction(new Coordinate(10, 10));
                            }
                        } else {
                            action = new FasterAction();
                        }
                    }  else {
                        action = new MoveAction(new Coordinate(10, 10));
                    }

                }
            }
            return action;
        }
    }

    static class EscapeCanonBallStrategyInCourse implements Strategy {

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Action action = null;

            boolean shipIsTargeted = false;

            for(int i = 1; i <= 3; i++) {
                Ship currentShip = ship.inNTurns(i);
                List<Coordinate> coordinates = currentShip.shipCoordinates().asList();

                for (CannonBall cannonBall : gameState.cannonBalls) {
                    //log(cannonBall + "-" + currentShip.shipCoordinates().asList() + "-" + i);
                    if (coordinates.contains(cannonBall.target) && cannonBall.impactDelay == i) {
                        shipIsTargeted = true;
                        break;
                    }
                }
            }

            if (shipIsTargeted) {
                log("EscapeCanonBallStrategyInCourse");
                if (canPort(ship)) {
                    action = new PortAction();
                } else if (canStarboard(ship)){
                    action = new StarboardAction();
                } else {
                    action = new FasterAction();
                }
            }
            return action;
        }

        private boolean canPort(Ship ship) {
            Ship fakeShip = new Ship(ship.id, ship.coordinate, ship.speed, (ship.orientation + 1) % 6);
            Coordinate head = fakeShip.shipCoordinates().head;
            Coordinate tail = fakeShip.shipCoordinates().tail;

            if (coordinateOk(head) && coordinateOk(tail)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean canStarboard(Ship ship) {
            Ship fakeShip = new Ship(ship.id, ship.coordinate, ship.speed, (ship.orientation + 5) % 6);
            Coordinate head = fakeShip.shipCoordinates().head;
            Coordinate tail = fakeShip.shipCoordinates().tail;

            if (coordinateOk(head) && coordinateOk(tail)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean coordinateOk(Coordinate coordinate) {
            return coordinate.isValid() && !gameState.hasMine(coordinate) && !gameState.getShipAt(coordinate).isPresent();
        }
    }

    static class AvoidMineStrategy implements Strategy {

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            Ship shipIn1Turn = ship.inNTurns(1);
            ShipCoordinates shipCoordinatesIn1Turn = shipIn1Turn.shipCoordinates();

            if (gameState.hasMine(shipCoordinatesIn1Turn.head) ) {
                return new SlowerAction();
            } else {
                Ship shipIn2Turns = shipIn1Turn.inNTurns(1);
                ShipCoordinates shipCoordinatesIn2Turn = shipIn2Turns.shipCoordinates();

                return gameState.hasMine(shipCoordinatesIn2Turn.head) ? new PortAction() : null;
            }
        }
    }

    static class PrepareMoveStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            if (ship.speed == 0) {
                Ship fakeShip = new Ship(ship.id, ship.coordinate, 1, ship.orientation).inNTurns(1);
                if (!ship.shipCoordinates().head.isValid() || !fakeShip.shipCoordinates().head.isValid()) {
                    return new MoveAction(new Coordinate(10, 10));
                }
            }
            return null;
        }
    }

    static class FireStaticStrategy implements Strategy {

        final boolean closeOnly;

        FireStaticStrategy(boolean closeOnly) {
            this.closeOnly = closeOnly;
        }

        @Override
        public Action getAction(Ship ship, GameState gameState) {
            if (canShoot(ship)) {
                Optional<Ship> staticShootableEnemyOrNot = gameState.enemyShips.stream()
                        .filter(enemyShip -> {
                            return  enemyShip.speed == 0 &&
                                    ship.shipCoordinates().head.distance(enemyShip) <= MAX_SHOOTABLE_DISTANCE;
                        })
                        .findFirst();

                log("staticShootableEnemyOrNot:" + staticShootableEnemyOrNot);
                if (staticShootableEnemyOrNot.isPresent()) {
                    boolean enemyTouching = ship.shipCoordinates().asList().stream().flatMap(coordinate -> coordinate.neighbors().stream()).anyMatch(coordinate -> staticShootableEnemyOrNot.get().shipCoordinates().asList().contains(coordinate));
                    if (!closeOnly || enemyTouching) {
                        log("FireStaticStrategy:" + closeOnly);
                        return new FireAction(staticShootableEnemyOrNot.get().coordinate);
                    }
                }
            }
            return null;
        }
    }

    static class GoToFightStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            int myMaxRhum = gameState.myShips.stream().mapToInt(myShip -> myShip.rhum).max().getAsInt();
            System.err.println("myRhum:"+myMaxRhum);
            int enemyMaxRhum = gameState.enemyShips.stream().mapToInt(myShip -> myShip.rhum).max().getAsInt();
            System.err.println("enemyRhum:"+enemyMaxRhum);

            if (myMaxRhum < enemyMaxRhum) {
                Optional<Ship> enemyShipOrNot = gameState.enemyShips.stream().findFirst();
                if (enemyShipOrNot.isPresent()) {

                    log("GoToFightStrategy");
                    return new MoveAction(enemyShipOrNot.get().coordinate);
                }
            }
            return null;
        }
    }

    static class DestroyMineStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            if (canShoot(ship)) {
                Coordinate shipTargetMove = shipMoves.get(ship);

                if (shipTargetMove != null) {
                    Optional<Path> path = ship.coordinate.pathTo(shipTargetMove, gameState);

                    if (path.isPresent()) {
                        List<Coordinate> shipAlreadyDestroyedMines = shipDestroyedMines.get(ship.id);
                        if (shipAlreadyDestroyedMines == null) {
                            shipAlreadyDestroyedMines = new ArrayList<>();
                            shipDestroyedMines.put(ship.id, shipAlreadyDestroyedMines);
                        }

                        Coordinate runningStep = ship.coordinate;
                        for (Coordinate step : path.get().steps) {
                            int orientation = runningStep.orientationTo(step);
                            Ship nextShipState = new Ship(ship.id, runningStep, ship.rhum, orientation);
                            ShipCoordinates shipCoordinates = nextShipState.shipCoordinates();

                            for (Coordinate coordinate : shipCoordinates.asList()) {

                                if (!shipAlreadyDestroyedMines.contains(coordinate) && gameState.hasMine(coordinate) && ship.distance(coordinate) > 6) {
                                    shipAlreadyDestroyedMines.add(coordinate);
                                    log("DestroyMineStrategy");
                                    lastFireTurns.put(ship.id, currentTurn);
                                    return new FireAction(coordinate);
                                }
                            }
                            runningStep = step;
                        }
                    }
                }
            }
            return null;
        }
    }

    static class DestroyMineOnEnemyStrategy implements Strategy {
        @Override
        public Action getAction(Ship ship, GameState gameState) {
            if (canShoot(ship)) {
                List<Mine> shootableMines = gameState.mines.stream()
                        .filter(mine -> ship.shipCoordinates().head.distance(mine) <= MAX_SHOOTABLE_DISTANCE)
                        .filter(mine -> shipDestroyedMines.get(ship.id) == null || !shipDestroyedMines.get(ship.id).contains(mine.coordinate))
                        .collect(Collectors.toList());

                Mine selectedMine = null;
                for (Mine shootableMine : shootableMines) {
                    for (Ship enemyShip : gameState.enemyShips) {
                        int delayToImpact = gameState.turnToCannonBallImpact(ship, shootableMine.coordinate);

                        int minDistanceFromMineToMyShip = (int) gameState.myShips.stream().mapToDouble(ship1 -> ship1.distance(shootableMine)).min().getAsDouble();
                        boolean shouldNotShootMine = gameState.myShips.stream()
                                .flatMap(ship1 -> ship1.inNTurns(delayToImpact).shipCoordinates().asList().stream())
                                .anyMatch(coordinate -> coordinate.distance(shootableMine.coordinate) < 2);

                        if (delayToImpact < minDistanceFromMineToMyShip && !shouldNotShootMine) {
                            selectedMine = shootableMine;
                            break;
                        }
                    }
                }

                if (selectedMine != null) {
                    log("DestroyMineOnEnemyStrategy");
                    lastFireTurns.put(ship.id, currentTurn);

                    List<Coordinate> shipAlreadyDestroyedMines = shipDestroyedMines.computeIfAbsent(ship.id, k -> new ArrayList<>());
                    shipAlreadyDestroyedMines.add(selectedMine.coordinate);

                    return new FireAction(selectedMine.coordinate);
                }
            }
            return null;
        }
    }

    private static class Shoot {
        private final Ship enemyShip;
        private final Coordinate coordinate;

        public Shoot(Ship enemyShip, Coordinate coordinate) {
            this.enemyShip = enemyShip;
            this.coordinate = coordinate;
        }
    }
}