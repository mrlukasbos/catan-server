package communication;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

public class ConnectionElement {
    AsynchronousSocketChannel channel;
    Future<Integer> result;
    Future<Integer> writeResult;
    ByteBuffer buffer;
    String partialMsg = "";
}
