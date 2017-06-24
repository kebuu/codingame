import java.util.*;
import java.util.stream.Collectors;

/**
 * Don't let the machines win. You are humanity's last hope...
 **/
class Player {

    public static final Player.Cell NO_NODE_CELL = new Player.Cell(-1, -1, false);

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt(); // the number of cells on the X axis
        int height = in.nextInt(); // the number of cells on the Y axis
        if (in.hasNextLine()) {
            in.nextLine();
        }

        List<Cell> cells = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            String line = in.nextLine(); // width characters, each either 0 or .
            System.err.println(line);

            for(int j = 0; j < width; j++) {
                char character = line.charAt(j);
                Cell cell = new Cell(j, i, character == '0');
                cells.add(cell);
            }
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");


        // Three coordinates: a node, its right neighbor, its bottom neighbor
        List<Cell> nodes = cells.stream().filter(cell -> cell.isNode).collect(Collectors.toList());

        for (Cell node : nodes) {
            String outputLine = String.format("%s %s %s",
                    node,
                    getRightNeighbour(nodes, node).orElse(NO_NODE_CELL),
                    getBelowNeighbour(nodes, node).orElse(NO_NODE_CELL));
            System.err.println(outputLine);
            System.out.println(outputLine);
        }
    }

    public static Optional<Cell> getRightNeighbour(List<Cell> cells, Cell sourceCell) {
        return cells.stream()
                .filter(cell -> cell.y == sourceCell.y && cell.x > sourceCell.x && cell.isNode)
                .min(Comparator.comparing(cell -> Integer.valueOf(cell.x)));
    }

    public static Optional<Cell> getBelowNeighbour(List<Cell> cells, Cell sourceCell) {
        return cells.stream()
                .filter(cell -> cell.x == sourceCell.x && cell.y > sourceCell.y && cell.isNode)
                .min(Comparator.comparing(cell -> Integer.valueOf(cell.y)));
    }

    private static class Cell {
        private final int x;
        private final int y;
        private final boolean isNode;

        private Cell(int x, int y, boolean isNode) {
            this.x = x;
            this.y = y;
            this.isNode = isNode;
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }
}