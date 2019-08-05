# Catan challenge
The goal of this challenge is to build a digital version of the catan boardgame, make a visualization for it, and then compete against each other by building a player ai.

## Definitions
| Type | Description                                                                                       |
|------|---------------------------------------------------------------------------------------------------|
| Tile | A tile represents a physical tile of which the board is compromised.                              |
| Edge | Represents the edge between two tiles. Roads can be placed on edges.                              |
| Node | Represents the point where three tiles come together. Villages and cities can be placed on nodes. |

## Coordinate system
Every tile is given a offset coordinate using the 'even-r' horizontal layout. Columns are named col (q). Rows are named row (r).

!['even-r' horizontal layout](./doc/img/even-r-coordinates.png)

A edge can be found using the coordinates of the two adjacent tiles. For example `([0,0],[1,0])` depicts the edge between tile `[0,0]` and `[1,0]`. Nodes can be located using the coordinates of the three ajacent tiles. Therefore `([0,0], [1,0], [1,1])` gives us the node between tiles `[0,0]`, `[1,0]` and `[1,1]`.
