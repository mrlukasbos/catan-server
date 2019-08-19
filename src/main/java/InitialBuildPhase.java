
import com.google.gson.JsonArray;

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

        // in the initial build phase the first two 'rounds' are quite uncommon.
        // The player with the highest dice throw starts, and then it follows the normal order.
        // After that round all players can build again but in the reversed order
        if (game.getBoard().getAllStructures().size() < game.getPlayers().size()*2) {
            if (game.getBoard().getAllStructures().size() < game.getPlayers().size()) {
                game.goToNextPlayer();
            } else if (game.getBoard().getAllStructures().size() != game.getPlayers().size()) {
                game.goToPreviousPlayer();
            }
            game.signalGameChange();
            return Phase.INITIAL_BUILDING;
        }
        game.signalGameChange();
        return Phase.THROW_DICE;
    }


    @Override
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.SETTLEMENT);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        if (streetCommands == null || villageCommands == null || cityCommands == null) {
            return false;
        }

        if (streetCommands.size() != 1 || villageCommands.size() != 1 || !cityCommands.isEmpty()) {
            game.sendResponse(Constants.NOTAVILLAGEANDSTREETERROR);
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

    // In the initial build phase everything is free
    @Override
    void payStructure(Player p, Structure structure) { }
}