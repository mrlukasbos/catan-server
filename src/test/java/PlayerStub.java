class PlayerStub extends Player {
    private String message = "";

    PlayerStub(Game game, int id, String name) {
        super(game, id, name);
    }

    @Override
    String listen() {
        return message;
    }

    @Override
    void send(String str) {
        // do nothing
    }

    void setMessageFromPlayer(String msg) {
        this.message = msg;
    }
}
