package tictactoe;

import minmax.Player;
import tictactoe.player.TicTacToePlayer;

class PlayOnAction implements Player.Action {

    final Coordinate coordinate;
    final TicTacToePlayer ticTacToePlayer;

    PlayOnAction(Coordinate coordinate, TicTacToePlayer ticTacToePlayer) {
        this.coordinate = coordinate;
        this.ticTacToePlayer = ticTacToePlayer;
    }

    @Override
    public String asString() {
        return "PLAY ON " + coordinate;
    }

    @Override
    public Player.GameState accept(Player.GameState gameState) {
        TicTacToeGameState ticTacToeGameState = (TicTacToeGameState) gameState;

        return ticTacToeGameState.withAction(this);
    }
}
