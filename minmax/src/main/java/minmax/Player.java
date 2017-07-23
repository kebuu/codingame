package minmax;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Player {


    public static void main(String[] args) {
        GameState gameState = null;
        MinMaxConfig<GameState> minMaxConfig = null;

        MinMaxRootNode<GameState> minMaxRootNode = new MinMaxRootNode<>(gameState, minMaxConfig);
        minMaxRootNode.bestAction()
                .ifPresent(action -> System.out.println(action.asString()));
    }

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
            System.err.println(Arrays.asList(logs).stream().map(Supplier::get).map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }

    private static void mmStrLog(Object... logs) {
        if(MIN_MAX_LOG) {
            System.err.println(Arrays.asList(logs).stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }
}