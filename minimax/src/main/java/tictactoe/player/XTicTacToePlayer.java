package tictactoe.player;

public class XTicTacToePlayer extends XYTicTacToePlayer {

    public XTicTacToePlayer(TicTacToePlayer delegate) {
        super(delegate);
    }

    public String symbol() {
        return "x";
    }
}
