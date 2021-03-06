import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
class Player {

    public static final int X_SIZE = 30;
    public static final int Y_SIZE = 20;
    private static long startTurnTimestamp;
    private static int turn;

    private static boolean LOG = true;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        TronGameState tronGameState = null;
        TronMinMaxConfig tronMinMaxConfig = null;
        while (true) {
            int N = in.nextInt(); // total number of players (2 to 4).

            startTurnTimestamp = System.currentTimeMillis();
            int myLumicycleIndex = in.nextInt(); // your player number (0 to 3).

            if (tronGameState == null) {
                tronGameState = new TronGameState(myLumicycleIndex, N);
                System.err.println("Index de mon lumicycle:" + N);
                logElapsedTime("After game state");
            }

            for (int lumicycleIndex = 0; lumicycleIndex < N; lumicycleIndex++) {
                int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
                int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

                Lumicycle lumicycle = tronGameState.lumicycles[lumicycleIndex];
                if (lumicycle == null) {
                    lumicycle = new Lumicycle(new TronPlayer(lumicycleIndex), xy(X1, Y1));
                } else {
                    lumicycle = lumicycle.withNewPosition(xy(X1, Y1));
                }
                tronGameState.setLumicycle(lumicycle);

                if (turn == 0) {
                    if (!xy(X1, Y1).equals(xy(X0, Y0))) {
                        tronGameState.addLumicycleTail(lumicycleIndex, xy(X0, Y0));
                    }
                }
            }

            if (tronMinMaxConfig == null) {
                tronMinMaxConfig = new TronMinMaxConfig(new TronPlayer(myLumicycleIndex));
            }

            logElapsedTime("After lumicycle");
            System.err.println("turn:"+turn);

            if (turn == 0) {
                tronMinMaxConfig.maxDistance = 5;
                tronMinMaxConfig.maxDepth = 1;
            } else if (turn < 5){
                tronMinMaxConfig.maxDistance = 15;
                tronMinMaxConfig.maxDepth = 1;
            } else {
                tronMinMaxConfig.maxDistance = 40;
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            MinMaxRootNode<TronGameState> minMaxRootNode = new MinMaxRootNode<>(tronGameState, tronMinMaxConfig);
            logElapsedTime("Before best action");
            Optional<Action> actionOptional = minMaxRootNode.bestAction();
            logElapsedTime("After best action");

            actionOptional.ifPresent(action -> {
                System.err.println(action.asString());
                System.out.println(action.asString()); // A single line with UP, DOWN, LEFT or RIGHT
            });

            System.gc();

            turn++;
        }
    }

    static class TronPlayer implements GamePlayer {
        final int lumicycleIndex;

        TronPlayer(int lumicycleIndex) {
            this.lumicycleIndex = lumicycleIndex;
        }

        @Override
        public String toString() {
            return "TronPlayer{" +
                    "lumicycleIndex=" + lumicycleIndex +
                    '}';
        }
    }

    static class CoordinateManager {

        static Coordinate[][] validCoordinates = new Coordinate[X_SIZE][Y_SIZE];

        static {
            for (int i = 0; i < X_SIZE; i ++) {
                for (int j = 0; j < Y_SIZE; j ++) {
                    validCoordinates[i][j] = new Coordinate(i, j);
                }
            }
        }

        static Coordinate get(int x, int y) {
            if (x < 0 || y < 0 || x >= X_SIZE || y >= Y_SIZE) {
                return null;
            } else {
                return validCoordinates[x][y];
            }
        }
    }

    static class TronMinMaxConfig extends MinMaxConfig<TronGameState> {

        private int maxDistance = 10;

        public TronMinMaxConfig(GamePlayer maxPlayer) {
            super(1, maxPlayer);
        }

