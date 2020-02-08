/*
The grid system of the board, including nodes and edges
 */

import java.util.*;

class Board {
    private static final int SIZE = 7;
    private static final ArrayList<Type> TERRAIN_TILES = new ArrayList<>() {{
        for (int i = 0; i < 4; i++) add(Type.GRAIN);
        for (int i = 0; i < 4; i++) add(Type.WOOD);
        for (int i = 0; i < 4; i++) add(Type.WOOL);
        for (int i = 0; i < 3; i++) add(Type.ORE);
        for (int i = 0; i < 3; i++) add(Type.STONE);
    }};
    private static final ArrayList<HarbourType> HARBOR_TYPES = new ArrayList<>() {{
        for (int i = 0; i < 4; i++) add(HarbourType.HARBOUR_ALL);
        add(HarbourType.HARBOUR_WOOL);
        add(HarbourType.HARBOUR_WOOD);
        add(HarbourType.HARBOUR_ORE);
        add(HarbourType.HARBOUR_STONE);
        add(HarbourType.HARBOUR_GRAIN);
    }};
    private static final ArrayList<DevelopmentCard> DEVELOPMENT_CARDS = new ArrayList<>() {{
        for (int i = 0; i < 14; i++) add(DevelopmentCard.KNIGHT);
        for (int i = 0; i < 5; i++) add(DevelopmentCard.VICTORY_POINT);
        for (int i = 0; i < 2; i++) add(DevelopmentCard.MONOPOLY);
        for (int i = 0; i < 2; i++) add(DevelopmentCard.ROAD_BUILDING);
        for (int i = 0; i < 2; i++) add(DevelopmentCard.YEAR_OF_PLENTY);

    }};
    private static final int[] TILE_NUMBERS = {5, 2, 6, 10, 9, 4, 3, 8, 11, 5, 8, 4, 3, 6, 10, 11, 12, 9};

    private static final ArrayList<String> HARBOUR_EDGES = new ArrayList<>() {{
        add("([1,0],[2,1])");
        add("([3,0],[3,1])");
        add("([4,1],[5,1])");
        add("([5,3],[6,3])");
        add("([4,4],[5,5])");
        add("([3,6],[4,5])");
        add("([1,6],[2,5])");
        add("([0,4],[1,4])");
        add("([0,2],[1,3])");
    }};

    private List<Tile> tiles;
    private List<Edge> edges;
    private List<Node> nodes;
    private List<DevelopmentCard> developmentCards;
    private Bandit bandit;
    private Map<String, Tile> tileMap;
    private Map<String, Edge> edgeMap;
    private Map<String, Node> nodeMap;

    Board() {
        init();
    }

    private void init() {
        tiles = new ArrayList<>();
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
        developmentCards = new ArrayList<>(DEVELOPMENT_CARDS);

        tileMap = new HashMap<>();
        edgeMap = new HashMap<>();
        nodeMap = new HashMap<>();

        ArrayList<Type> availableTerrains = new ArrayList<>(TERRAIN_TILES);
        int tileNumberIndex = 0;

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int distanceFromCenter = calculateDistanceFromCenter(x, y);

                if (distanceFromCenter == 0) {
                    Tile tile = new Tile(x, y, Type.DESERT);
                    bandit = new Bandit(tile);

                    addTile(tile);
                } else if (distanceFromCenter <= 2) {
                    Tile tile = new Tile(x, y, getRandomTerrainType(availableTerrains), TILE_NUMBERS[tileNumberIndex]);
                    tileNumberIndex++;

                    addTile(tile);
                } else if (distanceFromCenter == 3) {
                    addTile(new Tile(x, y, Type.SEA));
                }
            }
        }

        for (Tile tile : tiles) {
            if (tile.isTerrain()) {
                createEdgesForTile(tile);
                createNodesForTile(tile);
            }
        }

        // set the harbours to the edges and corresponding tiles
        ArrayList<HarbourType> availableHarbors = new ArrayList<>(HARBOR_TYPES);
        for (Edge edge : edges) {
            if (HARBOUR_EDGES.contains(edge.getKey())) {
                edge.setHarbour(getRandomHarborType(availableHarbors));
                for (Tile tile : edge.getTiles()   ) {
                    if (!tile.isTerrain()) {
                        tile.setHarbour(edge.getHarbourType());
                    }
                }
            }
        }

