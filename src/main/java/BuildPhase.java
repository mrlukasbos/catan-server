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
import javafx.util.Pair;

import java.util.ArrayList;

class BuildPhase implements GamePhase {
    Game game;
    String txt;

    BuildPhase(Game game) {
        this.game = game;
        txt = "Please build if you like. \n";
    }

    public Phase getPhaseType() {
        return Phase.BUILDING;
    }

    public Phase execute() {
        build();
        game.goToNextPlayer();
        return Phase.THROW_DICE;
    }

    void build() {
        Player currentPlayer = game.getCurrentPlayer();
        JsonArray jsonArray = getValidCommandFromUser(currentPlayer);

        // build the structures
        buildStructures(currentPlayer, jsonArray);
    }

    // build the structures if the command is valid
    private void buildStructures(Player currentPlayer, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();
            game.getBoard().placeStructure(currentPlayer, structure, key);
        }
    }

    // check for the whole command if the command is valid.
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<BuildCommand> streetCommands = new ArrayList<>();
        ArrayList<BuildCommand> villageCommands = new ArrayList<>();
        ArrayList<BuildCommand> cityCommands = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();

            // validate if data is formatted properly and corresponding objects exist
            if (structure == Structure.STREET) {
                if (!edgeExists(game.getBoard().getEdge(key), key)) return false;
                streetCommands.add(new BuildCommand(currentPlayer, structure, key));
            } else {
                if (!nodeExists(game.getBoard().getNode(key), key)) return false;

                if (structure == Structure.SETTLEMENT) {
                    villageCommands.add(new BuildCommand(currentPlayer, structure, key));
                } else if (structure == Structure.CITY) {
                    cityCommands.add(new BuildCommand(currentPlayer, structure, key));
                }
                game.print("The subcommand has an invalid key: " + key);
                return false;
            }
        }


        // make an array with all the streets for further validation
        ArrayList<Edge> streets = game.getBoard().getStreetsFromPlayer(currentPlayer);
        for (BuildCommand cmd : streetCommands) {
            streets.add(game.getBoard().getEdge((cmd.key)));
        }

        return streetsAreValid(streetCommands)
                && villagesAreValid(villageCommands, streets)
                && citiesAreValid(cityCommands);
    }

    private boolean citiesAreValid(ArrayList<BuildCommand> cityCommands) {
        for (BuildCommand cmd : cityCommands) {
            Node node = game.getBoard().getNode(cmd.key);
            if (!cityWasVillageFirst(cmd.player, node)) return true;
        }
        return false;
    }

    private boolean streetsAreValid(ArrayList<BuildCommand> streetCommands) {
        // validate if the streets are legal
        for (BuildCommand cmd : streetCommands) {
            Edge edge = game.getBoard().getEdge(cmd.key);
            if (!edgeIsFree(cmd.key, edge) || !edgeIsOnTerrain(cmd.key, edge)) return false;
        }

        return streetsAreConnected(streetCommands);
    }

    private boolean villagesAreValid(ArrayList<BuildCommand> villageCommands, ArrayList<Edge> streets) {
        ArrayList<Node> villagesToBuild = new ArrayList<>();
        for (BuildCommand cmd : villageCommands) {
            Node node = game.getBoard().getNode(cmd.key);

            if (nodeIsAvailable(cmd.player, node)
                    && nodeIsConnected(node, streets)
                    && nodeStructureIsAtLeastTwoEdgesFromOtherStructure(villagesToBuild, node)) {
                villagesToBuild.add(node);
            } else {
                return false;
            }
        }
        return true;
    }


    boolean edgeIsOnTerrain(String key, Edge edge) {
        // a street cannot be placed between two tiles of water
        if (!edge.isOnTerrain()) {
            game.print("Received message with illegal street placement: a street cannot be put between two tiles of water " + key);
            return false;
        }
        return true;
    }

    boolean edgeIsFree(String key, Edge edge) {
        if (edge.isRoad()) {
            game.print("Received message with illegal placement: there is already a road on the given edge " + key);
            return false;
        }
        return true;
    }

    boolean edgeExists(Edge edge, String key) {
        if (edge == null) {
            game.print("The given edge does not exist " + key);
            return false;
        }
        return true;
    }

    boolean nodeExists(Node node, String key) {
        if (node == null) {
            game.print("The given node does not exist " + key);
            return false;
        }
        return true;
    }

    private boolean edgeIsConnectedToStreet(Player currentPlayer, Edge edge) {
        boolean hasNeighbouringStreet = false;
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(edge)) {
            for (Edge surroundingEdge : game.getBoard().getSurroundingEdges(surroundingNode)) {
                if (surroundingEdge != null && surroundingEdge != edge && surroundingEdge.isRoad() && surroundingEdge.hasPlayer() && surroundingEdge.getPlayer() == currentPlayer) {
                    hasNeighbouringStreet = true;
                }
            }
        }
        if (!hasNeighbouringStreet) {
            game.print("Received message with illegal street placement: there is no other street to connect with " + edge.getKey());
            return false;
        }
        return true;
    }

    boolean nodeIsAvailable(Player currentPlayer, Node node) {
        if (node.hasPlayer() && node.getPlayer() != currentPlayer) {
            game.print("Received message with illegal location: There is already a structure of another player " + node.getKey());
            return false;
        }
        return true;
    }

    boolean cityWasVillageFirst(Player currentPlayer, Node node) {
        if (node.getPlayer() != currentPlayer || node.getStructure() != Structure.SETTLEMENT) {
            game.print("Received message with illegal city placement: there is no village and/or it is not yours " + node.getKey());
            return false;
        }
        return true;
    }

    boolean nodeStructureIsAtLeastTwoEdgesFromOtherStructure(ArrayList<Node> nodes, Node node) {
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(node)) {
            if (surroundingNode.hasStructure()) {
                game.print("Received message with illegal placement: another structure is too close to you location " + node.getKey());
                return false;
            } else {
                for (Node additionalVillage : nodes) {
                    if (additionalVillage == node) {
                        game.print("Received message with illegal placement: two structures in the same command are too close to each other" + node.getKey());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    boolean nodeIsConnected(Node node, ArrayList<Edge> streets) {
        boolean hasNeighbouringStreet = false;
        for (Edge surroundingEdge : game.getBoard().getSurroundingEdges(node)) {

            for (Edge street : streets) {
                if (surroundingEdge == street) {
                    hasNeighbouringStreet = true;
                }
            }
        }
        if (!hasNeighbouringStreet) {
            game.print("Received message with illegal placement: there is no street to connect with " + node.getKey());
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
    JsonArray getValidCommandFromUser(Player currentPlayer) {
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

    boolean streetsAreConnected(ArrayList<BuildCommand> streetCommands) {
        // make a division between connected streets and unconnected streets
        ArrayList<BuildCommand> connectedStreets = new ArrayList<>();
        ArrayList<BuildCommand> unconnectedStreets = new ArrayList<>();
        for (BuildCommand cmd : streetCommands) {
            Edge edge = game.getBoard().getEdge(cmd.key);
            if (edgeIsConnectedToStreet(cmd.player, edge)) {
                connectedStreets.add(cmd);
            } else {
                unconnectedStreets.add(cmd);
            }
        }


        // for all unconnected streets we must find a connected street
        // when we find one, we add it as 'connected' and remove it from the unconnected list
        while (unconnectedStreets.size() > 0) {

            boolean moreStreetsGotConnected = false;
            for (BuildCommand unconnectedStreetCmd : unconnectedStreets) {
                Edge edge = game.getBoard().getEdge(unconnectedStreetCmd.key);

                for (Node surroundingNode : game.getBoard().getSurroundingNodes(edge)) {
                    for (Edge neighbourEdge : game.getBoard().getSurroundingEdges(surroundingNode)) {
                        if (neighbourEdge != null && neighbourEdge != edge && neighbourEdge.hasPlayer() && neighbourEdge.getPlayer() == unconnectedStreetCmd.player && neighbourEdge.isRoad()) {
                            connectedStreets.add(unconnectedStreetCmd);
                            unconnectedStreets.remove(unconnectedStreetCmd);
                            moreStreetsGotConnected = true;
                        }
                    }

                }
            }
            if (!moreStreetsGotConnected) {
                game.print("the streets don't make a proper connection");
                return false;
            }
        }
        return true;
    }
}

