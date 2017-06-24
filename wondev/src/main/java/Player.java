import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // game loop
        while (true) {
            List<List<Cell>> cells = new ArrayList<>();
            List<Spawn> mySpawns = new ArrayList<>();
            List<Spawn> enemySpawns = new ArrayList<>();
            List<Action> allLegalActions = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < size; rowIndex++) {
                String row = in.next();
                List<Cell> cellsInRow = new ArrayList<>();
                cells.add(cellsInRow);

                for(int colIndex = 0; colIndex < row.length();colIndex++) {
                    char cellHeightAsString = row.charAt(colIndex);
                    int cellHeight = cellHeightAsString == '.' ? CELL_HOLE_HEIGHT : Integer.parseInt(String.valueOf(cellHeightAsString));
                    cellsInRow.add(new Cell(cellHeight, new Coordinate(rowIndex, colIndex)));
                }
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int unitCol = in.nextInt();
                int unitRow = in.nextInt();
                mySpawns.add(new Spawn(i, new Coordinate(unitRow, unitCol)));
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int otherCol = in.nextInt();
                int otherRow = in.nextInt();
                enemySpawns.add(new Spawn(i, new Coordinate(otherRow, otherCol)));
            }
            int legalActions = in.nextInt();
            for (int i = 0; i < legalActions; i++) {
                String atype = in.next();
                int index = in.nextInt();
                String dir1 = in.next();
                String dir2 = in.next();

                allLegalActions.add(new Action.LegalAction(String.format("%s %d %s %s", atype, index, dir1, dir2)));
            }

            GameState gameState = new GameState(size, mySpawns, enemySpawns, new BoardState(cells), allLegalActions);
            log(gameState);

            List<Strategy> strategies = new ArrayList<>();
            strategies.add(new Strategy.GoToThirdLevelStrategy());
            strategies.add(new Strategy.PreventFailStrategy());
            strategies.add(new Strategy.SimpleClimbStrategy());
            strategies.add(new Strategy.FirstLegalActionStrategy());

            for (Spawn mySpawn : gameState.mySpawns) {
                Optional<? extends Action> nextAction = strategies.stream()
                        .map(strategy -> {
                            Optional<? extends Action> optionalAction = strategy.execute(mySpawn, gameState);
                            if (optionalAction.isPresent()) {
                                log(strategy);
                            }
                            return optionalAction;
                        })
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElse(Optional.empty());

                nextAction.ifPresent(action -> {
                    log(action.execute());
                    System.out.println(action.execute());
                });
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
        }
    }

    static abstract class Strategy {
        abstract Optional<? extends Action> execute(Spawn spawn, GameState gameState);

        static class GoToThirdLevelStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                Optional<Cell> accessibleThirdLevelCell = gameState.getAccessibleThirdLevelCells(spawn).stream().findFirst();
                return accessibleThirdLevelCell
                    .map(cell ->  {
                        Direction direction = getDirection(spawn, cell);
                        return new Action.MoveBuildAction(spawn.index, direction, direction.opposite());
                    });
            }
        }

        static class SimpleClimbStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                int spawnHeight = gameState.getHeight(spawn);

                List<Cell> cells = gameState.getNeighbourCells(spawn).stream()
                        .filter(cell -> cell.height <= spawnHeight + 1)
                        .sorted(Comparator.comparingInt(Cell::getHeight).reversed())
                        .collect(Collectors.toList());

                return cells.stream()
                    .flatMap(movingToCell -> {
                        return gameState.getNeighbourCells(movingToCell).stream()
                            .filter(cell -> cell.height <= movingToCell.height)
                            .sorted(Comparator.comparingInt(Cell::getHeight).reversed())
                            .limit(1)
                            .map(buildCell -> {
                                log(movingToCell, buildCell);
                                Direction moveDirection = getDirection(spawn, movingToCell);
                                Direction buildDirection = getDirection(movingToCell, buildCell);
                                return new Action.MoveBuildAction(spawn, moveDirection, buildDirection);
                            });
                    })
                    .findFirst();
            }
        }

        static class PreventFailStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                return gameState.enemySpawns.stream()
                    .flatMap(enemySpawn -> gameState.getAccessibleThirdLevelCells(enemySpawn).stream())
                    .limit(1)
                    .flatMap(cellToBuild -> {
                        return gameState.accessibleCells(spawn).stream()
                            .filter(accessibleCell -> gameState.buildableCells(accessibleCell).contains(cellToBuild))
                            .limit(1)
                            .map(accessibleCell -> {
                                Direction moveDirection = getDirection(spawn, accessibleCell);
                                Direction buildDirection = getDirection(accessibleCell, cellToBuild);
                                return new Action.MoveBuildAction(spawn.index, moveDirection, buildDirection);
                            });
                    })
                    .findFirst();
            }
        }

        static class FirstLegalActionStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                return gameState.legalActions.stream().findFirst();
            }
        }
    }

    static class GameState {
        final int size;
        final List<Spawn> mySpawns;
        final List<Spawn> enemySpawns;
        final BoardState boardState;
        final List<Action> legalActions;

        GameState(int size, List<Spawn> mySpawns, List<Spawn> enemySpawns, BoardState boardState, List<Action> allLegalActions) {
            this.size = size;
            this.mySpawns = mySpawns;
            this.enemySpawns = enemySpawns;
            this.boardState = boardState;
            this.legalActions = allLegalActions;
        }

        List<Cell> getNeighbourCells(WithCoordinate withCoordinate) {
            List<Coordinate> neighbourCoordinates = new ArrayList<>();
            Coordinate source = withCoordinate.coordinate;

            neighbourCoordinates.add(source.plusRow(1));
            neighbourCoordinates.add(source.plusRow(-1));
            neighbourCoordinates.add(source.plusRow(1).plusCol(1));
            neighbourCoordinates.add(source.plusRow(1).plusCol(-1));
            neighbourCoordinates.add(source.plusRow(-1).plusCol(1));
            neighbourCoordinates.add(source.plusRow(-1).plusCol(-1));
            neighbourCoordinates.add(source.plusCol(1));
            neighbourCoordinates.add(source.plusCol(-1));

            return neighbourCoordinates.stream().filter(coordinate -> coordinate.row >= 0 && coordinate.col >= 0 &&
                    coordinate.row < size && coordinate.col < size)
            .map(boardState::getCellAt)
            .filter(cell -> cell.height < 4)
            .collect(Collectors.toList());
        }

        int getHeight(WithCoordinate withCoordinate) {
            return boardState.getCellAt(withCoordinate.coordinate).height;
        }

        GameState withAction(Spawn spawn, Action.MoveBuildAction moveBuildAction, boolean myAction) {
            Spawn movedSpawn = new Spawn(spawn.index, spawn.coordinateTo(moveBuildAction.moveDirection));
            Cell cellToUpgrade = boardState.getCellAt(movedSpawn.coordinateTo(moveBuildAction.buildDirection));

            List<Spawn> updatedSpawnList = new ArrayList<>();

            List<Spawn> spawnsFromListToUpdate = myAction ? mySpawns : enemySpawns;
            for (Spawn spawnFromListToUpdate : spawnsFromListToUpdate) {
                if (spawnFromListToUpdate.index == movedSpawn.index) {
                    updatedSpawnList.add(movedSpawn);
                } else {
                    updatedSpawnList.add(spawnFromListToUpdate);
                }
            }

            if (myAction) {
                return new GameState(size, updatedSpawnList, enemySpawns, boardState.upgradingCellAt(cellToUpgrade.coordinate), legalActions);
            } else {
                return new GameState(size, mySpawns, updatedSpawnList, boardState.upgradingCellAt(cellToUpgrade.coordinate), legalActions);

            }
        }

        @Override
        public String toString() {
            return "GameState{" +
                    "mySpawns=" + mySpawns +
                    "\n enemySpawns=" + enemySpawns +
                    "\n boardState=\n" + boardState +
                    "}\n";
        }

        List<Cell> getAccessibleThirdLevelCells(Spawn spawn) {
            return getNeighbourCells(spawn).stream()
                .filter(cell -> cell.height == 3 && getHeight(spawn) == 2)
                .collect(Collectors.toList());
        }

        List<Cell> accessibleCells(Spawn spawn) {
            List<Coordinate> spawnsCoordinate = getSpawnsCoordinate();

            return getNeighbourCells(spawn).stream()
                    .filter(cell -> cell.height <= getHeight(spawn) + 1)
                    .filter(cell -> !spawnsCoordinate.contains(cell.coordinate))
                    .collect(Collectors.toList());
        }

        List<Cell> buildableCells(WithCoordinate withCoordinate) {
            List<Coordinate> spawnsCoordinate = getSpawnsCoordinate();

            return getNeighbourCells(withCoordinate).stream()
                    .filter(cell -> !spawnsCoordinate.contains(cell.coordinate))
                    .collect(Collectors.toList());
        }

        List<Coordinate> getSpawnsCoordinate() {
            return Stream.concat(mySpawns.stream(), enemySpawns.stream())
                        .map(spawn1 -> spawn1.coordinate)
                        .collect(Collectors.toList());
        }
    }

    static class BoardState {
        final List<List<Cell>> cells;

        BoardState(List<List<Cell>> cells) {
            this.cells = cells;
        }

        Cell getCellAt(Coordinate coordinate) {
            return cells.get(coordinate.row).get(coordinate.col);
        }

        public String toString() {
            return cells.stream()
                .map(this::toStringRow)
                .collect(Collectors.joining("\n"));
        }

        String toStringRow(List<Cell> rowCells) {
            return rowCells.stream()
                    .map(this::toStringCell)
                    .collect(Collectors.joining());
        }

        String toStringCell(Cell cell) {
            return cell.isHole() ? "." : String.valueOf(cell.height);
        }

        BoardState upgradingCellAt(Coordinate coordinate) {
            List<List<Cell>> newBoardCells = new ArrayList<>();

            for (List<Cell> rowCells : cells) {
                ArrayList<Cell> newRowCells = new ArrayList<>();
                newBoardCells.add(newRowCells);

                for (Cell cell : rowCells) {
                    if (cell.coordinate.equals(coordinate)) {
                        newRowCells.add(new Cell(cell.height + 1, coordinate));
                    } else {
                        newRowCells.add(cell);
                    }
                }
            }

            return new BoardState(newBoardCells);
        }
    }

    static abstract class Action {
        abstract String execute();

        static class MoveBuildAction extends Action {

            final int spawnIndex;
            final Direction moveDirection;
            final Direction buildDirection;

            MoveBuildAction(Spawn spawn, Direction moveDirection, Direction buildDirection) {
                this(spawn.index, moveDirection, buildDirection);
            }

            MoveBuildAction(int spawnIndex, Direction moveDirection, Direction buildDirection) {
                this.spawnIndex = spawnIndex;
                this.moveDirection = moveDirection;
                this.buildDirection = buildDirection;
            }

            String execute() {
                return String.format("MOVE&BUILD %d %s %s", spawnIndex, moveDirection, buildDirection);
            }

            @Override
            public String toString() {
                return "MoveBuildAction{" +
                        "spawnIndex=" + spawnIndex +
                        ", moveDirection=" + moveDirection +
                        ", buildDirection=" + buildDirection +
                        "} " + super.toString();
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

            @Override
            public String toString() {
                return "LegalAction{" +
                        "legalAction='" + legalAction + '\'' +
                        "} " + super.toString();
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

        Coordinate plusRow(int plusRow) {
            return new Coordinate(row + plusRow, col);
        }

        Coordinate plusCol(int plusCol) {
            return new Coordinate(row, col + plusCol);
        }

        public String toString() {
            return "[" + row + " " + col + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coordinate that = (Coordinate) o;

            if (row != that.row) return false;
            return col == that.col;
        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + col;
            return result;
        }
    }

    static class Spawn extends WithCoordinate{
        final int index;

        Spawn(int index, Coordinate coordinate) {
            super(coordinate);
            this.index = index;
        }

        @Override
        public String toString() {
            return "Spawn(" + index + ":" + coordinate + ")";
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

        public Integer getHeight() {
            return height;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithCoordinate that = (WithCoordinate) o;

            return coordinate.equals(that.coordinate);
        }

        @Override
        public int hashCode() {
            return coordinate.hashCode();
        }

        Coordinate coordinateTo(Direction direction) {
            String directionAsString = direction.name();
            Coordinate coordinateAt = coordinate;

            if (directionAsString.contains(Direction.N.name())) {
                coordinateAt = coordinateAt.plusCol(1);
            }
            if (directionAsString.contains(Direction.S.name())) {
                coordinateAt = coordinateAt.plusCol(-1);
            }
            if (directionAsString.contains(Direction.E.name())) {
                coordinateAt = coordinateAt.plusRow(1);
            }
            if (directionAsString.contains(Direction.W.name())) {
                coordinateAt = coordinateAt.plusCol(-1);
            }
            return coordinateAt;
        }
    }

    static Direction getDirection(WithCoordinate from, WithCoordinate to) {
        return getDirection(from.coordinate, to.coordinate);
    }

    static Direction getDirection(Coordinate fromCoordinate, Coordinate toCoordinate) {
        String northSouthDirectionString = "";
        String eastWestDirectionString = "";

        if (fromCoordinate.row != toCoordinate.row) {
            northSouthDirectionString = fromCoordinate.row < toCoordinate.row ? Direction.S.name() : Direction.N.name();
        }
        if (fromCoordinate.col != toCoordinate.col) {
            eastWestDirectionString = fromCoordinate.col < toCoordinate.col ? Direction.E.name() : Direction.W.name();
        }

        return Direction.valueOf(northSouthDirectionString + eastWestDirectionString);
    }

    enum Direction {
        N {
            Direction opposite() {
                return S;
            }
        },S {
            Direction opposite() {
                return N;
            }
        },E {
            Direction opposite() {
                return W;
            }
        },W {
            Direction opposite() {
                return E;
            }
        },SE {
            Direction opposite() {
                return NW;
            }
        },SW {
            Direction opposite() {
                return NE;
            }
        },NE {
            Direction opposite() {
                return SW;
            }
        },NW {
            Direction opposite() {
                return SE;
            }
        };

        abstract Direction opposite();
    }

    static void log(Object... objects) {
        if (isLogEnabled) {
            String log = Arrays.stream(objects).map(Object::toString).collect(Collectors.joining("\n"));
            System.err.print(log + "\n");
        }
    }
}