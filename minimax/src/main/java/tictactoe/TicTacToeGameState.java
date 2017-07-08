package tictactoe;

import minmax.Player;
import tictactoe.player.OTicTacToePlayer;
import tictactoe.player.XYTicTacToePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class TicTacToeGameState implements Player.GameState {

    public static final String EMPTY_CELL_SYMBOL = "-";

    protected final List<List<XYTicTacToePlayer>> board;
    protected final OTicTacToePlayer oPlayer;
    protected final XYTicTacToePlayer xPlayer;
    protected XYTicTacToePlayer currentTurnPlayer;

    public TicTacToeGameState(OTicTacToePlayer oPlayer, XYTicTacToePlayer xPlayer) {
        this.currentTurnPlayer = oPlayer;
        this.oPlayer = oPlayer;
        this.xPlayer = xPlayer;
        this.board = new ArrayList<>();

        for(int i = 0; i < 3; i++) {
            List<XYTicTacToePlayer> row = new ArrayList<>();
            board.add(row);
            for(int j = 0; j < 3; j++) {
                row.add(null);
            }
        }
    }

    @Override
    public String asString() {
        return "No tostring yet";
    }

    @Override
    public Player.GameState fromString(String stateAsString) {
        return null;
    }

    @Override
    public List<Player.Action> possibleActions() {
        List<Player.Action> actions = new ArrayList<>();

        for(int i = 0; i < 3; i++) {
            List<XYTicTacToePlayer> row = board.get(i);
            for(int j = 0; j < 3; j++) {
                if (row.get(j) == null) {
                    actions.add(new PlayOnAction(TicTacToeGame.xy(i, j), currentTurnPlayer));
                }
            }
        }

        return actions;
    }

    @Override
    public int score() {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<XYTicTacToePlayer> row : board) {
            String rowToString = row.stream()
                    .map(xyTicTacToePlayer -> xyTicTacToePlayer == null ? EMPTY_CELL_SYMBOL : xyTicTacToePlayer.symbol())
                    .collect(Collectors.joining(", "));
            stringBuilder.append(rowToString).append("\n");
        }
        return stringBuilder.toString();
    }

    public XYTicTacToePlayer getCellOwner(TicTacToeGame.Coordinate coordinate) {
        assert coordinate.x >= 0 && coordinate.y >= 0 && coordinate.x < 3 && coordinate.y < 3;

        return board.get(coordinate.x).get(coordinate.y);
    }

    public Player.GameState withAction(PlayOnAction playOnAction) {
        board.get(playOnAction.coordinate.x).set(playOnAction.coordinate.y, playOnAction.ticTacToePlayer);
        currentTurnPlayer = currentTurnPlayer == oPlayer ? xPlayer : oPlayer;
        return this;
    }

    public boolean isEndOfGame() {
        return board.stream().flatMap(Collection::stream).noneMatch(Objects::isNull);
    }
}
