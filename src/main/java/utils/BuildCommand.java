package utils;

import board.Structure;
import game.Player;

public class BuildCommand {
    public Player player;
    public Structure structure;
    public String key;

    public BuildCommand(Player player, Structure structure, String key) {
        this.player = player;
        this.structure = structure;
        this.key = key;
    }
}