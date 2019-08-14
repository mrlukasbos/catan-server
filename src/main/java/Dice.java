import java.util.Random;

class Dice {
    private int amountOfDice = 1;

    Dice(int amountOfDice) {
        this.amountOfDice = amountOfDice;
    }

    // return the sum of dice throws
    int throwDice() {
        Random r = new Random();
        int output = 0;
        for (int i = 0; i < amountOfDice; i++) {
            output += Math.abs(r.nextInt() % 6) + 1;
        }
        return output;
    }
}


