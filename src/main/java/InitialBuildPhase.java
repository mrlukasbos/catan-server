import com.google.gson.JsonArray;
import java.util.ArrayList;

public class InitialBuildPhase extends BuildPhase {

    InitialBuildPhase(Game game) {
        super(game);
        request = Constants.INITIAL_BUILD_REQUEST;
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
        changePlayer();
        game.signalGameChange();
        return getNextPhase();
    }

    boolean ShouldProceedToNextPhase() {
        return game.getBoard().getAllStructures().size() >= game.getPlayers().size()*2;
    }

    Phase getNextPhase() {
        if (!ShouldProceedToNextPhase()) {
            return Phase.INITIAL_BUILDING;
        }
        return Phase.THROW_DICE;
    }

    void changePlayer() {
        if (!ShouldProceedToNextPhase()) {
            if (game.getBoard().getAllStructures().size() < game.getPlayers().size()) {
                game.goToNextPlayer();
            } else if (game.getBoard().getAllStructures().size() != game.getPlayers().size()) {
                game.goToPreviousPlayer();
            }
        }
    }


    @Override
    boolean commandIsValid(Player currentPlayer, JsonArray jsonArray) {
        if (jsonArray == null) {
            game.sendResponse(Constants.MALFORMED_JSON_ERROR.withAdditionalInfo("jsonArray is null"));
            return false;
        }

        ArrayList<BuildCommand> developmentCardCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.DEVELOPMENT_CARD);
        ArrayList<BuildCommand> streetCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.STREET);
        ArrayList<BuildCommand> villageCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.VILLAGE);
        ArrayList<BuildCommand> cityCommands = getCommandsFromInput(currentPlayer, jsonArray, Structure.CITY);

        if (streetCommands == null || villageCommands == null || cityCommands == null || developmentCardCommands == null) { return false; }


        if (streetCommands.size() != 1 || villageCommands.size() != 1 || !cityCommands.isEmpty() || !developmentCardCommands.isEmpty()) {
            game.sendResponse(Constants.NOT_A_VILLAGE_AND_STREET_ERROR);
            return false;
        }

        // make an array with all the streets for further validation
        ArrayList<Edge> streets = game.getBoard().getStreetsFromPlayer(currentPlayer);
        for (BuildCommand cmd : streetCommands) {
            streets.add(game.getBoard().getEdge((cmd.key)));
        }

        return streetsAreValid(streetCommands) && villagesAreValid(villageCommands, streets);
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