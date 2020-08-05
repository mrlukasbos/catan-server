import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PlayerSocket extends Player {
    private Socket socket;

    PlayerSocket(Game game, int id, String name) {
        super(game, id, name);
    }

    synchronized void setSocket(Socket s) {
        socket = s;
    }

    synchronized Socket getSocket() {
        return socket;
    }

    synchronized void send(String str) {
        if (getSocket() != null) {
            try {
                str += "\r\n";
                BufferedOutputStream bos = new BufferedOutputStream(getSocket().getOutputStream());
                bos.write(str.getBytes("UTF-8"));
                bos.flush();
            } catch (IOException e) {
                // we just quit the game whenever something goes wrong so we can reconnect
                game.quit();
                // e.printStackTrace();
            }
        }
    }

    // blocking implementation of reading
    synchronized String listen() {
        if (getSocket() != null) {
            try {
                BufferedInputStream bf = new BufferedInputStream(getSocket().getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));
                return r.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