        @Override
        public int score(TronGameState gameState) {
            Lumicycle myLumicycle = gameState.getMyLumicycle();

            Set<Coordinate> visited = new HashSet<>();
            visited.add(myLumicycle.position);

            Set<Coordinate> coordinatesToVisit = gameState.accessibleCoordinates(myLumicycle).stream()
                    .filter(gameState::isAllowed)
                    .collect(Collectors.toSet());

            Map<Integer, Integer> nbOfVisitedCoordinateByDistance = new HashMap<>();
            int distance = 1;

            logElapsedTime("Before while ");

            while (!coordinatesToVisit.isEmpty() && distance < maxDistance) {
                nbOfVisitedCoordinateByDistance.put(distance, coordinatesToVisit.size());
                Set<Coordinate> newOnTheRoad = new HashSet<>();
                Iterator<Coordinate> coordinatesToVisitIterator = coordinatesToVisit.iterator();
                while (coordinatesToVisitIterator.hasNext()) {
                    Coordinate visitedCoordinate = coordinatesToVisitIterator.next();
                    newOnTheRoad.addAll(visitedCoordinate.neighbors());
                    visited.add(visitedCoordinate);
                    coordinatesToVisitIterator.remove();
                }

                assert coordinatesToVisit.isEmpty();

                List<Coordinate> filterNewOnTheRoad = newOnTheRoad.stream()
                        .filter(coordinate -> gameState.isAllowed(coordinate) && !visited.contains(coordinate))
                        .collect(Collectors.toList());

                coordinatesToVisit.addAll(filterNewOnTheRoad);
                distance++;
            }

            log(() -> nbOfVisitedCoordinateByDistance);

            return nbOfVisitedCoordinateByDistance.size();
        }
    }

    static class TronGameState implements GameState {
        final int myLumicycleIndex;
        final Lumicycle[] lumicycles;
        Map<Integer, List<Coordinate>> usedCoordinatesByLumicycleIndex = new HashMap<>();
        Set<Coordinate> allUsedCoordinates = new HashSet<>();

        TronGameState(int myLumicycleIndex, int nbOfPlayers) {
            this.myLumicycleIndex = myLumicycleIndex;
            this.lumicycles = new Lumicycle[nbOfPlayers];

            for (int i = 0; i < nbOfPlayers; i++) {
                usedCoordinatesByLumicycleIndex.put(i, new ArrayList<>());
            }
        }

        List<Coordinate> accessibleCoordinates(Lumicycle lumicycle) {
            LumicycleDirection[] possibleDirections = possibleDirections(lumicycle);

            return Stream.of(possibleDirections)
                    .map(lumicycle.position::coordinateAt)
                    .filter(this::isAllowed)
                    .collect(Collectors.toList());
        }

        private Player.LumicycleDirection[] possibleDirections(Player.Lumicycle lumicycle) {
            return lumicycle.direction == null ? Player.LumicycleDirection.values() : lumicycle.direction.others();
        }

        @Override
        public List<Action> possibleActions(MinMaxNodeType minMaxNodeType) {
            Predicate<Lumicycle> lumicycleIdPredicate;
            if (minMaxNodeType.equals(MinMaxNodeType.MAX)) {
                lumicycleIdPredicate = lumicycle -> lumicycle.getIndex() == myLumicycleIndex;
            } else {
                lumicycleIdPredicate = lumicycle -> lumicycle != null && lumicycle.getIndex() != myLumicycleIndex;
            }

            List<List<TronGameAction>> lumicyclesPossibleActions = Stream.of(lumicycles)
                    .filter(lumicycleIdPredicate)
                    .map(lumicycle -> {
                        LumicycleDirection[] possibleDirections = possibleDirections(lumicycle);
                        return Stream.of(possibleDirections)
                                .filter(lumicycleDirection -> isAllowed(lumicycle.position.coordinateAt(lumicycleDirection)))
                                .map(lumicycleDirection -> new TronGameAction(lumicycle.getIndex(), lumicycleDirection))
                                .collect(Collectors.toList());
                    })
                    .collect(Collectors.toList());



            return collect;
        }

        private boolean isAllowed(Coordinate coordinate) {
            return coordinate != null && coordinate.x >= 0 && coordinate.x < X_SIZE && coordinate.y >= 0 && coordinate.y < Y_SIZE
                    && !allUsedCoordinates.contains(coordinate);
        }

        @Override
        public Optional<GamePlayer> getWinner() {
            long aliveLumicycleCount = Stream.of(lumicycles).filter(lumicycle -> lumicycle.state == LumicycleState.ALIVE).count();

            if (aliveLumicycleCount > 1) {
                return Optional.empty();
            } else {
                return Stream.of(lumicycles)
                        .filter(lumicycle -> lumicycle.state == LumicycleState.ALIVE)
                        .findFirst().map(lumicycle -> lumicycle.tronPlayer);
            }
        }

