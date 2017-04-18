import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(coordinate1.distance(coordinate1)).isEqualTo(0);
        assertThat(coordinate1.distance(coordinate2)).isEqualTo(5);
        assertThat(coordinate1.distance(coordinate3)).isEqualTo(4);
        assertThat(coordinate1.distance(coordinate4)).isEqualTo(5);
        assertThat(coordinate1.distance(coordinate5)).isEqualTo(2);
        assertThat(coordinate1.distance(coordinate6)).isEqualTo(1);
        assertThat(coordinate1.distance(coordinate7)).isEqualTo(2);
        assertThat(coordinate1.distance(coordinate8)).isEqualTo(4);
    }

    @Test
    public void testPath() {
        assertThat(coordinate1.pathTo(coordinate1, new Player.GameState())).isEmpty();
        assertThat(coordinate1.pathTo(coordinate2, new Player.GameState()).get().toString()).isEqualTo("[1,0][2,1][3,1][4,2][5,2]");
        assertThat(coordinate1.pathTo(coordinate3, new Player.GameState()).get().size()).isEqualTo(4);
        assertThat(coordinate1.pathTo(coordinate4, new Player.GameState()).get().size()).isEqualTo(5);
        assertThat(coordinate1.pathTo(coordinate8, new Player.GameState()).get().size()).isEqualTo(4);
    }

    @Test
    public void testPathWithMines() {
        Player.GameState gameState = new Player.GameState();
        gameState.addAllMines(mines(xy(3, 1), xy(2,2), xy(1, 2)));
        assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[1,0][2,1][3,0][4,1][5,1][5,2]");

        gameState.addAllMines(mines(xy(3, 0), xy(0,2)));
        assertThat(coordinate1.pathTo(coordinate2, gameState)).isEmpty();
    }

    @Test
    public void testPathWithCannonball() {
        Player.GameState gameState = new Player.GameState();
        gameState.addCannonBall(cannonBall(3, 1, 1));
        gameState.addCannonBall(cannonBall(1, 0, 3));
        gameState.addCannonBall(cannonBall(2, 1, 2));

        assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[1,0][2,1][3,1][4,2][5,2]");

        gameState.addCannonBall(cannonBall(1, 0, 1));
        assertThat(coordinate1.pathTo(coordinate2, gameState).get().toString()).isEqualTo("[0,1][1,1][2,2][3,2][4,2][5,2]");
    }

    @Test
    public void testGetOrientation() {
        assertThat(xy(0, 0).orientationTo(xy(0, 1))).isEqualTo(0);
        assertThat(xy(1, 1).orientationTo(xy(2, 2))).isEqualTo(5);
        assertThat(xy(2, 4).orientationTo(xy(1, 3))).isEqualTo(2);
        assertThat(xy(0, 0).orientationTo(xy(1, 0))).isEqualTo(5);
        assertThat(xy(3, 1).orientationTo(xy(3, 0))).isEqualTo(3);
        assertThat(xy(3, 1).orientationTo(xy(4, 1))).isEqualTo(4);
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
