import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        TronGameState tronGameState = null;
        while (true) {
            int N = in.nextInt(); // total number of players (2 to 4).
            int myLumicycleId = in.nextInt(); // your player number (0 to 3).

            if (tronGameState == null) {
                ArrayList<Lumicycle> lumicycles = new ArrayList<>();
                for (int lumicycleId = 0; lumicycleId < N; lumicycleId++) {
                    lumicycles.add(new Lumicycle(lumicycleId));
                }
                tronGameState = new TronGameState(myLumicycleId, lumicycles);
            }

            for (int lumicycleId = 0; lumicycleId < N; lumicycleId++) {
                int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
                int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

                Lumicycle lumicycle = tronGameState.lumicycles.get(lumicycleId);
                if (lumicycle != null && X1 != -1) {
                    lumicycle.setNewPosition(xy(X1, Y1));
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            List<Action> actions = tronGameState.possibleActions(MinMaxNodeType.MAX);
            System.err.println("Playing from: " + actions);
            System.err.println("Playing: " + actions.get(0).asString());
            System.out.println(actions.get(0).asString()); // A single line with UP, DOWN, LEFT or RIGHT
        }
    }

    static class TronGameState implements GameState {
        final int maxX = 29;
        final int maxY = 19;
        final int myLumicycleId;
        final List<Lumicycle> lumicycles;
        final List<Coordinate> usedCoordinates;

        TronGameState(int myLumicycleId, List<Lumicycle> lumicycles) {
            this.myLumicycleId = myLumicycleId;
            this.lumicycles = lumicycles;

            usedCoordinates = lumicycles.stream().flatMap(lumicycle -> lumicycle.oldPositions.stream()).collect(Collectors.toList());
            usedCoordinates.addAll(lumicycles.stream().map(lumicycle -> lumicycle.currentPosition).collect(Collectors.toList()));
        }

        @Override
        public List<Action> possibleActions(MinMaxNodeType minMaxNodeType) {
            Predicate<Lumicycle> lumicycleIdPredicate;
            if (minMaxNodeType.equals(MinMaxNodeType.MAX)) {
                lumicycleIdPredicate = lumicycle -> lumicycle.id == myLumicycleId;
            } else {
                lumicycleIdPredicate = lumicycle -> lumicycle != null && lumicycle.id != myLumicycleId;
            }

            return lumicycles.stream()
                .filter(lumicycleIdPredicate)
                .flatMap(lumicycle -> {
                    System.err.println("lumicycle:" + lumicycle);
                    LumicycleDirection[] possibleDirections = lumicycle.currentDirection == null ? LumicycleDirection.values() : lumicycle.currentDirection.others();
                    return Stream.of(possibleDirections)
                        .filter(lumicycleDirection -> isAllowed(lumicycle.currentPosition.coordinateAt(lumicycleDirection)))
                        .map(lumicycleDirection -> new TronGameAction(lumicycle.id, lumicycleDirection));
                })
                .collect(Collectors.toList());
        }

        private boolean isAllowed(Coordinate coordinate) {
            System.err.println("coordinate:" + coordinate);
            System.err.println(coordinate.x >= 0 && coordinate.x <= maxX && coordinate.y >= 0 && coordinate.y < maxY
                                && !usedCoordinates.contains(coordinate));
            return coordinate.x >= 0 && coordinate.x <= maxX && coordinate.y >= 0 && coordinate.y < maxY
                    && !usedCoordinates.contains(coordinate);
        }

        @Override
        public Optional<? extends GamePlayer> getWinner() {
            return null;
        }

        public <T extends GameState> T withAction(TronGameAction tronGameAction) {
            return null;
        }
    }

    static class TronGameAction implements Action {
        final int lumicycleId;
        final LumicycleDirection direction;

        TronGameAction(int lumicycleId, LumicycleDirection direction) {
            this.lumicycleId = lumicycleId;
            this.direction = direction;
        }

        public String asString() {
            return direction.name();
        }

        @Override
        public <T extends GameState> T accept(T gameState) {
            TronGameState ticTacToeGameState = (TronGameState) gameState;
            return ticTacToeGameState.withAction(this);
        }

        @Override
        public String toString() {
            return asString();
        }
    }

    static class Lumicycle {
        final int id;
        Coordinate currentPosition;
        LumicycleDirection currentDirection;
        final List<Coordinate> oldPositions;

        Lumicycle(int id) {
            this(id, null, null, new ArrayList<>());
        }

        Lumicycle(int id, Coordinate currentPosition, LumicycleDirection currentDirection) {
            this(id,currentPosition,currentDirection, new ArrayList<>());
        }

        Lumicycle(int id, Coordinate currentPosition, LumicycleDirection currentDirection, List<Coordinate> oldPositions) {
            this.id = id;
            this.currentPosition = currentPosition;
            this.currentDirection = currentDirection;
            this.oldPositions = oldPositions;
        }

        void setNewPosition(Coordinate newPosition) {
            if (currentPosition != null) {
                oldPositions.add(currentPosition);
                currentDirection = currentPosition.directionTo(newPosition);
            }
            currentPosition = newPosition;
            System.err.println("newPosition:" + newPosition);
            System.err.println(this);
        }

        @Override
        public String toString() {
            return "Lumicycle{" +
                    "id=" + id +
                    ", currentPosition=" + currentPosition +
                    ", currentDirection=" + currentDirection +
                    ", oldPositions=" + oldPositions +
                    '}';
        }
    }

    enum LumicycleDirection {
        UP, DOWN, LEFT, RIGHT;

        public LumicycleDirection[] others() {
            LumicycleDirection[] others = new LumicycleDirection[3];

            int otherIndex = 0;
            for (LumicycleDirection lumicycleDirection : LumicycleDirection.values()) {
                if (lumicycleDirection != this) {
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

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Coordinate coordinateAt(LumicycleDirection lumicycleDirection) {
            switch (lumicycleDirection) {
                case DOWN:
                    return xy(x, y - 1);
                case LEFT:
                    return xy(x -1 , y);
                case RIGHT:
                    return xy(x + 1, y);
                case UP:
                    return xy(x, y + 1);
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
                return LumicycleDirection.UP;
            } else {
                return LumicycleDirection.DOWN;
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
        return new Coordinate(x, y);
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
        private final int maxDepth;
        private final GamePlayer maxPlayer;

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
                        score = config.score(gameState);
                        depthOfScoringBestNextAction = depth;
                        minMaxStat.incNbOfTerminalNodes();
                    } else {
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
                    mmLog(() -> "Scoring node at depth " + depth + " (" + minMaxNodeType + ") > " + nodeScore);
                }
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
                    ", \nmeanComputingGameStateTimeMs=" + nbOfComputingGameState +
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