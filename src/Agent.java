///THE_REAL_CARLOS,h046759@stud.u-szeged.hu

import game.quoridor.MoveAction;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.BlockRandomPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;

import java.util.*;
import java.util.stream.Collectors;

public class Agent extends QuoridorPlayer {
    /**
     * A jatek soran lerakott falak listaja.
     * */
    private final List<WallObject> walls = new LinkedList();
    /**
     * A jatekosokat tartalmazo lista.
     * */
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    /**
     * Az altala lerakott falak szama.
     * */
    private int numWalls;
    /**
     * A megelozo lepeseit tartalmazo lista.
     * */
    private List<MoveAction> prevMoves;

    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        this.players[color] = this;
        this.players[1 - color] = new BlockRandomPlayer((1 - color) * 8, j, 1 - color, (Random) null);
        this.numWalls = 0;
        this.prevMoves = new ArrayList<>();
    }

    /**
     * Parameterul kapja az elozo akciot, amit az ellenfel lepett es, hogy mennyi lehetosege van.
     *
     * Eltarolja az elozo lepest majd, megnezi, hogy mik a lehetseges lepesek a poziciojabol.
     * Valaszt a lehetseges lepesek kozul, majd azt hozzaadja a sajat listalyahoz, amiben a megelozo lepeseket tarolja.
     * Vegul visszater az adott lepessel
     *
     * @param prevAction: az ellenfel lepese
     * @param remainingTimes: hatralevo ido
     * @return a lepes a valasztott mezore
     * */
    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {
        storeEnemyAction(prevAction);

        List<PlaceObject> passibleMoves = toPlace().getNeighbors(walls, players);
        MoveAction action = decideDirectionToMove(passibleMoves);

        prevMoves.add(action);
        return action;
    }

    /**
     * Eldonti, hogy merre lepjen es az adott lepessel visszater.
     *
     * Eloszor megnezi, hogy a parameterul kapott listban van-e olyan lehetoseg amivel a masik oldal fele lep.
     * Majd ellenorzi, hogy melyik iranyba van tobb elerheto mezo.
     * Ha mindket iranyba ugyanannyi lehetseges lepes van es nem tud elore lepni, akkor megprobal hatrafele lepni.
     *
     * Ha ezek utan az action valtozo, amiben a valasztott lepes kerul tarolasra nem null, akkor visszater vele.
     *
     * Ha az action null, akkor megnezi, hogy melyik iranyba van tobb lehetosege lepni es abba az iranyba lep,
     * amerre tobb lehetoseg van.
     *
     * Majd ellenorzi, hogy a korabbi lepesei kozott szerepel-e a cel mezo.
     * Ha szerepel es tobb lehetoseg van lepesre, akkor megkeresi azt a lepest, ami nem megeleozo lepesre megy vagy az elso lehetoseget a listabol.
     *
     * Ha valamilyen kivetelt dob, akkor a lehetosegek kozul az elsot valasztja
     *
     * @param passibleMoves: egy lista, amibe a korulotte levo mezok szerepelnek, amikre lephet
     * @return egy MoveAction, a kovetkezo lepessel a valasztott mezore
     * */
    private MoveAction decideDirectionToMove(List<PlaceObject> passibleMoves) {
        MoveAction action = moveForward(passibleMoves);
        List<Integer> szabaHelyek = getSzabadHelyek(getElotteLevoFalak());

        int balra = 0;
        int jobbra = 0;

        for (int k = 0; k < szabaHelyek.size(); k++) {
            if (k < this.j && szabaHelyek.get(k) == 1) {
                balra++;
            } else if (k > this.j && szabaHelyek.get(k) == 1) {
                jobbra++;
            }
        }

        if (balra == jobbra && Objects.isNull(action)) action = moveBackward(passibleMoves);
        if (Objects.nonNull(action)) return action;

        Optional<PlaceObject> oldalraLepes = balra > jobbra ?
                passibleMoves.stream().filter(step -> step.j < this.j).findFirst() :
                passibleMoves.stream().filter(step -> step.j > this.j).findFirst();


        if (oldalraLepes.isPresent()) action = new MoveAction(i, j, oldalraLepes.get().i, oldalraLepes.get().j);

        try {
            MoveAction finalAction1 = action;
            if (prevMoves.stream().anyMatch(prevMove -> prevMove.to_i == finalAction1.to_i && prevMove.to_j == finalAction1.to_j) && passibleMoves.size() > 1) {
                MoveAction finalAction = action;
                    var next = passibleMoves.stream().filter(move -> move.i != finalAction.to_i && move.j != finalAction.to_i).findFirst();

                    return next.map(placeObject -> new MoveAction(this.i, this.j, placeObject.i, placeObject.j))
                            .orElseGet(() -> new MoveAction(this.i, this.j, passibleMoves.get(0).i, passibleMoves.get(0).j));
            }

        } catch (Exception ignored) {
            return new MoveAction(this.i, this.j, passibleMoves.get(0).i, passibleMoves.get(0).j);
        }

        return action;
    }

    /**
     * Megnezi, hogy van-e egy sorral elotta vagy vele egy sorban fal.
     *
     * @return Egy lista amiben benne vannak az elotte levo vagy a vele megegyezo sorban levo falak.
     * */
    private List<WallObject> getElotteLevoFalak() {
        return this.color == 0 ?
                this.walls.stream().filter(wall -> wall.i == this.i + 1 || wall.i == this.i).collect(Collectors.toList()) :
                this.walls.stream().filter(wall -> wall.i == this.i - 1 || wall.i == this.i).collect(Collectors.toList());
    }

    /**
     * Megnezi, hogy van-e olyan lepes, amivel elorebb jut, ha van, akkor ezzel ter vissza, kulonben null-al.
     *
     * @return Ha elore tud lepni a masik oldal fele, azzal a lepessel ter vissza, ha nem akkor null.
     * */
    private MoveAction moveForward(List<PlaceObject> passibleMoves) {
        MoveAction action = null;
        Optional<PlaceObject> forward = this.color == 0 ?
                passibleMoves.stream().filter(step -> step.i > this.i).findFirst() :
                passibleMoves.stream().filter(step -> step.i < this.i).findFirst();

        if (forward.isPresent()) action = new MoveAction(i, j, forward.get().i, forward.get().j);

        return action;
    }

    /**
     * Megnezi, hogy van-e olyan lepes, amivel hatra lep egyet, ha van, akkor ezzel ter vissza, kulonben null-al.
     *
     * @return Ha vissza tud lepni a sajat oldala fele, azzal a lepessel ter vissza, ha nem akkor null.
     * */
    private MoveAction moveBackward(List<PlaceObject> passibleMoves) {
        MoveAction action = null;
        Optional<PlaceObject> forward = this.color == 0 ?
                passibleMoves.stream().filter(step -> step.i < this.i).findFirst() :
                passibleMoves.stream().filter(step -> step.i > this.i).findFirst();

        if (forward.isPresent()) action = new MoveAction(i, j, forward.get().i, forward.get().j);

        return action;
    }

    /**
     * A kapott lsita alapjan osszerak listat, aminek a mezoi a szomszedos mezok.
     * Ha az adott indexu mezore lehet lepni, nincs útban fal, akkor az elem erteke 1, kulonben 0;
     *
     * Ha van fuggoleges fal velemlyik iranyba, akkor az abba az iranyba eso lehetosegeket 0-ra allaitja.
     * Majd megnezi az elotte levo falakat, ha velemlyikkel azonos oszlopban van, akkor az a lehetoseg is 0 lesz.
     *
     * @param elotteLevoFalak: egy lista, benne az elotte vagy a vele egy sorban levo falakkal.
     * @return egy lista, amiben 0 vagy 1 ertekek vannak, ha az adott mezo elerheto, akkor 1 kulonben 0.
     * */
    private List<Integer> getSzabadHelyek(List<WallObject> elotteLevoFalak) {
        List<Integer> szabaHelyek = Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1);

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

    /**
     * Eltarolja ez elozo lepest, ez minden esetben az ellenfele lesz.
     * Ha ez a lepes WallAction, akkor frissit a tarolt falakat.
     * Ha MoveAction, akkor frissiti az ellenfel poziciojat.
     *
     * @param prevAction: a megelozo lepes.
     * */
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
}