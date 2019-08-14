import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class BuildPhase implements GamePhase {
    Game game;

    BuildPhase(Game game) {
        this.game = game;
    }

    public Phase execute() {
        build();
        return Phase.END_TURN;
    }



    // Example inputs
    // [] (build nothing)
    // [{ "structure": "village", "location": "([1,2],[2,1],[2,2])" }]
    // [{ "structure": "city", "location": "([1,2],[2,1],[2,2])" }]

    // [{ "structure": "village", "location": "([2,2],[3,1],[3,2])" }]
    // [{ "structure": "city", "location": "([2,2],[3,1],[3,2])" }]

    // [{ "structure": "street", "location": "([2,2],[3,1])" }]
    // [{ "structure": "street", "location": "([3,1],[3,2])" }, { "structure": "street", "location": "([2,2],[3,2])" }]
    private void build() {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.send("Please build if you like.");

        String message = currentPlayer.listen();
        if (message != null) { // the message is ready
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);

            JsonArray jsonArray = new JsonParser().parse(message).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();

                String structureString = object.get("structure").getAsString();
                Structure structure = game.stringToStructure(structureString);
                String key = object.get("location").getAsString();

                if (!isLegal(key)) {
                    game.print("Received message with illegal location (key): " + key);
                    break;
                }

                if (!isLegal(structure)) {
                    game.print("Received message with illegal structure: " + structureString);
                    break;
                }

                game.getBoard().placeStructure(currentPlayer, structure, key);
            }
        }
    }

    private boolean isLegal(String key) {
        return game.getBoard().hasEdge(key) || game.getBoard().hasNode(key);
    }

    private boolean isLegal(Structure struct) {
        return struct != Structure.NONE;
    }
}

