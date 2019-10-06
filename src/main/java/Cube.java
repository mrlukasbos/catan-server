public class Cube {

    private int x, y, z;
    Cube(Tile tile) {
        x = tile.getX() - (tile.getY() + (Math.abs(tile.getY())%2))/2;
        z = tile.getY();
        y = -x - z;
    }

    int getDistance(Cube cube) {
        return (Math.abs(x - cube.x) + Math.abs(y - cube.y) + Math.abs(z - cube.z)) / 2;
    }
}
