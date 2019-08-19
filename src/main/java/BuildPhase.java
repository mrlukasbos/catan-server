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
        if (game.getCurrentPlayer().canBuildSomething()) {
            build();
        } else {
            game.addEvent(new Event(game, EventType.GENERAL, game.getCurrentPlayer()).withGeneralMessage(" can't build"));
        }
        game.goToNextPlayer();

        game.signalGameChange();
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
        ArrayList<Structure> structuresToBuild = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = Game.stringToStructure(structureString);
            String key = object.get("location").getAsString();

            payStructure(currentPlayer, structure);
            game.getBoard().placeStructure(currentPlayer, structure, key);

            structuresToBuild.add(structure);
        }

        game.addEvent(new Event(game, EventType.BUILD, currentPlayer).withStructures(structuresToBuild));
    }

    ArrayList<BuildCommand> getCommandsFromInput(Player currentPlayer, JsonArray jsonArray, Structure structuresToReturn) {
        ArrayList<BuildCommand> commands = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            String structureString = object.get("structure").getAsString();
            Structure structure = Game.stringToStructure(structureString);
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


        return hasEnoughResources(currentPlayer, streetCommands.size(), villageCommands.size(), cityCommands.size())
                && streetsAreValid(streetCommands)
                && villagesAreValid(villageCommands, streets)
                && citiesAreValid(cityCommands, villages);
    }

    private boolean citiesAreValid(ArrayList<BuildCommand> cityCommands, ArrayList<Node> villages) {
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


    private boolean edgeIsOnTerrain(Edge edge) {
        // a street cannot be placed between two tiles of water
        if (!edge.isOnTerrain()) {
            game.sendResponse(game.getCurrentPlayer(), Constants.STRUCTURENOTONLANDERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    private boolean edgeIsFree(ArrayList<Edge> otherEdgesInSameCmd, Edge edge) {
        if (edge.isRoad() || otherEdgesInSameCmd.contains(edge)) {
            game.sendResponse(game.getCurrentPlayer(), Constants.STRUCTUREALREADYEXISTSERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    private boolean edgeExists(Edge edge, String key) {
        if (edge == null) {
            game.sendResponse(Constants.EDGEDOESNOTEXISTERROR.withAdditionalInfo(key));
            return false;
        }
        return true;
    }

    private boolean nodeExists(Node node, String key) {
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

    private boolean nodeIsEmpty(ArrayList<Node> villagesFromSameCommand, Node node) {
        if (node.hasPlayer() || node.hasStructure() || villagesFromSameCommand.contains(node)) {
            game.sendResponse(Constants.STRUCTUREALREADYEXISTSERROR.withAdditionalInfo(node.getKey()));
            return false;
        }
        return true;
    }

    private boolean cityWasVillageFirst(Node node, ArrayList<Node> villages) {
        if (villages.contains(node)) {
            return true;
        }
        game.sendResponse(Constants.CITYNOTBUILTONVILLAGEERROR.withAdditionalInfo(node.getKey()));
        return false;
    }

    private boolean nodeStructureIsAtLeastTwoEdgesFromOtherStructure(ArrayList<Node> nodes, Node node) {
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

    private boolean nodeIsConnected(Node node, ArrayList<Edge> streets) {
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

    // keep running this function until we get valid output from the user
    private JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send(txt);
        boolean buildSucceeded = false;
        JsonArray jsonArray = null;

        while (!buildSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = new jsonValidator().getJsonIfValid(currentPlayer, message);
            if (jsonArray == null) game.sendResponse(currentPlayer, Constants.MALFORMEDJSONERROR.withAdditionalInfo(message));
            buildSucceeded = jsonArray != null && commandIsValid(currentPlayer, jsonArray);

            if (!buildSucceeded) {
                currentPlayer.send("try again! \n");
            }
        }
        return jsonArray;
    }

    boolean streetsAreConnected(ArrayList<BuildCommand> streetCommands) {
        // make a division between connected streets and unconnected streets
        ArrayList<BuildCommand> unconnectedStreets = new ArrayList<>();
        for (BuildCommand cmd : streetCommands) {
            Edge edge = game.getBoard().getEdge(cmd.key);
            if (!edgeIsConnectedToStreet(cmd.player, edge)) {
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

    private boolean hasEnoughResources(Player player, int amountOfStreets, int amountOfVillages, int amountOfCities) {
        for (Resource resource : Constants.ALL_RESOURCES) {
            int amountNeeded = Constants.STREET_COSTS.getOrDefault(resource, 0) * amountOfStreets;
            amountNeeded += Constants.VILLAGE_COSTS.getOrDefault(resource, 0) * amountOfVillages;
            amountNeeded += Constants.CITY_COSTS.getOrDefault(resource, 0) * amountOfCities;

            if (amountNeeded > player.countResources(resource)) {
                game.sendResponse(Constants.NOTENOUGHRESOURCESERROR.withAdditionalInfo(" Not enough " + Player.resourceToString(resource)));
                return false;
            }
        }
        return true;
    }

    void payStructure(Player p, Structure structure) {
        p.pay(structure);
    }
}

