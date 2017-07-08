package minmax;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Player {

    public static void main(String[] args) {
        GameState gameState = null;

        int maxDepth = 4;
        MinMaxRootNode minMaxRootNode = new MinMaxRootNode(gameState, MinMaxNodeType.MAX, maxDepth);
        minMaxRootNode.bestAction()
                .ifPresent(action -> System.out.println(action.asString()));
    }

    public interface GameState {

        String asString();

        GameState fromString(String stateAsString);

        List<Action> possibleActions();

        int score();
    }

    public interface Action {
        String asString();

        GameState accept(GameState gameState);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class MinMaxNode {

        final GameState gameState;
        final MinMaxNodeType minMaxNodeType;
        final int depth;
        final int maxDepth;
        final Optional<Action> optionalAction;

        Optional<Integer> optionalSiblingsMaxScore = Optional.empty();
        Optional<Integer> optionalSiblingsMinScore = Optional.empty();
        List<MinMaxNode> children = new ArrayList<>();
        Integer nodeScore; // should never be accessed directly but via score()

        MinMaxNode(GameState gameState, MinMaxNodeType minMaxNodeType, Action action, int depth, int maxDepth) {
            this.gameState = gameState;
            this.minMaxNodeType = minMaxNodeType;
            this.optionalAction = Optional.ofNullable(action);
            this.depth = depth;
            this.maxDepth = maxDepth;

            if (depth <= maxDepth) {
                List<Action> possibleActions = gameState.possibleActions();
                for (Action possibleAction : possibleActions) {
                    children.add(new MinMaxNode(possibleAction.accept(gameState), minMaxNodeType.switchGamer(), possibleAction, depth + 1, maxDepth));
                }
            }
        }

        MinMaxNode(GameState gameState, MinMaxNodeType minMaxNodeType, int depth, int maxDepth) {
            this(gameState, minMaxNodeType, null, depth, maxDepth);
        }

        void addSiblingScore(int score) {
            optionalSiblingsMaxScore = optionalSiblingsMaxScore.map(siblingsMaxScore -> siblingsMaxScore < score ? score : siblingsMaxScore);
            optionalSiblingsMinScore = optionalSiblingsMinScore.map(siblingsMinScore -> siblingsMinScore > score ? score : siblingsMinScore);
        }

        boolean isLeaf() {
            return children.isEmpty();
        }

        int score() {
            if (nodeScore == null) {
                if (isLeaf()) {
                    nodeScore = gameState.score();
                } else {
                    Integer score = null;

                    for (MinMaxNode child : children) {
                        int childScore = child.score();

                        if (score == null) {
                            score = childScore;
                        } else {
                            score = minMaxNodeType.selectScore(score, childScore);
                        }

                        if (minMaxNodeType.shouldStopScoring(score, this)) {
                            break;
                        }
                    }

                    nodeScore = score;
                }
            }

            return nodeScore;
        }
    }

    public static class MinMaxRootNode extends MinMaxNode {

        MinMaxRootNode(GameState gameState, MinMaxNodeType minMaxNodeType, int maxDepth) {
            super(gameState, minMaxNodeType, 0, maxDepth);
        }

        Optional<Action> bestAction() {
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

            public boolean shouldStopScoring(int newChildScore, MinMaxNode minMaxNode) {
                return minMaxNode.optionalSiblingsMinScore.filter(siblingsMinScore -> newChildScore >= siblingsMinScore).isPresent();
            }
        }, MIN {
            Integer selectScore(Integer score1, int score2) {
                return Math.min(score1, score2);
            }

            MinMaxNodeType switchGamer() {
                return MAX;
            }

            public boolean shouldStopScoring(int newChildScore, MinMaxNode minMaxNode) {
                return minMaxNode.optionalSiblingsMaxScore.filter(siblingsMaxScore -> newChildScore <= siblingsMaxScore).isPresent();
            }
        };

        abstract Integer selectScore(Integer score1, int score2);

        abstract MinMaxNodeType switchGamer();

        abstract boolean shouldStopScoring(int newChildScore, MinMaxNode minMaxNode);
    }
}