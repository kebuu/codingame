import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerTest {

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
        assertThat(coordinate1.pathTo(coordinate1, new Player.GameState()).toString()).isEqualTo("");
        assertThat(coordinate1.pathTo(coordinate2, new Player.GameState()).toString()).isEqualTo("[1,0][2,1][3,1][4,2][5,2]");
        assertThat(coordinate1.pathTo(coordinate3, new Player.GameState()).size()).isEqualTo(4);
        assertThat(coordinate1.pathTo(coordinate4, new Player.GameState()).size()).isEqualTo(5);
        assertThat(coordinate1.pathTo(coordinate8, new Player.GameState()).size()).isEqualTo(4);
    }
}