        public GameState withAction(TronGameAction tronGameAction) {
            TronGameState newTronGameState = new TronGameState(myLumicycleIndex, lumicycles.length);
            newTronGameState.allUsedCoordinates.addAll(allUsedCoordinates);

            for (Lumicycle lumicycle : lumicycles) {
                if (lumicycle.getIndex() == tronGameAction.lumicycleIndex) {
                    Coordinate newPosition = lumicycle.position.coordinateAt(tronGameAction.direction);
                    Lumicycle newLumicycle = lumicycle.withNewPosition(newPosition);

                    newTronGameState.usedCoordinatesByLumicycleIndex.put(newLumicycle.getIndex(), new ArrayList<>(usedCoordinatesByLumicycleIndex.get(newLumicycle.getIndex())));
                    newTronGameState.setLumicycle(newLumicycle);
                } else {
                    newTronGameState.setLumicycle(lumicycle);
                    newTronGameState.usedCoordinatesByLumicycleIndex.put(lumicycle.getIndex(), usedCoordinatesByLumicycleIndex.get(lumicycle.getIndex()));
                }
            }


            return newTronGameState;
        }

        public Lumicycle getMyLumicycle() {
            return lumicycles[myLumicycleIndex];
        }

        public void setLumicycle(Lumicycle lumicycle) {
            lumicycles[lumicycle.getIndex()] = lumicycle;

            if (lumicycle.state == LumicycleState.ALIVE) {
                usedCoordinatesByLumicycleIndex.get(lumicycle.getIndex()).add(lumicycle.position);
                allUsedCoordinates.add(lumicycle.position);
            } else {
                allUsedCoordinates.removeAll(usedCoordinatesByLumicycleIndex.get(lumicycle.getIndex()));
                usedCoordinatesByLumicycleIndex.get(lumicycle.getIndex()).clear();
            }
        }

        public GameState withAction(MultiTronGameAction multiTronGameAction) {
            TronGameState newGameState = this;
            for (TronGameAction action : multiTronGameAction.actions) {
                newGameState = (TronGameState) newGameState.withAction(action);
            }
            return newGameState;
        }

        public void addLumicycleTail(int lumicycleIndex, Coordinate xy) {
            usedCoordinatesByLumicycleIndex.get(lumicycleIndex).add(xy);
            allUsedCoordinates.add(xy);
        }
    }

    static class TronGameAction implements Action {
        final int lumicycleIndex;
        final LumicycleDirection direction;

        TronGameAction(int lumicycleIndex, LumicycleDirection direction) {
            this.lumicycleIndex = lumicycleIndex;
            this.direction = direction;
        }

        public String asString() {
            return direction.name();
        }

        @Override
        public GameState accept(GameState gameState) {
            TronGameState tronGameState = (TronGameState) gameState;
            return tronGameState.withAction(this);
        }

        @Override
        public String toString() {
            return asString();
        }
    }

    static class MultiTronGameAction implements Action {
        final List<TronGameAction> actions;

        MultiTronGameAction(List<TronGameAction> actions) {
            this.actions = actions;
        }

        @Override
        public String asString() {
            return actions.toString();
        }

        @Override
        public GameState accept(GameState gameState) {
            TronGameState tronGameState = (TronGameState) gameState;
            return tronGameState.withAction(this);
        }
    }

    static class Lumicycle {
        final TronPlayer tronPlayer;
        final Coordinate position;
        final LumicycleDirection direction;
        final LumicycleState state;

        Lumicycle(TronPlayer tronPlayer, Coordinate position) {
            this(tronPlayer, position, null);
        }

        Lumicycle(TronPlayer tronPlayer, Coordinate position, LumicycleDirection direction) {
            this(tronPlayer, position, direction, LumicycleState.ALIVE);
        }

        Lumicycle(TronPlayer tronPlayer, Coordinate position, LumicycleDirection direction, LumicycleState state) {
            this.tronPlayer = tronPlayer;
            this.position = position;
            this.direction = direction;
            this.state = state;
        }

        Lumicycle withNewPosition(Coordinate newPosition) {
            LumicycleDirection newDirection = position == null ? null : position.directionTo(newPosition);
            LumicycleState newState = newPosition.x == -1 ? LumicycleState.DEAD : LumicycleState.ALIVE;
            return new Lumicycle(tronPlayer, newPosition, newDirection, newState);
        }

