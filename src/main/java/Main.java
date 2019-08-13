public class Main {
    // the amount of players that need to be connected before a game can be started
    private static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;

    public static void main(String[] args) {
        Server server = new Server(10006);
        Interface iface = new Interface( 10007);
        Game game = new Game();

        server.start();
        iface.start();

        while (true) {

            // Broadcast data to interface
            broadcastStatus(server, iface); // if the game started, or waiting for players/takeoff
            broadcastPlayerInfo(server, iface); // the connected players and their colors

            if (readyToStart(server, iface)) {

                // start a game with the connections from the server
                if (!game.isRunning()) {
                    game.start(server.getConnections());
                }

                // broadcast the board
                iface.broadcast(broadcastType.BOARD, game.getBoard().toString());
            } else {
                if (game.isRunning()) {
                    game.quit();
                }
            }

            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastPlayerInfo(Server server, Interface iface) {
        // publish player info
        String playersString = "[";
        for (Player player :  server.getConnections()) {
            playersString = playersString.concat(player.toString() + ",");
        }
        playersString = playersString.substring(0, playersString.length() - 1);
        playersString = playersString.concat("]");
        iface.broadcast(broadcastType.PLAYERS, playersString);
    }

    private static void broadcastStatus(Server server, Interface iface) {
        if (server.getConnections().size() < MINIMUM_AMOUNT_OF_PLAYERS) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_PLAYERS");
        } else if (!iface.isReadyToStart()) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_TAKEOFF");
        } else {
            iface.broadcast(broadcastType.STATUS, "GAME_RUNNING");
        }
    }

    private static boolean readyToStart(Server server, Interface s) {
        boolean playersAreReady = s.isReadyToStart();
        boolean enoughPlayers = server.getConnections().size() >= MINIMUM_AMOUNT_OF_PLAYERS;
        return playersAreReady && enoughPlayers;
    }
}
