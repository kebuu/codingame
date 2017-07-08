package tictactoe.player;

import minmax.Player;

import java.util.Optional;

public interface TicTacToePlayer {

    Optional<Player.Action> play(Player.GameState gameState);
}