        int getIndex() {
            return tronPlayer.lumicycleIndex;
        }

        @Override
        public String toString() {
            return "Lumicycle{" +
                    "tronPlayer=" + tronPlayer +
                    ", position=" + position +
                    ", direction=" + direction +
                    '}';
        }
    }

    enum LumicycleState {
        DEAD, ALIVE
    }

    enum LumicycleDirection {
        LEFT, RIGHT, UP, DOWN;

        LumicycleDirection opposite() {
            switch (this) {
                case UP: return DOWN;
                case DOWN: return UP;
                case LEFT: return RIGHT;
                case RIGHT: return LEFT;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public LumicycleDirection[] others() {
            LumicycleDirection[] others = new LumicycleDirection[3];

            int otherIndex = 0;
            for (LumicycleDirection lumicycleDirection : LumicycleDirection.values()) {
                if (lumicycleDirection != this.opposite()) {
                    others[otherIndex] = lumicycleDirection;
                    otherIndex++;
                }
            }

            return others;
        }
    }

    static class Coordinate {
        final int x;
        final int y;
        List<Coordinate> neighbors;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        List<Coordinate> neighbors() {
            if (neighbors == null) {
                neighbors = Arrays.asList(xy(x, y + 1), xy(x, y - 1), xy(x - 1, y), xy(x + 1, y)).stream()
                        .filter(Objects::nonNull).collect(Collectors.toList());
            }
            return neighbors;
        }


        int distanceTo(Coordinate coordinate) {
            return Math.abs(x - coordinate.x) + Math.abs(y - coordinate.y);
        }

        Coordinate coordinateAt(LumicycleDirection lumicycleDirection) {
            switch (lumicycleDirection) {
                case DOWN:
                    return xy(x, y + 1);
                case LEFT:
                    return xy(x -1 , y);
                case RIGHT:
                    return xy(x + 1, y);
                case UP:
                    return xy(x, y - 1);
                default:
                    throw new IllegalStateException("Java...");
            }
        }

        LumicycleDirection directionTo(Coordinate toCoordinate) {
            if (x < toCoordinate.x) {
                return LumicycleDirection.RIGHT;
            } else if (x > toCoordinate.x) {
                return LumicycleDirection.LEFT;
            } else if (y < toCoordinate.y) {
                return LumicycleDirection.DOWN;
            } else {
                return LumicycleDirection.UP;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Coordinate that = (Coordinate) o;

            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public String toString() {
            return "[" + x + " " + y + "]";
        }
    }

    static Coordinate xy(int x, int y) {
        return CoordinateManager.get(x, y);
    }

    static void logElapsedTime(String info) {
        log(() -> ">>>>> " + info + ":  Elapsed time:" + (System.currentTimeMillis() - startTurnTimestamp));
    }

    @SafeVarargs
    private static void log(Supplier<Object>... logs) {
        if(LOG) {
            System.err.println(
                    Arrays.asList(logs).stream()
                            .map(Supplier::get)
                            .map(o -> {
                                if (o != null && o.getClass().isArray()) {
                                    return Arrays.asList(o).toString();
                                } else {
                                    return Objects.toString(o);
                                }
                            })
                            .collect(Collectors.joining(", ")));
        }
    }

    static class ListIndexesShifter<T> implements Iterable<List<T>>, Iterator<List<T>>{

        final List<List<T>> lists;
        final SortedMap<Integer, Integer> shiftedIndexesByListIndex = new TreeMap<>();
        final SortedMap<Integer, Integer> maxIndexesByListIndex = new TreeMap<>();

        final int maxNbOfNextValue;
        int nbOfNextValueReturned;

        public ListIndexesShifter(List<List<T>> lists) {
            this.lists = lists.stream().filter(list -> !list.isEmpty()).collect(Collectors.toList());

            if (this.lists.isEmpty()) {
                maxNbOfNextValue = 0;
            } else {
                maxNbOfNextValue = this.lists.stream().mapToInt(List::size).reduce((size1, size2) -> size1 * size2).getAsInt();

                for (int i = 0; i < lists.size(); i++) {
                    int listSize = lists.get(i).size();
                    shiftedIndexesByListIndex.put(i, 0);
                    maxIndexesByListIndex.put(i, listSize - 1);
                }
            }
        }

        @Override
        public Iterator<List<T>> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return nbOfNextValueReturned <  maxNbOfNextValue;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                List<T> valueToReturn = getIndexedValues();
                shiftIndexes();
                nbOfNextValueReturned++;
                return valueToReturn;
            }
        }

        private List<T> getIndexedValues() {
            return shiftedIndexesByListIndex.entrySet().stream()
                .map(shiftedIndexesByListIndexEntry -> lists.get(shiftedIndexesByListIndexEntry.getKey()).get(shiftedIndexesByListIndexEntry.getValue()))
                .collect(Collectors.toList());
        }

        private void shiftIndexes() {
            for (Map.Entry<Integer, Integer> shiftedIndexesByListIndexEntry : shiftedIndexesByListIndex.entrySet()) {
                if (shiftedIndexesByListIndexEntry.getValue() < maxIndexesByListIndex.get(shiftedIndexesByListIndexEntry.getKey())) {
                    shiftedIndexesByListIndex.put(shiftedIndexesByListIndexEntry.getKey(), shiftedIndexesByListIndexEntry.getValue() + 1);
                    break;
                } else {
                    shiftedIndexesByListIndex.put(shiftedIndexesByListIndexEntry.getKey(), 0);
                }
            }
        }
    }
	/**************************************
     * MIN MAX COMMON
     **************************************/
    private static final boolean MIN_MAX_LOG = true;

    public interface GameState {
        List<Action> possibleActions(MinMaxNodeType minMaxNodeType);

        default Optional<? extends GamePlayer> getWinner() {
            return Optional.empty();
        }
    }

    public interface GamePlayer {

    }

    public interface Action {
        String asString();
        <T extends GameState> T accept(T gameState);
    }

    public static abstract class MinMaxConfig<T extends GameState> {
        protected int maxDepth;
        protected final GamePlayer maxPlayer;

        public abstract int score(T gameState);

        public MinMaxConfig(int maxDepth, GamePlayer maxPlayer) {
            this.maxDepth = maxDepth;
            this.maxPlayer = maxPlayer;
        }

        int maxDepth() {
            return maxDepth;
        }
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "Duplicates"})
    public static class MinMaxNode<T extends GameState> {

