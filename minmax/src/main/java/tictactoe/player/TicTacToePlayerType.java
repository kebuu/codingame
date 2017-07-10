package tictactoe.player;

public enum TicTacToePlayerType {
    X("x"), O("o");

    private final String symbol;

    TicTacToePlayerType(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
