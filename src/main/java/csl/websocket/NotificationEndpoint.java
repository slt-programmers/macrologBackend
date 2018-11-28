package csl.websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/notify")
public class NotificationEndpoint {

    private Session session;
    private static Set<NotificationEndpoint> endpoints = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        endpoints.add(this);
        broadcast("Hello, world!");
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        broadcast(message);
    }

    @OnClose
    public void onClose() throws IOException {
        endpoints.remove(this);
        broadcast("Goodbye, world!");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(String message) {
        endpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
