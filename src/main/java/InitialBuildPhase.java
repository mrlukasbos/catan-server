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

import java.util.ArrayList;

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

        if (game.getBoard().getAllStructures().size() < game.getPlayers().size()) {
            return Phase.INITIAL_BUILDING;
        }
        return Phase.THROW_DICE;
    }


    @Override
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.SETTLEMENT);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        if (streetCommands == null || villageCommands == null || cityCommands == null) {
            game.print("There was something illegal about the command");
            return false;
        }

        if (streetCommands.size() != 1 || villageCommands.size() != 1 || !cityCommands.isEmpty()) {
            game.print("command invalid, the command does not exist out of a street and village");
            return false;
        }

        // make an array with all the streets for further validation
        ArrayList<Edge> streets = game.getBoard().getStreetsFromPlayer(currentPlayer);
        for (BuildCommand cmd : streetCommands) {
            streets.add(game.getBoard().getEdge((cmd.key)));
        }

        return streetsAreValid(streetCommands)
                && villagesAreValid(villageCommands, streets);
    }

    // in the initial building phase the streets don't need to be connected
    @Override
    boolean streetsAreConnected(ArrayList<BuildCommand> streetCommands) {
        return true;
    }
}