class BuildCommand {
    Player player;
    Structure structure;
    String key;

    BuildCommand(Player player, Structure structure, String key) {
        this.player = player;
        this.structure = structure;
        this.key = key;
    }
}