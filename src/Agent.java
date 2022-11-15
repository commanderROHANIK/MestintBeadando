///THE_REAL_CARLOS,h046759@stud.u-szeged.hu

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.BlockRandomPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;

import java.util.*;
import java.util.stream.Collectors;

public class Agent extends QuoridorPlayer {
    private final List<WallObject> walls = new LinkedList();
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    private int numWalls;
    private List<MoveAction> prevMoves;

    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        this.players[color] = this;
        this.players[1 - color] = new BlockRandomPlayer((1 - color) * 8, j, 1 - color, (Random) null);
        this.numWalls = 0;
        this.prevMoves = new ArrayList<>();
    }

    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {
        storeEnemyAction(prevAction);

        List<PlaceObject> passibleMoves = toPlace().getNeighbors(walls, players);
        MoveAction action = decideDirectionToMove(passibleMoves);


//        MoveAction finalAction = action;
//        if (prevMoves.stream().anyMatch(earlyerAction -> finalAction.to_i == earlyerAction.to_i && finalAction.to_j == earlyerAction.to_j) && passibleMoves.size() > 1) {
//            MoveAction finalAction1 = action;
//            var passibleAction = passibleMoves.stream().filter(passibleMove -> finalAction1.to_i != passibleMove.i && finalAction1.to_j != passibleMove.j).findFirst();
//            action = new MoveAction(this.i, this.j, passibleAction.get().i, passibleAction.get().j);
//        }
//
//        if (Objects.isNull(action)) {
//            action = new MoveAction(this.i, this.j, passibleMoves.get(0).i, passibleMoves.get(0).j);
//        }

        prevMoves.add(action);
        return action;
    }

    private MoveAction decideDirectionToMove(List<PlaceObject> passibleMoves) {
        MoveAction action;
        List<WallObject> elotteLevoFalak;

        action = moveForward(passibleMoves);

        if (Objects.nonNull(action)) {
            return action;
        }

        if (this.color == 0) {
            elotteLevoFalak = this.walls.stream().filter(wall -> wall.i == this.i + 1).collect(Collectors.toList());
        } else {
            elotteLevoFalak = this.walls.stream().filter(wall -> wall.i == this.i - 1).collect(Collectors.toList());
        }

        List<Integer> szabaHelyek = getSzabadHelyek(elotteLevoFalak);

        int balra = 0;
        int jobbra = 0;

        for (int k = 0; k < szabaHelyek.size(); k++) {
            if (k < this.j && szabaHelyek.get(k) == 1) {
                balra++;
            } else if (k > this.j && szabaHelyek.get(k) == 1) {
                jobbra++;
            }
        }

        if (balra == jobbra) {
            if (this.color == 0) {
                var hatraLepes = passibleMoves.stream().filter(step -> step.i < this.i).findFirst();
                if (hatraLepes.isPresent()) {
                    return new MoveAction(i, j, hatraLepes.get().i, hatraLepes.get().j);
                }
            } else {
                var hatraLepes = passibleMoves.stream().filter(step -> step.i > this.i).findFirst();
                if (hatraLepes.isPresent()) {
                    return new MoveAction(i, j, hatraLepes.get().i, hatraLepes.get().j);
                }
            }
        }

        if (balra > jobbra) {
            var balraLepes = passibleMoves.stream().filter(step -> step.j < this.j).findFirst();
            action = new MoveAction(i, j, balraLepes.get().i, balraLepes.get().j);
        } else {
            var jobbraLepes = passibleMoves.stream().filter(step -> step.j > this.j).findFirst();
            action = new MoveAction(i, j, jobbraLepes.get().i, jobbraLepes.get().j);
        }

        return action;
    }

    private MoveAction moveForward(List<PlaceObject> passibleMoves) {
        MoveAction action = null;

        if (this.color == 0) {
            var forward = passibleMoves.stream().filter(step -> step.i > this.i).findFirst();
            if (forward.isPresent()) {
                action = new MoveAction(i, j, forward.get().i, forward.get().j);
            }
        } else {
            var forward = passibleMoves.stream().filter(step -> step.i < this.i).findFirst();
            if (forward.isPresent()) {
                action = new MoveAction(i, j, forward.get().i, forward.get().j);
            }
        }

        return action;
    }

    private List<Integer> getSzabadHelyek(List<WallObject> elotteLevoFalak) {
        List<Integer> szabaHelyek = new ArrayList<>();


        for (int k = 0; k < 8; k++) {
            szabaHelyek.add(1);
        }

        elotteLevoFalak.stream()
                .filter(wall -> !wall.horizontal)
                .forEach(wall -> {
                    if (wall.j < this.j) {
                        for (int k = 0; k < szabaHelyek.size(); k++) {
                            if (k < this.j) szabaHelyek.set(k, 0);
                        }
                    } else {
                        for (int k = 0; k < szabaHelyek.size(); k++) {
                            if (k > this.j) szabaHelyek.set(k, 0);
                        }
                    }
                });

        for (int k = 0; k < szabaHelyek.size(); k++) {
            int k1 = k;
            if (elotteLevoFalak.stream().anyMatch(wall -> wall.j == k1)) szabaHelyek.set(k, 0);
        }

        return szabaHelyek;
    }

    private void storeEnemyAction(QuoridorAction prevAction) {
        if (prevAction instanceof WallAction) {
            WallAction a = (WallAction) prevAction;
            walls.add(new WallObject(a.i, a.j, a.horizontal));
        } else if (prevAction instanceof MoveAction) {
            MoveAction a = (MoveAction) prevAction;
            players[1 - color].i = a.to_i;
            players[1 - color].j = a.to_j;
        }
    }

    private WallAction putWall(WallObject wall) {
        numWalls++;
        walls.add(wall);
        return wall.toWallAction();
    }

    private List<WallObject> getWallObjects() {
        int di = (color * (QuoridorGame.HEIGHT - 1)) - players[1 - color].i < 0 ? -1 : 0;
        List<WallObject> wallObjects = new LinkedList<WallObject>();
        wallObjects.add(new WallObject(players[1 - color].i + di, players[1 - color].j - color, true));
        wallObjects.add(new WallObject(players[1 - color].i + di, players[1 - color].j - 1 + color, true));
        wallObjects.add(new WallObject(players[1 - color].i + di, players[1 - color].j - color, false));
        wallObjects.add(new WallObject(players[1 - color].i + di, players[1 - color].j - 1 + color, false));
        return wallObjects;
    }

}