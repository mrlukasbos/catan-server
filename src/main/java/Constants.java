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
    static final Response OK = new Response(0,  Helpers.toJSONMessage("[Ok] The received message is processed succesfully."));

    // raw data (data to be filled in later)
    static final Response BOARD = new Response(100);

    // requests
    static final Response BUILD_REQUEST = new Response(200, Helpers.toJSONMessage("Build something if you like"));
    static final Response INITIAL_BUILD_REQUEST = new Response(201, Helpers.toJSONMessage("Build a street and a village"));
    static final Response TRADE_REQUEST = new Response(202, Helpers.toJSONMessage("Please trade if you like"));

    // Parsing issues
    static final Response MALFORMEDJSONERROR = new Response(500, Helpers.toJSONMessage("[MalformedJSON] The received JSON message is invalid."));
    static final Response NODEDOESNOTEXISTERROR = new Response(501, Helpers.toJSONMessage("[NodeDoesNotExist] The received node does not exist."));
    static final Response EDGEDOESNOTEXISTERROR = new Response(502, Helpers.toJSONMessage("[EdgeDoesNotExist] The received edge does not exist."));
    static final Response INVALIDSTRUCTUREERROR = new Response(503, Helpers.toJSONMessage("[InvalidStructure] The received structure is invalid."));
    static final Response INVALID_TRADE_ERROR = new Response(504, Helpers.toJSONMessage("[InvalidTrade] An invalid resource was given to the trade command"));

    // Rule issues
    static final Response STRUCTURENOTCONNECTEDERROR = new Response(600, Helpers.toJSONMessage("[StructureNotConnected] The received structure is not connected."));
    static final Response CITYNOTBUILTONPLAYERSVILLAGEERROR = new Response(601, Helpers.toJSONMessage("[CityNotBuiltOnVillage] The received city is not built on top of a/your village."));
    static final Response STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR = new Response(602, Helpers.toJSONMessage("[StructureTooClose] There is another structure too close to another structure."));
    static final Response STRUCTUREALREADYEXISTSERROR = new Response(604, Helpers.toJSONMessage("[StructureAlreadyExists] There is already a structure on the given node."));
    static final Response NOTENOUGHRESOURCESERROR = new Response(605, Helpers.toJSONMessage("[InsufficientResources] You have not enough resources to build this."));
    static final Response NOTAVILLAGEANDSTREETERROR = new Response(606, Helpers.toJSONMessage("[NotAVillageAndStreet] In the initial buildphase a command must exist out of a village and a street."));
}
