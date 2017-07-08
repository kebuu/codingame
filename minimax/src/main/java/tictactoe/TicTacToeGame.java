package tictactoe;

import minmax.Player;
import tictactoe.player.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TicTacToeGame {

    public static void main(String[] args) {
        TicTacToeGame ticTacToeGame = new TicTacToeGame();

        OTicTacToePlayer oPlayer = new OTicTacToePlayer(new RandomPlayer());
        XTicTacToePlayer xPlayer = new XTicTacToePlayer(new FirstPossibleActionPlayer());
        Optional<XYTicTacToePlayer> winner = ticTacToeGame.play(oPlayer, xPlayer);
        System.out.println(winner);
    }

    public Optional<XYTicTacToePlayer> play(OTicTacToePlayer oPlayer, XTicTacToePlayer xPlayer) {

        TicTacToeGameState gameState = new TicTacToeGameState(oPlayer, xPlayer);
        System.out.println(gameState);

        TicTacToePlayer nextPlayer = oPlayer;
        Optional<XYTicTacToePlayer> optionalWinner = getWinner(gameState);

        while (!isEndOfGame(gameState) && !optionalWinner.isPresent()) {
            Optional<Player.Action> optionalAction = nextPlayer.play(gameState);

            if (optionalAction.isPresent()) {
                gameState = (TicTacToeGameState) optionalAction.get().accept(gameState);
            }

            optionalWinner = getWinner(gameState);
            nextPlayer = nextPlayer == oPlayer ? xPlayer : oPlayer;

            System.out.println(gameState);
        }

        return optionalWinner;
    }

    private Optional<XYTicTacToePlayer> getWinner(TicTacToeGameState gameState) {
        return winningSituations().stream()
                .map(coordinates -> coordinates.stream()
                        .map(gameState::getCellOwner)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(Function.identity())))
                .filter(xyTicTacToePlayerListMap ->  {
                    return xyTicTacToePlayerListMap.entrySet().stream()
                                    .anyMatch(xyTicTacToePlayerListEntry -> xyTicTacToePlayerListEntry.getValue().size() == 3);
                })
                .flatMap(xyTicTacToePlayerListMap -> xyTicTacToePlayerListMap.keySet().stream())
                .findFirst();
    }

    private boolean isEndOfGame(TicTacToeGameState gameState) {
        return gameState.isEndOfGame();
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

    protected static Coordinate xy(int x, int y) {
        return new Coordinate(x, y);
    }

    protected static class Coordinate {
        final int x;
        final int y;

        private Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }
}
