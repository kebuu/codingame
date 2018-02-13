import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerTest {

    List<Player.Box> defaultBoxes = createBoxes(new float[] {1, 2, 3, 3, 1, 2}, new float[] {1, 2, 2, 3, 1, 3});
    Player.Solution defaultSolution = new Player.Solution(new int[] {0, 1, 2, 2, 1, 1});
    Player.GameContext defaultGameContext = gameContext(defaultBoxes);
    Player.GameParameters defaultGameParameters = gameParameters();
    Player.ContextualSolution defaultContextualSolution = new Player.ContextualSolution(defaultSolution, defaultGameContext, defaultGameParameters);

    @Test
    public void testPrintSolution() {
        Player.Solution solution = new Player.Solution(new int[] {1, 2, 3});
        Assertions.assertThat(solution.print()).isEqualTo("1 2 3");
    }

    @Test
    public void testSolutionIsValid() {
        Assertions.assertThat(contextualSolution(defaultSolution, defaultBoxes, 6).isValid()).isTrue();
        Assertions.assertThat(contextualSolution(defaultSolution, defaultBoxes, 5).isValid()).isFalse();
    }

    @Test
    public void testGetWeightRepartition() {
        Map<Integer, Float> weightRepartition = new HashMap<>();
        weightRepartition.put(0, 1F);
        weightRepartition.put(1, 5F);
        weightRepartition.put(2, 6F);

        Player.ContextualSolution contextualSolution =
                new Player.ContextualSolution(defaultSolution, gameContext(defaultBoxes, 3, 10), defaultGameParameters);
        Assertions.assertThat(contextualSolution.weightByTruck).isEqualTo(weightRepartition);
    }

    @Test
    public void testScore() {
        Player.GameContext gameContext = gameContext(defaultBoxes, 3, 10);

        Player.ContextualSolution contextualSolution1 = new Player.ContextualSolution(defaultSolution, gameContext, defaultGameParameters);
        Assertions.assertThat(contextualSolution1.score).isEqualTo(5L);

        Player.ContextualSolution contextualSolution2 = new Player.ContextualSolution(new Player.Solution(new int[] {0, 1, 2, 0, 2, 1}), gameContext, defaultGameParameters);
        Assertions.assertThat(contextualSolution2.score).isEqualTo(0F);

        Player.ContextualSolution invalidSolution = new Player.ContextualSolution(new Player.Solution(new int[] {0, 0, 0, 0, 0, 0}), gameContext, defaultGameParameters);
        Assertions.assertThat(invalidSolution.score).isEqualTo(312F);
    }

    @Test
    public void testGenerateRandomSolution() {
        int truckMaxVolume = 100;
        List<Player.Box> boxes = createBoxes(new float[]{1, 2, 3, 3, 1, 2}, new float[]{1, 10, 5, 100, 1, 3});
        Player.GameContext gameContext = gameContext(boxes, 3, truckMaxVolume);

        Player.Solution solution = Player.generateRandomSolution(gameContext);
        Assertions.assertThat(solution.repartition).hasSize(6);
        Assertions.assertThat(solution.repartition).contains(0, 1, 2, 0, 1, 2);

        Player.ContextualSolution contextualSolution = new Player.ContextualSolution(solution, gameContext, defaultGameParameters);
        System.out.println(contextualSolution.volumeByTruck);
        Assertions.assertThat(contextualSolution.volumeByTruck.values().stream().noneMatch(volume -> volume > truckMaxVolume)).isEqualTo(true);
    }

    private List<Player.Box> createBoxes(float[] weights, float[] volumes) {
        List<Player.Box> boxes = new ArrayList<>();

        for (int i = 0; i < weights.length; i++) {
            boxes.add(new Player.Box(i, weights[i], volumes[i]));
        }
        return boxes;
    }

    private Player.GameContext gameContext(List<Player.Box> boxes) {
        return new Player.GameContext(boxes);
    }

    private Player.GameContext gameContext(List<Player.Box> boxes, int nbOfTrucks, int truckMaxVolume) {
        return new Player.GameContext(boxes, nbOfTrucks, truckMaxVolume);
    }

    private Player.GameParameters gameParameters() {
        return new Player.GameParameters();
    }

    private Player.ContextualSolution contextualSolution(Player.Solution solution, List<Player.Box> boxes, int truckMaxVolume ) {
        return new Player.ContextualSolution(solution, gameContext(boxes, boxes.size(), truckMaxVolume), defaultGameParameters);
    }

    private Player.ContextualSolution contextualSolution(Player.Solution solution) {
        return new Player.ContextualSolution(solution, defaultGameContext, defaultGameParameters);
    }

}
