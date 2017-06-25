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
            strategies.add(new Strategy.PushStrategy());
            strategies.add(new Strategy.GoToThirdLevelStrategy());
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

                if (nextAction.isPresent()) {
                    break;
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
        }
    }

    static abstract class Strategy {
        abstract Optional<? extends Action> execute(Spawn spawn, GameState gameState);

        static class GoToThirdLevelStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {

                return gameState.getAccessibleThirdLevelCells(spawn).stream()
                    .flatMap(thirdLevelCell -> {
                        return gameState.buildableCells(thirdLevelCell, spawn).stream()
                        .map(buildableCell -> new MoveBuildCandidate(spawn, thirdLevelCell, buildableCell));
                    })
                    .max(Comparator.comparing(gameState::score))
                    .map(MoveBuildCandidate::toMoveBuildAction);
            }
        }

        static class PushStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {

                return gameState.pushableEnemies(spawn).stream()
                    .flatMap(enemySpawn -> {
                        Direction targetSpawnDirection = getDirection(spawn, enemySpawn);

                        List<Direction> pushableDirections = new ArrayList<>();
                        pushableDirections.add(targetSpawnDirection);
                        pushableDirections.addAll(targetSpawnDirection.siblings());

                        return pushableDirections.stream()
                                .filter(direction -> enemySpawn.coordinateTo(direction).isInBoard(gameState.size))
                                .map(direction -> {
                            Coordinate targetPushCoordinate = enemySpawn.coordinateTo(direction);
                            log(targetPushCoordinate);
                            Cell targetPush = gameState.boardState.getCellAt(targetPushCoordinate);
                            return new PushBuildCandidate(spawn, enemySpawn, targetPush);
                        })
                        .filter(pushBuildCandidate -> pushBuildCandidate.pushTo.height == 0 || pushBuildCandidate.pushTo.height == 1)
                        .filter(pushBuildCandidate -> !gameState.getSpawnsCoordinate().contains(pushBuildCandidate.pushTo.coordinate));
                    })
                    .min(Comparator.comparingInt(pushBuildCandidate -> pushBuildCandidate.pushTo.height))
                    .map(PushBuildCandidate::toPushBuildAction);
            }
        }

        static class SimpleClimbStrategy extends Strategy {
            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                int spawnHeight = gameState.getHeight(spawn);

                return gameState.accessibleCells(spawn).stream()
                        .filter(cell -> cell.height <= spawnHeight + 1)
                        .sorted(Comparator.comparingInt(Cell::getHeight).reversed())
                        .flatMap(movingToCell -> {
                            return gameState.buildableCells(movingToCell, spawn).stream()
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
            log(moveBuildAction, movedSpawn, movedSpawn.coordinateTo(moveBuildAction.buildDirection));
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
                return new GameState(size, updatedSpawnList, enemySpawns, boardState.upgradingCellAt(cellToUpgrade.coordinate), new ArrayList<>());
            } else {
                return new GameState(size, mySpawns, updatedSpawnList, boardState.upgradingCellAt(cellToUpgrade.coordinate), new ArrayList<>());

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
            return accessibleCells(spawn).stream()
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

        List<Spawn> pushableEnemies(Spawn spawn) {
            return getNeighbourCells(spawn).stream()
                    .filter(cell -> cell.height <= getHeight(spawn) + 1)
                    .flatMap(cell -> {
                        return enemySpawns.stream()
                                .filter(enemySpawn -> enemySpawn.coordinate.equals(cell.coordinate))
                                .filter(enemySpawn -> getHeight(enemySpawn) >= 2);
                    })
                    .collect(Collectors.toList());
        }

        List<Cell> buildableCells(WithCoordinate withCoordinate, Spawn builder) {
            List<Coordinate> spawnsCoordinate = getSpawnsCoordinate();
            spawnsCoordinate.remove(builder.coordinate);

            return getNeighbourCells(withCoordinate).stream()
                    .filter(cell -> !spawnsCoordinate.contains(cell.coordinate))
                    .collect(Collectors.toList());
        }

        List<Coordinate> getSpawnsCoordinate() {
            return Stream.concat(mySpawns.stream(), enemySpawns.stream())
                        .map(spawn -> spawn.coordinate)
                        .collect(Collectors.toList());
        }

        Long score(MoveBuildCandidate candidate) {
            long moveToCellLevel3Bonus = candidate.moveTo.height == 3 ? 5 : 0;

            long botherEnemies = enemySpawns.stream()
                    .flatMap(spawn -> accessibleCells(spawn).stream())
                    .filter(cell -> cell.coordinate.equals(candidate.moveTo.coordinate) || cell.coordinate.equals(candidate.build.coordinate))
                    .count();

            GameState gameWithCandidateAction = withAction(candidate.spawn, candidate.toMoveBuildAction(), true);
            long helpedEnemies = enemySpawns.stream()
                    .flatMap(spawn -> gameWithCandidateAction.accessibleCells(spawn).stream())
                    .filter(cell -> cell.coordinate.equals(candidate.build.coordinate))
                    .count();

            long helpedFriends = mySpawns.stream()
                    .flatMap(spawn -> gameWithCandidateAction.accessibleCells(spawn).stream())
                    .filter(cell -> cell.coordinate.equals(candidate.build.coordinate))
                    .count();

            long lockEnemies = enemySpawns.stream()
                    .filter(spawn -> gameWithCandidateAction.accessibleCells(spawn).size() == 0)
                    .count();

            long lockFriends = mySpawns.stream()
                    .filter(spawn -> gameWithCandidateAction.accessibleCells(spawn).size() == 0)
                    .count();

            long enemySpawnAccessibleThirdLevelCells = enemySpawns.stream()
                    .flatMap(spawn -> gameWithCandidateAction.getAccessibleThirdLevelCells(spawn).stream())
                    .count();

            long mySpawnAccessibleThirdLevelCells = mySpawns.stream()
                    .flatMap(spawn -> gameWithCandidateAction.getAccessibleThirdLevelCells(spawn).stream())
                    .count();

            long score = botherEnemies - helpedEnemies + helpedFriends + (lockEnemies - lockFriends) * 10 +
                    moveToCellLevel3Bonus + candidate.moveTo.height +
                    (enemySpawnAccessibleThirdLevelCells + 1 - mySpawnAccessibleThirdLevelCells);
            log("candidate", candidate);
            log("score", score);
            return score;
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

        static class PushBuildAction extends Action{

            final int spawnIndex;
            final Direction targetSpawnDirection;
            final Direction pushDirection;

            PushBuildAction(int spawnIndex, Direction targetSpawnDirection, Direction pushDirection) {
                this.spawnIndex = spawnIndex;
                this.targetSpawnDirection = targetSpawnDirection;
                this.pushDirection = pushDirection;
            }

            PushBuildAction(Spawn spawn, Direction targetSpawnDirection, Direction pushDirection) {
                this(spawn.index, targetSpawnDirection, pushDirection);
            }

            String execute() {
                return String.format("PUSH&BUILD %d %s %s", spawnIndex, targetSpawnDirection, pushDirection);
            }

            @Override
            public String toString() {
                return "PushBuildAction{" +
                        "spawnIndex=" + spawnIndex +
                        ", targetSpawnDirection=" + targetSpawnDirection +
                        ", pushDirection=" + pushDirection +
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

        boolean isInBoard(int size) {
            return row >= 0 && col >= 0 && row < size && col < size;
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
                coordinateAt = coordinateAt.plusRow(-1);
            }
            if (directionAsString.contains(Direction.S.name())) {
                coordinateAt = coordinateAt.plusRow(1);
            }
            if (directionAsString.contains(Direction.E.name())) {
                coordinateAt = coordinateAt.plusCol(1);
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

            List<Direction> siblings() {
                return Arrays.asList(NE, NW);
            }
        },S {
            Direction opposite() {
                return N;
            }

            List<Direction> siblings() {
                return Arrays.asList(SE, SW);
            }
        },E {
            Direction opposite() {
                return W;
            }

            List<Direction> siblings() {
                return Arrays.asList(SE, NE);
            }
        },W {
            Direction opposite() {
                return E;
            }

            List<Direction> siblings() {
                return Arrays.asList(NW, SW);
            }
        },SE {
            Direction opposite() {
                return NW;
            }

            List<Direction> siblings() {
                return Arrays.asList(E, S);
            }
        },SW {
            Direction opposite() {
                return NE;
            }

            List<Direction> siblings() {
                return Arrays.asList(S, W);
            }
        },NE {
            Direction opposite() {
                return SW;
            }

            List<Direction> siblings() {
                return Arrays.asList(E, N);
            }
        },NW {
            Direction opposite() {
                return SE;
            }

            List<Direction> siblings() {
                return Arrays.asList(N, W);
            }
        };

        abstract Direction opposite();
        abstract List<Direction> siblings();
    }

    static void log(Object... objects) {
        if (isLogEnabled) {
            String log = Arrays.stream(objects).map(Object::toString).collect(Collectors.joining("\n"));
            System.err.print(log + "\n");
        }
    }

    static class MoveBuildCandidate {
        final Spawn spawn;
        final Cell moveTo;
        final Cell build;

        MoveBuildCandidate(Spawn spawn, Cell moveTo, Cell build) {
            this.spawn = spawn;
            this.moveTo = moveTo;
            this.build = build;
        }

        Action.MoveBuildAction toMoveBuildAction() {
            Direction moveToDirection = getDirection(spawn, moveTo);
            Direction buildToDirection = getDirection(moveTo, build);
            return new Action.MoveBuildAction(spawn, moveToDirection, buildToDirection);
        }
    }

    static class PushBuildCandidate {
        final Spawn spawn;
        final Spawn targetSpawn;
        final Cell pushTo;

        PushBuildCandidate(Spawn spawn, Spawn targetSpawn, Cell pushTo) {
            this.spawn = spawn;
            this.targetSpawn = targetSpawn;
            this.pushTo = pushTo;
        }

        Action.PushBuildAction toPushBuildAction() {
            Direction targetSpawnDirection = getDirection(spawn, targetSpawn);
            Direction pushToDirection = getDirection(targetSpawn, pushTo);
            return new Action.PushBuildAction(spawn, targetSpawnDirection, pushToDirection);
        }
    }
}