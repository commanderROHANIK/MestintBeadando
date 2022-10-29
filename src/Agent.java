///THE_REAL_CARLOS,h046759@stud.u-szeged.hu
import java.util.Random;

import java.util.LinkedList;
import java.util.List;

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.DummyPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;

public class Agent extends QuoridorPlayer {
    private final List<WallObject> walls = new LinkedList();
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    private int numWalls;

    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        this.players[color] = this;
        this.players[1 - color] = new DummyPlayer((1 - color) * 8, j, 1 - color, (Random) null);
        this.numWalls = 0;
    }

    @Override
    public QuoridorAction getAction(QuoridorAction previousAction, long[] remainingTimes) {
        storePreviousAciton(previousAction);

        return decideNextMove();
    }

    private void storePreviousAciton(QuoridorAction prevAction) {
        if (prevAction instanceof WallAction) {
            storePreviousMoveAciton((WallAction) prevAction);
        } else if (prevAction instanceof MoveAction) {
            storePreviousMoveAction((MoveAction) prevAction);
        }
    }

    private void storePreviousMoveAciton(WallAction prevAction) {
        WallAction prevWallAction = prevAction;
        this.walls.add(new WallObject(prevWallAction.i, prevWallAction.j, prevWallAction.horizontal));
    }

    private void storePreviousMoveAction(MoveAction prevAction) {
        MoveAction prevMoveAction = prevAction;
        this.players[1 - this.color].i = prevMoveAction.to_i;
        this.players[1 - this.color].j = prevMoveAction.to_j;
    }


    private QuoridorAction decideNextMove() {
        if (this.numWalls < 10 && this.random.nextDouble() < 0.5) {
            return putWall();
        } else {
            return move();
        }
    }

    private WallAction putWall() {
        WallObject candidate;
        for (candidate = null; !QuoridorGame.checkWall(candidate, this.walls, this.players); candidate = new WallObject(this.random.nextInt(8), this.random.nextInt(8), this.random.nextBoolean())) {
        }

        this.walls.add(candidate);
        ++this.numWalls;
        return new WallAction(candidate.i, candidate.j, candidate.horizontal);
    }

    private MoveAction move() {
        List<PlaceObject> steps = this.toPlace().getNeighbors(this.walls, this.players);
        PlaceObject step = (PlaceObject) steps.get(this.random.nextInt(steps.size()));
        return new MoveAction(this.i, this.j, step.i, step.j);
    }

    private class AlfaBeta {

    }
}
