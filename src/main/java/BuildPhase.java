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

    ArrayList<BuildCommand> getCommandsFromInput(Player currentPlayer, JsonArray jsonArray, Structure structuresToReturn) {
        ArrayList<BuildCommand> commands = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = game.stringToStructure(structureString);
            String key = object.get("location").getAsString();

            if (structure == Structure.NONE) {
                game.sendResponse(Constants.INVALIDSTRUCTUREERROR);
                return null;
            }

            // validate if data is formatted properly and corresponding objects exist
            if (structure == structuresToReturn) {
                 if (structuresToReturn == Structure.STREET) {
                     if (!edgeExists(game.getBoard().getEdge(key), key)) return null;
                     commands.add(new BuildCommand(currentPlayer, structure, key));
                 } else {
                     if (!nodeExists(game.getBoard().getNode(key), key)) return null;
                     commands.add(new BuildCommand(currentPlayer, structure, key));
                 }
            }
        }

        return commands;
    }


    // check for the whole command if the command is valid.
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.SETTLEMENT);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        if (streetCommands == null || villageCommands == null || cityCommands == null) {
            game.print("There was something illegal about the command");
            return false;
        }

        // make an array with all the streets for further validation
        ArrayList<Edge> streets = game.getBoard().getStreetsFromPlayer(currentPlayer);
        for (BuildCommand cmd : streetCommands) {
            streets.add(game.getBoard().getEdge((cmd.key)));
        }

        // make an array with all the villages for further validation
        ArrayList<Node> villages = game.getBoard().getStructuresFromPlayer(Structure.SETTLEMENT, currentPlayer);
        for (BuildCommand cmd : villageCommands) {
            villages.add(game.getBoard().getNode(cmd.key));
        }


        return streetsAreValid(streetCommands)
                && villagesAreValid(villageCommands, streets)
                && citiesAreValid(cityCommands, villages);
    }

    boolean citiesAreValid(ArrayList<BuildCommand> cityCommands, ArrayList<Node> villages) {
        for (BuildCommand cmd : cityCommands) {
            Node node = game.getBoard().getNode(cmd.key);
            if (!cityWasVillageFirst(node, villages)) return false;
        }
        return true;
    }

    boolean streetsAreValid(ArrayList<BuildCommand> streetCommands) {
        ArrayList<Edge> streetsToBuild = new ArrayList<>();

        // validate if the streets are legal
        for (BuildCommand cmd : streetCommands) {
            Edge edge = game.getBoard().getEdge(cmd.key);
            if (edgeIsFree(streetsToBuild, edge) && !edgeIsOnTerrain(edge)) {
                streetsToBuild.add(edge);
            }
        }

        return streetsAreConnected(streetCommands);
    }

    boolean villagesAreValid(ArrayList<BuildCommand> villageCommands, ArrayList<Edge> streets) {
        ArrayList<Node> villagesToBuild = new ArrayList<>();
        for (BuildCommand cmd : villageCommands) {
            Node node = game.getBoard().getNode(cmd.key);

            if (nodeIsEmpty(villagesToBuild, node)
                    && nodeIsConnected(node, streets)
                    && nodeStructureIsAtLeastTwoEdgesFromOtherStructure(villagesToBuild, node)) {
                villagesToBuild.add(node);
            } else {
                return false;
            }
        }
        return true;
    }


    boolean edgeIsOnTerrain(Edge edge) {
        // a street cannot be placed between two tiles of water
        if (!edge.isOnTerrain()) {
            game.sendResponse(game.getCurrentPlayer(), Constants.STRUCTURENOTONLANDERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    boolean edgeIsFree(ArrayList<Edge> otherEdgesInSameCmd, Edge edge) {
        if (edge.isRoad() || otherEdgesInSameCmd.contains(edge)) {
            game.sendResponse(game.getCurrentPlayer(), Constants.STRUCTUREALREADYEXISTSERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    boolean edgeExists(Edge edge, String key) {
        if (edge == null) {
            game.sendResponse(Constants.EDGEDOESNOTEXISTERROR.withAdditionalInfo(key));
            return false;
        }
        return true;
    }

    boolean nodeExists(Node node, String key) {
        if (node == null) {
            game.sendResponse(Constants.NODEDOESNOTEXISTERROR.withAdditionalInfo(key));
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
            game.sendResponse(Constants.STRUCTURENOTCONNECTEDERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    boolean nodeIsEmpty(ArrayList<Node> villagesFromSameCommand, Node node) {
        if (node.hasPlayer() || node.hasStructure() || villagesFromSameCommand.contains(node)) {
            game.sendResponse(Constants.STRUCTUREALREADYEXISTSERROR.withAdditionalInfo(node.getKey()));
            return false;
        }
        return true;
    }

    boolean cityWasVillageFirst(Node node, ArrayList<Node> villages) {
        if (villages.contains(node)) {
            return true;
        }
        game.sendResponse(Constants.CITYNOTBUILTONVILLAGEERROR.withAdditionalInfo(node.getKey()));
        return false;
    }

    boolean nodeStructureIsAtLeastTwoEdgesFromOtherStructure(ArrayList<Node> nodes, Node node) {
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(node)) {
            if (surroundingNode.hasStructure()) {
                game.sendResponse(Constants.STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR.withAdditionalInfo(node.getKey()));
                return false;
            } else {
                for (Node additionalVillage : nodes) {
                    if (additionalVillage == node) {
                        game.sendResponse(Constants.STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR.withAdditionalInfo("The conflicting structures are in the same command" + node.getKey()));
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
            game.sendResponse(Constants.STRUCTURENOTCONNECTEDERROR.withAdditionalInfo(node.getKey()));
            return false;
        }
        return true;
    }

    // returns a json array if the json is valid, otherwise null
    private JsonArray getJsonIfValid(Player player, String message) {
        if (message == null) return null;

        try {
            JsonParser parser = new JsonParser();
            JsonElement elem = parser.parse(message);
            return elem.getAsJsonArray();
        } catch (Exception e) {
            game.sendResponse(player, Constants.MALFORMEDJSONERROR.withAdditionalInfo(message));
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
            jsonArray = getJsonIfValid(currentPlayer, message);
            buildSucceeded = jsonArray != null && commandIsValid(currentPlayer, jsonArray);
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
                game.sendResponse(Constants.STRUCTURENOTCONNECTEDERROR);
                return false;
            }
        }
        return true;
    }
}

