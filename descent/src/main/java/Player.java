import java.util.*;

/**
 * The while loop represents the game.
 * Each iteration represents a turn of the game
 * where you are given inputs (the heights of the mountains)
 * and where you have to print an output (the index of the mountain to fire on)
 * The inputs you are given are automatically updated according to your last actions.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            List<MountainInfo> moutainInfos = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                int mountainH = in.nextInt(); // represents the height of one mountain.
                moutainInfos.add(new MountainInfo(i, mountainH));
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            int maxHeigth = moutainInfos.stream().max(Comparator.comparingInt(MountainInfo::getHeigth)).get().index;
            System.out.println(maxHeigth); // The index of the mountain to fire on.
        }
    }

    static class MountainInfo {
        final int index;
        final int heigth;

        MountainInfo(int index, int heigth) {
            this.index = index;
            this.heigth = heigth;
        }

        public int getIndex() {
            return index;
        }

        public int getHeigth() {
            return heigth;
        }
    }
}