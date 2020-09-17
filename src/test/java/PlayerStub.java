import game.Game;
import game.Player;

class PlayerStub extends Player {

    PlayerStub(Game game, int id, String name) {
        super(null, game, id, name);
    }

    @Override
    public void send(String str) {
        // do nothing
    }
}
