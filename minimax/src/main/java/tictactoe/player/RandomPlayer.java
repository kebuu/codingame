package tictactoe.player;

import minmax.Player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomPlayer implements TicTacToePlayer {
    Random random = new Random();

    public Optional<Player.Action> play(Player.GameState gameState) {
        List<Player.Action> actions = gameState.possibleActions();

        return actions.isEmpty() ? Optional.empty() : Optional.of(actions.get(random.nextInt(actions.size())));
    }
}
