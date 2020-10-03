package utils;

import board.DevelopmentCard;
import board.HarbourType;
import board.Structure;
import game.Resource;
import game.Response;

import java.util.Map;

public class Constants {
    public static final int MINIMUM_AMOUNT_OF_PLAYERS = 1;
    public static final int MINIMUM_CARDS_FOR_TRADE = 4;
    public static final int VICTORY_POINTS_TO_WIN = 10;
    public static final int MINIMUM_AMOUNT_OF_KNIGHTS_FOR_AWARD = 3;
    public static final int MINIMUM_AMOUNT_OF_ROADS_FOR_AWARD = 5;

    // some helpers for the hexagon coordinate system
    public static final int[][] AXIAL_DIRECTIONS_ODD = {
            {-1, -1},
            {0, -1},
            {1, 0},
            {0, 1},
            {-1, 1},
            {-1, 0}
    };
    public static final int[][] AXIAL_DIRECTIONS_EVEN = {
            {0, -1},
            {1, -1},
            {1, 0},
            {1, 1},
            {0, 1},
            {-1, 0}
    };

    public static final Resource[] ALL_RESOURCES = {
            Resource.ORE,
            Resource.GRAIN,
            Resource.STONE,
            Resource.WOOD,
            Resource.WOOL
    };

    public static final Structure[] ALL_STRUCTURES = {
            Structure.STREET,
            Structure.VILLAGE,
            Structure.CITY,
            Structure.DEVELOPMENT_CARD
    };

    public static final DevelopmentCard[] ALL_DEVELOPMENT_CARDS = {
            DevelopmentCard.VICTORY_POINT,
            DevelopmentCard.KNIGHT,
            DevelopmentCard.MONOPOLY,
            DevelopmentCard.ROAD_BUILDING,
            DevelopmentCard.YEAR_OF_PLENTY
    };

    public static final Map<Resource, Integer> STREET_COSTS = Map.of(
            Resource.STONE,1,
            Resource.WOOD, 1);

    public static final Map<Resource, Integer> VILLAGE_COSTS = Map.of(
            Resource.WOOL,1,
            Resource.GRAIN, 1,
            Resource.STONE, 1,
            Resource.WOOD, 1);

    public static final Map<Resource, Integer> CITY_COSTS = Map.of(
            Resource.ORE,3,
            Resource.GRAIN, 2);

    public static final Map<Resource, Integer> DEVELOPMENT_CARD_COSTS = Map.of(
            Resource.WOOL,1,
            Resource.GRAIN, 1,
            Resource.ORE, 1);

    public static final Map<Structure, Map<Resource, Integer>> STRUCTURE_COSTS = Map.of(
            Structure.STREET, STREET_COSTS,
            Structure.VILLAGE, VILLAGE_COSTS,
            Structure.CITY, CITY_COSTS,
            Structure.DEVELOPMENT_CARD, DEVELOPMENT_CARD_COSTS
        );

    public static final Map<Resource, HarbourType> RESOURCES_HARBOURS = Map.of(
            Resource.GRAIN, HarbourType.HARBOUR_GRAIN,
            Resource.ORE, HarbourType.HARBOUR_ORE,
            Resource.WOOL, HarbourType.HARBOUR_WOOL,
            Resource.WOOD, HarbourType.HARBOUR_WOOD,
            Resource.STONE, HarbourType.HARBOUR_STONE
    );

    // RESPONSES

    // Acknowledgement
    public static final Response OK = new Response(0, "Ok", "The received message is processed succesfully.");
    public static final Response ID_ACK  = new Response(1, "Id_Ack", "Your id is in the given additional info");

    // Requests
    public static final Response TRADE_REQUEST = new Response(100, "TradeRequest", "Please trade something if you like.");
    public static final Response BUILD_REQUEST = new Response(101, "BuildRequest", "Please build something if you like.");
    public static final Response INITIAL_BUILD_REQUEST = new Response(102, "InitialBuildRequest", "Build a street and a village.");
    public static final Response MOVE_BANDIT_REQUEST = new Response(103, "MoveBanditRequest", "Move the bandit to a tile.");
    public static final Response DISCARD_RESOURCES_REQUEST = new Response(104, "DiscardResourcesRequest", "Discard at least the amount of resources given in the additional info.");

    // Parsing issues
    public static final Response MALFORMED_JSON_ERROR = new Response(500, "MalformedJSON", "The received JSON message is invalid.");
    public static final Response NODE_DOES_NOT_EXIST_ERROR = new Response(501, "NodeDoesNotExist", "The received node does not exist.");
    public static final Response EDGE_DOES_NOT_EXIST_ERROR = new Response(502, "EdgeDoesNotExist", "The received edge does not exist.");
    public static final Response INVALID_STRUCTURE_ERROR = new Response(503, "InvalidStructure", "The received structure is invalid.");
    public static final Response INVALID_TRADE_ERROR = new Response(504, "InvalidTrade", "An invalid resource was given to the trade command.");
    public static final Response TOO_MUCH_FAILURES = new Response(505, "TooMuchFailures", "You failed to often so your turn is skipped.");
    public static final Response INVALID_BANDIT_MOVE_ERROR = new Response(506, "InvalidBanditMove", "An invalid location was given to the move command.");

    // Rule issues
    public static final Response STRUCTURE_NOT_CONNECTED_ERROR = new Response(600, "StructureNotConnected", "The received structure is not connected.");
    public static final Response CITY_NOT_BUILT_ON_VILLAGE_ERROR = new Response(601, "CityNotBuiltOnVillage", "The received city is not built on top of a/your village.");
    public static final Response STRUCTURE_TOO_CLOSE_TO_OTHER_STRUCTURE_ERROR = new Response(602, "StructureTooClose", "There is another structure too close to another structure.");
    public static final Response STRUCTURE_ALREADY_EXISTS_ERROR = new Response(604, "StructureAlreadyExists", "There is already a structure on the given node.");
    public static final Response INSUFFICIENT_RESOURCES_ERROR = new Response(605, "InsufficientResources", "You have not enough resources to build this.");
    public static final Response NOT_A_VILLAGE_AND_STREET_ERROR = new Response(606, "NotAVillageAndStreet", "In the initial buildphase a command must exist out of a village and a street.");
    public static final Response CAN_NOT_PLACE_BANDIT_ON_SEA_TILE_ERROR = new Response(607, "CanNotPlaceBanditOnSeaTile", "The bandit can not be placed on a sea tile.");
    public static final Response CAN_NOT_PLACE_BANDIT_ON_SAME_TILE_ERROR = new Response(608, "CanNotPlaceBanditOnSameTile", "The bandit can not be placed on the same tile as it is currently at.");
    public static final Response NOT_ENOUGH_RESOURCES_DISCARDED_ERROR = new Response(609, "NotEnoughResourcesDiscarded", "You should discard at least half your resources (rounded down).");
    public static final Response MORE_RESOURCES_DISCARDED_THAN_OWNED_ERROR = new Response(610, "MoreResourcesDiscardedThanOwned", "You can not discard more resources than you currently have.");

}
