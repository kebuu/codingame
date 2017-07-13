package minmax;

import java.util.*;
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
        String asString();
        GameState fromString(String stateAsString);
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

        Integer siblingsMaxScore;
        Integer siblingsMinScore;
        List<MinMaxNode> children = new ArrayList<>();
        Integer nodeScore; // should never be accessed directly but via score()

        MinMaxNode(T gameState, MinMaxConfig<T> config, MinMaxNodeType minMaxNodeType, Action action, int depth) {
            this.gameState = gameState;
            this.minMaxNodeType = minMaxNodeType;
            this.optionalAction = Optional.ofNullable(action);
            this.depth = depth;
            this.config = config;

            if (depth < config.maxDepth()) {
                Optional<? extends GamePlayer> winner = gameState.getWinner();

                if (winner.isPresent()) {
                    mmlog("Winner found, stopping search on this branch at depth" + depth + ". Winner: " + winner.get());
                    mmlog(gameState);
                } else {
                    List<Action> possibleActions = gameState.possibleActions();
                    for (Action possibleAction : possibleActions) {
                        children.add(new MinMaxNode<>(possibleAction.accept(gameState), config, minMaxNodeType.switchGamer(), possibleAction, depth + 1));
                    }
                }
            }
        }

        MinMaxNode(T gameState, MinMaxConfig<T> config, MinMaxNodeType minMaxNodeType, int depth) {
            this(gameState, config, minMaxNodeType, null, depth);
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
            return children.isEmpty();
        }

        int score() {
            if (nodeScore == null) {

                Optional<? extends GamePlayer> winner = gameState.getWinner();

                if (winner.isPresent()) {
                    mmlog("Winner found at depth " + depth + ". Winner: " + winner.get());
                    mmlog(gameState);
                    nodeScore = winner.get() == config.maxPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                } else if (isLeaf()) {
                    nodeScore = config.score(gameState);
                    mmlog("Scoring leaf at depth " + depth + " : " + nodeScore);
                    mmlog(gameState);
                } else {
                    mmlog("\nScoring node at depth " + depth + " (" + minMaxNodeType + ")");
                    mmlog(gameState);
                    Integer score = null;

                    List<Integer> childrenScores = new ArrayList<>();
                    for (MinMaxNode child : children) {
                        mmlog(childrenScores);
                        child.addSiblingScores(childrenScores);

                        int childScore = child.score();
                        childrenScores.add(childScore);

                        if (score == null) {
                            score = childScore;
                        } else {
                            score = minMaxNodeType.selectScore(score, childScore);
                        }

                        if (minMaxNodeType.shouldStopScoring(score, this)) {
                            mmlog("Pruning branch : " + score + " siblings=" + siblingsMaxScore + "," + siblingsMinScore + "," + minMaxNodeType);
                            break;
                        }
                    }

                    mmlog("\nScoring node at depth " + depth + " (" + minMaxNodeType + ") > " + score);
                    nodeScore = score;
                }
            }

            return nodeScore;
        }
    }

    public static class MinMaxRootNode<T extends GameState> extends MinMaxNode<T> {

        public MinMaxRootNode(T gameState, MinMaxConfig<T> config) {
            super(gameState, config, MinMaxNodeType.MAX, 0);
        }

        public Optional<Action> bestAction() {
            return children.stream()
                    .max(Comparator.comparingInt(MinMaxNode::score))
                    .flatMap(minMaxNode -> minMaxNode.optionalAction);
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

    private static void mmlog(Object... logs) {
        if(MIN_MAX_LOG) {
            System.err.println(Arrays.asList(logs).stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }
}