        final T gameState;
        final MinMaxConfig<T> config;
        final MinMaxNodeType minMaxNodeType;
        final int depth;
        final Action comingFromAction;
        final MinMaxStat minMaxStat;

        Action bestNextAction;
        MinMaxNode<T> bestNextNode;
        int depthOfScoringBestNextAction;

        Integer siblingsMaxScore;
        Integer siblingsMinScore;
        List<MinMaxNode> children = new ArrayList<>();
        Integer nodeScore; // should never be accessed directly but via score()

        MinMaxNode(T gameState, MinMaxConfig<T> config, MinMaxNodeType minMaxNodeType, Action action, int depth, MinMaxStat minMaxStat) {
            this.gameState = gameState;
            this.minMaxNodeType = minMaxNodeType;
            this.comingFromAction = action;
            this.depth = depth;
            this.config = config;
            this.minMaxStat = minMaxStat;
            depthOfScoringBestNextAction = config.maxDepth;

            minMaxStat.incNbOfNodes();
        }

        MinMaxNode(T gameState, MinMaxConfig<T> config, MinMaxNodeType minMaxNodeType, int depth, MinMaxStat minMaxStat) {
            this(gameState, config, minMaxNodeType, null, depth, minMaxStat);
        }

        void addSiblingScores(List<Integer> scores) {
            scores.forEach(this::addSiblingScore);
        }

        void addSiblingScore(int score) {
            if (siblingsMaxScore == null || siblingsMaxScore < score) {
                siblingsMaxScore = score;
            }

            if (siblingsMinScore == null || siblingsMinScore > score) {
                siblingsMinScore = score;
            }
        }

