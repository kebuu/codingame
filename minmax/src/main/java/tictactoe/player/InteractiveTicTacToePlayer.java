package tictactoe.player;

import minmax.Player;
import tictactoe.PlayOnAction;
import tictactoe.TicTacToeGameState;

import java.util.Optional;
import java.util.Scanner;

public class InteractiveTicTacToePlayer extends BaseTicTacToePlayer {

    Scanner scanner = new Scanner(System.in);

    public InteractiveTicTacToePlayer(TicTacToePlayerType type) {
        super(type);
    }

    public Optional<Player.Action> play(TicTacToeGameState gameState) {
		try {
			System.out.println("Quel est ta prochaine actions ? (format attendu : x y)");
			String input = scanner.nextLine();
			String[] split = input.split(" +");

			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);

			TicTacToePlayer cellOwner = gameState.getCellOwner(x, y);
			if (cellOwner == null) {
				return Optional.of(new PlayOnAction(x, y, this));
			} else {
				System.out.println("Cette position est prise...");
				return play(gameState);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return play(gameState);
		}
	}
}
