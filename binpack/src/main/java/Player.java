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
    
    static int NB_OF_INITIAL_SOLUTION = 1;
    static Random RANDOM = new Random(1);

    public static void main(String args[]) {
        System.err.println("Debug messages...");
        Boxes allBoxes = new Boxes();

        Scanner in = new Scanner(System.in);
        int boxCount = in.nextInt();
        for (int i = 0; i < boxCount; i++) {
            float weight = in.nextFloat();
            float volume = in.nextFloat();
            allBoxes.add(new Box(i, weight, volume));
        }

//        List<Solution> solutions = generateRandomSolutions(NB_OF_INITIAL_SOLUTION, allBoxes, TRUCK_MAX_VOLUME);
//        Solution solution = solutions.stream().min(Comparator.comparingDouble(s -> s.score(allBoxes))).get();

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        Solution solution = generateRandomSolution(allBoxes, NB_OF_TRUCK);

        Map<Integer, Float> volumeRepartition = solution.getVolumeRepartition(NB_OF_TRUCK, allBoxes);
        System.err.println(volumeRepartition.toString());
        System.out.println(solution.print());
    }
    
    static List<Solution> generateRandomSolutions(int nbOfSolutionToGenerate, Boxes allBoxes, int truckMaxVolume) {
        List<Solution> generatedSolutions = new ArrayList<>();
        
        while(generatedSolutions.size() < nbOfSolutionToGenerate) {
            Solution solution = generateRandomSolution(allBoxes, NB_OF_TRUCK);

            if(solution.isValid(truckMaxVolume, allBoxes)) {
                generatedSolutions.add(solution);
            }
        }
        
        return generatedSolutions;
    }
    
    static Solution generateRandomSolution(Boxes allBoxes, int nbOfTruck) {
        int[] repartition = new int[allBoxes.size()];

        List<Truck> trucks = new ArrayList<>();
        for(int i = 0; i < nbOfTruck; i++) {
            trucks.add(new Truck(i));
        }

        List<Box> sortedBoxes = new ArrayList<>(allBoxes.boxes);
        Collections.sort(sortedBoxes, (o1, o2) -> o1.volume.compareTo(o1.volume));

        for(int i=0; i < sortedBoxes.size(); i++) {
            Box box = sortedBoxes.get(i);
            Collections.sort(trucks);
            Truck truck = trucks.get(0);
            repartition[box.id] = truck.id;
            truck.addVolume(box.volume);
        }
        
        return new Solution(repartition);
    }

    public static class Solution {
        public final int[] repartition;
        
        public Solution(int[] repartition) {
            this.repartition = repartition;
        }

        public String print() {
            return IntStream.of(repartition).mapToObj(String::valueOf).collect(Collectors.joining(" "));
        }
        
        public boolean isValid(int maxTruckVolume, Boxes allBoxes) {
            Map<Integer, Float> volumeByTruck = new HashMap<>();
            
            for(int i=0; i < NB_OF_TRUCK; i++) {
                volumeByTruck.put(i, 0F);
            }

            for(int i = 0; i < repartition.length; i++) {
                Float newTruckVolume = volumeByTruck.get(repartition[i]) + allBoxes.getBox(i).volume;
                if (newTruckVolume > maxTruckVolume) {
                    return false;
                } else {
                    volumeByTruck.put(repartition[i], newTruckVolume);
                }
            }
            
            return true;
        }
        
        public Map<Integer, Float> getWeightRepartition(int nbOfTruck, Boxes allBoxes) {
            Map<Integer, Float> weightByTruck = new HashMap<>();
            
            for(int i=0; i < nbOfTruck; i++) {
                weightByTruck.put(i, 0F);
            }
            
            for(int i = 0; i < repartition.length; i++) {
                Float newTruckWeight = weightByTruck.get(repartition[i]) + allBoxes.getBox(i).weight;
                weightByTruck.put(repartition[i], newTruckWeight);
            }
            
            return weightByTruck;
        }

        public Map<Integer, Float> getVolumeRepartition(int nbOfTruck, Boxes allBoxes) {
            Map<Integer, Float> volumeByTruck = new HashMap<>();

            for(int i=0; i < nbOfTruck; i++) {
                volumeByTruck.put(i, 0F);
            }

            for(int i = 0; i < repartition.length; i++) {
                Float newTruckVolume = volumeByTruck.get(repartition[i]) + allBoxes.getBox(i).volume;
                volumeByTruck.put(repartition[i], newTruckVolume);
            }

            return volumeByTruck;
        }
        
        public Float score(Boxes allBoxes) {
            Map<Integer, Float> weightByTruck = getWeightRepartition(3, allBoxes);
            
            Float minWeight = null;
            Float maxWeight = null;
            
            for(Map.Entry<Integer, Float> weightByTruckEntry : weightByTruck.entrySet()) {
                Float weight = weightByTruckEntry.getValue();
                
                if(minWeight ==  null) {
                    minWeight = weightByTruckEntry.getValue();
                    maxWeight = weightByTruckEntry.getValue();
                } else if (weight < minWeight) {
                    minWeight = weight;
                } else if (weight > maxWeight) {
                    maxWeight = weight;
                }
            }

            return maxWeight - minWeight;
        }       
    }
    
    public static class Boxes {
        public final List<Box> boxes = new ArrayList<>();
        
        public void add(Box box) {
            boxes.add(box);
        }
        
        public int size() {
            return boxes.size();
        }
        
        public Box getBox(int i) {
            return boxes.get(i);
        }
    }
    
    public static class Box {
        public final int id;
        public final Float weight;
        public final Float volume;
        
        public Box(int id, float weight, float volume) {
            this.id = id;
            this.weight = weight;
            this.volume = volume;
        }
    }

    public static class Truck implements Comparable<Truck>{
        public final Integer id;
        public Float volume = 0F;

        public Truck(int id) {
            this.id = id;
        }

        public Truck addVolume(float volume) {
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
            Arrays.asList(logs).stream()
                .map(Supplier::get)
                .map(o -> {
                    if (o != null && o.getClass().isArray()) {
                        return Arrays.asList(o).toString();
                    } else {
                        return Objects.toString(o);
                    }
                })
                .collect(Collectors.joining(", ")));
    }
}