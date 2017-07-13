package tictactoe.player;

import minmax.Player;
import tictactoe.TicTacToeGameState;

import java.util.Optional;

public interface TicTacToePlayer extends Player.GamePlayer {

    Optional<Player.Action> play(TicTacToeGameState gameState);

    TicTacToePlayerType getType();
}
