package tictactoe;

import minmax.Player;
import tictactoe.player.XYTicTacToePlayer;

class PlayOnAction implements Player.Action {

    final TicTacToeGame.Coordinate coordinate;
    final XYTicTacToePlayer ticTacToePlayer;

    PlayOnAction(TicTacToeGame.Coordinate coordinate, XYTicTacToePlayer ticTacToePlayer) {
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
