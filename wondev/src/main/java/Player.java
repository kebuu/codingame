import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    private static final int CELL_HOLE_HEIGHT = -1;
    private static final Coordinate INVISIBLE_COORDINATE = new Coordinate(-1, -1);
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
            //strategies.add(new Strategy.GoToThirdLevelStrategy());
            strategies.add(new Strategy.SimpleClimbStrategy(true));
            strategies.add(new Strategy.SimpleClimbStrategy(false));


            Strategy.PushStrategy enemyPushStrategy = new Strategy.PushStrategy(true);
            boolean played = false;

            List<Spawn> sortedSpawn = gameState.mySpawns.stream()
                    .sorted(Comparator.comparingInt(spawn -> {

                        Optional<? extends Action> enemyPushAction = gameState.enemySpawns.stream()
                                .filter(enemySpawn -> enemySpawn.coordinate.row != -1)
                                .map(enemySpawn -> enemyPushStrategy.execute(enemySpawn, gameState))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .findFirst();

                        if(enemyPushAction.isPresent()) {
                            return -1;
                        } else {
                            int size1 = gameState.accessibleCells(spawn).size();
                            return size1 >= 3 ? 0 : -3 + size1;
                        }

                    }))
                    .collect(Collectors.toList());

            Strategy.PushStrategy pushStrategy = new Strategy.PushStrategy();
            for (Spawn mySpawn : sortedSpawn) {
                Optional<? extends Action> optionalAction = pushStrategy.execute(mySpawn, gameState);

                if (optionalAction.isPresent()) {
                    Action action = optionalAction.get();
                    log(pushStrategy, action.execute(), mySpawn);
                    played = true;
                    System.out.println(action.execute());
                    break;
                }
            }

            for (Spawn mySpawn : sortedSpawn) {
                for (Strategy strategy : strategies) {
                    if(!played) {
                        Optional<? extends Action> optionalAction = strategy.execute(mySpawn, gameState);

                        if (optionalAction.isPresent()) {
                            Action action = optionalAction.get();
                            log(strategy, action.execute(), mySpawn);
                            played = true;
                            System.out.println(action.execute());
                            break;
                        }
                    }
                }
            }

            if (!played) {
                Strategy.FirstLegalActionStrategy firstLegalActionStrategy = new Strategy.FirstLegalActionStrategy();
                for (Spawn mySpawn : sortedSpawn) {
                    Optional<? extends Action> optionalAction = firstLegalActionStrategy.execute(mySpawn, gameState);

                    if (optionalAction.isPresent()) {
                        Action action = optionalAction.get();
                        log(firstLegalActionStrategy, action.execute(), mySpawn);
                        System.out.println(action.execute());
                        break;
                    }
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

                List<Action.MoveBuildAction> moveBuildActionList = gameState.getAccessibleThirdLevelCells(spawn).stream()
                        .flatMap(thirdLevelCell -> {
                            return gameState.buildableCells(thirdLevelCell, spawn).stream()
                                    .map(buildableCell -> new MoveBuildCandidate(spawn, thirdLevelCell, buildableCell));
                        })
                        .map(MoveBuildCandidate::toMoveBuildAction)
                        .filter(moveBuildAction -> {
                            GameState gameStateWithAction = gameState.withAction(spawn, moveBuildAction, true);
                            Spawn updatedSpawn = gameStateWithAction.getMySpawn(spawn.index);
                            List<Cell> cells = gameStateWithAction.accessibleCells(updatedSpawn);
                            return cells.size() > 0;
                        }).collect(Collectors.toList());

                return moveBuildActionList.stream()
                    .sorted(Comparator.comparingInt(moveBuildAction -> {
                        GameState gameStateWithAction = gameState.withAction(spawn, moveBuildAction, true);
                        Spawn updatedSpawn = gameStateWithAction.getMySpawn(spawn.index);
                        Coordinate builtCellCoordinate = updatedSpawn.coordinateTo(moveBuildAction.buildDirection);
                        Cell builtCell = gameStateWithAction.boardState.getCellAt(builtCellCoordinate);

                        int isOnBorderValue = gameState.isOnBorder(updatedSpawn.coordinate) ? -1 : 1;

                        if (builtCell.height == 3 && gameState.enemySpawns.stream()
                                .flatMap(enemy -> gameState.accessibleCells(enemy).stream())
                                .noneMatch(cell -> cell.equals(builtCell))) {
                            return -100 + isOnBorderValue + gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1;
                        } else if (builtCell.height == 4 && gameState.enemySpawns.stream()
                                .flatMap(enemy -> gameState.accessibleCells(enemy).stream())
                                .noneMatch(cell -> cell.equals(builtCell))) {
                            int overallAccessibleCells = gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1;
                            return 100 + overallAccessibleCells;
                        } else if (gameState.getLockedEnemies() < gameStateWithAction.getLockedEnemies()) {
                            return -1000;
                        } else if (gameState.enemySpawns.stream()
                                .flatMap(enemy -> gameStateWithAction.getNeighbourCells(enemy).stream())
                                .anyMatch(cell -> cell.coordinate.equals(updatedSpawn.coordinate))) {
                            return 10;
                        } else {
                            return isOnBorderValue + gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1;
                        }
                    }))
                    .findFirst();
            }
        }

        static class PushStrategy extends Strategy {
            private final boolean isEnemyStrategy;

            PushStrategy(boolean isEnemyStrategy) {
                this.isEnemyStrategy = isEnemyStrategy;
            }

            PushStrategy() {
                this(false);
            }

            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {

                List<Spawn> spawns = gameState.pushableEnemies(spawn);

                log(spawns);

                return spawns.stream()
                    .flatMap(enemySpawn -> {
                        Direction targetSpawnDirection = getDirection(spawn, enemySpawn);

                        List<Direction> pushableDirections = new ArrayList<>();
                        pushableDirections.add(targetSpawnDirection);
                        pushableDirections.addAll(targetSpawnDirection.siblings());

                        return pushableDirections.stream()
                                .filter(direction -> {
                                    Coordinate coordinate = enemySpawn.coordinateTo(direction);
                                    return coordinate.isInBoard(gameState.size) && gameState.boardState.getCellAt(coordinate).isValidPosition();
                                })
                                .map(direction -> {
                            Coordinate targetPushCoordinate = enemySpawn.coordinateTo(direction);

                            Cell targetPush = gameState.boardState.getCellAt(targetPushCoordinate);
                            return new PushBuildCandidate(spawn, enemySpawn, targetPush);
                        })
                        .filter(pushBuildCandidate -> pushBuildCandidate.pushTo.height == 0 || pushBuildCandidate.pushTo.height == 1)
                        .filter(pushBuildCandidate -> !gameState.getSpawnsCoordinate().contains(pushBuildCandidate.pushTo.coordinate));
                    })
                    .filter(pushBuildCandidate -> {
                        GameState gameStateWithAction = gameState.withAction(spawn, pushBuildCandidate);
                        Spawn updatedSpawn = gameStateWithAction.getMySpawn(spawn.index);
                        int accessibleCells = gameStateWithAction.accessibleCells(updatedSpawn).size();
                        log("pushBuildCandidate", pushBuildCandidate, isEnemyStrategy ,accessibleCells);
                        return isEnemyStrategy || accessibleCells > 0;
                    })
                    .filter(pushBuildCandidate -> {
                        if (isEnemyStrategy) {
                            Action.PushBuildAction pushBuildAction = pushBuildCandidate.toPushBuildAction();
                            return gameState.withAction(spawn, pushBuildCandidate).accessibleCells(pushBuildCandidate.targetSpawn.movingTo(pushBuildAction.pushDirection)).isEmpty();
                        } else {
                            return true;
                        }
                    })
                    .min((pushBuildCandidate1, pushBuildCandidate2) -> {
                        Action.PushBuildAction pushBuildAction1 = pushBuildCandidate1.toPushBuildAction();
                        Action.PushBuildAction pushBuildAction2 = pushBuildCandidate2.toPushBuildAction();

                        if (gameState.withAction(spawn, pushBuildCandidate1).accessibleCells(pushBuildCandidate1.targetSpawn.movingTo(pushBuildAction1.pushDirection)).isEmpty()) {
                            return -1;
                        }
                        if (gameState.withAction(spawn, pushBuildCandidate2).accessibleCells(pushBuildCandidate2.targetSpawn.movingTo(pushBuildAction2.pushDirection)).isEmpty()) {
                            return 1;
                        }

                        return Integer.valueOf(pushBuildCandidate1.pushTo.height).compareTo(Integer.valueOf(pushBuildCandidate1.pushTo.height));
                    })
                    .map(PushBuildCandidate::toPushBuildAction);
            }
        }

        static class SimpleClimbStrategy extends Strategy {

            private final boolean alwaysUp;

            SimpleClimbStrategy(boolean alwaysUp) {
                this.alwaysUp = alwaysUp;
            }

            Optional<? extends Action> execute(Spawn spawn, GameState gameState) {
                int spawnHeight = gameState.getHeight(spawn);

                List<Cell> cells1 = gameState.accessibleCells(spawn);
                List<Cell> sorted1 = cells1.stream()
                        .filter(cell -> alwaysUp ^ cell.height < spawnHeight)
                        .sorted(Comparator.comparingInt(Cell::getHeight).reversed()).collect(Collectors.toList());

                return sorted1.stream()
                        .flatMap(movingToCell -> {
                            List<Cell> sorted = gameState.buildableCells(movingToCell, spawn).stream()
                                    /*.filter(cell -> cell.height <= spawnHeight)
                                    .sorted((buildableCell1, buildableCell2) -> {
                                        if (buildableCell1.height == 3) {
                                            boolean accessibleByEnemy = gameState.enemySpawns.stream().anyMatch(enemySpawn -> gameState.accessibleCells(enemySpawn).contains(buildableCell1));
                                            return accessibleByEnemy ? -1 : 1;
                                        } else if (buildableCell2.height == 3) {
                                            boolean accessibleByEnemy = gameState.enemySpawns.stream().anyMatch(enemySpawn -> gameState.accessibleCells(enemySpawn).contains(buildableCell2));
                                            return accessibleByEnemy ? 1 : -1;
                                        } else if (buildableCell1.height == spawnHeight) {
                                            return -1;
                                        } else if (buildableCell2.height == spawnHeight) {
                                            return 1;
                                        } else {
                                            return Integer.valueOf(buildableCell2.height).compareTo(Integer.valueOf(buildableCell1.height));
                                        }
                                    })*/.collect(Collectors.toList());

                            return sorted.stream()
                                    .map(buildCell -> {
                                        Direction moveDirection = getDirection(spawn, movingToCell);
                                        Direction buildDirection = getDirection(movingToCell, buildCell);
                                        return new Action.MoveBuildAction(spawn, moveDirection, buildDirection);
                                    }).sorted(Comparator.comparingInt(moveBuildAction -> {
                                        GameState gameStateWithAction = gameState.withAction(spawn, moveBuildAction, true);
                                        Spawn updatedSpawn = gameStateWithAction.getMySpawn(spawn.index);
                                        Cell updatedCell = gameStateWithAction.boardState.getCellAt(updatedSpawn.coordinateTo(moveBuildAction.buildDirection));

                                        int isOnBorderValue = gameState.isOnBorder(updatedSpawn.coordinate) ? -1 : 1;

                                        Strategy.PushStrategy enemyPushStrategy = new Strategy.PushStrategy(true);

                                        Optional<? extends Action> enemyPushAction = gameStateWithAction.enemySpawns.stream()
                                                .filter(enemySpawn -> enemySpawn.coordinate.row != -1)
                                                .map(enemySpawn -> enemyPushStrategy.execute(enemySpawn, gameStateWithAction))
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
//                                                .filter(action -> {
//                                                    Action.PushBuildAction pushBuildAction =(Action.PushBuildAction)action;
//                                                    Spawn spawn2 = gameStateWithAction.enemySpawns.stream().filter(spawn1 -> spawn1.index == pushBuildAction.spawnIndex).findFirst().get();
//                                                    return spawn.coordinate.equals(spawn2.coordinateTo(pushBuildAction.targetSpawnDirection));
//                                                })
                                                .findFirst();

                                        if(enemyPushAction.isPresent()) {
                                            return 1000;
                                        } else if (updatedCell.height == 3 && gameState.enemySpawns.stream()
                                                .flatMap(enemy -> gameState.accessibleCells(enemy).stream())
                                                .noneMatch(cell -> cell.equals(updatedCell))) {
                                            return -100 + isOnBorderValue + gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1;
                                        } else if (updatedCell.height == 4 && gameState.enemySpawns.stream()
                                                .flatMap(enemy -> gameState.accessibleCells(enemy).stream())
                                                .noneMatch(cell -> cell.equals(updatedCell))) {
                                            int overallAccessibleCells = gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1;
                                            return 100 + overallAccessibleCells;
                                        } else if (gameState.getLockedEnemies() < gameStateWithAction.getLockedEnemies()) {
                                            return -1000;
                                        } else if (gameState.enemySpawns.stream()
                                                .flatMap(enemy -> gameState.accessibleCells(enemy).stream())
                                                .anyMatch(cell -> cell.coordinate.equals(updatedSpawn.coordinate))) {
                                            return 10;
                                        } else {
                                            return gameStateWithAction.overallAccessibleCells(updatedSpawn).getValue().size() * -1  + isOnBorderValue;
                                        }
                                    }))
                                    .filter(moveBuildAction -> {
                                        GameState gameStateWithAction = gameState.withAction(spawn, moveBuildAction, true);
                                        Spawn updatedSpawn = gameStateWithAction.getMySpawn(spawn.index);
                                        int accessibleCells = gameStateWithAction.accessibleCells(updatedSpawn).size();
                                        return accessibleCells > 0;
                                    });
                        })

                        .findFirst();
            }

            @Override
            public String toString() {
                return "SimpleClimbStrategy{" +
                        "alwaysUp=" + alwaysUp +
                        "} " + super.toString();
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
            if (withCoordinate.coordinate.equals(INVISIBLE_COORDINATE)) {
                return new ArrayList<>();
            }

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
            .filter(cell -> cell.height < 4 && cell.height >= 0)
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
                .filter(cell -> cell.height == 3 && getHeight(spawn) >= 2)
                .collect(Collectors.toList());
        }

        AbstractMap.SimpleEntry<Integer, Set<Coordinate>> overallAccessibleCells(Spawn spawn, Set<Coordinate> allAccessibleCoordinates, int turn) {
            List<Coordinate> accessibleCoordinates = accessibleCells(spawn).stream().map(cell -> cell.coordinate).collect(Collectors.toList());

            Set<Coordinate> newAllAccessibleCoordinates = new HashSet<>();
            newAllAccessibleCoordinates.addAll(allAccessibleCoordinates);
            newAllAccessibleCoordinates.addAll(accessibleCoordinates);

            if (accessibleCoordinates.isEmpty() || turn >= 3) {
                return new AbstractMap.SimpleEntry(turn, allAccessibleCoordinates);
            } else {
                return accessibleCoordinates.stream()
                        .map(coordinate -> {
                            return overallAccessibleCells(new Spawn(spawn.index, coordinate), newAllAccessibleCoordinates, turn + 1);
                        })
                        .max(Comparator.comparingInt(AbstractMap.SimpleEntry::getKey))
                        .get();
            }
        }

        AbstractMap.SimpleEntry<Integer, Set<Coordinate>> overallAccessibleCells(Spawn spawn) {
            AbstractMap.SimpleEntry<Integer, Set<Coordinate>> integerListSimpleEntry = overallAccessibleCells(spawn, new HashSet<>(), 1);

            return integerListSimpleEntry;
        }

        List<Cell> accessibleCells(Spawn spawn) {
            List<Coordinate> spawnsCoordinate = getSpawnsCoordinate();

            return getNeighbourCells(spawn).stream()
                    .filter(cell -> cell.height <= getHeight(spawn) + 1)
                    .filter(cell -> !spawnsCoordinate.contains(cell.coordinate))
                    .collect(Collectors.toList());
        }

        List<Spawn> pushableEnemies(Spawn spawn) {

            boolean isSpawnMySpawn = mySpawns.stream().anyMatch(mySpawn -> mySpawn.coordinate.equals(spawn.coordinate));
            List<Spawn> enemies = isSpawnMySpawn ? enemySpawns : mySpawns;

            return getNeighbourCells(spawn).stream()
                    .flatMap(cell -> {
                        return enemies.stream()
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

        long getLockedEnemies() {
            return enemySpawns.stream()
                    .filter(spawn -> accessibleCells(spawn).size() == 0)
                    .count();
        }

        long getLockedSpawns() {
            return mySpawns.stream()
                    .filter(spawn -> accessibleCells(spawn).size() == 0)
                    .count();
        }


        GameState withAction(Spawn spawn, PushBuildCandidate pushBuildCandidate) {

            boolean isSpawnMySpawn = mySpawns.stream().anyMatch(mySpawn -> mySpawn.coordinate.equals(spawn.coordinate));

            List<Spawn> spawnListToUpdate = isSpawnMySpawn ? mySpawns : enemySpawns;

            Spawn movedSpawn = new Spawn(pushBuildCandidate.targetSpawn.index, pushBuildCandidate.pushTo.coordinate);
            Cell cellToUpgrade = boardState.getCellAt(pushBuildCandidate.targetSpawn.coordinate);

            List<Spawn> updatedSpawnList = new ArrayList<>();
            for (Spawn enemySpawn : spawnListToUpdate) {
                if (enemySpawn.index == movedSpawn.index) {
                    updatedSpawnList.add(movedSpawn);
                } else {
                    updatedSpawnList.add(enemySpawn);
                }
            }

            if (isSpawnMySpawn) {
                return new GameState(size, updatedSpawnList, enemySpawns, boardState.upgradingCellAt(cellToUpgrade.coordinate), new ArrayList<>());
            } else {
                return new GameState(size, mySpawns, updatedSpawnList, boardState.upgradingCellAt(cellToUpgrade.coordinate), new ArrayList<>());
            }

        }

        Spawn getMySpawn(int index) {
            return mySpawns.stream().filter(spawn -> spawn.index == index).findFirst().get();
        }

        public boolean isOnBorder(Coordinate coordinate) {
            return coordinate.row == 0 || coordinate.col == 0 || coordinate.row == size - 1 || coordinate.col == size - 1;
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

        Spawn movingTo(Direction moveDirection) {
            return new Spawn(index, coordinateTo(moveDirection));
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Cell cell = (Cell) o;

            return height == cell.height;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + height;
            return result;
        }

        @Override
        public String toString() {
            return "Cell(" + height + ":" + coordinate + ")";
        }

        public boolean isValidPosition() {
            return height >= 0 && height < 4;
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
            String log = Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(", "));
            System.err.println(log);
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