import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PlayerTest {

    Player.Boxes allBoxes = createBoxes(new float[] {1, 2, 3, 3, 1, 2}, new float[] {1, 2, 2, 3, 1, 3});
    Player.Solution solution = new Player.Solution(new int[] {0, 1, 2, 2, 1, 1});

    @Test
    public void testPrintSolution() {
        Player.Solution solution = new Player.Solution(new int[] {1, 2, 3});
        Assertions.assertThat(solution.print()).isEqualTo("1 2 3");
    }

    @Test
    public void testSolutionIsValid() {
        Assertions.assertThat(solution.isValid(6, allBoxes)).isTrue();
        Assertions.assertThat(solution.isValid(5, allBoxes)).isFalse();
    }

    @Test
    public void testGetWeightRepartition() {
        Map<Integer, Float> weightRepartition = new HashMap<>();
        weightRepartition.put(0, 1F);
        weightRepartition.put(1, 5F);
        weightRepartition.put(2, 6F);
        Assertions.assertThat(solution.getWeightRepartition(3, allBoxes)).isEqualTo(weightRepartition);
    }

    @Test
    public void testScore() {
        Assertions.assertThat(solution.score(allBoxes)).isEqualTo(5L);
        Assertions.assertThat(new Player.Solution(new int[] {0, 1, 2, 0, 2, 1}).score(allBoxes)).isEqualTo(0F);
    }

    @Test
    public void testTruckComparison() {
        Assertions.assertThat(new Player.Truck(0).addVolume(5).compareTo(new Player.Truck(1))).isEqualTo(1);
        Assertions.assertThat(new Player.Truck(0).compareTo(new Player.Truck(1).addVolume(5))).isEqualTo(-1);
        Assertions.assertThat(new Player.Truck(0).compareTo(new Player.Truck(1))).isEqualTo(-1);
    }

    @Test
    public void testGenerateRandomSolution() {
        Player.Boxes boxes = createBoxes(new float[]{1, 2, 3, 3, 1, 2}, new float[]{1, 10, 5, 100, 1, 3});
        Player.Solution solution = Player.generateRandomSolution(boxes, 3);
        Assertions.assertThat(solution.repartition).hasSize(6);
        Assertions.assertThat(solution.repartition).contains(0, 1, 2, 0, 1, 2);

        Map<Integer, Float> volumeRepartition = new HashMap<>();
        volumeRepartition.put(0, 101F);
        volumeRepartition.put(1, 10F);
        volumeRepartition.put(2, 9F);
        Assertions.assertThat(solution.getVolumeRepartition(3, boxes)).isEqualTo(volumeRepartition);
    }

    private Player.Boxes createBoxes(float[] weights, float[] volumes) {
        Player.Boxes boxes = new Player.Boxes();

        for (int i = 0; i < weights.length; i++) {
            boxes.add(new Player.Box(weights[i], volumes[i]));
        }
        return boxes;
    }
}
