/*
The gamemanager is responsible that the game can run. It connects the game with the players and the visualization.
 */


public class GameManager  {

    private Game currentGame;
    private boolean gameIsRunning = false;


    GameManager()  {
        currentGame = new Game();
    }

    void start() {
        gameIsRunning = true;
    }

    void end() {
        currentGame = new Game();
        gameIsRunning = false;
    }

    void run(Sock sock, Server server) throws InterruptedException {

        var wrapper = new Object() {
            int nodeId = 0;
        };

        while(IsRunning()) {
            // output to visualization
            sock.broadcast(currentGame.getBoard().toString());

            // output to players
            currentGame.getPlayers().forEach((p) -> p.send(currentGame.getBoard().toString()));

            System.out.println( "broadcasting visuals on" + sock.getAddress() + sock.getPort() );
            Thread.sleep(1000);

            Player p = currentGame.getPlayers().get(wrapper.nodeId % currentGame.getPlayers().size());
            if (wrapper.nodeId < currentGame.getBoard().getNodes().size()) {
                currentGame.getBoard().placeCity(p, currentGame.getBoard().getNodes().get(wrapper.nodeId));
            }
            if (wrapper.nodeId < currentGame.getBoard().getEdges().size()) {
                currentGame.getBoard().placeStreet(p, currentGame.getBoard().getEdges().get(wrapper.nodeId));
            }

            wrapper.nodeId++;
        }
    }

    Game getCurrentGame() {
        return currentGame;
    }

    boolean IsRunning() {
        return gameIsRunning;
    }
}
