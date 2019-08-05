import java.util.*;

class Board {
    private static final int SIZE = 7;
    private static final Type[] POSSIBLE_TYPES = {Type.WHOOL, Type.WHOOL, Type.WHOOL, Type.WHOOL, Type.WOOD, Type.WOOD, Type.WOOD, Type.WOOD, Type.GRAIN, Type.GRAIN, Type.GRAIN, Type.GRAIN, Type.ORE, Type.ORE, Type.ORE, Type.STONE, Type.STONE, Type.STONE};
    private static final int[] TILE_NUMBERS = {5, 2, 6, 10, 9, 4, 3, 8, 11, 5, 8, 4, 3, 6, 10, 11, 12, 9};
    private ArrayList<Type> availableTypes = new ArrayList<>(Arrays.asList(POSSIBLE_TYPES));

    private List<Tile> tiles = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();

    private Map<String, Tile> tileMap = new HashMap<>();
    private Map<String, Edge> edgeMap = new HashMap<>();
    private Map<String, Node> nodeMap = new HashMap<>();

    Board() {
        int tileNumberIndex = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int distanceFromCenter = calculateDistanceFromCenter(x, y);

                if (distanceFromCenter == 0) {
                    addTile(new Tile(x, y, Type.DESERT));
                } else if (distanceFromCenter <= 2) {
                    addTile(new Tile(x, y, getRandomType(), TILE_NUMBERS[tileNumberIndex]));
                    tileNumberIndex++;
                } else if (distanceFromCenter == 3) {
                    addTile(new Tile(x, y, Type.SEA));
                }
            }
        }
    }

    private int calculateDistanceFromCenter(int col, int row) {
        int x = col - (row + (row & 1)) / 2;
        int z = row;
        int y = (-1 * x) - z;

        int center = 3;
        int centerX = center - (center + (center & 1)) / 2;
        int centerZ = center;
        int centerY = (-1 * centerX) - centerZ;

        return (Math.abs(centerX - x) + Math.abs(centerY - y) + Math.abs(centerZ - z)) / 2;
    }

    private Type getRandomType() {
        return availableTypes.remove(new Random().nextInt(availableTypes.size()));
    }

    private void addTile(Tile tile) {
        tiles.add(tile);
        tileMap.put(tile.getKey(), tile);
    }

    void placeVillage(Player p) {

    }

    void placeCity(Player p) {

    }

    void placeStreet(Player p) {

    }

    @Override
    public String toString() {
        String tilesString = "[";
        for (Tile tile : tiles) {
            tilesString = tilesString.concat(tile.toString() + ",");
        }
        tilesString = tilesString.substring(0, tilesString.length() - 1);
        tilesString = tilesString.concat("]");

        return "{" +
                "\"model\": \"board\", " +
                "\"attributes\": {" +
                "\"tiles\": " + tilesString +
                "}" +
                '}';
    }
}
