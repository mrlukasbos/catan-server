public class GameManager  {
    public Game getCurrentGame() {
        return currentGame;
    }

    private Game currentGame;
    private int amountOfPlayers = 1;

    GameManager()  {
        restart();
    }

    void restart() {
        currentGame = new Game(amountOfPlayers);
    }

    void run(Sock s) throws InterruptedException {

        var wrapper = new Object(){ int nodeId = 0; };

        while (true) {
            // output to visualization
            s.broadcast(currentGame.getBoard().toString());

            // output to players
            currentGame.getPlayers().forEach((p) -> p.send(currentGame.getBoard().toString()));
            System.out.println( "broadcasting visuals on" + s.getAddress() + s.getPort() );
            Thread.sleep(1000);

            currentGame.getPlayers().forEach((p) -> {
                currentGame.getBoard().placeCity(p, currentGame.getBoard().getNodes().get(wrapper.nodeId));
            });

            wrapper.nodeId++;
        }
    }
}
