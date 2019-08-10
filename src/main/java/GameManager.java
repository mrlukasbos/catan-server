public class GameManager  {
    public Game getCurrentGame() {
        return currentGame;
    }

    private Game currentGame;

    public boolean IsRunning() {
        return gameIsRunning;
    }

    public void setGameIsRunning(boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }

    private boolean gameIsRunning = false;

    GameManager()  {
        currentGame = new Game();

        // default start a game with two players
        //start();
    }

    void start() {
        gameIsRunning = true;
    }

    void end() {
        currentGame = new Game();
        gameIsRunning = false;
    }

    void run(Sock sock, Server server) throws InterruptedException {

        var wrapper = new Object(){ int nodeId = 0; };

        while(IsRunning()) {
            // output to visualization
            sock.broadcast(currentGame.getBoard().toString());

            // output to players
            currentGame.getPlayers().forEach((p) -> p.send(currentGame.getBoard().toString()));
            System.out.println( "broadcasting visuals on" + sock.getAddress() + sock.getPort() );
            Thread.sleep(1000);

            currentGame.getPlayers().forEach((p) -> {
                currentGame.getBoard().placeCity(p, currentGame.getBoard().getNodes().get(wrapper.nodeId));
            });

            currentGame.getPlayers().forEach((p) -> {
                currentGame.getBoard().placeStreet(p, currentGame.getBoard().getEdges().get(wrapper.nodeId));
            });

            wrapper.nodeId++;
        }
    }
}
