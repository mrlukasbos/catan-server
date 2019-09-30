import java.util.Map;

class Constants {
    static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;
    static final int MINIMUM_CARDS_FOR_TRADE = 4;
    static final int VICTORY_POINTS_TO_WIN = 10;

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
            Resource.WOOL
    };

    static final Structure[] ALL_STRUCTURES = {
            Structure.STREET,
            Structure.VILLAGE,
            Structure.CITY,
            Structure.DEVELOPMENT_CARD
    };

    static final DevelopmentCard[] ALL_DEVELOPMENT_CARDS = {
            DevelopmentCard.VICTORY_POINT,
            DevelopmentCard.KNIGHT,
            DevelopmentCard.MONOPOLY,
            DevelopmentCard.ROAD_BUILDING,
            DevelopmentCard.YEAR_OF_PLENTY
    };

    static final Map<Resource, Integer> STREET_COSTS = Map.of(
            Resource.STONE,1,
            Resource.WOOD, 1);

    static final Map<Resource, Integer> VILLAGE_COSTS = Map.of(
            Resource.WOOL,1,
            Resource.GRAIN, 1,
            Resource.STONE, 1,
            Resource.WOOD, 1);

    static final Map<Resource, Integer> CITY_COSTS = Map.of(
            Resource.ORE,3,
            Resource.GRAIN, 2);

    static final Map<Resource, Integer> DEVELOPMENT_CARD_COSTS = Map.of(
            Resource.WOOL,1,
            Resource.GRAIN, 1,
            Resource.ORE, 1);

    static final Map<Structure, Map<Resource, Integer>> STRUCTURE_COSTS = Map.of(
            Structure.STREET, STREET_COSTS,
            Structure.VILLAGE, VILLAGE_COSTS,
            Structure.CITY, CITY_COSTS,
            Structure.DEVELOPMENT_CARD, DEVELOPMENT_CARD_COSTS
        );


    // RESPONSES

    // Acknowledgement
    static final Response OK = new Response(0, "Ok", "The received message is processed succesfully.");

    // Requests
    static final Response TRADE_REQUEST = new Response(100, "TradeRequest", "Please trade something if you like");
    static final Response BUILD_REQUEST = new Response(101, "BuildRequest", "Please build something if you like");
    static final Response INITIAL_BUILD_REQUEST = new Response(102, "InitialBuildRequest", "Build a street and a village");

    // Parsing issues
    static final Response MALFORMEDJSONERROR = new Response(500, "MalformedJSON", "The received JSON message is invalid.");
    static final Response NODEDOESNOTEXISTERROR = new Response(501, "NodeDoesNotExist", "The received node does not exist.");
    static final Response EDGEDOESNOTEXISTERROR = new Response(502, "EdgeDoesNotExist", "The received edge does not exist.");
    static final Response INVALIDSTRUCTUREERROR = new Response(503, "InvalidStructure", "The received structure is invalid.");

    static final Response INVALID_TRADE_ERROR = new Response(504, "InvalidTrade", "An invalid resource was given to the trade command");


    // Rule issues
    static final Response STRUCTURENOTCONNECTEDERROR = new Response(600, "StructureNotConnected", "The received structure is not connected.");
    static final Response CITYNOTBUILTONPLAYERSVILLAGEERROR = new Response(601, "CityNotBuiltOnVillage", "The received city is not built on top of a/your village.");
    static final Response STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR = new Response(602, "StructureTooClose", "There is another structure too close to another structure.");
    static final Response STRUCTUREALREADYEXISTSERROR = new Response(604, "StructureAlreadyExists", "There is already a structure on the given node.");
    static final Response NOTENOUGHRESOURCESERROR = new Response(605, "InsufficientResources", "You have not enough resources to build this.");
    static final Response NOTAVILLAGEANDSTREETERROR = new Response(606, "NotAVillageAndStreet", "In the initial buildphase a command must exist out of a village and a street.");
}
