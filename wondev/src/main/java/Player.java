import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static final int CELL_HOLE_HEIGHT = -1;
    private static boolean isLogEnabled = true;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int size = in.nextInt();
        int unitsPerPlayer = in.nextInt();

        List<Cell> cells = new ArrayList<>();
        List<Spawn> mySpawns = new ArrayList<>();
        List<Spawn> enemySpawns = new ArrayList<>();
        List<Action> allLegalActions = new ArrayList<>();

        // game loop
        while (true) {
            for (int rowIndex = 0; rowIndex < size; rowIndex++) {
                String row = in.next();

                for(int colIndex = 0; colIndex < row.length();colIndex++) {
                    char cellHeightAsString = row.charAt(colIndex);
                    int cellHeight = cellHeightAsString == '.' ? CELL_HOLE_HEIGHT : Integer.parseInt(String.valueOf(cellHeightAsString));
                    cells.add(new Cell(cellHeight, new Coordinate(rowIndex, colIndex)));
                }
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int unitX = in.nextInt();
                int unitY = in.nextInt();
                mySpawns.add(new Spawn(i, new Coordinate(unitX, unitY)));
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int otherX = in.nextInt();
                int otherY = in.nextInt();
                enemySpawns.add(new Spawn(i, new Coordinate(otherX, otherY)));
            }
            int legalActions = in.nextInt();
            for (int i = 0; i < legalActions; i++) {
                String atype = in.next();
                int index = in.nextInt();
                String dir1 = in.next();
                String dir2 = in.next();

                allLegalActions.add(new Action.LegalAction(String.format("%s %d %s %s", atype, index, dir1, dir2)));
            }

            log(allLegalActions);

            GameState gameState = new GameState(mySpawns, enemySpawns, new BoardState(cells), allLegalActions);

            List<Strategy> strategies = new ArrayList<>();
            strategies.add(new Strategy.FirstLegalActionStrategy());

            for (Spawn mySpawn : gameState.mySpawns) {
                Optional<Action> nextAction = strategies.stream()
                        .map(strategy -> strategy.execute(mySpawn, gameState))
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElse(Optional.empty());

                nextAction.ifPresent(action -> System.out.println(action.execute()));
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
        }
    }

    static class GameState {
        final List<Spawn> mySpawns;
        final List<Spawn> enemySpawns;
        final BoardState boardState;
        final List<Action> legalActions;

        GameState(List<Spawn> mySpawns, List<Spawn> enemySpawns, BoardState boardState, List<Action> allLegalActions) {
            this.mySpawns = mySpawns;
            this.enemySpawns = enemySpawns;
            this.boardState = boardState;
            this.legalActions = allLegalActions;
        }
    }

    static class BoardState {
        final Map<Coordinate, Cell> cellByCoordinate;

        BoardState(List<Cell> cells) {
            cellByCoordinate = cells.stream().collect(Collectors.toMap(Cell::getCoordinate, Function.identity()));
        }
    }

    static abstract class Strategy {
        abstract Optional<Action> execute(Spawn spawn, GameState gameState);

        static class GoToThirdLevelStrategy extends Strategy {
            Optional<Action> execute(Spawn spawn, GameState gameState) {
                return Optional.empty();
            }
        }

        static class FirstLegalActionStrategy extends Strategy {
            Optional<Action> execute(Spawn spawn, GameState gameState) {
                return gameState.legalActions.stream().findFirst();
            }
        }
    }

    static abstract class Action {
        abstract String execute();

        static class MoveBuildAction extends Action {

            final int spawnIndex;
            final Direction moveDirection;
            final Direction buildDirection;

            MoveBuildAction(int spawnIndex, Direction moveDirection, Direction buildDirection) {
                this.spawnIndex = spawnIndex;
                this.moveDirection = moveDirection;
                this.buildDirection = buildDirection;
            }

            String execute() {
                return String.format("MOVE&BUILD %d %s %s", spawnIndex, moveDirection, buildDirection);
            }
        }

        static class LegalAction extends Action{

            final String legalAction;

            LegalAction(String legalAction) {
                this.legalAction = legalAction;
            }

            String execute() {
                return legalAction;
            }
        }
    }

    static class Coordinate {
        final int row;
        final int col;

        Coordinate(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "[" + row + " " + col + "]";
        }
    }

    static class Spawn extends WithCoordinate{
        final int id;

        Spawn(int id, Coordinate coordinate) {
            super(coordinate);
            this.id = id;
        }

        @Override
        public String toString() {
            return "Spawn(" + id + ":" + coordinate + ")";
        }
    }

    static class Cell extends WithCoordinate {
        final int height;

        Cell(int height, Coordinate coordinate) {
            super(coordinate);
            this.height = height;
        }

        boolean isHole() {
            return height == CELL_HOLE_HEIGHT;
        }

        @Override
        public String toString() {
            return "Cell(" + height + ":" + coordinate + ")";
        }
    }

    static abstract class WithCoordinate {
        final Coordinate coordinate;

        WithCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }
    }

    enum Direction {
        N,S,E,W,SE,SW,NE,NW
    }

    static void log(Object... objects) {
        if (isLogEnabled) {
            String log = Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(","));
            System.err.print(log);
        }
    }
}