import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    public void testGetDirection() {
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(2, 2))).isEqualTo(Player.Direction.NW);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(2, 3))).isEqualTo(Player.Direction.N);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(2, 4))).isEqualTo(Player.Direction.NE);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(3, 4))).isEqualTo(Player.Direction.E);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(4, 4))).isEqualTo(Player.Direction.SE);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(4, 3))).isEqualTo(Player.Direction.S);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(4, 2))).isEqualTo(Player.Direction.SW);
        Assertions.assertThat(Player.getDirection(coordinate(3, 3), coordinate(3, 2))).isEqualTo(Player.Direction.W);
    }

    private Player.Coordinate coordinate(int row, int col) {
        return new Player.Coordinate(row, col);
    }
}