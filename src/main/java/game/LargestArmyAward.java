package game;

public class LargestArmyAward {
    private Player player;
    private int amountOfKnights;

    LargestArmyAward(){}

    public void setPlayer(Player player, int amountOfKnights) {
        this.player = player;
        this.amountOfKnights = amountOfKnights;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAmountOfKnights() {
        return amountOfKnights;
    }
}
