package tictactoe.player;

import minmax.Player;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tictactoe.Coordinate;
import tictactoe.PlayOnAction;
import tictactoe.TicTacToeGameState;

import java.util.Arrays;
import java.util.Optional;

public class MinMaxTicTacToePlayerTest {

   @Test
   public void test_should_win() {
      MinMaxTicTacToePlayer player1 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
      RandomTicTacToePlayer player2 = new RandomTicTacToePlayer(TicTacToePlayerType.X);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
            .withPlayer1(player1, Arrays.asList(xy(0, 0), xy(0, 2)))
            .withPlayer2(player2, Arrays.asList(xy(1, 0), xy(1, 1)))
            .build();

      Optional<Player.Action> actionOptional = player1.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(0, 1));
   }

   @Test
   public void test_should_defend_direct_lose() {
      MinMaxTicTacToePlayer player1 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
      RandomTicTacToePlayer player2 = new RandomTicTacToePlayer(TicTacToePlayerType.X);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
              .withPlayer1(player1, Arrays.asList(xy(0, 0), xy(2, 1)))
              .withPlayer2(player2, Arrays.asList(xy(1, 0), xy(1, 1)))
              .build();

      Optional<Player.Action> actionOptional = player1.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(1, 2));
   }

   @Test
   public void test_should_play_indirect_win() {
      MinMaxTicTacToePlayer player1 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
      RandomTicTacToePlayer player2 = new RandomTicTacToePlayer(TicTacToePlayerType.X);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
              .withPlayer1(player1, Arrays.asList(xy(1, 0), xy(0, 2)))
              .withPlayer2(player2, Arrays.asList(xy(0, 0), xy(0, 1)))
              .build();

      Optional<Player.Action> actionOptional = player1.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(1, 1));
   }

   @Test
   public void test_should_play_last_possible_action() {
      MinMaxTicTacToePlayer player1 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
      RandomTicTacToePlayer player2 = new RandomTicTacToePlayer(TicTacToePlayerType.X);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
              .withPlayer1(player1, Arrays.asList(xy(0, 0), xy(0, 1), xy(1, 2), xy(2, 0)))
              .withPlayer2(player2, Arrays.asList(xy(0, 2), xy(1, 0), xy(1, 1), xy(2, 1)))
              .build();

      Optional<Player.Action> actionOptional = player1.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(2, 2));
   }

   @Test
   public void test_should_win_as_fast_as_possible() {
      MinMaxTicTacToePlayer player1 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);
      RandomTicTacToePlayer player2 = new RandomTicTacToePlayer(TicTacToePlayerType.X);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
              .withPlayer1(player1, Arrays.asList(xy(1, 2), xy(2, 2)))
              .withPlayer2(player2, Arrays.asList(xy(2, 0), xy(2, 1)))
              .build();

      Optional<Player.Action> actionOptional = player1.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(0, 2));
   }

   @Test
   public void test_should_defend_as_long_as_possible() {
      RandomTicTacToePlayer player1 = new RandomTicTacToePlayer(TicTacToePlayerType.X);
      MinMaxTicTacToePlayer player2 = new MinMaxTicTacToePlayer(TicTacToePlayerType.O, 3);

      TicTacToeGameState gameState = new TicTacToeGameState.TicTacToeGameStateBuilder()
              .withPlayer1(player1, Arrays.asList(xy(1, 2), xy(2, 2), xy(0, 1)))
              .withPlayer2(player2, Arrays.asList(xy(2, 0), xy(2, 1)))
              .build();

      Optional<Player.Action> actionOptional = player2.play(gameState);

      Assertions.assertThat(actionOptional).isPresent();
      Assertions.assertThat(((PlayOnAction)actionOptional.get()).getCoordinate()).isEqualTo(xy(0, 2));
   }

   private Coordinate xy(int x, int y) {
      return new Coordinate(x, y);
   }
}