package tictactoe.player;

public abstract class BaseTicTacToePlayer implements TicTacToePlayer{

    private final TicTacToePlayerType type;

    protected BaseTicTacToePlayer(TicTacToePlayerType type) {
        this.type = type;
    }

    public TicTacToePlayerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " : " + type;
    }
}
