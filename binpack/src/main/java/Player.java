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
    static GameStatistic gameStatistics = new GameStatistic();

    public static void main(String args[]) {
        List<Box> allBoxes = new ArrayList<>();

        Scanner in = new Scanner(System.in);
        int boxCount = in.nextInt();
        for (int i = 0; i < boxCount; i++) {
            float weight = in.nextFloat();
            float volume = in.nextFloat();
            allBoxes.add(new Box(i, weight, volume));
        }

        GameParameters gameParameters = new GameParameters();
        GameContext gameContext = new GameContext(allBoxes);

        ContextualSolution bestSolution = findSolution(gameContext, gameParameters);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println(bestSolution.solution.print());
    }

    static Player.ContextualSolution findSolution(GameContext gameContext, GameParameters gameParameters) {
        log(() ->"Finding solution with parameters : " + gameParameters);
        ContextualSolution solution = findSolution(gameContext, gameParameters, System.currentTimeMillis(), generateRandomSolutions(gameContext, gameParameters));

        log(() -> gameStatistics.toString());
        return solution;
    }

    private static Player.ContextualSolution findSolution(GameContext gameContext, GameParameters gameParameters,
          long startTime, List<Solution> initialSolutions) {

        Player.ContextualSolution bestSolution = null;
        List<Solution> solutions = initialSolutions;

        while (System.currentTimeMillis() - startTime < gameParameters.maxSearchDuration) {
            log(() -> ">>> Elapsed time: " + (System.currentTimeMillis() - startTime));
            List<ContextualSolution> solutionsSortedByScore = solutions.stream()
                    .map(solution -> new Player.ContextualSolution(solution, gameContext, gameParameters))
                    .sorted()
                    .collect(Collectors.toList());

            log(() -> "Average score : " + solutionsSortedByScore.stream().mapToDouble(solution -> solution.score).average().getAsDouble());

            Player.ContextualSolution newBestSolution = solutionsSortedByScore.get(0);
            if (bestSolution == null || newBestSolution.score < bestSolution.score) { // lower is better
                bestSolution = newBestSolution;
            }

            Player.ContextualSolution finalBestSolution = bestSolution;
            log(() -> "Best score : " + finalBestSolution.score + " (real: " + finalBestSolution.realScore + ")");
            log(() -> "InvalidSolutionRate : " + gameStatistics.getInvalidSolutionRate());
            gameStatistics.reset();

            solutions = crossover(solutionsSortedByScore, gameContext, gameParameters);
        }

        return bestSolution;
    }

    static class GameParameters {
        int crossOverSelectionCount = 100;
        int crossOverExchangeRate = 1;
        int populationSize = 500;
        long maxSearchDuration = 48000;
        double invalidSolutionPenalty = 300d;
        int mutationRate = 0;

        @Override
        public String toString() {
            return "GameParameters{" +
                    ", crossOverSelectionCount=" + crossOverSelectionCount +
                    ", crossOverExchangeRate=" + crossOverExchangeRate +
                    ", populationSize=" + populationSize +
                    ", maxSearchDuration=" + maxSearchDuration +
                    ", invalidSolutionPenalty=" + invalidSolutionPenalty +
                    '}';
        }
    }
    static class GameStatistic {
        int scoredSolutionCount;
        int invalidSolutionCount;
        LongSummaryStatistics crossoverTime = new LongSummaryStatistics();

        @Override
        public String toString() {
            return "GameStatistics{" +
                    "invalidSolutionRate=" + getInvalidSolutionRate() +
                    ",crossover mean duration=" + crossoverTime.getAverage() +
                    ",crossover occurrence=" + crossoverTime.getCount() +
                    '}';
        }

        private int getInvalidSolutionRate() {
            return scoredSolutionCount== 0 ? -1 : 100 * invalidSolutionCount / scoredSolutionCount;
        }

        public void reset() {
            invalidSolutionCount = 0;
            scoredSolutionCount = 0;
        }
    }
    static class GameContext {
        final List<Box> boxes;
        final List<Box> boxesSortedByWeight;
        final int nbOfTrucks;
        final int maxTruckVolume;

        GameContext(List<Box> boxes, int nbOfTrucks, int maxTruckVolume) {
            this.boxes = boxes;
            this.nbOfTrucks = nbOfTrucks;
            this.maxTruckVolume = maxTruckVolume;
            this.boxesSortedByWeight = boxes.stream().sorted(Comparator.comparingDouble(box -> box.weight.doubleValue())).collect(Collectors.toList());
        }

        GameContext(List<Box> boxes) {
            this(boxes, NB_OF_TRUCK, TRUCK_MAX_VOLUME);
        }

    }

    static class ContextualSolution implements Comparable<ContextualSolution> {
        final Solution solution;
        final GameContext gameContext;
        final GameParameters gameParameters;

        final Double score;
        Double realScore;
        final Map<Integer, Float> volumeByTruck = new HashMap<>();
        final Map<Integer, Float> weightByTruck = new HashMap<>();
        Boolean isValid;

        ContextualSolution(Solution solution, GameContext gameContext, GameParameters gameParameters) {
            this.solution = solution;
            this.gameContext = gameContext;
            this.gameParameters = gameParameters;

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
            return true;
        }

        double calculateScore() {
            DoubleSummaryStatistics doubleSummaryStatistics = weightByTruck.values().stream().mapToDouble(Float::doubleValue).summaryStatistics();
            realScore = doubleSummaryStatistics.getMax() - doubleSummaryStatistics.getMin();

            double[] weightsAsDouble = weightByTruck.values().stream().mapToDouble(Float::doubleValue).toArray();
            return new Statistics(weightsAsDouble).getVariance();
        }

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(solution.repartition, ((ContextualSolution)obj).solution.repartition);
        }

        @Override
        public int hashCode() {
            return solution.repartition[0];
        }
    }

    static List<Solution> crossover(List<ContextualSolution> sortedSolutions, GameContext gameContext, GameParameters gameParameters) {
        long startCrossoverTime = System.currentTimeMillis();
        List<Solution> solutions = new ArrayList<>();

        int selectionCount = gameParameters.crossOverSelectionCount;
        int crossOverRate = gameParameters.crossOverExchangeRate;
        int mutationRate = gameParameters.mutationRate;

        List<ContextualSolution> selectedValidSolutions = sortedSolutions.stream()
                .filter(ContextualSolution::isValid)
                .collect(Collectors.toList());

        log(() -> "Valid solution count : " + selectedValidSolutions.size() + " sur " + sortedSolutions.size());

        List<ContextualSolution> selectedSolutions = selectedValidSolutions.stream()
                .distinct()
                .collect(Collectors.toList());

        log(() -> "distinct solution count : " + selectedSolutions.size() + " sur " + selectedValidSolutions.size());

        List<ContextualSolution> limitedSelectedSolutions = selectedSolutions.stream()
                .limit(selectionCount)
                .collect(Collectors.toList());

        log(() -> "limited solution count : " + limitedSelectedSolutions.size());

        solutions.addAll(limitedSelectedSolutions.stream().map(contextualSolution -> contextualSolution.solution).collect(Collectors.toList()));

        while (solutions.size() < gameParameters.populationSize) {
            ContextualSolution parent1 = limitedSelectedSolutions.get(RANDOM.nextInt(limitedSelectedSolutions.size()));
            ContextualSolution parent2 = limitedSelectedSolutions.get(RANDOM.nextInt(limitedSelectedSolutions.size()));

            if (!parent1.equals(parent2)) {
                int nbOfBoxes = gameContext.boxes.size();

                int[] child1 = new int[nbOfBoxes];
                int[] child2 = new int[nbOfBoxes];

                for (int i = 0; i < nbOfBoxes; i++) {
                    int crossoverGene = RANDOM.nextInt(1000);

                    if (crossoverGene <= crossOverRate) {
                        child1[i] = parent1.solution.repartition[i];
                        child2[i] = parent2.solution.repartition[i];
                    } else {
                        child1[i] = parent2.solution.repartition[i];
                        child2[i] = parent1.solution.repartition[i];
                    }
                }

                int mutation = RANDOM.nextInt(100);
                if (mutation < mutationRate) {
                    int geneSwitch1 = RANDOM.nextInt(nbOfBoxes);
                    int geneSwitch2 = RANDOM.nextInt(nbOfBoxes);

                    int savedGene1ValueChild1 = child1[geneSwitch1];
                    child1[geneSwitch1] = child1[geneSwitch2];
                    child1[geneSwitch2] = savedGene1ValueChild1;


                    int savedGene1ValueChild2 = child2[geneSwitch1];
                    child2[geneSwitch1] = child2[geneSwitch2];
                    child2[geneSwitch2] = savedGene1ValueChild2;
                }

                solutions.add(new Solution(child1));
                solutions.add(new Solution(child2));
            }
        }

        gameStatistics.crossoverTime.accept(System.currentTimeMillis() - startCrossoverTime);

        log(() -> "Crossover iteration:" + gameStatistics.crossoverTime.getCount());
        return solutions;
    }

    static List<Solution> generateRandomSolutions(GameContext gameContext, GameParameters gameParameters) {
        List<Solution> generatedSolutions = new ArrayList<>();

        while(generatedSolutions.size() < gameParameters.populationSize) {
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
        sortedBoxes.sort((box1, box2) -> box2.volume.compareTo(box1.volume));

        for(int i=0; i < sortedBoxes.size(); i++) {
            Box box = sortedBoxes.get(i);

            Truck truck;
            do {
                truck = trucks.get(RANDOM.nextInt(gameContext.nbOfTrucks));
            } while (truck.volume + box.volume > gameContext.maxTruckVolume);

            truck.addVolume(box.volume);
            if (truck.volume > gameContext.maxTruckVolume) {
                System.out.println("failed");
            }
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

    static int selectRandomBoxIndexByWeight(GameContext gameContext) {
        List<Box> boxesSortedByWeight = gameContext.boxesSortedByWeight;

        List<Double> boxSelectionStrength = boxesSortedByWeight.stream().map(box -> Math.sqrt(box.weight)).collect(Collectors.toList());

        double boxStrengthSum = boxSelectionStrength.stream().mapToDouble(myDouble -> myDouble).sum();

        double randomSelector = RANDOM.nextDouble() * boxStrengthSum;

        double runningStrengthSum = 0d;
        for (int i = 0; i < boxesSortedByWeight.size(); i++) {
            runningStrengthSum += boxSelectionStrength.get(i);

            if (runningStrengthSum >= randomSelector) {
                return boxesSortedByWeight.get(i).id;
            }
        }

        throw new IllegalStateException();
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

        @Override
        public String toString() {
            return weight + " " + volume;
        }
    }

    static class Truck {
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

    public static class Statistics {
        double[] data;
        int size;

        public Statistics(double[] data) {
            this.data = data;
            size = data.length;
        }

        double getMean() {
            double sum = 0.0;
            for(double a : data)
                sum += a;
            return sum/size;
        }

        double getVariance() {
            double mean = getMean();
            double temp = 0;
            for(double a :data)
                temp += (a-mean)*(a-mean);
            return temp/(size-1);
        }

        double getStdDev() {
            return Math.sqrt(getVariance());
        }

        public double median() {
            Arrays.sort(data);

            if (data.length % 2 == 0) {
                return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
            }
            return data[data.length / 2];
        }
    }
}