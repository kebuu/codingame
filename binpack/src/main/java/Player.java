import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    static Random randomGenerator = new Random(1);

    public static void main(String args[]) {
        List<Box> allBoxes = new ArrayList<>();

        Scanner in = new Scanner(System.in);
        int boxCount = in.nextInt();
        for (int i = 0; i < boxCount; i++) {
            double weight = in.nextDouble();
            double volume = in.nextDouble();
            allBoxes.add(new Box(i, weight, volume));
        }

        Params params = new Params();
        params.boxes = allBoxes;
        params.nbOfGroup = 5;
        params.populationSize = 1000;
        params.bestSolutionSelectionCount = 30;
        params.executionMaxTime = 48000;
        params.startTime = System.currentTimeMillis();

        int[] codinGameSolution = play(params);
        System.out.println(IntStream.of(codinGameSolution).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
    }

    static int[] play(Params params) {
        List<Solution> currentSolutions = generateInitialSolutions(params);

        // Initialisation du meilleur resultat...
        Collections.sort(currentSolutions);
        Solution bestSolutionEver = currentSolutions.get(0);

        int iterationCount = 0;
        while (System.currentTimeMillis() - params.startTime < params.executionMaxTime  && iterationCount < params.executionMaxIteration) {
            System.err.println(System.currentTimeMillis() - params.startTime + ". Iteration : " + iterationCount++);

            // Selection des meilleures solutions
            int selectionSize = Math.min(currentSolutions.size(), params.bestSolutionSelectionCount);
            System.err.println("Effective selection size:" + selectionSize);
            List<Solution> bestScoredGroupedSolutions = currentSolutions.subList(0, selectionSize);
            debug(bestScoredGroupedSolutions, params);

            // Generation de la prochaine generation a partir des meilleurs solutions de la population courante
            currentSolutions = generateNextGenerationSolutions(bestScoredGroupedSolutions, params);
            Collections.sort(currentSolutions);
            Solution bestSolutionInNextPopulation = bestScoredGroupedSolutions.get(0);

            if (bestSolutionEver.score > bestSolutionInNextPopulation.score) {
                bestSolutionEver = bestSolutionInNextPopulation;
                System.err.println("Best solution score : " + bestSolutionEver);
            } else {
                System.err.println("Best solution in current population : " + bestSolutionInNextPopulation);
            }
        }

        // Ecriture de la solution au format attendu
        System.err.print("Best solution ever : ");
        print(bestSolutionEver);
        return toCodinGameSolution(bestSolutionEver, params);
    }

    static List<Solution> generateInitialSolutions(Params params) {
        List<Solution> solutions = new ArrayList<>();

        List<Box> sortedBoxes = params.boxes;//.stream().sorted((b1, b2) -> Double.compare(b2.volume, b1.volume)).collect(Collectors.toList());

        for (int i = 0; i < params.populationSize; i++) {
            Map<Integer, Double> volumeByGroup = new HashMap<>();
            Map<Integer, List<Integer>> contentByGroup = new HashMap<>();

            for (int j = 0; j < sortedBoxes.size(); j++) {
                Box sortedBox = sortedBoxes.get(j);

                boolean boxAffected = false;
                do {
                    int randomGroup = randomGenerator.nextInt(params.nbOfGroup);
                    Double groupVolume = volumeByGroup.computeIfAbsent(randomGroup, k -> 0.);
                    double maybeNewVolumeOfGroup = groupVolume + sortedBox.volume;

                    if (maybeNewVolumeOfGroup <= params.maxVolume) {
                        volumeByGroup.put(randomGroup, maybeNewVolumeOfGroup);
                        List<Integer> groupContent = contentByGroup.computeIfAbsent(randomGroup, k -> new ArrayList<>());
                        groupContent.add(j);
                        boxAffected = true;
                    }
                } while (!boxAffected);
            }

            int[] solution = new int[sortedBoxes.size()];

            for (List<Integer> groupContent : contentByGroup.values()) {
                for (Integer boxIndex : groupContent) {
                    solution[boxIndex] = groupContent.get(0);
                }
            }

            solutions.add(new Solution(solution, params));
        }

        return solutions;
    }

    private static int[] toCodinGameSolution(Solution solution, Params params) {
        int[] codinGameSolution = new int[params.nbOfBox()];

        for (Map.Entry<Integer, List<Integer>> organizedSolutionEntry : solution.boxesByGroup.entrySet()) {
            for (Integer boxIndex : organizedSolutionEntry.getValue()) {
                codinGameSolution[boxIndex] = organizedSolutionEntry.getKey();
            }
        }

        return codinGameSolution;
    }

    private static List<Solution> generateNextGenerationSolutions(List<Solution> solutions, Params params) {
        List<Solution> nextGenerationSolutions = new ArrayList<>();
        nextGenerationSolutions.addAll(solutions);

        while (nextGenerationSolutions.size() < params.populationSize) {
            int firstParentIndex = randomGenerator.nextInt(solutions.size());
            int secondParentIndex = randomGenerator.nextInt(solutions.size());

            List<Solution> children = crossover(solutions.get(firstParentIndex), solutions.get(secondParentIndex), params);

            for (Solution child : children) {
                nextGenerationSolutions.add(maybeMutate(child, params).toValidSolution());
            }
        }

        return nextGenerationSolutions;
    }

    private static List<Solution> crossover(Solution solution1, Solution solution2, Params params) {
        int[] crossoverSolution1 = new int[params.nbOfBox()];
        int[] crossoverSolution2 = new int[params.nbOfBox()];

        int splitBoxIndex = randomGenerator.nextInt(params.nbOfBox());
        for (int i = 0; i < params.nbOfBox(); i++) {
            if (i < splitBoxIndex) {
                crossoverSolution1[i] = solution1.rawSolution[i];
                crossoverSolution2[i] = solution2.rawSolution[i];
            } else {
                crossoverSolution1[i] = solution2.rawSolution[i];
                crossoverSolution2[i] = solution1.rawSolution[i];
            }
        }

        List<Solution> solutions = new ArrayList<>();
        solutions.add(new Solution(crossoverSolution1, params));
        solutions.add(new Solution(crossoverSolution2, params));

        return solutions;
    }

    private static Solution maybeMutate(Solution solution, Params params) {
        Solution maybeMutatedSolution = solution;

        double random = randomGenerator.nextDouble();
        if (random < params.mutationProbability) {
            int[] maybeMutatedRawSolution = solution.rawSolution.clone();

            int boxToMutate1 = randomGenerator.nextInt(params.nbOfBox());
            int boxToMutate2 = randomGenerator.nextInt(params.nbOfBox());

            int initialGroupOfBox1 = solution.rawSolution[boxToMutate1];
            int initialGroupOfBox2 = solution.rawSolution[boxToMutate2];

            maybeMutatedRawSolution[boxToMutate1] = initialGroupOfBox2;
            maybeMutatedRawSolution[boxToMutate2] = initialGroupOfBox1;

            maybeMutatedSolution = new Solution(maybeMutatedRawSolution, params);
        }

        return maybeMutatedSolution;
    }

    private static void debug(double[][] doubleMatrix, Params params) {
        if (params.debug) {
            print(doubleMatrix);
            System.err.println("---");
        }
    }

    private static void debug(Params params, List<int[]> solutions) {
        if (params.debug) {
            print(solutions);
            System.err.println("---");
        }
    }

    private static void debug(List<Solution> solutions, Params params) {
        if (params.debug) {
            for (Solution scoredGroupedSolution : solutions) {
                print(scoredGroupedSolution);
            }
            System.err.println("---");
        }
    }

    static void print(List<int[]> arrays) {
        for (int[] array : arrays) {
            System.err.println(asString(array));
        }
    }

    static void print(int[] array) {
        System.err.println(asString(array));
    }

    private static String asString(int[] array) {
        return IntStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining("|"));
    }

    private static void print(double[][] array) {
        for (double[] doubles : array) {
            print(doubles);
        }
    }

    private static void print(double[] array) {
        System.err.println(DoubleStream.of(array).mapToObj(number -> String.format("%.3f", number)).collect(Collectors.joining("|")));
    }

    public static void printCol(double[][] matrix, int colIndex) {
        double[] values = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            values[i] = matrix[i][colIndex];
        }
        System.err.println(DoubleStream.of(values).mapToObj(number -> String.format("%.3f", number)).collect(Collectors.joining("|")));
    }

    private static void print(Solution solution) {
        System.err.println(asString(solution.rawSolution) + " -> " + solution.score + " (isValid: " + solution.valid + ")");
    }

    protected static void printSolution(List<Solution> solutions) {
        for (Solution solution : solutions) {
            print(solution);
        }
    }

    public static void print(Map<Integer, List<Integer>> groupedSolution) {
        SortedMap<Integer, List<Integer>> sortedGroupedSolution = new TreeMap<>(groupedSolution);
        for (Map.Entry<Integer, List<Integer>> sortedGroupedSolutionEntry : sortedGroupedSolution.entrySet()) {
            System.err.println(sortedGroupedSolutionEntry.getKey() + " -> " + sortedGroupedSolutionEntry.getValue());
        }
    }

    static class Box {
        final int id;
        final double weight;
        final double volume;

        Box(int id, double weight, double volume) {
            this.id = id;
            this.weight = weight;
            this.volume = volume;
        }

        @Override
        public String toString() {
            return "Box{" +
                    "id=" + id +
                    ", weight=" + weight +
                    ", volume=" + volume +
                    '}';
        }
    }

    static class Params {
         List<Box> boxes;
         int nbOfGroup = 100;
         int populationSize = 1000;
         int bestSolutionSelectionCount = 50;
         long startTime = System.currentTimeMillis();
         int executionMaxTime = 48000;
         double invalidSolutionPenalty = 100.;
         double maxVolume = 100.;
         boolean debug;
         int executionMaxIteration = Integer.MAX_VALUE;
         double mutationProbability = .1;

         int nbOfBox() {
            return boxes.size();
        }
    }

    static class Solution implements Comparable<Solution> {
        final int[] rawSolution;
        final boolean valid;
        final double score;
        final Params params;

        Map<Integer, Double> volumeByGroup = new HashMap<>();
        Map<Integer, Double> weightByGroup = new HashMap<>();
        Map<Integer, List<Integer>> boxesByGroup = new HashMap<>();

        Solution(int[] rawSolution, Params params) {
            this.params = params;
            this.rawSolution = rawSolution;

            for (int solutionArrayIndex : rawSolution) {
                Box box = params.boxes.get(solutionArrayIndex);
                Integer group = rawSolution[solutionArrayIndex];

                volumeByGroup.compute(group, (key, currentValue) -> currentValue == null ? box.volume : currentValue + box.volume);
                weightByGroup.compute(group, (key, currentValue) -> currentValue == null ? box.weight : currentValue + box.weight);

                List<Integer> groupContent = boxesByGroup.getOrDefault(group, new ArrayList<>());
                groupContent.add(solutionArrayIndex);
            }

            // On force le nombre attendu de groupe
            while (boxesByGroup.size() < params.nbOfGroup) {
                boxesByGroup.put(boxesByGroup.size(), new ArrayList<>());
            }

            valid = volumeByGroup.values().stream().noneMatch(volume -> volume > params.maxVolume);

            DoubleSummaryStatistics weightSummary = weightByGroup.values().stream().mapToDouble(value -> value).summaryStatistics();
            score = weightSummary.getMax() - weightSummary.getMin();
        }

        Solution toValidSolution() {
            if (valid) {
                return this;
            } else {
                int[] newSolution = rawSolution.clone();
                Map<Integer, Double> newVolumeByGroup = new HashMap<>(volumeByGroup);
                Map<Integer, Double> newWeightByGroup = new HashMap<>(weightByGroup);
                Map<Integer, List<Integer>> newBoxesByGroup = new HashMap<>(boxesByGroup);

                Integer invalidGroup = nextInvalidGroup();
                while (invalidGroup != null) {
                    Integer smallerBoxIdInInvalidGroup = newBoxesByGroup.get(invalidGroup).stream()
                            .min(Comparator.comparingDouble(boxId -> params.boxes.get(boxId).volume))
                            .get();

                    Integer lessLoadedGroup = newVolumeByGroup.entrySet().stream()
                            .min(Comparator.comparingDouble(Map.Entry::getValue))
                            .map(Map.Entry::getKey)
                            .get();

                    Box box = params.boxes.get(smallerBoxIdInInvalidGroup);
                    newVolumeByGroup.put(invalidGroup, newVolumeByGroup.get(invalidGroup) - box.volume);
                    newVolumeByGroup.put(lessLoadedGroup, newVolumeByGroup.get(lessLoadedGroup) + box.volume);
                    newWeightByGroup.put(invalidGroup, newWeightByGroup.get(invalidGroup) - box.weight);
                    newWeightByGroup.put(lessLoadedGroup, newWeightByGroup.get(lessLoadedGroup) + box.weight);
                    newBoxesByGroup.get(invalidGroup).remove(smallerBoxIdInInvalidGroup);
                    newBoxesByGroup.get(lessLoadedGroup).add(smallerBoxIdInInvalidGroup);

                    newSolution[smallerBoxIdInInvalidGroup] = lessLoadedGroup;
                    invalidGroup = nextInvalidGroup();
                }

                return new Solution(newSolution, params);
            }
        }

        private Integer nextInvalidGroup() {
            return volumeByGroup.entrySet().stream()
                    .filter(volumeByGroupEntry -> volumeByGroupEntry.getValue() > params.maxVolume)
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        @Override
        public String toString() {
            return "Solution{" +
                    "valid=" + valid +
                    ", score=" + score +
                    '}';
        }

        @Override
        public int compareTo(Solution solution) {
            double diff = score - solution.score;

            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
