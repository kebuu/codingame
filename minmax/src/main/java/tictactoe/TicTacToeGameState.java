package tictactoe;

import minmax.Player;
import tictactoe.player.TicTacToePlayer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TicTacToeGameState implements Player.GameState {

    public static final String EMPTY_CELL_SYMBOL = "-";

    protected final List<List<TicTacToePlayer>> board;
    protected final TicTacToePlayer player1;
    protected final TicTacToePlayer player2;
    protected TicTacToePlayer currentTurnPlayer;

    public TicTacToeGameState(TicTacToePlayer player1, TicTacToePlayer player2) {
        assert player1.getType() != player1.getType();

        this.currentTurnPlayer = player1;
        this.player1 = player1;
        this.player2 = player2;
        this.board = new ArrayList<>();

        for(int i = 0; i < 3; i++) {
            List<TicTacToePlayer> row = new ArrayList<>();
            board.add(row);
            for(int j = 0; j < 3; j++) {
                row.add(null);
            }
        }

        board.get(0).set(0, player1);
        board.get(0).set(1, player2);
        board.get(2).set(1, player2);
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
            List<TicTacToePlayer> row = board.get(i);
            for(int j = 0; j < 3; j++) {
                if (row.get(j) == null) {
                    actions.add(new PlayOnAction(xy(i, j), currentTurnPlayer));
                }
            }
        }

        return actions;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<TicTacToePlayer> row : board) {
            String rowToString = row.stream()
                    .map(xyTicTacToePlayer -> xyTicTacToePlayer == null ? EMPTY_CELL_SYMBOL : xyTicTacToePlayer.getType().symbol())
                    .collect(Collectors.joining(", "));
            stringBuilder.append(rowToString).append("\n");
        }
        return stringBuilder.toString().trim();
    }

    public TicTacToePlayer getCellOwner(Coordinate coordinate) {
        assert coordinate.x >= 0 && coordinate.y >= 0 && coordinate.x < 3 && coordinate.y < 3;

        return board.get(coordinate.x).get(coordinate.y);
    }

    public Player.GameState withAction(PlayOnAction playOnAction) {
        TicTacToeGameState ticTacToeGameState = new TicTacToeGameState(player1, player2);

        for(int i = 0; i < 3; i++) {
            List<TicTacToePlayer> row = board.get(i);
            for(int j = 0; j < 3; j++) {
                List<TicTacToePlayer> cellOwners = ticTacToeGameState.board.get(i);
                if (playOnAction.coordinate.x == i && playOnAction.coordinate.y == j) {
                    cellOwners.set(j, playOnAction.ticTacToePlayer);
                } else {
                    cellOwners.set(j, board.get(i).get(j));
                }
            }
        }

        ticTacToeGameState.currentTurnPlayer = currentTurnPlayer == player1 ? player2 : player1;
        return ticTacToeGameState;
    }

    public Optional<TicTacToePlayer> getWinner() {
        return winningSituations().stream()
                .map(coordinates -> coordinates.stream()
                        .map(this::getCellOwner)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(Function.identity())))
                .filter(xyTicTacToePlayerListMap ->  {
                    return xyTicTacToePlayerListMap.entrySet().stream()
                            .anyMatch(xyTicTacToePlayerListEntry -> xyTicTacToePlayerListEntry.getValue().size() == 3);
                })
                .flatMap(xyTicTacToePlayerListMap -> xyTicTacToePlayerListMap.keySet().stream())
                .findFirst();
    }

    private List<List<Coordinate>> winningSituations() {
        List<List<Coordinate>> winningSituations = new ArrayList<>();

        winningSituations.add(Arrays.asList(xy(0, 0), xy(0, 1), xy(0, 2)));
        winningSituations.add(Arrays.asList(xy(0, 0), xy(1, 0), xy(2, 0)));
        winningSituations.add(Arrays.asList(xy(1, 0), xy(1, 1), xy(1, 2)));
        winningSituations.add(Arrays.asList(xy(0, 1), xy(1, 1), xy(2, 1)));
        winningSituations.add(Arrays.asList(xy(2, 0), xy(2, 1), xy(2, 2)));
        winningSituations.add(Arrays.asList(xy(0, 2), xy(1, 2), xy(2, 2)));
        winningSituations.add(Arrays.asList(xy(0, 0), xy(1, 1), xy(2, 2)));
        winningSituations.add(Arrays.asList(xy(2, 0), xy(1, 1), xy(0, 2)));

        return winningSituations;
    }

    public boolean isEndOfGame() {
        return board.stream().flatMap(Collection::stream).noneMatch(Objects::isNull);
    }

    protected static Coordinate xy(int x, int y) {
        return new Coordinate(x, y);
    }
}
