package ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// https://github.com/TooTallNate/Java-WebSocket
public class WebSocketTest {
    public static void main(String[] args) throws URISyntaxException {
        WebSocketClient mWs = new WebSocketClient(new URI("ws://192.168.1.245:1240/websocket/info?t=1610617067839")) {
            @Override
            public void onMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("opened connection");
                WebSocketClient self = this;

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    self.send("{\"type1\": \"Noop\"}");
                }, 0, 5, TimeUnit.SECONDS);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("closed connection");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        mWs.connect();
    }
}
