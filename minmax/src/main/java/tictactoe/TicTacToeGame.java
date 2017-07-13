package tictactoe;

import minmax.Player;
import tictactoe.player.*;

import java.util.*;

public class TicTacToeGame {

    public static void main(String[] args) {
        TicTacToeGame ticTacToeGame = new TicTacToeGame();

        TicTacToePlayer xPlayer = new RandomTicTacToePlayer(TicTacToePlayerType.X);
        TicTacToePlayer oPlayer = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
        //TicTacToePlayer xPlayer = new FirstPossibleActionPlayer();
        Optional<TicTacToePlayer> winner = ticTacToeGame.play(oPlayer, xPlayer);
        System.err.println(winner);
    }

    public Optional<TicTacToePlayer> play(TicTacToePlayer player1, TicTacToePlayer player2) {
        TicTacToeGameState gameState = new TicTacToeGameState(player1, player2);
        System.err.println(gameState);

        TicTacToePlayer nextPlayer = player1;
        Optional<TicTacToePlayer> optionalWinner = gameState.getWinner();

        int turn = 1;
        while (!isEndOfGame(gameState) && !optionalWinner.isPresent()) {
            long currentTimeMillis = System.currentTimeMillis();
            Optional<Player.Action> optionalAction = nextPlayer.play(gameState);
            System.err.println("Time to play (" + nextPlayer + ") : " + (System.currentTimeMillis() - currentTimeMillis));
            if (optionalAction.isPresent()) {
                gameState = optionalAction.get().accept(gameState);
            }

            optionalWinner = gameState.getWinner();
            nextPlayer = nextPlayer == player1 ? player2 : player1;

            System.err.println("Game state end of turn " + turn + " : ");
            System.err.println(gameState);
            turn++;
        }

        return optionalWinner;
    }

    private boolean isEndOfGame(TicTacToeGameState gameState) {
        return gameState.isEndOfGame();
    }
}
