// Example inputs
// [] (build nothing)
// [{ "structure": "village", "location": "([1,2],[2,1],[2,2])" }]
// [{ "structure": "city", "location": "([1,2],[2,1],[2,2])" }]

// [{ "structure": "village", "location": "([2,2],[3,1],[3,2])" }]
// [{ "structure": "city", "location": "([2,2],[3,1],[3,2])" }]

// [{ "structure": "street", "location": "([2,2],[3,1])" }]
// [{ "structure": "street", "location": "([3,1],[3,2])" }, { "structure": "street", "location": "([2,2],[3,2])" }]

// illegal input: [{ "structure": "village", "location": "([2,2],[3,0],[3,2])" }]

import com.google.gson.*;

class BuildPhase implements GamePhase {
    Game game;

    BuildPhase(Game game) {
        this.game = game;
    }

    public Phase getPhaseType() {
        return Phase.BUILDING;
    }

    public Phase execute() {
        build();
        return Phase.END_TURN;
    }

    private void build() {
        Player currentPlayer = game.getCurrentPlayer();
        JsonArray jsonArray = getValidCommandFromUser(currentPlayer);

        // build the structures
        buildStructures(currentPlayer, jsonArray);
    }

    // build the structures if the json is valid
    private void buildStructures(Player currentPlayer, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();
            game.getBoard().placeStructure(currentPlayer, structure, key);
        }
    }

    private boolean isLegal(String key) {
        return game.getBoard().hasEdge(key) || game.getBoard().hasNode(key);
    }

    private boolean isLegal(Structure struct) {
        return struct != Structure.NONE;
    }

    // check for the whole command if the command is valid.
    private boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();

            // check if the location (key) of the node is valid (the node exists, we do not check if there is another node too close)
            if (!isLegal(key)) {
                game.print("Received message with invalid key: " + key);
                return false;
            }

            // check if the given structure is valid
            if (!isLegal(structure)) {
                game.print("Received message with invalid structure: " + structureString);
                return false;
            }

            // check if there is not already a structure of another player
            Node node = game.getBoard().getNode(key);
            if (node.hasPlayer() && node.getPlayer() != currentPlayer) {
                game.print("Received message with illegal location: There is already a structure of another player " + key);
                return false;
            }

            // if it is a city, check if it was a village from the same player before
            if (structure == Structure.CITY && (node.getPlayer() != currentPlayer || node.getStructure() != Structure.SETTLEMENT)) {
                game.print("Received message with illegal city placement: there is no village and/or it is not yours" + key);
                return false;
            }

        }
        return true;
    }

    // returns a json array if the json is valid, otherwise null
    private JsonArray getJsonIfValid(String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonArray();
        } catch (Exception e) {
            game.print("Message could not be interpreted as JSON: \n" + message);
            return null;
        }
    }

    // keep running this function until we get valid output from the user
    private JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send("Please build if you like.");
        boolean buildSucceeded = false;
        JsonArray jsonArray = null;
        while (!buildSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = getJsonIfValid(message);
            buildSucceeded = jsonArray != getJsonIfValid(message) && commandIsValid(currentPlayer, jsonArray);
        }
        return jsonArray;
    }
}

