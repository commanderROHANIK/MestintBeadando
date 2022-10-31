///THE_REAL_CARLOS,h046759@stud.u-szeged.hu
import java.util.*;

import java.util.stream.Collectors;

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.BlockRandomPlayer;
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
        this.players[1 - color] = new BlockRandomPlayer((1 - color) * 8, j, 1 - color, (Random) null);
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
        return getEnemy().i > 2 && getEnemy().i < 6 && this.numWalls < 10 ? putWall() : move();
    }

    private WallAction putWall() {
        QuoridorPlayer enemy = getEnemy();
        WallObject candidate = new WallObject(enemy.i + 2, enemy.j, true);

        if (!QuoridorGame.checkWall(candidate, this.walls, this.players)) {
            candidate = new WallObject(enemy.i, enemy.j + 3, false);
            if (!QuoridorGame.checkWall(candidate, this.walls, this.players))
                candidate = new WallObject(enemy.i, enemy.j - 3, false);
        }

        this.walls.add(candidate);
        ++this.numWalls;
        return new WallAction(candidate.i, candidate.j, candidate.horizontal);
    }

    private MoveAction move() {
        List<PlaceObject> steps = this.toPlace().getNeighbors(this.walls, this.players);

        Optional<PlaceObject> stepForward = steps.stream()
                .filter(step -> step.i < this.i)
                .findFirst();

        PlaceObject step = (PlaceObject) steps.get(this.random.nextInt(steps.size()));

        return stepForward.map(placeObject -> new MoveAction(this.i, this.j, placeObject.i, placeObject.j))
                .orElseGet(() -> new MoveAction(this.i, this.j, step.i, step.j));
    }

    private QuoridorPlayer getEnemy() {
        return Arrays.stream(this.players).filter(player -> !(player instanceof Agent)).findFirst().get();
    }
}