//      Useful for debugging
//        for (Node n : nodes) {
//            System.out.println("distance from " + nodes.get(0).getKey() + " to " + n.getKey() + " is " +  getDistance(nodes.get(0), n));
//        }
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

    private Type getRandomTerrainType(ArrayList<Type> availableTerrains) {
        return availableTerrains.remove(new Random().nextInt(availableTerrains.size()));
    }

    private HarbourType getRandomHarborType(ArrayList<HarbourType> availableHarbors) {
        return availableHarbors.remove(new Random().nextInt(availableHarbors.size()));
    }

    public String tileCoordinatesToKey(int x, int y) {
        return "[" + x + "," + y + "]";
    }

    private void addTile(Tile tile) {
        tiles.add(tile);
        tileMap.put(tile.getKey(), tile);
    }

    private void createEdgesForTile(Tile a) {
        int[][] directions;
        if (a.getY() % 2 == 0) {
            directions = Constants.AXIAL_DIRECTIONS_EVEN;
        } else {
            directions = Constants.AXIAL_DIRECTIONS_ODD;
        }
        for (int i = 0; i < 6; i++) {
            Tile b = tileMap.get(tileCoordinatesToKey(a.getX() + directions[i][0], a.getY() + directions[i][1]));

            Edge newEdge = new Edge(a,b);
            if (!edgeMap.containsKey(newEdge.getKey())) {
                addEdge(new Edge(a, b));
            }
        }
    }

    private void createNodesForTile(Tile a) {
        int[][] directions;
        if (a.getY() % 2 == 0) {
            directions = Constants.AXIAL_DIRECTIONS_EVEN;
        } else {
            directions = Constants.AXIAL_DIRECTIONS_ODD;
        }
        for (int i = 0; i < 6; i++) {
            Tile b = tileMap.get(tileCoordinatesToKey(a.getX() + directions[i][0], a.getY() + directions[i][1]));
            Tile c = tileMap.get(tileCoordinatesToKey(a.getX() + directions[(i + 1) % 6][0], a.getY() + directions[(i + 1) % 6][1]));
            Node n = new Node(a, b, c);
            if (!nodeMap.containsKey(n.getKey())) {
                addNode(new Node(a, b, c));
            }
        }
    }

    ArrayList<Node> getSurroundingNodes(Node n) {
        ArrayList<Node> surroundingNodes = new ArrayList<Node>();
        for(Node node : nodes) {
            int tilesInCommon = 0;

            for (Tile t : node.getTiles()) {
                for (Tile theirTile : n.getTiles()) {
                    if (t.getKey().equals(theirTile.getKey())) {
                        tilesInCommon++;
                    }
                }
            }

            if (tilesInCommon == 2) {
                surroundingNodes.add(node);
            }
        }
        return surroundingNodes;
    }

    // get nodes surrounding an edge, there should usually be two of them
    ArrayList<Node> getSurroundingNodes(Edge edge) {
        ArrayList<Node> surroundingNodes = new ArrayList<Node>();
        for(Node node : nodes) {
            int tilesInCommon = 0;
            for (Tile t : node.getTiles()) {
                if (t == edge.getTiles()[0] || t == edge.getTiles()[1]) {
                    tilesInCommon++;
                }
            }

            if (tilesInCommon == 2) {
                surroundingNodes.add(node);
            }
        }
        return surroundingNodes;
    }

    double getDistance(Node a, Node b) {
        // count the high-y tiles (either 1 or 2)
        // sort on y
        class sortByY implements Comparator<Tile> {
            public int compare(Tile a, Tile b) {
                return a.getY() - b.getY();
            }
        }

        // the same node has distance 0
        if (a.getKey().equals(b.getKey())) return 0;

        // determine if they are high y or low y
        Tile[] bTiles = b.getSortedNeighboursTiles();
        Arrays.sort(bTiles, new sortByY());
        boolean bNodeIsHighY = bTiles[0].getY() == bTiles[1].getY();

        // determine if we are high y or low y
        Tile[] aTiles = a.getSortedNeighboursTiles();
        Arrays.sort(aTiles, new sortByY());
        boolean aNodeIsHighY = aTiles[0].getY() == aTiles[1].getY();


        Tile keyTileA = aTiles[0];
        Tile keyTileB = bTiles[0];


        // if we are of the same kind:
        int tileDistance;

        if (aNodeIsHighY == bNodeIsHighY) {
            tileDistance = keyTileA.getDistance(keyTileB) * 2;
        } else {
            // things get more complex, but we just let our neighbours fix it

            int closestNeighbourDistance = 1000;
            for (Node neighbourNode : getSurroundingNodes(a)) {

                // determine his keytile
                Tile[] nTiles = neighbourNode.getSortedNeighboursTiles();
                Arrays.sort(nTiles, new sortByY());
                closestNeighbourDistance = Math.min(closestNeighbourDistance, nTiles[0].getDistance(keyTileB));
            }

            tileDistance = closestNeighbourDistance * 2 + 1;
        }
        return tileDistance;
    }


    // Get the edges surrounding a node
    // Tiles are sorted from small to big, and keys are noted from small to big coordinate sums
    ArrayList<Edge> getSurroundingEdges(Node n) {

        Tile [] tiles = n.getSortedNeighboursTiles();

        String edgeKey1 = "(" + tiles[0].getKey() + "," + tiles[1].getKey() + ")";
        String edgeKey2 = "(" + tiles[0].getKey() + "," + tiles[2].getKey() + ")";
        String edgeKey3 = "(" + tiles[1].getKey() + "," + tiles[2].getKey() + ")";

        ArrayList<Edge> surroundingEdges = new ArrayList<Edge>();
        surroundingEdges.add(getEdge(edgeKey1));
        surroundingEdges.add(getEdge(edgeKey2));
        surroundingEdges.add(getEdge(edgeKey3));

        return surroundingEdges;
    }

    private void addEdge(Edge edge) {
        edges.add(edge);
        edgeMap.put(edge.getKey(), edge);
    }

    private void addNode(Node node) {
        nodes.add(node);
        nodeMap.put(node.getKey(), node);
    }

    void placeVillage(Player p, Node node) {
        node.setPlayer(p);
        node.setStructure(Structure.VILLAGE);
    }

    void placeCity(Player p, Node node) {
        node.setPlayer(p);
        node.setStructure(Structure.CITY);
    }

    void placeStreet(Player p, Edge edge) {
        edge.setPlayer(p);
        edge.setRoad(true);
    }

    void placeStructure(Player p, Structure structure, String key) {
        switch (structure) {
            case STREET: placeStreet(p, getEdge(key)); break;
            case VILLAGE: placeVillage(p, getNode(key)); break;
            case CITY: placeCity(p, getNode(key)); break;
            default: {

            }
        }
    }

    ArrayList<Structure> getAllStructures() {
        ArrayList<Structure> structures = new ArrayList<>();
        for (Node node : nodes) {
            if (node.hasStructure() && node.hasPlayer()) {
                structures.add(node.getStructure());
            }
        }
        return structures;
    }


    ArrayList<Node> getStructuresFromPlayer(Structure structureType, Player player) {
        ArrayList<Node> structures = new ArrayList<>();

        if (structureType == Structure.CITY || structureType == Structure.VILLAGE) {
            for (Node node : nodes) {
                if (node.hasStructure() && node.getStructure() == structureType && node.hasPlayer() && node.getPlayer() == player) {
                    structures.add(node);
                }
            }
        }
        return structures;
    }

    ArrayList<Edge> getStreetsFromPlayer(Player player) {
        ArrayList<Edge> streets = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.isRoad() && edge.hasPlayer() && edge.getPlayer() == player) {
                streets.add(edge);
            }
        }

        return streets;
    }

    @Override
    public String toString() {
        String tilesString = Helpers.toJSONArray(tiles, false);
        String edgeString = Helpers.toJSONArray(edges, false);
        String nodeString = Helpers.toJSONArray(nodes, false);

        return "{" +
                "\"model\": \"board\", " +
                "\"attributes\": {" +
                "\"tiles\": " + tilesString + ", " +
                "\"edges\": " + edgeString + ", " +
                "\"nodes\": " + nodeString + ", " +
                "\"bandits\": [" + bandit.toString() + "]" +
                "}" +
                '}';
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Tile> getTilesForDiceNumber(int number) {
        List<Tile> tilesWithNumber = new ArrayList<>();
        for (Tile t: tiles ) {
            if(t.getNumber() == number) {
                tilesWithNumber.add(t);
            }
        }
        return tilesWithNumber;
    }

    // get all nodes around a tile
    public List<Node> getNodes(Tile tile) {
        return tile.getNodes();
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    Node getNode(String key) {
        return nodeMap.get(key);
    }

    boolean hasNode(String key) {
        return nodeMap.containsKey(key);
    }

    Edge getEdge(String key) {
        return edgeMap.get(key);
    }

    boolean hasEdge(String key) {
        return edgeMap.containsKey(key);
    }

    Tile getTile(String key) { return tileMap.get(key); }

    public Bandit getBandit() {
        return bandit;
    }

    HarbourType getHarbourTypeForNode(Node node) {
        for (Edge edge : getSurroundingEdges(node) ) {
            if (edge != null && edge.isHarbour()) {
                return edge.getHarbourType();
            }
        }
        return HarbourType.HARBOUR_NONE;
    }

    boolean nodeIsHarbour(Node node) {
        for (Edge edge : getSurroundingEdges(node) ) {
            if (edge != null && edge.isHarbour()) {
                return true;
            }
        }
        return false;
    }
}

enum DevelopmentCard {
    KNIGHT,
    MONOPOLY,
    ROAD_BUILDING,
    YEAR_OF_PLENTY,
    VICTORY_POINT
}
