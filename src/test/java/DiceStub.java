import java.util.ArrayList;

class DiceStub extends Dice {
    private int value = 8;
    private ArrayList<Integer> values = new ArrayList<>();
    boolean throwsMultipleValues = false;

    DiceStub() {
        super(2);
    }

    @Override
    int throwDice() {
        if (throwsMultipleValues) {
            int value = values.get(0);
            values.remove(0);
            return value;
        }
        return value;
    }

    void shouldThrow(int value) {
        this.throwsMultipleValues = false;
        this.value = value;
    }

    void shouldThrowInOrder(ArrayList<Integer> values) {
        this.throwsMultipleValues = true;
        this.values = values;
    }
}