import game.engine.Engine;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        int rand_int1 = ThreadLocalRandom.current().nextInt();
        String szam = Integer.toString(rand_int1);

        String[] args1 = {"0", "game.quoridor.QuoridorGame", szam, "5000" ,"Agent", "game.quoridor.players.BlockRandomPlayer"};
//        String[] args1 = {"0", "game.quoridor.QuoridorGame", szam, "5000", "game.quoridor.players.BlockRandomPlayer" ,"Agent"};

        try {
            Engine.main(args1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}