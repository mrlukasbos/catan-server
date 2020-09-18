package communication;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

public class ConnectionElement {
    boolean isReading = false;
    boolean isWriting = false;
    AsynchronousSocketChannel channel;
    Future<Integer> result;
    ByteBuffer buffer;
}
