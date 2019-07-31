// a board consists of multiple tiles

class Board {
    private Tile[][] tiles = new Tile[7][];

    // for each player we need


    Board() {
        // lets construct a board (rows x cols)
        tiles[0] = new Tile[4];
        tiles[0][0] = new Tile(Type.SEA);
        tiles[0][1] = new Tile(Type.SEA);
        tiles[0][2] = new Tile(Type.SEA);
        tiles[0][3] = new Tile(Type.SEA);

        tiles[1] = new Tile[5];
        tiles[1][0] = new Tile(Type.SEA);
        tiles[1][1] = new Tile(Type.GRAIN, 8);
        tiles[1][2] = new Tile(Type.STONE, 4);
        tiles[1][3] = new Tile(Type.GRAIN, 5);
        tiles[1][4] = new Tile(Type.SEA);

        tiles[2] = new Tile[6];
        tiles[2][0] = new Tile(Type.SEA);
        tiles[2][1] = new Tile(Type.STONE, 11);
        tiles[2][2] = new Tile(Type.WHOOL, 2);
        tiles[2][3] = new Tile(Type.GRAIN, 11);
        tiles[2][4] = new Tile(Type.WHOOL, 9);
        tiles[2][5] = new Tile(Type.SEA);

        tiles[3] = new Tile[7];
        tiles[3][0] = new Tile(Type.SEA);
        tiles[3][1] = new Tile(Type.WOOD, 10);
        tiles[3][2] = new Tile(Type.ORE, 6);
        tiles[3][3] = new Tile(Type.DESERT);
        tiles[3][4] = new Tile(Type.ORE, 3);
        tiles[3][5] = new Tile(Type.WOOD, 8);
        tiles[3][6] = new Tile(Type.SEA);

        tiles[4] = new Tile[6];
        tiles[4][0] = new Tile(Type.SEA);
        tiles[4][1] = new Tile(Type.WHOOL, 5);
        tiles[4][2] = new Tile(Type.WOOD, 3);
        tiles[4][3] = new Tile(Type.STONE, 9);
        tiles[4][4] = new Tile(Type.GRAIN, 4);
        tiles[4][5] = new Tile(Type.SEA);

        tiles[5] = new Tile[5];
        tiles[5][0] = new Tile(Type.SEA);
        tiles[5][1] = new Tile(Type.WOOD, 6);
        tiles[5][2] = new Tile(Type.ORE, 10);
        tiles[5][3] = new Tile(Type.WHOOL, 12);
        tiles[5][3] = new Tile(Type.SEA);

        tiles[6] = new Tile[4];
        tiles[6][0] = new Tile(Type.SEA);
        tiles[6][1] = new Tile(Type.SEA);
        tiles[6][2] = new Tile(Type.SEA);
        tiles[6][3] = new Tile(Type.SEA);
    }

    void placeVillage(Player p) {

    }

    void placeCity(Player p) {

    }

    void placeStreet(Player p) {

    }
}
