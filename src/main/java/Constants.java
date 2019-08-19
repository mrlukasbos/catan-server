import java.util.HashMap;
import java.util.Map;

class Constants {
    static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;
    static final int MINIMUM_CARDS_FOR_TRADE = 4;

    // some helpers for the hexagon coordinate system
    static final int[][] AXIAL_DIRECTIONS_ODD = {
            {-1, -1},
            {0, -1},
            {1, 0},
            {0, 1},
            {-1, 1},
            {-1, 0}
    };
    static final int[][] AXIAL_DIRECTIONS_EVEN = {
            {0, -1},
            {1, -1},
            {1, 0},
            {1, 1},
            {0, 1},
            {-1, 0}
    };

    static final Resource[] ALL_RESOURCES = {
            Resource.ORE,
            Resource.GRAIN,
            Resource.STONE,
            Resource.WOOD,
            Resource.WHOOL
    };

    static final Map<Resource, Integer> STREET_COSTS = Map.of(
            Resource.STONE,1,
            Resource.WOOD, 1);

    static final Map<Resource, Integer> VILLAGE_COSTS = Map.of(
            Resource.WHOOL,1,
            Resource.GRAIN, 1,
            Resource.STONE, 1,
            Resource.WOOD, 1);

    static final Map<Resource, Integer> CITY_COSTS = Map.of(
            Resource.ORE,3,
            Resource.GRAIN, 2);

    static final Map<Resource, Integer> DEVELOPMENT_CARD_COSTS = Map.of(
            Resource.WHOOL,1,
            Resource.GRAIN, 1,
            Resource.ORE, 1);

    static final Map<Structure, Map<Resource, Integer>> STRUCTURE_COSTS = Map.of(
            Structure.STREET, STREET_COSTS,
            Structure.SETTLEMENT, VILLAGE_COSTS,
            Structure.CITY, CITY_COSTS
        );


            // RESPONSES

    // Acknowledgement
    static final Response OK = new Response(100, "Ok", "The received message is processed succesfully.");

    // Parsing issues
    static final Response MALFORMEDJSONERROR = new Response(500, "MalformedJSON", "The received JSON message is invalid.");
    static final Response NODEDOESNOTEXISTERROR = new Response(501, "NodeDoesNotExist", "The received node does not exist.");
    static final Response EDGEDOESNOTEXISTERROR = new Response(502, "EdgeDoesNotExist", "The received edge does not exist.");
    static final Response INVALIDSTRUCTUREERROR = new Response(503, "InvalidStructure", "The received structure is invalid.");

    static final Response INVALID_TRADE_ERROR = new Response(504, "InvalidTrade", "An invalid resource was given to the trade command");


    // Rule issues
    static final Response STRUCTURENOTCONNECTEDERROR = new Response(600, "StructureNotConnected", "The received structure is not connected.");
    static final Response CITYNOTBUILTONVILLAGEERROR = new Response(601, "CityNotBuiltOnVillage", "The received city is not built on top of a village.");
    static final Response STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR = new Response(602, "StructureTooClose", "There is another structure too close to another structure.");
    static final Response STRUCTURENOTONLANDERROR = new Response(603, "StructureNotOnLand", "The given structure is not placed on land.");
    static final Response STRUCTUREALREADYEXISTSERROR = new Response(604, "StructureAlreadyExists", "There is already a structure on the given node.");
    static final Response NOTENOUGHRESOURCESERROR = new Response(605, "InsufficientResources", "You have not enough resources to build this.");
    static final Response NOTAVILLAGEANDSTREETERROR = new Response(606, "NotAVillageAndStreet", "In the initial buildphase a command must exist out of a village and a street.");
}
