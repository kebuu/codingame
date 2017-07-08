package tictactoe.player;

import minmax.Player;

import java.util.Optional;

public class FirstPossibleActionPlayer implements TicTacToePlayer {
    public Optional<Player.Action> play(Player.GameState gameState) {
        return gameState.possibleActions().stream().findFirst();
    }
}
