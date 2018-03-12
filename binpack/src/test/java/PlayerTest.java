import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
//        double[][] probabilityMatrix = Player.extractProbability(solutions, params);
//
//        Player.print(probabilityMatrix);
//        System.out.println("------------------------");
//
//        List<int[]> nextGenerationSolutions = Player.generateNextGenerationSolutions(probabilityMatrix, params);
//        Player.print(nextGenerationSolutions);
//
//        System.out.println("------------------------");
//        Map<Integer, List<Integer>> groupedSolution1 = Player.organizeSolution(solutions.get(0), params);
//        Player.print(groupedSolution1);
//        Player.print(Player.toCodinGameSolution(groupedSolution1, params));
//        System.out.println(Player.score(groupedSolution1, params));
//        System.out.println("------------------------");
//
//        Map<Integer, List<Integer>> groupedSolution2 = Player.organizeSolution(solutions.get(1), params);
//        Player.print(groupedSolution2);
//        Player.print(Player.toCodinGameSolution(groupedSolution2, params));
//        System.out.println(Player.score(groupedSolution2, params));
//        System.out.println("------------------------");
//
//        Map<Integer, List<Integer>> groupedSolution3 = Player.organizeSolution(solutions.get(4), params);
//        Player.print(groupedSolution3);
//        Player.print(Player.toCodinGameSolution(groupedSolution3, params));
//        System.out.println(Player.score(groupedSolution3, params));
//        System.out.println("------------------------");
//
//        Map<Integer, List<Integer>> groupedSolution4 = Player.organizeSolution(solutions.get(5), params);
//        Player.print(groupedSolution4);
//        Player.print(Player.toCodinGameSolution(groupedSolution4, params));
//        System.out.println(Player.score(groupedSolution4, params));
//
//        double[][] initialProbabilities = Player.getInitialProbabilities(params);
//
//        System.out.println("------------------------");
//        Player.print(initialProbabilities);
//
//        List<int[]> initialSolutions = Player.generateNextGenerationSolutions(initialProbabilities, params);
//        double[][] calculatedInitialProbabilities = Player.extractProbability(initialSolutions, params);
//
//        System.out.println("------------------------");
//        Player.print(calculatedInitialProbabilities);

        List<Player.Box> boxes = new ArrayList<>();
        boxes.add(new Player.Box(0, 1, 1));
        boxes.add(new Player.Box(1, 2, 2));
        boxes.add(new Player.Box(2, 5, 4));
        boxes.add(new Player.Box(3, 3, 8));
        boxes.add(new Player.Box(4, 2, 1));
        boxes.add(new Player.Box(5, 9, 2));
        boxes.add(new Player.Box(6, 9, 4));
        boxes.add(new Player.Box(7, 5, 8));
        boxes.add(new Player.Box(8, 5, 1));
        boxes.add(new Player.Box(9, 7, 2));
        boxes.add(new Player.Box(10, 10, 4));
        boxes.add(new Player.Box(11, 1, 8));
        boxes.add(new Player.Box(12, 4, 1));
        boxes.add(new Player.Box(13, 4, 2));
        boxes.add(new Player.Box(13, 8, 4));

        double volumeSum = boxes.stream().mapToDouble(b -> b.volume).sum();
        double weightSum = boxes.stream().mapToDouble(b -> b.weight).sum();

        System.out.println("volumeSum:" + volumeSum);
        System.out.println("weigthSum:" + weightSum);

        params.boxes = boxes;
        params.nbOfGroup = 3;
        params.maxVolume = 20.;
        params.invalidSolutionPenalty = 100.;
        params.debug = true;
        params.populationSize = 50;
        params.bestSolutionSelectionCount = 8;
        params.executionMaxIteration = 10;
        Player.play(params);
    }

    @Test
    public void testGenerateInitialSolutions() {
        params.populationSize = 10;
        params.bestSolutionSelectionCount = 3;
        params.nbOfGroup = 3;
        params.maxVolume = 1000;
        params.executionMaxTime = 1000000;
        params.executionMaxIteration = 10;
        params.debug = false;
        boxes.clear();
        boxes.add(new Player.Box(0, 10, 5));
        boxes.add(new Player.Box(1, 15, 10));
        boxes.add(new Player.Box(2, 20, 25));
        boxes.add(new Player.Box(3, 20, 10));
        boxes.add(new Player.Box(4, 30, 5));
        boxes.add(new Player.Box(5, 35, 75));
        boxes.add(new Player.Box(6, 35, 25));
        Collections.sort(boxes, (o1, o2) -> Double.valueOf(o1.weight - o2.weight).intValue());

        List<Player.Solution> solutions = Player.generateInitialSolutions(params);
        Player.printSolution(solutions);

        Player.play(params);
    }
}