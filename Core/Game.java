package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;

public class Game implements Serializable {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    private boolean gapCheckingFlag = false;
    private Integer[] lastLocation;
    private long seed;
    private TETile[][] world;
    private int[] entityLoc;
    private int[] enemyLoc;
    private String lastInput = "";
    private String displayMessage = "ENTER A SEED NUMBER (PRESS 's' WHEN DONE):\n";

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        boolean notStarted = true;
        int endIndex = -1;
        ter.initialize(WIDTH / 2, HEIGHT);
        promptForInput();
        showOptions();
        while (true) {
//            System.out.println(Math.random());
            if (StdDraw.hasNextKeyTyped()) {
                lastInput += StdDraw.nextKeyTyped();
                lastInput = lastInput.toLowerCase();
                System.out.println(lastInput);
                if (lastInput.length() == 0) {
                    continue;
                }
                if (notStarted && lastInput.endsWith("q")) {
                    System.exit(0);
                }
                if (lastInput.endsWith(":q")) {
                    save();
                    System.exit(0);
                } else if (lastInput.endsWith("q")) {
                    System.exit(0);
                } else if (notStarted && (lastInput.endsWith("n") || lastInput.endsWith("l"))) {
                    endIndex = lastInput.length();
                    display(displayMessage);
                } else if (lastInput.endsWith("s") && notStarted) {
                    seed = Long.parseLong(lastInput.substring(endIndex, lastInput.length() - 1));
                    if (lastInput.charAt(0) == 'n') {
                        world = new TETile[WIDTH][HEIGHT];
                        gridInitialize(world);
                        worldGenerator(world, seed);
                        entityLoc = entityInitial(world, seed, true);
                        enemyLoc = entityInitial(world, seed, false);
                    } else {
                        Game g = load(seed);
                        entityLoc = g.entityLoc;
                        enemyLoc = g.enemyLoc;
                        world = g.world;
                    }
                    ter.initialize(WIDTH, HEIGHT);
                    ter.renderFrame(world);
                    notStarted = false;
                } else if (notStarted && Character.isDigit(lastInput.charAt(lastInput.length() - 1))) {
                    displayMessage += lastInput.charAt(lastInput.length() - 1);
                    display(displayMessage);
                } else if ("wasd".contains(lastInput.substring(lastInput.length() - 1))){
                    System.out.println("Moving");
                    entityMoving(world, entityLoc, lastInput.substring(lastInput.length() - 1), true);
                    ter.renderFrame(world);
                } else if ("ijkl".contains(lastInput.substring(lastInput.length() - 1))) {
                    String instr = "";
                    System.out.println("Moving enemy");
                    switch (lastInput.charAt(0)) {
                        case 'i': instr = "w"; break;
                        case 'j': instr = "a"; break;
                        case 'k': instr = "s"; break;
                        case 'l': instr = "d"; break;
                    }
                    entityMoving(world, enemyLoc, instr, false);
                    ter.renderFrame(world);
                }
            }
            else if (StdDraw.isKeyPressed(KeyEvent.VK_W)) {
                System.out.println("Pressed w");
            }
        }
    }

    public void promptForInput() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 4 , HEIGHT / 2 + 2, "CS 61B: THE GAME");
        StdDraw.show();
    }

    public void showOptions() {
        StdDraw.clear();
        promptForInput();
        StdDraw.text(WIDTH / 4, HEIGHT / 2, "NEW GAME (n)");
        StdDraw.text(WIDTH / 4, HEIGHT / 2 - 2, "LOAD GAME (l)");
        StdDraw.text(WIDTH / 4, HEIGHT / 2 - 4, "QUIT (q)");
        StdDraw.show();
    }

    public void display(String message) {
        StdDraw.clear();
        promptForInput();
        StdDraw.text(WIDTH / 4, HEIGHT / 2, message);
        StdDraw.show();
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        seed = seedGenerator(input);
        String[] instruction = instructionGenerator(input);
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        gridInitialize(finalWorldFrame);
        if (instruction[0].equalsIgnoreCase("new")) {
            worldGenerator(finalWorldFrame, seed);
            int[] entityLocation = entityInitial(finalWorldFrame,seed, true);
            int[] enemyLocation = entityInitial(finalWorldFrame,seed, false);
            entityMoving(finalWorldFrame,entityLocation,instruction[1], true);
            entityMoving(finalWorldFrame,enemyLocation,enemyStringGenerator(instruction[1],seed), false);

        } else {
            Game savedGame = load(seed);
            finalWorldFrame = savedGame.world;
        }
        world = finalWorldFrame;
        if (instruction[2].equalsIgnoreCase("save")) {
            save();
            System.exit(0);
        }
        return finalWorldFrame;
    }

    public static long seedGenerator(String input) {
        String temp = "";
        for (int i = 0; i < input.length(); i++) {
            //https://stackoverflow.com/questions/4047808/
            // what-is-the-best-way-to-tell-if-a-character-is-a-letter-or-number-in-java-withou
            if (Character.isDigit(input.charAt(i))) {
                temp += input.charAt(i);
            }
        }
        return Long.parseLong(temp);
    }



    public String enemyStringGenerator(String input,Long seed) {
        String resources = "wasd";
        final Random rnd = new Random(seed);
        StringBuilder outcome = new StringBuilder();
        while (outcome.length() < input.length()) { // length of the random string.
            int index = (int) (rnd.nextFloat() * resources.length());
            outcome.append(resources.charAt(index));
        }
        String result = outcome.toString();
        return result;

    }

    public static String[] instructionGenerator(String input){
        String[] temp = new String[3];

        if (input.substring(0, 1).equalsIgnoreCase("n")){
            temp[0] = "new";
        }
        else {
            temp[0] = "load";
        }

        if (input.substring(input.length() - 2).equalsIgnoreCase(":q")){
            temp[2] = "save";
        } else {
            temp[2] = "lost";
        }
        int i;
        for(i = 1; Character.isDigit(input.charAt(i)); i++){
        }
        temp[1] = input.substring(i);
        return temp;
    }

    public int[] entityInitial(TETile[][] inputWorld, Long seed, boolean isEnemy){
        final Random random = new Random(seed);
        int[] temp = new int[]{RandomUtils.uniform(random,WIDTH), RandomUtils.uniform(random,HEIGHT)};
        while (!inputWorld[temp[0]][temp[1]].description().equalsIgnoreCase("floor")) {
            temp = new int[]{RandomUtils.uniform(random,WIDTH), RandomUtils.uniform(random,HEIGHT)};
        }
        inputWorld[temp[0]][temp[1]] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
        return temp;
    }


    public int[] entityMoving(TETile[][] inputWorld, int[] entityLocation,String insturction, boolean isEnemy) {
        int xPos = entityLocation[0];
        int yPos = entityLocation[1];
        inputWorld[xPos][yPos] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
        int index = 0;
        while(index != insturction.length()){
            char temp = insturction.charAt(index);
            if (temp == 'w' && !inputWorld[xPos][yPos+1].description().equalsIgnoreCase("wall")) {
                inputWorld[xPos][yPos] = Tileset.FLOOR;
                inputWorld[xPos][yPos+1] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
                yPos++;
            }
            if (temp == 's' && !inputWorld[xPos][yPos-1].description().equalsIgnoreCase("wall")) {
                inputWorld[xPos][yPos] = Tileset.FLOOR;
                inputWorld[xPos][yPos-1] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
                yPos--;
            }
            if (temp == 'd' && !inputWorld[xPos+1][yPos].description().equalsIgnoreCase("wall")) {
                inputWorld[xPos][yPos] = Tileset.FLOOR;
                inputWorld[xPos+1][yPos] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
                xPos++;
            }
            if (temp == 'a' && !inputWorld[xPos-1][yPos].description().equalsIgnoreCase("wall")) {
                inputWorld[xPos][yPos] = Tileset.FLOOR;
                inputWorld[xPos-1][yPos] = (isEnemy) ? Tileset.FLOWER : Tileset.GRASS;
                xPos--;
            }
            index++;
        }
        if (isEnemy) {
            entityLoc = new int[] {xPos, yPos};
            return entityLoc;
        } else {
            enemyLoc = new int[] {xPos, yPos};
            return enemyLoc;
        }
    }

    public void gridInitialize(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void worldGenerator(TETile[][] inputWorld, Long seed) {
        makeWorld(inputWorld, seed);

    }


    /**
     * Generates the world. I learned the algorithm for maze generation from Wikipedia.
     *
     * @param inputWorld
     * @param seed
     * @source https://en.wikipedia.org/wiki/Maze_generation_algorithm#Depth-first_search
     */

    public void makeWorld(TETile[][] inputWorld, Long seed) {
        int[][] visited = new int[inputWorld.length][inputWorld[0].length];
        for (int i = 0; i < visited.length; i++) {
            for (int j = 0; j < visited[i].length; j++) {
                if (i % 2 == 0 || j % 2 == 0) {
                    visited[i][j] = 0;
                } else {
                    visited[i][j] = -1;
                }
            }
        }
        final Random random = new Random(seed);
        int i = 0, j = 0;
        while (visited[i][j] != -1) {
            i = RandomUtils.uniform(random, visited.length);
            j = RandomUtils.uniform(random, visited[i].length);
        }
        genMaze(visited, random, i, j);
        for (int k = 0; k < visited.length; k++) {
            for (int l = 0; l < visited[k].length; l++) {
                if (visited[k][l] == 1 || RandomUtils.uniform(random) < 0.2) {
                    inputWorld[k][l] = Tileset.FLOOR;
                } else {
                    inputWorld[k][l] = Tileset.WALL;
                }
            }
        }

        for(int m =0; m<2000;m++) {
            try {
                makeSquare(inputWorld, random);
            } catch (ArrayIndexOutOfBoundsException e){
                break;
            }
        }
        addBorder(inputWorld);
        fillBubbles(inputWorld, lastLocation);
    }

    /**
     * Removing all the bubbles, I learned this algorithm online, it's called Flood fill.
     *
     * @param InputWorld
     * @param lastLocation
     * @source https://en.wikipedia.org/wiki/Flood_fill#Alternative_implementations
     */
    private void fillBubbles(TETile[][] InputWorld, Integer[] lastLocation){
        Queue <Integer[]> queque1 = new LinkedList<>();
        boolean[][] visted = new boolean[WIDTH][HEIGHT];
        queque1.add(lastLocation);
        while(!queque1.isEmpty()){
            Integer[] temp = queque1.remove();
            int xPos = temp[0];
            int yPos = temp[1];
            if(checkConditions(InputWorld,xPos+1,yPos,visted)){
                queque1.add(new Integer[]{xPos+1, yPos});
                visted[xPos+1][yPos] = true;

            }
            if(checkConditions(InputWorld,xPos-1,yPos,visted)){
                queque1.add(new Integer[]{xPos-1, yPos});
                visted[xPos-1][yPos] = true;

            }
            if(checkConditions(InputWorld,xPos,yPos+1,visted)){
                queque1.add(new Integer[]{xPos, yPos+1});
                visted[xPos][yPos+1] = true;

            }
            if(checkConditions(InputWorld,xPos,yPos-1,visted)){
                queque1.add(new Integer[]{xPos, yPos-1});
                visted[xPos][yPos-1] = true;

            }
        }

        for(int i =0; i< WIDTH;i++){
            for(int j=0; j< HEIGHT;j++){
                if(!visted[i][j] && InputWorld[i][j].description().equalsIgnoreCase("floor")) {
                    InputWorld[i][j] = Tileset.NOTHING;
                }
            }
        }

    }

    private boolean checkConditions(TETile[][] InputWorld, int xPos, int yPos, boolean[][] visted){
        if(xPos < WIDTH && xPos >= 0 && yPos < HEIGHT && yPos >=0){
            if(!visted[xPos][yPos]){
                if(!InputWorld[xPos][yPos].description().equalsIgnoreCase("wall")) {
                    return true;
                }
            }
        }
        return false;
    }


    private void genMaze(int[][] visited, Random r, int i, int j) {
        visited[i][j] = 1;
        int[][] neighbors = {
                {i, j + 2},
                {i, j - 2},
                {i + 2, j},
                {i - 2, j}
        };
        int randIndex = RandomUtils.uniform(r, neighbors.length);
        boolean looped = false;
        for (int k = randIndex; !looped || k != randIndex; k = (k + 1) % neighbors.length) {
            looped = true;
            int[] coords = neighbors[k];
            if (isValid(coords, visited)) {
                visited[(i + coords[0]) / 2][(j + coords[1]) / 2] = 1;
                genMaze(visited, r, coords[0], coords[1]);
            }
        }
    }

    private boolean isValid(int[] coords, int[][] visited) {
        return (
                coords[0] >= 0
                        && coords[0] < visited.length
                        && coords[1] >= 0 && coords[1] < visited[0].length
                        && visited[coords[0]][coords[1]] == -1
        );
    }


    private boolean gapdecisionMaker(Random RANDOM1){
        if(gapCheckingFlag){
            return RandomUtils.bernoulli(RANDOM1, 0.3);
        }
        return false;
    }

    public void addBorder(TETile[][] inputWorld){
        for (int i = 0; i< WIDTH; i++){
            inputWorld[i][0] = Tileset.WALL;
            inputWorld[i][HEIGHT-1] = Tileset.WALL;
        }

        for (int j = 0; j < HEIGHT; j++){
            inputWorld[0][j] = Tileset.WALL;
            inputWorld[WIDTH-1][j] = Tileset.WALL;
        }


    }

    public void makeSquare(TETile[][] inputWorld, Random RANDOM1) throws ArrayIndexOutOfBoundsException {

        gapCheckingFlag = true;
        int width = RandomUtils.uniform(RANDOM1, 2, inputWorld.length * 3 / 4);
        int height = RandomUtils.uniform(RANDOM1, 2, inputWorld[0].length * 3 / 4);
        int xPos = RandomUtils.uniform(RANDOM1, inputWorld.length-1);
        int yPos = RandomUtils.uniform(RANDOM1, inputWorld[0].length-1);

        while (xPos + width >= WIDTH || xPos - width < 0 || yPos + height >= HEIGHT || yPos - height < 0) {
            width = RandomUtils.uniform(RANDOM1, 2, inputWorld.length);
            height = RandomUtils.uniform(RANDOM1, 2, inputWorld[0].length);
            xPos = RandomUtils.uniform(RANDOM1, inputWorld.length-1);
            yPos = RandomUtils.uniform(RANDOM1, inputWorld[0].length-1);
        }
        for (int i = xPos; i < xPos + width; i++) {
            inputWorld[i][yPos] = Tileset.WALL;
            if (inputWorld[i][yPos-1].description().equalsIgnoreCase("floor") && gapdecisionMaker(RANDOM1)){
                inputWorld[i][yPos] = Tileset.FLOOR;
            }
            inputWorld[i][yPos + height] = Tileset.WALL;
            if (inputWorld[i][yPos + height + 1].description().equalsIgnoreCase("floor") && gapdecisionMaker(RANDOM1)){
                inputWorld[i][yPos + height] = Tileset.FLOOR;
            }
        }

        for (int j = yPos; j <= yPos + height; j++) {
            inputWorld[xPos][j] = Tileset.WALL;
            if (inputWorld[xPos-1][j].description().equalsIgnoreCase("floor") && gapdecisionMaker(RANDOM1)){
                inputWorld[xPos][j] = Tileset.FLOOR;
            }
            inputWorld[xPos + width][j] = Tileset.WALL;
            if (inputWorld[xPos+width+1][j].description().equalsIgnoreCase("floor") && gapdecisionMaker(RANDOM1)){
                inputWorld[xPos + width][j] = Tileset.FLOOR;
            }

        }

        for (int i = xPos + 1; i < xPos + width; i++) {
            for (int j = yPos + 1; j < yPos + height; j++) {
                inputWorld[i][j] = Tileset.FLOOR;
            }
        }
        lastLocation = new Integer[]{xPos+2, yPos+2};


    }

    public static Game load(long seed) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(seed + ".txt")));
            return (Game) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error when loading Game from file");
            System.exit(1);
        }
        return null;
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(new File(seed + ".txt"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            fos.close();
            oos.close();
        } catch (IOException e) {
            System.err.println("Error when saving object");
            e.printStackTrace();
            System.exit(1);
        }

    }

//    private void writeObject(ObjectOutputStream out)
//            throws IOException {
//        System.out.println("Writing object");
//        out.writeObject(this);
//    }
//
//    private void readObject(ObjectInputStream in)
//            throws IOException, ClassNotFoundException {
//    }
//
//    private void readObjectNoData()
//            throws ObjectStreamException {
//
//    }
}