public class Main {
    // the amount of players that need to be connected before a game can be started
    private static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;

    public static void main(String[] args) {
        Server server = new Server(10006);
        Sock iface = new Sock( 10007);
        Game game = new Game();

        try {
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
                    game.getPlayers().forEach((p) -> p.send(game.getBoard().toString()));

                    game.run();
                } else {
                    if (game.isRunning()) {
                        game.stop();
                    }
                    server.getConnections().forEach((p) -> p.send("MSG Waiting for game to start"));
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

    private static void broadcastPlayerInfo(Server server, Sock iface) {
        // publish player info
        String playersString = "[";
        for (Player player :  server.getConnections()) {
            playersString = playersString.concat(player.toString() + ",");
        }
        playersString = playersString.substring(0, playersString.length() - 1);
        playersString = playersString.concat("]");
        iface.broadcast(broadcastType.PLAYERS, playersString);
    }

    private static void broadcastStatus(Server server, Sock iface) {
        if (server.getConnections().size() < MINIMUM_AMOUNT_OF_PLAYERS) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_PLAYERS");
        } else if (!iface.isReadyToStart()) {
            iface.broadcast(broadcastType.STATUS, "WAITING_FOR_TAKEOFF");
        } else {
            iface.broadcast(broadcastType.STATUS, "GAME_RUNNING");
        }
    }

    private static boolean readyToStart(Server server, Sock s) {
        boolean playersAreReady = s.isReadyToStart();
        boolean enoughPlayers = server.getConnections().size() >= MINIMUM_AMOUNT_OF_PLAYERS;
        return playersAreReady && enoughPlayers;
    }
}
