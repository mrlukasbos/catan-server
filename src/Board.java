import java.util.*;

class Board {
    private static final int SIZE = 7;
    private static final Type[] POSSIBLE_TERRAINS = {Type.WOOL, Type.WOOL, Type.WOOL, Type.WOOL, Type.WOOD, Type.WOOD, Type.WOOD, Type.WOOD, Type.GRAIN, Type.GRAIN, Type.GRAIN, Type.GRAIN, Type.ORE, Type.ORE, Type.ORE, Type.STONE, Type.STONE, Type.STONE};
    private static final Type[] POSSIBLE_HARBORS = {Type.HARBOUR_GRAIN, Type.HARBOUR_ORE, Type.HARBOUR_WOOL, Type.HARBOUR_STONE, Type.HARBOUR_WOOD, Type.HARBOUR_ALL, Type.HARBOUR_ALL, Type.HARBOUR_ALL, Type.HARBOUR_ALL};
    private static final int[] TILE_NUMBERS = {5, 2, 6, 10, 9, 4, 3, 8, 11, 5, 8, 4, 3, 6, 10, 11, 12, 9};
    private static final Map<String, Orientation> HARBOR_ORIENTATIONS  = new HashMap<String, Orientation>() {{
        put("[1,0]", Orientation.BOTTOM_RIGHT);
        put("[3,0]", Orientation.BOTTOM_LEFT);
        put("[5,1]", Orientation.BOTTOM_LEFT);
        put("[6,3]", Orientation.LEFT);
        put("[5,5]", Orientation.TOP_LEFT);
        put("[3,6]", Orientation.TOP_LEFT);
        put("[1,6]", Orientation.TOP_RIGHT);
        put("[0,4]", Orientation.RIGHT);
        put("[0,2]", Orientation.RIGHT);
    }};
    private ArrayList<Type> availableTerrains;
    private ArrayList<Type> availableHarbors;

    private List<Tile> tiles = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();

    private Map<String, Tile> tileMap = new HashMap<>();
    private Map<String, Edge> edgeMap = new HashMap<>();
    private Map<String, Node> nodeMap = new HashMap<>();

    Board() {
        init();
    }

    private void init() {
        availableTerrains = new ArrayList<>(Arrays.asList(POSSIBLE_TERRAINS));
        availableHarbors = new ArrayList<>(Arrays.asList(POSSIBLE_HARBORS));
        int tileNumberIndex = 0;

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int distanceFromCenter = calculateDistanceFromCenter(x, y);

                if (distanceFromCenter == 0) {
                    Tile tile = new Tile(x, y, Type.DESERT);

                    addTile(tile);
                } else if (distanceFromCenter <= 2) {
                    Tile tile = new Tile(x, y, getRandomTerrainType(), TILE_NUMBERS[tileNumberIndex]);
                    tileNumberIndex++;

                    addTile(tile);
                } else if (distanceFromCenter == 3) {
                    if (HARBOR_ORIENTATIONS.containsKey(tileCoordinatesToKey(x,y))) {
                        addTile(new Tile(x, y, getRandomHarborType(), HARBOR_ORIENTATIONS.get(tileCoordinatesToKey(x,y))));
                    } else {
                        addTile(new Tile(x, y, Type.SEA));
                    }
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

    private Type getRandomTerrainType() {
        return availableTerrains.remove(new Random().nextInt(availableTerrains.size()));
    }

    private Type getRandomHarborType() {
        return availableHarbors.remove(new Random().nextInt(availableHarbors.size()));
    }

    public String tileCoordinatesToKey(int x, int y) {
        return "[" + x + "," + y + "]";
    }

    private void addTile(Tile tile) {
        tiles.add(tile);
        tileMap.put(tile.getKey(), tile);
    }

    private void createEdgesForTile(Tile tile) {

    }

    private void createNodesForTile(Tile tile) {

    }

    private void addEdge(Edge edge) {
        edges.add(edge);
        edgeMap.put(edge.getKey(), edge);
    }

    private void addNode(Node node) {
        nodes.add(node);
        nodeMap.put(node.getKey(), node);
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
