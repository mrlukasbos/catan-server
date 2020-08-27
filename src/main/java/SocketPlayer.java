import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketPlayer extends Player {
    private Socket socket;

    SocketPlayer(Game game, int id, String name) {
        super(game, id, name);
    }

    synchronized void setSocket(Socket s) {
        socket = s;
    }

    synchronized Socket getSocket() {
        return socket;
    }

    @Override
    void stop() {
        try {
            socket.close();
        } catch(IOException ignored) {

        }
    }

    synchronized void send(String str) {
        if (getSocket() != null && !getSocket().isClosed()) {
            try {
                str += "\r\n";
                BufferedOutputStream bos = new BufferedOutputStream(getSocket().getOutputStream());
                bos.write(str.getBytes("UTF-8"));
                bos.flush();
            } catch (IOException e) {
                // we just quit the game whenever something goes wrong so we can reconnect
               //  game.quit();
                // e.printStackTrace();
            }
        }
    }

    // blocking implementation of reading
    synchronized String listen() {
        if (getSocket() != null && !getSocket().isClosed()) {
            try {
                BufferedInputStream bf = new BufferedInputStream(getSocket().getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));
                return r.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
