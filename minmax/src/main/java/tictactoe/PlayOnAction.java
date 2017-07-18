package tictactoe;

import minmax.Player;
import tictactoe.player.TicTacToePlayer;

public class PlayOnAction implements Player.Action {

    final Coordinate coordinate;
    final TicTacToePlayer ticTacToePlayer;

    public PlayOnAction(Coordinate coordinate, TicTacToePlayer ticTacToePlayer) {
        this.coordinate = coordinate;
        this.ticTacToePlayer = ticTacToePlayer;
    }

    public PlayOnAction(int x, int y, TicTacToePlayer ticTacToePlayer) {
        this(new Coordinate(x, y), ticTacToePlayer);
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

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public TicTacToePlayer getTicTacToePlayer() {
        return ticTacToePlayer;
    }
}