        int score() {
            if (nodeScore == null) {
                mmLog(() -> "Scoring node at depth " + depth + " (" + minMaxNodeType + ")");
                mmLog(() -> gameState);

                if (depth == config.maxDepth) {
                    mmLog(() -> "Reached max depth");
                    long nanoTimeBeforeScoring = System.nanoTime();
                    nodeScore = config.score(gameState);
                    minMaxStat.addScoringTimeNano(System.nanoTime() - nanoTimeBeforeScoring);
                    depthOfScoringBestNextAction = depth;
                } else if (gameState.getWinner().isPresent()) {
                    Optional<? extends GamePlayer> winner = gameState.getWinner();
                    mmLog(() -> "Winner found at depth " + depth + ". Winner: " + winner.get());
                    nodeScore = winner.get() == config.maxPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                    depthOfScoringBestNextAction = depth;
                    minMaxStat.incNbOfTerminalNodes();
                } else  {
                    Integer score = null;

                    List<Integer> childrenScores = new ArrayList<>();
                    List<Action> possibleActions = gameState.possibleActions(minMaxNodeType);

                    if (possibleActions.isEmpty()) {
                        mmLog(() -> "No possible actions");
                        score = config.score(gameState);
                        depthOfScoringBestNextAction = depth;
                        minMaxStat.incNbOfTerminalNodes();
                    } else {
                        mmLog(() -> "Scoring children: " + possibleActions);
                        for (Action possibleAction : possibleActions) {
                            long nanoTimeComputingGameState = System.nanoTime();
                            T nextGameState = possibleAction.accept(gameState);
                            minMaxStat.addTimeComputingGameState(System.nanoTime() - nanoTimeComputingGameState);

                            MinMaxNode<T> childNode = new MinMaxNode<>(nextGameState, config, minMaxNodeType.switchGamer(), possibleAction, depth + 1, minMaxStat);
                            children.add(childNode);

                            childNode.addSiblingScores(childrenScores);

                            int childScore = childNode.score();
                            childrenScores.add(childScore);

                            if (score == null) {
                                score = childScore;
                                bestNextAction = possibleAction;
                                bestNextNode = childNode;
                                depthOfScoringBestNextAction = childNode.depthOfScoringBestNextAction;
                            } else {
                                if (score == childScore) {
                                    int selectedDepthOfScoringBestNextAction = minMaxNodeType.selectDepth(depthOfScoringBestNextAction, childNode.depthOfScoringBestNextAction);

                                    if (selectedDepthOfScoringBestNextAction != depthOfScoringBestNextAction) {
                                        depthOfScoringBestNextAction = selectedDepthOfScoringBestNextAction;
                                        bestNextAction = possibleAction;
                                        bestNextNode = childNode;
                                    }
                                } else {
                                    int selectedScore = minMaxNodeType.selectScore(score, childScore);

                                    if (selectedScore != score) {
                                        score = selectedScore;
                                        bestNextAction = possibleAction;
                                        bestNextNode = childNode;
                                    }
                                }
                            }

                            if (minMaxNodeType.shouldStopScoring(score, this)) {
                                mmLog(() -> "Pruning branch at depth  " + depth + " : " + childScore + " siblings=" + siblingsMaxScore + "," + siblingsMinScore + "," + minMaxNodeType);
                                break;
                            }
                        }
                    }

                    nodeScore = score;
                }

                mmLog(() -> "Scored node at depth " + depth + " action: " + comingFromAction + ",(" + minMaxNodeType + ") -> " + nodeScore);
            }

            return nodeScore;
        }
    }

    public static class MinMaxRootNode<T extends GameState> extends MinMaxNode<T> {

        public MinMaxRootNode(T gameState, MinMaxConfig<T> config) {
            super(gameState, config, MinMaxNodeType.MAX, 0, new MinMaxStat());
            minMaxStat.declareNodesCreationEndTime();
        }

        @SuppressWarnings("unchecked")
        public Optional<Action> bestAction() {
            long timeBeforeScoring = System.currentTimeMillis();
            if (nodeScore == null) {
                score();
            }
            minMaxStat.setTotalScoringTime(System.currentTimeMillis() - timeBeforeScoring);

            mmLog(() -> minMaxStat);
            mmLog(() -> gameState);
            mmLog(() -> "\nAction path: ");
            MinMaxNode<T> runningBestNextNode = bestNextNode;
            while (runningBestNextNode != null) {
                mmStrLog(runningBestNextNode.comingFromAction);
                runningBestNextNode = runningBestNextNode.bestNextNode;
            }

            return Optional.ofNullable(this.bestNextAction);
        }

        public MinMaxStat getStat() {
            return minMaxStat;
        }
    }

