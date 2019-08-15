/* Example inputs

 LEGAL

 (A street and a village)
 [{ "structure": "street", "location": "([3,1],[3,2])" }, { "structure": "village", "location": "([2,2],[3,1],[3,2])" }]
 [{ "structure": "street", "location": "([3,4],[4,3])" }, { "structure": "village", "location": "([3,3],[3,4],[4,3])" }]

 (A village and a street)
 [{"structure": "village", "location": "([1,2],[1,3],[2,3])" }, { "structure": "street", "location": "([1,2],[1,3])" }]
 [{"structure": "village", "location": "([1,2],[2,1],[2,2])" }, { "structure": "street", "location": "([1,2],[2,2])" }]


 ILLEGAL

 (build nothing)
 []

 (only a village, city or street)
 [{ "structure": "village", "location": "([1,2],[2,1],[2,2])" }]
 [{ "structure": "city", "location": "([1,2],[2,1],[2,2])" }]
 [{ "structure": "street", "location": "([2,2],[3,1])" }]

 (a city and a street)
 [{ "structure": "city", "location": "([1,2],[2,1],[2,2])" }, { "structure": "street", "location": "([2,2],[3,1])" }]

 (a village and street that are not connected)
 [{ "structure": "village", "location": "([1,2],[2,1],[2,2])" }, { "structure": "street", "location": "([2,2],[3,1])" }]

 (two streets)
 [{ "structure": "street", "location": "([3,1],[3,2])" }, { "structure": "street", "location": "([2,2],[3,2])" }]
*/

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InitialBuildPhase extends BuildPhase {

    InitialBuildPhase(Game game) {
        super(game);
        txt = "Please build a settlement and a street. \n";
    }

    @Override
    public Phase getPhaseType() {
        return Phase.INITIAL_BUILDING;
    }

    @Override
    public Phase execute() {
        build();
        game.goToNextPlayer();

        if (game.getBoard().getStructures().size() < game.getPlayers().size()) {
            return Phase.INITIAL_BUILDING;
        }
        return Phase.THROW_DICE;
    }

    @Override
    protected boolean nodeSubcommandIsValid(Player currentPlayer, Structure structure, String key, Node node) {
        return nodeExists(node, key)
                && structureIsVillageOrCity(structure)
                && nodeIsAvailable(currentPlayer, key, node)
                && cityWasVillageFirst(currentPlayer, structure, key, node)
                && nodeStructureIsAtLeastTwoEdgesFromOtherStructure(key, node);
    }

    @Override
    protected boolean edgeSubcommandIsValid(Player currentPlayer, Structure structure, String key, Edge edge) {
        return edgeExists(edge, key)
                && structureIsStreet(structure, key)
                && edgeIsFree(key, edge)
                && edgeIsOnTerrain(key, edge);
    }

    @Override
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {

        if (jsonArray.size() != 2) {
            game.print("command invalid, there are no 2 elements in the array");
            return false;
        }

        JsonObject object1 = jsonArray.get(0).getAsJsonObject();
        String structureString1 = object1.get("structure").getAsString();
        Structure structure1 = game.stringToStructure(structureString1);
        String key1 = object1.get("location").getAsString();

        JsonObject object2 = jsonArray.get(1).getAsJsonObject();
        String structureString2 = object2.get("structure").getAsString();
        Structure structure2 = game.stringToStructure(structureString2);
        String key2 = object2.get("location").getAsString();

        // first place the street, then we try to connect the village to it
        if (structure1 == Structure.STREET) {
            game.print("the first item is a street");

            return commandHasValidStreetAndVillage(currentPlayer, key1, structure1, key2, structure2);
        } else if (structure2 == Structure.STREET) {
            game.print("the second item is a street");

            return commandHasValidStreetAndVillage(currentPlayer, key2, structure2, key1, structure1);
        }
        game.print("Received message does not contain a street");
        return false;
    }

    /*
    The first settlement has to be a village and road connected to each other
     */
    private boolean commandHasValidStreetAndVillage(Player currentPlayer, String streetKey, Structure structure1, String villageKey, Structure structure2) {
        // validate the street
        Edge edge = game.getBoard().getEdge(streetKey);
        if (!edgeSubcommandIsValid(currentPlayer, structure1, streetKey, edge)) return false;
        game.print("The street is valid " + streetKey);

        // validate structure 2
        Node node = game.getBoard().getNode(villageKey);
        if (!nodeSubcommandIsValid(currentPlayer, structure2, villageKey, node)) return false;

        // The village must be connected to the street!
        if (!nodeIsConnectedToStreet(villageKey, node, edge)) return false;
        game.print("The village is valid " + streetKey);
        return true;
    }

    boolean nodeIsConnectedToStreet(String key, Node node, Edge street) {
        boolean hasNeighbouringStreet = false;
        for (Edge surroundingEdge : game.getBoard().getSurroundingEdges(node)) {
            if (surroundingEdge != null && surroundingEdge.getKey().equals(street.getKey())) {
                hasNeighbouringStreet = true;
            }
        }
        if (!hasNeighbouringStreet) {
            game.print("Received message with illegal placement: there is no street to connect with " + key);
            return false;
        }
        return true;
    }
}
