package tictactoe.player;

import minmax.Player;
import tictactoe.TicTacToeGameState;

import java.util.Optional;

public class MinMaxTicTacToePlayer extends BaseTicTacToePlayer {

    private final Player.MinMaxConfig<TicTacToeGameState> config;

    public MinMaxTicTacToePlayer(TicTacToePlayerType type, int maxDepth) {
        super(type);

        this.config = new Player.MinMaxConfig<TicTacToeGameState>(maxDepth, this) {
            public int score(TicTacToeGameState gameState) {
                Optional<TicTacToePlayer> optionalWinner = gameState.getWinner();

                return optionalWinner
                        .map(ticTacToePlayer -> type == ticTacToePlayer.getType() ? Integer.MAX_VALUE : Integer.MIN_VALUE)
                        .orElse(0);
            }
        };
    }

    public Optional<Player.Action> play(TicTacToeGameState gameState) {
        Player.MinMaxRootNode<?> minMaxRootNode = new Player.MinMaxRootNode<>(gameState, config);
        return minMaxRootNode.bestAction();
    }
}
