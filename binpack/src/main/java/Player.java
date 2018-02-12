import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    static int NB_OF_TRUCK = 100;
    static int TRUCK_MAX_VOLUME = 100;
    static Random RANDOM = new Random(1);

    public static void main(String args[]) {
        List<Box> allBoxes = new ArrayList<>();

        Scanner in = new Scanner(System.in);
        int boxCount = in.nextInt();
        for (int i = 0; i < boxCount; i++) {
            float weight = in.nextFloat();
            float volume = in.nextFloat();
            allBoxes.add(new Box(i, weight, volume));
        }

        GameParameter gameParameter = new GameParameter();
        GameContext gameContext = new GameContext(allBoxes);

        ContextualSolution bestSolution = findSolution(gameContext, gameParameter);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println(bestSolution.solution.print());
    }

    private static Player.ContextualSolution findSolution(GameContext gameContext, GameParameter gameParameter) {
        return findSolution(gameContext, gameParameter, System.currentTimeMillis(), null, generateRandomSolutions(gameContext, gameParameter));
    }

    private static Player.ContextualSolution findSolution(GameContext gameContext, GameParameter gameParameter,
          long startTime, Player.ContextualSolution currentBestSolution, List<Solution> currentSolutions) {

        List<ContextualSolution> solutionsSortedByScore = currentSolutions.stream()
                .map(solution -> new Player.ContextualSolution(solution, gameContext))
                .sorted()
                .collect(Collectors.toList());

        Player.ContextualSolution bestSolution = solutionsSortedByScore.get(0);
        if (currentBestSolution != null && bestSolution.score < currentBestSolution.score) {
            bestSolution = currentBestSolution;
        }

        if (System.currentTimeMillis() - startTime < gameParameter.maxSearchDuration) {
            return bestSolution;
        } else {
            List<Solution> solutions = crossover(solutionsSortedByScore, gameContext, gameParameter);
            return findSolution(gameContext, gameParameter, startTime, bestSolution, solutions);
        }
    }

    static class GameParameter {
        int crossOverSelectionRate = 10;
        int crossOverExchangeRate = 1;
        int populationSize = 500;
        long maxSearchDuration = 48000;
    }

    static class GameContext {
        final List<Box> boxes;
        final int nbOfTrucks;
        final int maxTruckVolume;

        GameContext(List<Box> boxes, int nbOfTrucks, int maxTruckVolume) {
            this.boxes = boxes;
            this.nbOfTrucks = nbOfTrucks;
            this.maxTruckVolume = maxTruckVolume;
        }

        GameContext(List<Box> boxes) {
            this(boxes, NB_OF_TRUCK, TRUCK_MAX_VOLUME);
        }
    }

    static class ContextualSolution implements Comparable<ContextualSolution> {
        final Solution solution;
        final GameContext gameContext;

        final Double score;
        final Map<Integer, Float> volumeByTruck = new HashMap<>();
        final Map<Integer, Float> weightByTruck = new HashMap<>();

        ContextualSolution(Solution solution, GameContext gameContext) {
            this.solution = solution;
            this.gameContext = gameContext;

            for(int i=0; i < gameContext.nbOfTrucks; i++) {
                weightByTruck.put(i, 0F);
                volumeByTruck.put(i, 0F);
            }

            int[] solutionRepartition = solution.repartition;
            for(int i = 0; i < solutionRepartition.length; i++) {
                Box box = gameContext.boxes.get(i);

                Float newTruckWeight = weightByTruck.get(solutionRepartition[i]) + box.weight;
                weightByTruck.put(solutionRepartition[i], newTruckWeight);

                Float newTruckVolume = volumeByTruck.get(solutionRepartition[i]) + box.volume;
                volumeByTruck.put(solutionRepartition[i], newTruckVolume);
            }

            this.score = calculateScore();
        }

        public int compareTo(ContextualSolution contextualSolution) {
            return score.compareTo(contextualSolution.score);
        }

        boolean isValid() {
            return volumeByTruck.values().stream()
                .noneMatch(volume -> volume > gameContext.maxTruckVolume);
        }

        double calculateScore() {
            DoubleSummaryStatistics weightSummaryStatistics = weightByTruck.values().stream()
                    .mapToDouble(Float::doubleValue)
                    .summaryStatistics();

            double score = weightSummaryStatistics.getMax() - weightSummaryStatistics.getMin();

            if(!isValid()) {
                score += 300d;
            }

            return score;
        }
    }


    private static List<Solution> crossover(List<ContextualSolution> sortedSolutions, GameContext gameContext, GameParameter gameParameter) {
        List<Solution> solutions = new ArrayList<>();

        int selectionPercent = gameParameter.crossOverSelectionRate;
        int crossOverRate = gameParameter.crossOverExchangeRate;

        List<ContextualSolution> selectedSolutions = sortedSolutions.subList(0, sortedSolutions.size() * selectionPercent / 100);

        while (solutions.size() < gameParameter.populationSize) {
            ContextualSolution parent1 = selectedSolutions.get(RANDOM.nextInt(selectedSolutions.size()));
            ContextualSolution parent2 = selectedSolutions.get(RANDOM.nextInt(selectedSolutions.size()));

            int nbOfBoxes = gameContext.boxes.size();
            int[] child1 = new int[nbOfBoxes];
            int[] child2 = new int[nbOfBoxes];

            for (int i = 0; i < nbOfBoxes; i++) {
                int crossover = RANDOM.nextInt(100);
                if (crossover < crossOverRate) {
                    child1[i] = parent2.solution.repartition[i];
                    child2[i] = parent1.solution.repartition[i];
                } else {
                    child1[i] = parent1.solution.repartition[i];
                    child2[i] = parent2.solution.repartition[i];
                }
            }
        }

        return solutions;
    }

    static List<Solution> generateRandomSolutions(GameContext gameContext, GameParameter gameParameter) {
        List<Solution> generatedSolutions = new ArrayList<>();

        while(generatedSolutions.size() < gameParameter.populationSize) {
            generatedSolutions.add(generateRandomSolution(gameContext));
        }

        return generatedSolutions;
    }

    static Solution generateRandomSolution(GameContext gameContext) {
        int[] repartition = new int[gameContext.boxes.size()];

        List<Truck> trucks = new ArrayList<>();
        for(int i = 0; i < gameContext.nbOfTrucks; i++) {
            trucks.add(new Truck(i));
        }

        List<Box> sortedBoxes = new ArrayList<>(gameContext.boxes);
        sortedBoxes.sort((o1, o2) -> o2.volume.compareTo(o1.volume));

        for(int i=0; i < sortedBoxes.size(); i++) {
            Box box = sortedBoxes.get(i);

            Truck truck;
            do {
                truck = trucks.get(RANDOM.nextInt(gameContext.nbOfTrucks));
            } while (truck.volume + box.volume > TRUCK_MAX_VOLUME);

            truck.addVolume(box.volume);
            repartition[box.id] = truck.id;
        }

        return new Solution(repartition);
    }

    static class Solution {
        final int[] repartition;

        Solution(int[] repartition) {
            this.repartition = repartition;
        }

        String print() {
            return IntStream.of(repartition).mapToObj(String::valueOf).collect(Collectors.joining(" "));
        }
    }

    static class Box {
        final int id;
        final Float weight;
        final Float volume;

        Box(int id, float weight, float volume) {
            this.id = id;
            this.weight = weight;
            this.volume = volume;
        }
    }

    static class Truck implements Comparable<Truck>{
        final Integer id;
        Float volume = 0F;

        Truck(int id) {
            this.id = id;
        }

        Truck addVolume(float volume) {
            this.volume += volume;
            return this;
        }

        @Override
        public int compareTo(Truck o) {
            int compareTo = volume.compareTo(o.volume);
            if (compareTo == 0) {
                return id.compareTo(o.id);
            } else {
                return compareTo;
            }
        }

        @Override
        public String toString() {
            return "Truck{" +
                    "id=" + id +
                    ", volume=" + volume +
                    '}';
        }
    }

    private static void log(Supplier<Object>... logs) {
        System.err.println(
                Arrays.stream(logs)
                        .map(Supplier::get)
                        .map(o -> {
                            if (o != null && o.getClass().isArray()) {
                                return Collections.singletonList(o).toString();
                            } else {
                                return Objects.toString(o);
                            }
                        })
                        .collect(Collectors.joining(", ")));
    }
}