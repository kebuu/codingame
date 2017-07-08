package tictactoe.player;

public class OTicTacToePlayer extends XYTicTacToePlayer {

    public OTicTacToePlayer(TicTacToePlayer delegate) {
        super(delegate);
    }

    public String symbol() {
        return "o";
    }
}
