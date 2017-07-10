package tictactoe.player;

import minmax.Player;
import tictactoe.TicTacToeGameState;

import java.util.Optional;

public class FirstPossibleActionPlayer extends BaseTicTacToePlayer {

    protected FirstPossibleActionPlayer(TicTacToePlayerType type) {
        super(type);
    }

    public Optional<Player.Action> play(TicTacToeGameState gameState) {
        return gameState.possibleActions().stream().findFirst();
    }
}
