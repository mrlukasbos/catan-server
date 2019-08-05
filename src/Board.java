// a board consists of multiple tiles

import java.lang.reflect.Array;
import java.util.*;

class Board {
    private static final int SIZE = 7;

    private List<Tile> tiles = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();

    private Map<String, Tile> tileMap = new HashMap<>();
    private Map<String, Edge> edgeMap = new HashMap<>();
    private Map<String, Node> nodeMap = new HashMap<>();

    Board() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int distanceFromCenter = calculateDistanceFromCenter(x, y);

                if (distanceFromCenter == 0) {
                    addTile(new Tile(x, y, Type.DESERT));
                } else if (distanceFromCenter <= 2) {
                    addTile(new Tile(x, y, Type.GRAIN, 6));
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
                "\"model\": \"board\"," +
                "\"attributes\": {" +
                "\"tiles\": " + tilesString +
                "}" +
                '}';
    }
}
