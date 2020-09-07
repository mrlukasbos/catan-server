class PlayerStub extends Player {

    PlayerStub(Game game, int id, String name) {
        super(null, game, id, name);
    }

    @Override
    void send(String str) {
        // do nothing
    }
}
