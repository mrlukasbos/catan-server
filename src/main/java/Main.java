public class Main {
    // the amount of players that need to be connected before a game can be started
    private static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;

    public static void main(String[] args) {
        GameManager gm = new GameManager();
        Server server = new Server(10006, gm);
        Sock iface = new Sock( 10007, gm, server);

        try {
            server.start();
            iface.start();

            while (true) {
                // handle broadcasting for interface
                broadcastPlayerConnections(gm, iface); // the names of players connected
                iface.broadcast(broadcastType.GAME_RUNNING, String.valueOf(readyToStart(server, iface))); // whether the game is running or not


                // publish player info
                String playersString = "[";
                for (Player player :  gm.getCurrentGame().getPlayers()) {
                    playersString = playersString.concat(player.toString() + ",");
                }
                playersString = playersString.substring(0, playersString.length() - 1);
                playersString = playersString.concat("]");
                iface.broadcast(broadcastType.PLAYERS, playersString);


                if (readyToStart(server, iface)) {

                    // broadcast the board
                    iface.broadcast(broadcastType.BOARD, gm.getCurrentGame().getBoard().toString());
                    gm.getCurrentGame().getPlayers().forEach((p) -> p.send(gm.getCurrentGame().getBoard().toString()));

                    gm.run();
                } else if (gm.IsRunning()) {
                    // first shut down the server, so we can kill the sockets which are hooked to the players
                    server.shutDown();
                    gm.end();
                } else {
                    gm.getCurrentGame().getPlayers().forEach((p) -> p.send("MSG Waiting for game to start"));
                }

                Thread.sleep(200); // 5fps
            }
        } catch (InterruptedException e) {
            server.shutDown();
            iface.shutDown();
            e.printStackTrace();
        } finally {
            server.shutDown();
            iface.shutDown();
        }
    }

    private static void broadcastPlayerConnections(GameManager gm, Sock s) {
        String connectedPlayerNames = "";
        for (Player p : gm.getCurrentGame().getPlayers()) {
            connectedPlayerNames += p.getName() + ",";
        }
        s.broadcast(broadcastType.COMMUNICATION, connectedPlayerNames);
    }

    private static boolean readyToStart(Server server, Sock s) {
        boolean playersAreReady = s.isReadyToStart();
        boolean enoughPlayers = server.getAmountOfConnections() >= MINIMUM_AMOUNT_OF_PLAYERS;
        return playersAreReady && enoughPlayers;
    }
}
