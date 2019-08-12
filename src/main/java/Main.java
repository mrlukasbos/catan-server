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

                // Broadcast data to interface
                broadcastStatus(server, iface); // if the game started, or waiting for players/takeoff
                broadcastPlayerInfo(gm, iface); // the connected players and their colors

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

    private static void broadcastPlayerInfo(GameManager gm, Sock iface) {
        // publish player info
        String playersString = "[";
        for (Player player :  gm.getCurrentGame().getPlayers()) {
            playersString = playersString.concat(player.toString() + ",");
        }
        playersString = playersString.substring(0, playersString.length() - 1);
        playersString = playersString.concat("]");
        iface.broadcast(broadcastType.PLAYERS, playersString);
    }

    private static void broadcastStatus(Server server, Sock iface) {
        if (server.getAmountOfConnections() < MINIMUM_AMOUNT_OF_PLAYERS) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_PLAYERS");
        } else if (!iface.isReadyToStart()) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_TAKEOFF");
        } else {
            iface.broadcast(broadcastType.STATUS, "GAME_RUNNING");
        }
    }

    private static boolean readyToStart(Server server, Sock s) {
        boolean playersAreReady = s.isReadyToStart();
        boolean enoughPlayers = server.getAmountOfConnections() >= MINIMUM_AMOUNT_OF_PLAYERS;
        return playersAreReady && enoughPlayers;
    }
}
