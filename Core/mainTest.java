package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.Random;
public class mainTest {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 12;
    private static final String startKey = "n" + SEED + "s";
    private static final Random RANDOM = new Random(SEED);

    public static void main(String[] args) {

        Game game = new Game();
        TETile[][] finalWorldFrame = game.playWithInputString(startKey);




    }

}
