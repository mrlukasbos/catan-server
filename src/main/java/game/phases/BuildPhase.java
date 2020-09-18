package game.phases;

import board.Edge;
import board.Node;
import board.Structure;
import com.google.gson.*;
import utils.*;
import game.*;

import java.util.ArrayList;
import java.util.HashMap;

public class BuildPhase implements GamePhase {
    Game game;
    Response request;

    // structure of the messages
    HashMap<String, ValidationType> props = new HashMap<>() {{
        put("structure", ValidationType.STRUCTURE);
        put("location", ValidationType.EDGE_OR_NODE_KEYS);
    }};

    HashMap<String, ValidationType> propsDevelopmentCard = new HashMap<>() {{
        put("structure", ValidationType.DEVELOPMENT_CARD);
    }};

    public BuildPhase(Game game) {
        this.game = game;
        request = Constants.BUILD_REQUEST;
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

        JsonArray jsonArray;
        int amountOfFailures = 0;

        do {
            jsonArray = getCommandFromUser(currentPlayer);

            if (amountOfFailures > 2) {
                game.sendResponse(currentPlayer, Constants.TOO_MUCH_FAILURES.withAdditionalInfo("You failed to build"));
                return;
            }
            amountOfFailures++;
        } while (game.isRunning() && (jsonArray == null || !commandIsValid(currentPlayer, jsonArray)));

        if (jsonArray == null) return;

        // build the structures
        buildStructures(currentPlayer, jsonArray);
        game.sendResponse(currentPlayer, Constants.OK.withAdditionalInfo("Message processed succesfully!"));
    }

    JsonArray getJsonIfValid(String message) {
        // Try if the message fits a structure with a location
        JsonArray structureArray = jsonValidator.getJsonArrayIfCorrect(message, props, game.getBoard());
        if (structureArray == null) {
            // otherwise, it may be a development card (which has no location)
            return jsonValidator.getJsonArrayIfCorrect(message, propsDevelopmentCard, game.getBoard());
        }
        return structureArray;
    }

    // build the structures if the command is valid
    public void buildStructures(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<Structure> builtStructures = new ArrayList<>();

        ArrayList<BuildCommand> developmentCardRequests = getCommandsFromInput(currentPlayer, jsonArray, Structure.DEVELOPMENT_CARD);
        for (int i = 0; i < developmentCardRequests.size(); i++) {
            payStructure(currentPlayer, Structure.DEVELOPMENT_CARD);
            game.getBoard().giveDevelopmentCardToPlayer(currentPlayer);
            builtStructures.add(Structure.DEVELOPMENT_CARD);
        }

        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.VILLAGE);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        ArrayList<Structure> builtStreets = buildStructures(currentPlayer, streetCommands);
        ArrayList<Structure> builtVillages = buildStructures(currentPlayer, villageCommands);
        ArrayList<Structure> builtCities = buildStructures(currentPlayer, cityCommands);

