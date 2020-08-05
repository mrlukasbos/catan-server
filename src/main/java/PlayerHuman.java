import java.net.Socket;

public class PlayerHuman extends Player {
    @Override
    void send(String str) {

    }

    @Override
    String listen() {
        return null;
    }

    PlayerHuman(Game game, int id, String name) {
        super(game, id, name);
    }
}
