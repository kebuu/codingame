package minmax;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Player {

    private static final boolean MIN_MAX_LOG = true;
    
    public static void main(String[] args) {
        GameState gameState = null;
        MinMaxConfig<GameState> minMaxConfig = null;

        MinMaxRootNode<GameState> minMaxRootNode = new MinMaxRootNode<>(gameState, minMaxConfig);
        minMaxRootNode.bestAction()
                .ifPresent(action -> System.out.println(action.asString()));
    }

    public interface GameState {
        List<Action> possibleActions();

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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class MinMaxNode<T extends GameState> {

        final T gameState;
        final MinMaxConfig<T> config;
        final MinMaxNodeType minMaxNodeType;
        final int depth;
        final Optional<Action> optionalAction;
        final MinMaxStat minMaxStat;

        Integer siblingsMaxScore;
        Integer siblingsMinScore;
        List<MinMaxNode> children = new ArrayList<>();
        Integer nodeScore; // should never be accessed directly but via score()

        MinMaxNode(T gameState, MinMaxConfig<T> config, MinMaxNodeType minMaxNodeType, Action action, int depth, MinMaxStat minMaxStat) {
            this.gameState = gameState;
            this.minMaxNodeType = minMaxNodeType;
            this.optionalAction = Optional.ofNullable(action);
            this.depth = depth;
            this.config = config;
            this.minMaxStat = minMaxStat;

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

        boolean isLeaf() {
            return depth == config.maxDepth;
        }

        int score() {
            if (nodeScore == null) {

                Optional<? extends GamePlayer> winner = gameState.getWinner();

                if (winner.isPresent()) {
                    mmlog(() -> "Winner found at depth " + depth + ". Winner: " + winner.get());
                    mmlog(() -> gameState);
                    nodeScore = winner.get() == config.maxPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                } else if (isLeaf()) {
                    long nanoTimeBeforeScoring = System.nanoTime();
                    nodeScore = config.score(gameState);
                    minMaxStat.addScoringTimeNano(System.nanoTime() - nanoTimeBeforeScoring);
                    mmlog(() -> "Scoring leaf at depth " + depth + " : " + nodeScore);
                    mmlog(() -> gameState);
                } else {
                    mmlog(() -> "Scoring node at depth " + depth + " (" + minMaxNodeType + ")");
                    mmlog(() -> gameState);
                    Integer score = null;

                    List<Integer> childrenScores = new ArrayList<>();
                    List<Action> possibleActions = gameState.possibleActions();
                    for (Action possibleAction : possibleActions) {
                        MinMaxNode childNode = new MinMaxNode<>(possibleAction.accept(gameState), config, minMaxNodeType.switchGamer(), possibleAction, depth + 1, minMaxStat);
                        children.add(childNode);

                        mmlog(() -> childrenScores);
                        childNode.addSiblingScores(childrenScores);

                        int childScore = childNode.score();
                        childrenScores.add(childScore);

                        if (score == null) {
                            score = childScore;
                        } else {
                            score = minMaxNodeType.selectScore(score, childScore);
                        }

                        if (minMaxNodeType.shouldStopScoring(score, this)) {
                            mmlog(() -> "Pruning branch at depth  " + depth + " : " + childScore + " siblings=" + siblingsMaxScore + "," + siblingsMinScore + "," + minMaxNodeType);
                            break;
                        }
                    }

                    nodeScore = score;
                    mmlog(() -> "Scoring node at depth " + depth + " (" + minMaxNodeType + ") > " + nodeScore);
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

            int score = score();

            Optional<Action> optionalAction = children.stream()
                    .max(Comparator.comparingInt(MinMaxNode::score))
                    .flatMap(minMaxNode -> minMaxNode.optionalAction);

            minMaxStat.setTotalScoringTime(System.currentTimeMillis() - timeBeforeScoring);
            mmlog(() -> minMaxStat);

            return optionalAction;
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
        };

        abstract Integer selectScore(Integer score1, int score2);

        abstract MinMaxNodeType switchGamer();

        abstract boolean shouldStopScoring(int newChildScore, MinMaxNode<?> minMaxNode);
    }

    public static class MinMaxStat {
        final long nodesCreationStartTime;
        long nodesCreationEndTime;
        long nbOfNodes;
        long nbOfTerminalNodes;
        long totalScoreTime;
        long cumulativeScoringTimeNano;
        long nbOfScoring;

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
            mmlog(() -> "addScoringTimeNano:" + scoringTime);
            cumulativeScoringTimeNano += scoringTime;
            nbOfScoring++;
        }

        public void setTotalScoringTime(long totalScoreTime) {
            this.totalScoreTime = totalScoreTime;
        }

        public float meanScoringTimeMs() {
            return (cumulativeScoringTimeNano / 1_000_000) / (float) Math.max(1, nbOfScoring);
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
                    '}';
        }
    }

    @SafeVarargs
    private static void mmlog(Supplier<Object>... logs) {
        if(MIN_MAX_LOG) {
            System.err.println(Arrays.asList(logs).stream().map(Supplier::get).map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }
}