        // for showing what we built in the events;
        builtStructures.addAll(builtStreets);
        builtStructures.addAll(builtVillages);
        builtStructures.addAll(builtCities);
        game.addEvent(new Event(game, EventType.BUILD, currentPlayer).withStructures(builtStructures));
    }

    private ArrayList<Structure> buildStructures(Player currentPlayer, ArrayList<BuildCommand> streetCommands) {
        ArrayList<Structure> builtStructures = new ArrayList<>();
        for (BuildCommand streetCmd : streetCommands) {
            payStructure(currentPlayer, streetCmd.structure);
            game.getBoard().placeStructure(currentPlayer, streetCmd.structure, streetCmd.key);
            builtStructures.add(streetCmd.structure);
        }
        return builtStructures;
    }

    ArrayList<BuildCommand> getCommandsFromInput(Player currentPlayer, JsonArray jsonArray, Structure structuresToReturn) {
        ArrayList<BuildCommand> commands = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();
            Structure structure = Helpers.getStructureByName(object.get("structure").getAsString());

            // validate if data is formatted properly and corresponding objects exist
            if (structure == structuresToReturn) {
                 if (structuresToReturn == Structure.STREET) {
                     String key = object.get("location").getAsString();
                     commands.add(new BuildCommand(currentPlayer, structure, key));
                 } else if (structuresToReturn == Structure.DEVELOPMENT_CARD) {
                     commands.add(new BuildCommand(currentPlayer, structure, null));
                 } else {
                     String key = object.get("location").getAsString();
                     commands.add(new BuildCommand(currentPlayer, structure, key));
                 }
            }
        }
        return commands;
    }

    // check for the whole command if the command is valid.
    public boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<BuildCommand> developmentCardRequests = getCommandsFromInput(currentPlayer, jsonArray, Structure.DEVELOPMENT_CARD);
        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.VILLAGE);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        if (developmentCardRequests == null || streetCommands == null || villageCommands == null || cityCommands == null) { return false; }

        // make an array with all the streets for further validation
        ArrayList<Edge> streets = game.getBoard().getStreetsFromPlayer(currentPlayer);
        for (BuildCommand cmd : streetCommands) {
            streets.add(game.getBoard().getEdge((cmd.key)));
        }

        // make an array with all the villages for further validation
        ArrayList<Node> villages = game.getBoard().getStructuresFromPlayer(Structure.VILLAGE, currentPlayer);
        for (BuildCommand cmd : villageCommands) {
            villages.add(game.getBoard().getNode(cmd.key));
        }

        return hasEnoughResources(currentPlayer, developmentCardRequests.size(), streetCommands.size(), villageCommands.size(), cityCommands.size())
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
            if (edgeIsFree(streetsToBuild, edge)) {
                streetsToBuild.add(edge);
            } else {
                return false;
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

    private boolean edgeIsFree(ArrayList<Edge> otherEdgesInSameCmd, Edge edge) {
        if (edge.isRoad() || otherEdgesInSameCmd.contains(edge)) {
            game.sendResponse(game.getCurrentPlayer(), Constants.STRUCTURE_ALREADY_EXISTS_ERROR.withAdditionalInfo(edge.getKey()));
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
            game.sendResponse(Constants.STRUCTURE_NOT_CONNECTED_ERROR.withAdditionalInfo(edge.getKey()));
            return false;
        }
        return true;
    }

    private boolean nodeIsEmpty(ArrayList<Node> villagesFromSameCommand, Node node) {
        if (node.hasPlayer() || node.hasStructure() || villagesFromSameCommand.contains(node)) {
            game.sendResponse(Constants.STRUCTURE_ALREADY_EXISTS_ERROR.withAdditionalInfo(node.getKey()));
            return false;
        }
        return true;
    }

    private boolean cityWasVillageFirst(Node node, ArrayList<Node> villages) {
        if (villages.contains(node)) {
            return true;
        }
        game.sendResponse(Constants.CITY_NOT_BUILT_ON_VILLAGE_ERROR.withAdditionalInfo(node.getKey()));
        return false;
    }

    private boolean nodeStructureIsAtLeastTwoEdgesFromOtherStructure(ArrayList<Node> nodes, Node node) {
        for (Node surroundingNode : game.getBoard().getSurroundingNodes(node)) {
            if (surroundingNode.hasStructure()) {
                game.sendResponse(Constants.STRUCTURE_TOO_CLOSE_TO_OTHER_STRUCTURE_ERROR.withAdditionalInfo(node.getKey()));
                return false;
            } else {
                for (Node additionalVillage : nodes) {
                    if (additionalVillage == surroundingNode) {
                        game.sendResponse(Constants.STRUCTURE_TOO_CLOSE_TO_OTHER_STRUCTURE_ERROR.withAdditionalInfo("The conflicting structures are in the same command" + node.getKey()));
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
                    break;
                }
            }
        }
        if (!hasNeighbouringStreet) {
            game.sendResponse(Constants.STRUCTURE_NOT_CONNECTED_ERROR.withAdditionalInfo(node.getKey()));
            return false;
        }
        return true;
    }

    // keep running this function until we get valid output from the user
    private JsonArray getCommandFromUser(Player currentPlayer) {
        currentPlayer.send(request.toString());
        JsonArray jsonArray;

        String message = currentPlayer.listen();
        game.print("Received message from player " + currentPlayer.getName() + ": " + message);
        jsonArray = getJsonIfValid(message);
        if (jsonArray == null) {
            game.sendResponse(currentPlayer, Constants.MALFORMED_JSON_ERROR.withAdditionalInfo(message));
            return null;
        }

        return jsonArray;
    }

    boolean streetsAreConnected(ArrayList<BuildCommand> streetCommands) {
        // make a division between connected streets and unconnected streets
        ArrayList<BuildCommand> unconnectedStreets = new ArrayList<>();
        ArrayList<BuildCommand> connectedStreets = new ArrayList<>();

        for (BuildCommand cmd : streetCommands) {
            Edge edge = game.getBoard().getEdge(cmd.key);
            if (!edgeIsConnectedToStreet(cmd.player, edge)) {
                unconnectedStreets.add(cmd);
            } else {
                connectedStreets.add(cmd);
            }
        }


        // for all unconnected streets we must find a connected street
        // when we find one, we add it as 'connected' and remove it from the unconnected list
        while (connectedStreets.size() < streetCommands.size()) {

            boolean moreStreetsGotConnected = false;
            for (BuildCommand unconnectedStreetCmd : unconnectedStreets) {
                Edge edge = game.getBoard().getEdge(unconnectedStreetCmd.key);

                for (Node surroundingNode : game.getBoard().getSurroundingNodes(edge)) {
                    for (Edge neighbourEdge : game.getBoard().getSurroundingEdges(surroundingNode)) {
                        boolean connectedWithOtherConnectedEdge = false;
                        for (BuildCommand connectedCmd : connectedStreets) {
                            if (connectedCmd.key.equals(neighbourEdge.getKey())) connectedWithOtherConnectedEdge = true;
                        }

                        if (connectedWithOtherConnectedEdge) {
                            connectedStreets.add(unconnectedStreetCmd);
                            moreStreetsGotConnected = true;
                        }
                    }

                }
            }

            if (!moreStreetsGotConnected) {
                game.sendResponse(Constants.STRUCTURE_NOT_CONNECTED_ERROR);
                return false;
            }
        }
        return true;
    }

    private boolean hasEnoughResources(Player player, int amountOfDevelopmentCards, int amountOfStreets, int amountOfVillages, int amountOfCities) {
        for (Resource resource : Constants.ALL_RESOURCES) {
            int amountNeeded = Constants.STREET_COSTS.getOrDefault(resource, 0) * amountOfStreets;
            amountNeeded += Constants.VILLAGE_COSTS.getOrDefault(resource, 0) * amountOfVillages;
            amountNeeded += Constants.CITY_COSTS.getOrDefault(resource, 0) * amountOfCities;
            amountNeeded += Constants.DEVELOPMENT_CARD_COSTS.getOrDefault(resource, 0) * amountOfDevelopmentCards;

            if (amountNeeded > player.countResources(resource)) {
                game.sendResponse(Constants.INSUFFICIENT_RESOURCES_ERROR.withAdditionalInfo(" Not enough " + resource.toString()));
                return false;
            }
        }
        return true;
    }

    public void payStructure(Player p, Structure structure) {
        p.pay(structure);
    }
}

