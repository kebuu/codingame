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
    static Statistic stats = new Statistic();

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
        // Initialisation des premieres solutions
        double[][] currentProbabilities = getInitialProbabilities(params);
        //List<int[]> currentSolutions = generateSolutions(currentProbabilities, params);
        List<int[]> currentSolutions = generateInitialSolutions(params);

        // Initialisation du meilleur resultat...
        ScoredGroupedSolution bestSolutionEver = null;

        int iterationCount = 0;
        while (System.currentTimeMillis() - params.startTime < params.executionMaxTime  && iterationCount < params.executionMaxIteration) {
            System.err.println(System.currentTimeMillis() - params.startTime + ". Iteration : " + iterationCount++);
            debug(currentProbabilities, params);

            // Calcul du score de chaque solution et reorganisation des solutions en groupe
            List<ScoredGroupedSolution> scoredGroupedSolutions = new ArrayList<>();
            for (int[] solution : currentSolutions) {
                Map<Integer, List<Integer>> organizedSolution = organizedSolution(solution, params);
                double score = score(organizedSolution, params);
                scoredGroupedSolutions.add(new ScoredGroupedSolution(score, organizedSolution, solution));
            }
            debug(scoredGroupedSolutions, params);
//            scoredGroupedSolutions = scoredGroupedSolutions.stream()
//                    .filter(scoredGroupedSolution -> scoredGroupedSolution.isValid)
//                    .distinct()
//                    .collect(Collectors.toList());

            // Selection des meilleurs solutions
            Collections.sort(scoredGroupedSolutions);
            int selectionSize = Math.min(scoredGroupedSolutions.size(), params.bestSolutionSelectionCount);
            System.err.println("selectionSize:" + selectionSize);
            List<ScoredGroupedSolution> bestScoredGroupedSolution = scoredGroupedSolutions.subList(0, selectionSize);

            List<int[]> bestSolutions = new ArrayList<>();
            for (ScoredGroupedSolution scoredGroupedSolution : bestScoredGroupedSolution) {
                bestSolutions.add(scoredGroupedSolution.solution);
            }
            debug(bestScoredGroupedSolution, params);

            // Generation de la prochaine generation a partir des meilleurs solutions de la population courante
            currentProbabilities = extractProbability(bestSolutions, params);
            currentSolutions = generateSolutions(currentProbabilities, params);
            //currentSolutions.addAll(bestSolutions);

            // Recuperation de la meilleur solution connue
            ScoredGroupedSolution bestSolutionInCurrentPopulation = bestScoredGroupedSolution.get(0);
            if (bestSolutionEver == null || bestSolutionEver.score > bestSolutionInCurrentPopulation.score) {
                bestSolutionEver = bestSolutionInCurrentPopulation;
                System.err.println("Best solution score : " + bestSolutionEver);
            } else {
                System.err.println("Best solution in current population : " + bestSolutionInCurrentPopulation);
            }
        }

        // Ecriture de la solution au format attendu
        System.err.print("Best solution ever : ");
        print(bestSolutionEver);
        return toCodinGameSolution(bestSolutionEver.organizedSolution, params);
    }

    static List<int[]> generateInitialSolutions(Params params) {
        List<int[]> solutions = new ArrayList<>();

        List<Box> sortedBoxes = params.boxes;//.stream().sorted((b1, b2) -> Double.compare(b2.volume, b1.volume)).collect(Collectors.toList());

        for (int i = 0; i < params.populationSize; i++) {
            Map<Integer, Double> volumeByGroup = new HashMap<>();
            Map<Integer, SortedSet<Integer>> contentByGroup = new HashMap<>();

            for (int j = 0; j < sortedBoxes.size(); j++) {
                Box sortedBox = sortedBoxes.get(j);

                boolean boxAffected = false;
                do {
                    int randomGroup = randomGenerator.nextInt(params.nbOfGroup);
                    Double groupVolume = volumeByGroup.computeIfAbsent(randomGroup, k -> 0.);
                    double maybeNewVolumeOfGroup = groupVolume + sortedBox.volume;

                    if (maybeNewVolumeOfGroup <= params.maxVolume) {
                        volumeByGroup.put(randomGroup, maybeNewVolumeOfGroup);
                        SortedSet<Integer> groupContent = contentByGroup.computeIfAbsent(randomGroup, k -> new TreeSet<>());
                        groupContent.add(j);
                        boxAffected = true;
                    }
                } while (!boxAffected);
            }

            int[] solution = new int[sortedBoxes.size()];

            for (SortedSet<Integer> groupContent : contentByGroup.values()) {
                for (Integer boxIndex : groupContent) {
                    solution[boxIndex] = groupContent.first();
                }
            }

            solutions.add(solution);
        }

        return solutions;
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

    private static void debug(List<ScoredGroupedSolution> scoredGroupedSolutions, Params params) {
        if (params.debug) {
            for (ScoredGroupedSolution scoredGroupedSolution : scoredGroupedSolutions) {
                print(scoredGroupedSolution);
            }
            System.err.println("---");
        }
    }

    public static double[][] getInitialProbabilities(Params params) {
        int nbOfBox = params.nbOfBox();
        double[][] initialProbabilities = new double[nbOfBox][nbOfBox];

        for (int j = 0; j < nbOfBox; j++) {
            double uniformProbability = 1. / (j + 1.);
            for (int i = 0; i <= j; i++) {
                initialProbabilities[i][j] = uniformProbability;
            }
        }
        return initialProbabilities;
    }

    public static Map<Integer, List<Integer>> organizedSolution(int[] solution, Params params) {
        Map<Integer, List<Integer>> organizedSolution = new HashMap<>();
        Map<Integer, List<Integer>> groupByBoxIndex = new HashMap<>();

        for (int boxIndex = 0; boxIndex < solution.length; boxIndex++) {
            Integer linkedBoxIndex = solution[boxIndex];

            List<Integer> group = groupByBoxIndex.get(linkedBoxIndex);
            if (group == null) {
                group = new ArrayList<>();
                organizedSolution.put(organizedSolution.size(), group);
            }
            group.add(boxIndex);
            groupByBoxIndex.put(boxIndex, group);
        }

        // On force le nombre attendu de groupe
        while (organizedSolution.size() < params.nbOfGroup) {
            organizedSolution.put(organizedSolution.size(), new ArrayList<>());
        }

        return organizedSolution;
    }

    public static double score(Map<Integer, List<Integer>> groupedSolution, Params params) {
        DoubleSummaryStatistics weightsSummary = groupedSolution.values().stream()
                .mapToDouble(groupedBoxes -> groupedBoxes.stream().mapToDouble(groupedBox -> params.boxes.get(groupedBox).weight).sum())
                .summaryStatistics();

        double maxVolume = groupedSolution.values().stream()
                .mapToDouble(groupedBoxes -> groupedBoxes.stream().mapToDouble(groupedBox -> params.boxes.get(groupedBox).volume).sum())
                .max().getAsDouble();

        double scorePenalty = maxVolume > params.maxVolume ? params.invalidSolutionPenalty : 0.;
        double isValidMultiplier = maxVolume > params.maxVolume ? -1 : 1;

        return ((weightsSummary.getMax() - weightsSummary.getMin()) + scorePenalty) * isValidMultiplier;
    }

    public static int[] toCodinGameSolution(Map<Integer, List<Integer>> organizedSolution, Params params) {
        int[] codinGameSolution = new int[params.nbOfBox()];

        for (Map.Entry<Integer, List<Integer>> organizedSolutionEntry : organizedSolution.entrySet()) {
            for (Integer boxIndex : organizedSolutionEntry.getValue()) {
                codinGameSolution[boxIndex] = organizedSolutionEntry.getKey();
            }
        }

        return codinGameSolution;
    }

    public static double[][] extractProbability(List<int[]> solutions, Params params) {
        int nbOfBox = params.nbOfBox();

        double[][] cardinalityMatrix = new double[nbOfBox][nbOfBox];

        for (int[] solution : solutions) {
            for (int i = 0; i < solution.length; i++) {
                cardinalityMatrix[solution[i]][i] += 1.;
            }
        }

        double[][] probabilities = new double[nbOfBox][nbOfBox];
        for (int i = 0; i < nbOfBox; i++) {
            for (int j = i; j < nbOfBox; j++) {
                probabilities[i][j] = cardinalityMatrix[i][j] / (double) solutions.size();
            }
        }

        return probabilities;
    }

    public static int[] generateSolution(Params params, double[][] probabilityMatrix) {
        int nbOfBox = params.nbOfBox();
        int remainingEmptyGroups = params.nbOfGroup;

        Map<Integer, Double> volumeByGroup = new HashMap<>();
        Map<Integer, Integer> boxToGroupMapping = new HashMap<>();

        int[] solution = new int[nbOfBox];

        for (int boxId = 0; boxId < nbOfBox; boxId++) {
            solution[boxId] = -1;

            int effectiveMaxPredecessorId = boxId;
            double totalProbability = 1.0;

            if (remainingEmptyGroups == 0) {
                effectiveMaxPredecessorId -= 1;
                totalProbability -= probabilityMatrix[boxId][boxId];
            }

            double random = randomGenerator.nextDouble() * totalProbability;

            double probabilitySum = 0.;
            for (int predecessorId = 0; predecessorId <= effectiveMaxPredecessorId; predecessorId++) {
                double probability = probabilityMatrix[predecessorId][boxId];

                probabilitySum += probability;
                if (probabilitySum >= random || boxId == predecessorId) {
                    if (boxId == predecessorId) {
                        if (remainingEmptyGroups > 0) {
                            remainingEmptyGroups--;
                        }
                    }

                    Integer targetGroup = boxToGroupMapping.computeIfAbsent(predecessorId, k -> k);
                    affectTargetGroupToBoxId(targetGroup, boxId, solution, volumeByGroup, boxToGroupMapping, params);
                    break;
                }
            }

            if (solution[boxId] == -1) {
                if (remainingEmptyGroups == 0) {
                    Integer leastVolumedGroup = getLeastVolumedGroup(volumeByGroup);

                    affectTargetGroupToBoxId(leastVolumedGroup, boxId, solution, volumeByGroup, boxToGroupMapping, params);
                } else {
                    throw new RuntimeException();
                }
            }
        }

        return solution;
    }

    private static Integer getLeastVolumedGroup(Map<Integer, Double> volumeByGroup) {
        return volumeByGroup.entrySet()
            .stream().sorted(Comparator.comparing(Map.Entry::getValue))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElseThrow(RuntimeException::new);
    }

    private static void affectTargetGroupToBoxId(Integer targetGroup, int boxId, int[] solution, Map<Integer, Double> volumeByGroup, Map<Integer, Integer> boxToGroupMapping, Params params) {
        Integer effectiveTargetGroup = targetGroup;

        Double volume = volumeByGroup.computeIfAbsent(effectiveTargetGroup, k -> 0.);
        double newVolume = volume + params.boxes.get(boxId).volume;

        if (newVolume > params.maxVolume) {
            if (volumeByGroup.size() < params.nbOfGroup) {
                effectiveTargetGroup = boxId;
            } else {
                effectiveTargetGroup = getLeastVolumedGroup(volumeByGroup);
            }

            volume = volumeByGroup.computeIfAbsent(effectiveTargetGroup, k -> 0.);
            newVolume = volume + params.boxes.get(boxId).volume;
        }

        volumeByGroup.put(effectiveTargetGroup, newVolume);
        boxToGroupMapping.put(boxId, effectiveTargetGroup);
        solution[boxId] = effectiveTargetGroup;
    }

    public static List<int[]> generateSolutions(double[][] probabilityMatrix, Params params) {
        List<int[]> solutions = new ArrayList<>();

        for (int i = 0; i < params.populationSize; i++) {
            solutions.add(generateSolution(params, probabilityMatrix));
        }

        return solutions;
    }

    public static void print(List<int[]> arrays) {
        for (int[] array : arrays) {
            System.err.println(asString(array));
        }
    }

    public static void print(int[] array) {
        System.err.println(asString(array));
    }

    private static String asString(int[] array) {
        return IntStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining("|"));
    }

    public static void print(double[][] array) {
        for (double[] doubles : array) {
            print(doubles);
        }
    }

    public static void print(double[] array) {
        System.err.println(DoubleStream.of(array).mapToObj(number -> String.format("%.3f", number)).collect(Collectors.joining("|")));
    }

    public static void printCol(double[][] matrix, int colIndex) {
        double[] values = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            values[i] = matrix[i][colIndex];
        }
        System.err.println(DoubleStream.of(values).mapToObj(number -> String.format("%.3f", number)).collect(Collectors.joining("|")));
    }

    private static void print(ScoredGroupedSolution scoredGroupedSolution) {
        System.err.println(asString(scoredGroupedSolution.solution) + " -> " + scoredGroupedSolution.score + " (isValid: " + scoredGroupedSolution.isValid + ")");
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

    static class Statistic {

    }

    static class Params {
        public List<Box> boxes;
        public int nbOfGroup = 100;
        public int populationSize = 1000;
        public int bestSolutionSelectionCount = 50;
        public long startTime = System.currentTimeMillis();
        public int executionMaxTime = 48000;
        public double invalidSolutionPenalty = 100.;
        public double maxVolume = 100.;
        public boolean debug;
        public int executionMaxIteration = Integer.MAX_VALUE;
        public int maxNbOfAffectationTry = 10;

        public int nbOfBox() {
            return boxes.size();
        }
    }

    static class ScoredGroupedSolution implements Comparable<ScoredGroupedSolution> {
        final double score;
        final boolean isValid;
        final int[] solution;
        final Map<Integer, List<Integer>> organizedSolution;

        ScoredGroupedSolution(double score, Map<Integer, List<Integer>> organizedSolution, int[] solution) {
            this.score = Math.abs(score);
            this.organizedSolution = organizedSolution;
            this.solution = solution;
            this.isValid = score >= 0;
        }

        @Override
        public int compareTo(ScoredGroupedSolution scoredGroupedSolution) {
            double diff = score - scoredGroupedSolution.score;

            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "ScoredGroupedSolution{" +
                    "score=" + score +
                    ",isValid=" + isValid +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScoredGroupedSolution that = (ScoredGroupedSolution) o;

            return Double.compare(that.score, score) == 0;
        }

        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(score);
            return (int) (temp ^ (temp >>> 32));
        }
    }
}