    public enum MinMaxNodeType {
        MAX {
            Integer selectScore(Integer score1, int score2) {
                return Math.max(score1, score2);
            }

            MinMaxNodeType switchGamer() {
                return MIN;
            }

            public boolean shouldStopScoring(int newChildScore, MinMaxNode<?> minMaxNode) {
                return newChildScore == Integer.MAX_VALUE ||  (minMaxNode.siblingsMinScore != null && minMaxNode.siblingsMinScore <= newChildScore);
            }

            public int selectDepth(int depth1, int depth2) {
                return Math.min(depth1, depth2);
            }
        }, MIN {
            Integer selectScore(Integer score1, int score2) {
                return Math.min(score1, score2);
            }

            MinMaxNodeType switchGamer() {
                return MAX;
            }

            public boolean shouldStopScoring(int newChildScore, MinMaxNode<?> minMaxNode) {
                return newChildScore == Integer.MIN_VALUE || (minMaxNode.siblingsMaxScore != null && minMaxNode.siblingsMaxScore >= newChildScore);
            }

            public int selectDepth(int depth1, int depth2) {
                return Math.max(depth1, depth2);
            }
        };

        abstract Integer selectScore(Integer score1, int score2);

        abstract MinMaxNodeType switchGamer();

        abstract boolean shouldStopScoring(int newChildScore, MinMaxNode<?> minMaxNode);

        public abstract int selectDepth(int depth1, int depth2);
    }

    public static class MinMaxStat {
        final long nodesCreationStartTime;
        long nodesCreationEndTime;
        long nbOfNodes;
        long nbOfTerminalNodes;
        long totalScoreTime;
        long cumulativeScoringTimeNano;
        long nbOfScoring;
        long cumulativeComputingGameStateTimeNano;
        long nbOfComputingGameState;

        public MinMaxStat() {
            nodesCreationStartTime = System.currentTimeMillis();
        }

        public void incNbOfNodes() {
            nbOfNodes++;
        }

        public void incNbOfTerminalNodes() {
            nbOfTerminalNodes++;
        }

        public void declareNodesCreationEndTime() {
            nodesCreationEndTime = System.currentTimeMillis();
        }

        public long nodesCreationTime() {
            return nodesCreationEndTime - nodesCreationStartTime;
        }

        public void addScoringTimeNano(long scoringTime) {
            cumulativeScoringTimeNano += scoringTime;
            nbOfScoring++;
        }

        public void setTotalScoringTime(long totalScoreTime) {
            this.totalScoreTime = totalScoreTime;
        }

        public float meanScoringTimeMs() {
            return (cumulativeScoringTimeNano / 1_000_000) / (float) Math.max(1, nbOfScoring);
        }

        public float meanComputingGameStateTimeMs() {
            return (cumulativeComputingGameStateTimeNano / 1_000_000) / (float) Math.max(1, nbOfComputingGameState);
        }

        @Override
        public String toString() {
            return "MinMaxStat{" +
                    "\nnodesCreationTimeMs=" + nodesCreationTime() +
                    ", \nnbOfNodes=" + nbOfNodes +
                    ", \nnbOfTerminalNodes=" + nbOfTerminalNodes +
                    ", \ntotalScoreTime=" + totalScoreTime +
                    ", \ncumulativeScoringTimeMs=" + (cumulativeScoringTimeNano / 1_000_000) +
                    ", \nnbOfScoring=" + nbOfScoring +
                    ", \nmeanScoringTimeMs=" + meanScoringTimeMs() +
                    ", \ncumulativeComputingGameStateTimeNano=" + (cumulativeComputingGameStateTimeNano / 1_000_000) +
                    ", \nnbOfComputingGameState=" + nbOfComputingGameState +
                    ", \nmeanComputingGameStateTimeMs=" + meanComputingGameStateTimeMs() +
                    '}';
        }

        public void addTimeComputingGameState(long timeComputingGameState) {
            cumulativeComputingGameStateTimeNano += timeComputingGameState;
            nbOfComputingGameState++;
        }
    }

    @SafeVarargs
    private static void mmLog(Supplier<Object>... logs) {
        if(MIN_MAX_LOG) {
            System.err.println(
                    Arrays.asList(logs).stream().map(Supplier::get).map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }

    private static void mmStrLog(Object... logs) {
        if(MIN_MAX_LOG) {
            System.err.println(Arrays.asList(logs).stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }
}