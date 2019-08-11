/*
The gamemanager is responsible that the game can run. It connects the game with the players and the visualization.
 */


public class GameManager  {

    private Game currentGame;
    private boolean gameIsRunning = false;


    GameManager()  {
        currentGame = new Game();
    }

    void start() {
        gameIsRunning = true;
    }

    void end() {
       // currentGame = new Game();
        gameIsRunning = false;
    }

    void run() {

    }


    Game getCurrentGame() {
        return currentGame;
    }

    boolean IsRunning() {
        return gameIsRunning;
    }
}
