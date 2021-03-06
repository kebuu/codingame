import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTest {

    AtomicInteger ids = new AtomicInteger();

    Player.Coordinate coordinate1 = new Player.Coordinate(0, 0);
    Player.Coordinate coordinate2 = new Player.Coordinate(5, 2);
    Player.Coordinate coordinate3 = new Player.Coordinate(1, 3);
    Player.Coordinate coordinate4 = new Player.Coordinate(3, 3);
    Player.Coordinate coordinate5 = new Player.Coordinate(1, 1);
    Player.Coordinate coordinate6 = new Player.Coordinate(1, 0);
    Player.Coordinate coordinate7 = new Player.Coordinate(0, 2);
    Player.Coordinate coordinate8 = new Player.Coordinate(4, 1);

    @Test
    public void testDistance() {
        Assertions.assertThat(coordinate1.distance(coordinate1)).isEqualTo(0);
        Assertions.assertThat(coordinate1.distance(coordinate2)).isEqualTo(5);
        Assertions.assertThat(coordinate1.distance(coordinate3)).isEqualTo(4);
        Assertions.assertThat(coordinate1.distance(coordinate4)).isEqualTo(5);
        Assertions.assertThat(coordinate1.distance(coordinate5)).isEqualTo(2);
        Assertions.assertThat(coordinate1.distance(coordinate6)).isEqualTo(1);
        Assertions.assertThat(coordinate1.distance(coordinate7)).isEqualTo(2);
        Assertions.assertThat(coordinate1.distance(coordinate8)).isEqualTo(4);
    }

    @Test
    public void testFindClosestShootableShipFrom() {
        Player.Ship ship1 = new Player.Ship(0, 10, 5, 1, 5);
        Player.Ship ship2 = new Player.Ship(0, 22, 6, 0, 1);

        Player.GameState gameState = new Player.GameState();
        gameState.enemyShips.add(ship1);

        Assertions.assertThat(gameState.findBestShootFor(ship2, gameState)).isEmpty();
    }

    @Test
    public void testPath() {
        Assertions.assertThat(coordinate1.pathTo(coordinate1, new Player.GameState())).isEmpty();
        Assertions.assertThat(coordinate1.pathTo(coordinate2, new Player.GameState()).get().toString()).isEqualTo("[1,0][1,1][2,2][3,2][4,2][5,2]");
        Assertions.assertThat(coordinate1.pathTo(coordinate3, new Player.GameState()).get().size()).isEqualTo(3);
        Assertions.assertThat(coordinate1.pathTo(coordinate4, new Player.GameState()).get().size()).isEqualTo(5);
        Assertions.assertThat(coordinate1.pathTo(coordinate8, new Player.GameState()).get().size()).isEqualTo(5);
    }

    @Test
    public void testPathWithMines() {
        Player.GameState gameState = new Player.GameState();
        gameState.addAllMines(mines(xy(1, 1), xy(2,1)));
        Assertions.assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[1,0][2,0][3,0][3,1][4,2][5,2]");

        gameState.addAllMines(mines(xy(3, 0), xy(0,1)));
        Assertions.assertThat(coordinate1.pathTo(coordinate2, gameState)).isEmpty();
    }

    @Test
    public void testInTurn() {
        Player.Ship ship0 = new Player.Ship(0, 10, 5, 0, 0);
        Player.Ship ship1 = new Player.Ship(1, 15, 8, 1, 1);
        Player.Ship ship2 = new Player.Ship(2, 9, 9, 2, 2);

        Assertions.assertThat(ship0.inNTurns(2).coordinate).isEqualTo(xy(10, 5));
        Assertions.assertThat(ship1.inNTurns(2).coordinate).isEqualTo(xy(16, 6));
        Assertions.assertThat(ship2.inNTurns(1).coordinate).isEqualTo(xy(8, 7));
    }

    @Test
    public void testPathWithCannonball() {
        Player.GameState gameState = new Player.GameState();
        gameState.addCannonBall(cannonBall(3, 2, 1));
        gameState.addCannonBall(cannonBall(1, 0, 3));
        gameState.addCannonBall(cannonBall(2, 1, 2));

        Assertions.assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[1,0][1,1][2,2][3,2][4,2][5,2]");

        gameState.addCannonBall(cannonBall(1, 0, 1));
        Assertions.assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[0,1][1,1][2,2][3,2][4,2][5,2]");
    }

    @Test
    public void testGetOrientation() {
        Assertions.assertThat(xy(0, 0).orientationTo(xy(0, 1))).isEqualTo(0);
        Assertions.assertThat(xy(1, 1).orientationTo(xy(2, 2))).isEqualTo(5);
        Assertions.assertThat(xy(2, 4).orientationTo(xy(1, 3))).isEqualTo(2);
        Assertions.assertThat(xy(0, 0).orientationTo(xy(1, 0))).isEqualTo(5);
        Assertions.assertThat(xy(3, 1).orientationTo(xy(3, 0))).isEqualTo(3);
        Assertions.assertThat(xy(3, 1).orientationTo(xy(4, 1))).isEqualTo(4);
    }

    @Test
    public void testShipCoordinates() {
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 0).shipCoordinates().asList().toString()).isEqualTo("[[3,2], [2,2], [1,2]]");
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 1).shipCoordinates().asList().toString()).isEqualTo("[[2,1], [2,2], [1,3]]");
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 2).shipCoordinates().asList().toString()).isEqualTo("[[1,1], [2,2], [2,3]]");
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 3).shipCoordinates().asList().toString()).isEqualTo("[[1,2], [2,2], [3,2]]");
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 4).shipCoordinates().asList().toString()).isEqualTo("[[1,3], [2,2], [2,1]]");
        Assertions.assertThat(new Player.Ship(0, 2, 2, 0, 5).shipCoordinates().asList().toString()).isEqualTo("[[2,3], [2,2], [1,1]]");
        Assertions.assertThat(new Player.Ship(0, 14, 7, 0, 4).shipCoordinates().asList().toString()).isEqualTo("[[14,8], [14,7], [15,6]]");
    }

    @Test
    public void testEscapeCanonBallStrategy() {
        Player.EscapeCanonBallStrategy strategy = new Player.EscapeCanonBallStrategy();
        Player.Ship ship = new Player.Ship(0, 2, 2, 0, 0);
        Player.GameState gameState = new Player.GameState();

        Assertions.assertThat(strategy.getAction(ship, gameState)).isNull();

        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 0, 0, 1));
        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 1, 1, 1));
        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 2, 3, 1));
        Assertions.assertThat(strategy.getAction(ship, gameState)).isNull();

        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 2, 2, 1));
        Assertions.assertThat(strategy.getAction(ship, gameState)).isNotNull();

        gameState.cannonBalls.clear();
        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 1, 2, 1));
        Assertions.assertThat(strategy.getAction(ship, gameState)).isNotNull();

        gameState.cannonBalls.clear();
        gameState.cannonBalls.add(new Player.CannonBall(ids.incrementAndGet(), 3, 2, 1));
        Assertions.assertThat(strategy.getAction(ship, gameState)).isNotNull();
    }

    @Test
    public void testFireStrategy() {
        Player.Ship ship = new Player.Ship(0, 3, 20, 0, 4);
        Player.currentTurn = 10;
        Player.GameState gameState = new Player.GameState();

        gameState.addShip(new Player.Ship(0, 8, 5, 3, 4), 0);
//        gameState.addShip(new Player.Ship(0, 10, 9, 0, 2), 0);
//        gameState.addShip(new Player.Ship(0, 9, 7, 0, 0), 0);
        Player.FireAction action1 = (Player.FireAction) new Player.SimpleFireStrategy().getAction(ship, gameState);
        Assertions.assertThat(action1.coordinate).isEqualTo(xy(5, 4));

        Player.currentTurn = 20;
        gameState.enemyShips.clear();
        gameState.addShip(new Player.Ship(0, 4, 4, 0, 0), 0);
        Player.FireAction action2 = (Player.FireAction) new Player.SimpleFireStrategy().getAction(ship, gameState);
        Assertions.assertThat(action2.coordinate).isEqualTo(xy(4, 4));
    }

    private Set<Player.Mine> mines(Player.Coordinate... coordinates) {
        Set<Player.Mine> mines = new HashSet<>();
        for (Player.Coordinate coordinate : coordinates) {
            mines.add(new Player.Mine(ids.getAndIncrement(), coordinate));
        }
        return mines;
    }

    private Player.CannonBall cannonBall(int x, int y, int delay) {
        return new Player.CannonBall(ids.getAndIncrement(), x, y, delay);
    }

    private Player.Coordinate xy(int x, int y) {
        return new Player.Coordinate(x, y);
    }
}
