package tictactoe.player;

import minmax.Player;

import java.util.Optional;

public abstract class XYTicTacToePlayer implements TicTacToePlayer {

    private final TicTacToePlayer delegate;

    public XYTicTacToePlayer(TicTacToePlayer delegate) {
        this.delegate = delegate;
    }

    public Optional<Player.Action> play(Player.GameState gameState) {
        return delegate.play(gameState);
    }

    public abstract String symbol();

    @Override
    public String toString() {
        return symbol() + " " + delegate.getClass().getSimpleName();
    }
}
