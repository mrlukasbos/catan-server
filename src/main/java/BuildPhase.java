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
    boolean freeBuilding = false;

    BuildPhase(Game game, boolean freeBuilding) {
        this.game = game;
        this.freeBuilding = freeBuilding;
    }

    public Phase getPhaseType() {
        if (freeBuilding) {
            return Phase.FREE_BUILDING;
        }
        return Phase.BUILDING;
    }

    public Phase execute() {
        build();
        game.goToNextPlayer();

        // all players should go through the free building phase first
        if (freeBuilding && game.getBoard().getStructures().size() < game.getPlayers().size()) {
            return Phase.FREE_BUILDING;
        }
        return Phase.THROW_DICE;
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

    // check for the whole command if the command is valid.
    private boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();

            Node node = game.getBoard().getNode(key);
            Edge edge = game.getBoard().getEdge(key);

            // building villages/cities
            if (node != null) {
                if (!structureIsVillageOrCity(structure)) return false;
                if (!nodeIsAvailable(currentPlayer, key, node)) return false;
                if (!cityWasVillageFirst(currentPlayer, structure, key, node)) return false;
                if (!nodeStructureIsAtLeastTwoEdgesFromOtherStructure(key, node)) return false;

                // rule does not apply when building free
                if (!freeBuilding) {
                    if (!nodeIsConnectedToStreet(currentPlayer, key, node, edge)) return false;
                }
            }

            // building streets
            else if (edge != null) {
                if (!structureIsStreet(structure)) return false;
                if (!edgeIsFree(key, edge)) return false;
                if (!edgeIsOnTerrain(key, edge)) return false;

                // rule does not apply when building free
                if (!freeBuilding) {
                    if (!edgeIsConnectedToStreet(currentPlayer, key, edge)) return false;
                }
            } else {
                game.print("Received message with invalid key: " + key);
                return false;
            }
            game.print("at least up to this part the command is valid " + key);
        }
        return true;
    }

    private boolean edgeIsOnTerrain(String key, Edge edge) {
        // a street cannot be placed between two tiles of water
        if (!edge.isOnTerrain()) {
            game.print("Received message with illegal street placement: a street cannot be put between two tiles of water " + key);
            return true;
        }
        return false;
    }

    private boolean edgeIsFree(String key, Edge edge) {
        if (edge.isRoad()) {
            game.print("Received message with illegal placement: there is already a road on the given edge " + key);
            return true;
        }
        return false;
    }

    private boolean edgeIsConnectedToStreet(Player currentPlayer, String key, Edge edge) {
        boolean hasNeighbouringStreet = false;
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(edge)) {
            for (Edge surroundingEdge : game.getBoard().getSurroundingEdges(surroundingNode)) {
                if (surroundingEdge != null && surroundingEdge != edge && surroundingEdge.isRoad() && surroundingEdge.hasPlayer() && surroundingEdge.getPlayer() == currentPlayer) {
                    hasNeighbouringStreet = true;
                }
            }
        }
        if (!hasNeighbouringStreet) {
            game.print("Received message with illegal street placement: there is no other street to connect with " + key);
            return false;
        }
        return true;
    }

    private boolean structureIsVillageOrCity(Structure structure) {
        return structure == Structure.SETTLEMENT || structure == Structure.CITY;
    }

    private boolean structureIsStreet(Structure structure) {
        return structure == Structure.STREET;
    }

    private boolean nodeIsAvailable(Player currentPlayer, String key, Node node) {
        if (node.hasPlayer() && node.getPlayer() != currentPlayer) {
            game.print("Received message with illegal location: There is already a structure of another player " + key);
            return false;
        }
        return true;
    }

    private boolean cityWasVillageFirst(Player currentPlayer, Structure structure, String key, Node node) {
        if (structure == Structure.CITY && (node.getPlayer() != currentPlayer || node.getStructure() != Structure.SETTLEMENT)) {
            game.print("Received message with illegal city placement: there is no village and/or it is not yours " + key);
            return false;
        }
        return true;
    }

    private boolean nodeStructureIsAtLeastTwoEdgesFromOtherStructure(String key, Node node) {
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(node)) {
            if (surroundingNode.hasStructure()) {
                game.print("Received message with illegal placement: another structure is too close to you location " + key);
                return false;
            }
        }
        return true;
    }

    private boolean nodeIsConnectedToStreet(Player currentPlayer, String key, Node node, Edge edge) {
        boolean hasNeighbouringStreet = false;
        for (Edge surroundingEdge : game.getBoard().getSurroundingEdges(node)) {
            if (surroundingEdge != edge && surroundingEdge.isRoad() && surroundingEdge.getPlayer() == currentPlayer) {
                hasNeighbouringStreet = true;
            }
        }
        if (!hasNeighbouringStreet) {
            game.print("Received message with illegal placement: there is no street to connect with " + key);
            return false;
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

        String txt = "Please build if you like";
        if (freeBuilding) {
            txt += ", it's free! \n";
        } else {
            txt += ".\n";
        }
        currentPlayer.send(txt);
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

