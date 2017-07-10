package tictactoe.player;

import minmax.Player;
import tictactoe.TicTacToeGameState;

import java.util.Optional;

public interface TicTacToePlayer {

    Optional<Player.Action> play(TicTacToeGameState gameState);

    TicTacToePlayerType getType();
}
