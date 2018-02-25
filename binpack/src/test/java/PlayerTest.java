import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerTest {

    List<Player.Box> boxes;
    List<int[]> solutions;
    Player.Params params;

    @BeforeEach
    public void setUp() {
        boxes = new ArrayList<>();
        boxes.add(new Player.Box(0, 1, 1));
        boxes.add(new Player.Box(1, 1, 1));
        boxes.add(new Player.Box(2, 1, 1));
        boxes.add(new Player.Box(3, 1, 1));
        boxes.add(new Player.Box(4, 1, 1));

        solutions = new ArrayList<>();
        solutions.add(new int[]{0, 0, 2, 1, 3});
        solutions.add(new int[]{0, 1, 2, 1, 2});
        solutions.add(new int[]{0, 0, 1, 2, 0});
        solutions.add(new int[]{0, 1, 1, 1, 0});
        solutions.add(new int[]{0, 1, 1, 1, 4});
        solutions.add(new int[]{0, 1, 1, 1, 1});

        params = new Player.Params();
        params.boxes = boxes;
    }

    @Test
    public void testExtractProbability() {
        double[][] probabilityMatrix = Player.extractProbability(solutions, params);

        Player.print(probabilityMatrix);
        System.out.println("------------------------");

        List<int[]> nextGenerationSolutions = Player.generateSolutions(probabilityMatrix, params);
        Player.print(nextGenerationSolutions);

        System.out.println("------------------------");
        Map<Integer, List<Integer>> groupedSolution1 = Player.organizedSolution(solutions.get(0), params);
        Player.print(groupedSolution1);
        Player.print(Player.toCodinGameSolution(groupedSolution1, params));
        System.out.println(Player.score(groupedSolution1, params));
        System.out.println("------------------------");

        Map<Integer, List<Integer>> groupedSolution2 = Player.organizedSolution(solutions.get(1), params);
        Player.print(groupedSolution2);
        Player.print(Player.toCodinGameSolution(groupedSolution2, params));
        System.out.println(Player.score(groupedSolution2, params));
        System.out.println("------------------------");

        Map<Integer, List<Integer>> groupedSolution3 = Player.organizedSolution(solutions.get(4), params);
        Player.print(groupedSolution3);
        Player.print(Player.toCodinGameSolution(groupedSolution3, params));
        System.out.println(Player.score(groupedSolution3, params));
        System.out.println("------------------------");

        Map<Integer, List<Integer>> groupedSolution4 = Player.organizedSolution(solutions.get(5), params);
        Player.print(groupedSolution4);
        Player.print(Player.toCodinGameSolution(groupedSolution4, params));
        System.out.println(Player.score(groupedSolution4, params));

        double[][] initialProbabilities = Player.getInitialProbabilities(params);

        System.out.println("------------------------");
        Player.print(initialProbabilities);

        List<int[]> initialSolutions = Player.generateSolutions(initialProbabilities, params);
        double[][] calculatedInitialProbabilities = Player.extractProbability(initialSolutions, params);

        System.out.println("------------------------");
        Player.print(calculatedInitialProbabilities);
    }

    @Test
    public void testGenerateInitialSolutions() {
        params.populationSize = 100;
        params.bestSolutionSelectionCount = 50;
        params.nbOfGroup = 4;
        params.maxVolume = 1000;
        params.executionMaxTime = 1000000;
        params.executionMaxIteration = 10;
        params.debug = true;
        boxes.clear();
        boxes.add(new Player.Box(0, 10, 5));
        boxes.add(new Player.Box(1, 15, 10));
        boxes.add(new Player.Box(2, 20, 25));
        boxes.add(new Player.Box(3, 20, 10));
        boxes.add(new Player.Box(4, 30, 5));
        boxes.add(new Player.Box(5, 35, 75));
        boxes.add(new Player.Box(6, 35, 25));

        List<int[]> solutions = Player.generateInitialSolutions(params);
        Player.print(solutions);

        Player.play(params);
    }
}
