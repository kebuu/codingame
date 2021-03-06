package tictactoe.player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import minmax.Player;
import tictactoe.TicTacToeGameState;

public class RandomTicTacToePlayer extends BaseTicTacToePlayer {

    Random random = new Random();

    public RandomTicTacToePlayer(TicTacToePlayerType type) {
        super(type);
    }

    public Optional<Player.Action> play(TicTacToeGameState gameState) {
        List<Player.Action> actions = gameState.possibleActions();

        return actions.isEmpty() ? Optional.empty() : Optional.of(actions.get(random.nextInt(actions.size())));
    }
}
