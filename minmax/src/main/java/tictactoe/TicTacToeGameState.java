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
    private Optional<TicTacToePlayer> optionalWinner;

    public  TicTacToeGameState(TicTacToePlayer player1, TicTacToePlayer player2) {
        assert player1.getType() != player2.getType();

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

    public TicTacToePlayer getCellOwner(int x, int y) {
        assert x >= 0 && y >= 0 && x < 3 && y < 3;

        return board.get(x).get(y);
    }

    public TicTacToePlayer getCellOwner(Coordinate coordinate) {
        return getCellOwner(coordinate.x, coordinate.y);
    }

    public Player.GameState withAction(PlayOnAction playOnAction) {
        TicTacToeGameState ticTacToeGameState = new TicTacToeGameState(player1, player2);

        for(int i = 0; i < 3; i++) {
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
        if (optionalWinner == null) {
            optionalWinner = winningSituations().stream()
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

        return optionalWinner;
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

    public static class TicTacToeGameStateBuilder {
        TicTacToePlayer player1;
        TicTacToePlayer player2;
        List<Coordinate> player1Coordinates = new ArrayList<>();
        List<Coordinate> player2Coordinates = new ArrayList<>();

        public TicTacToeGameStateBuilder withPlayer1(TicTacToePlayer player1, List<Coordinate> player1Coordinates) {
            this.player1 = player1;
            this.player1Coordinates.clear();
            this.player1Coordinates.addAll(player1Coordinates);
            return this;
        }

        public TicTacToeGameStateBuilder withPlayer2(TicTacToePlayer player2, List<Coordinate> player2Coordinates) {
            this.player2 = player2;
            this.player2Coordinates.clear();
            this.player2Coordinates.addAll(player2Coordinates);
            return this;
        }

        public TicTacToeGameStateBuilder withPlayer1(TicTacToePlayer player1) {
            withPlayer1(player1, new ArrayList<>());
            return this;
        }

        public TicTacToeGameStateBuilder withPlayer2(TicTacToePlayer player2) {
            withPlayer2(player2, new ArrayList<>());
            return this;
        }

        public TicTacToeGameState build() {
            assert player1 != null;
            assert player2 != null;
            assert player1.getType() !=  player2.getType();
            assert player1Coordinates.size() == player2Coordinates.size() || player1Coordinates.size() == player2Coordinates.size() + 1;
            assert player1Coordinates.stream().noneMatch(coordinate -> player2Coordinates.contains(coordinate));

            TicTacToeGameState gameState = new TicTacToeGameState(player1, player2);

            for(int i = 0; i < 3; i++) {
                List<TicTacToePlayer> row = gameState.board.get(i);
                for(int j = 0; j < 3; j++) {
                    Coordinate coordinate = new Coordinate(i, j);
                    if (player1Coordinates.contains(coordinate)) {
                        row.set(j, player1);
                    } else if (player2Coordinates.contains(coordinate)) {
                        row.set(j, player2);
                    }
                }
            }

            if (player1Coordinates.size() == player2Coordinates.size()) {
                gameState.currentTurnPlayer = player1;
            } else {
                gameState.currentTurnPlayer = player2;
            }

            return gameState;
        }
    }
}
