package com.hvduong.detectiontomatoes.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcastEvent(Object event) {
        broadcast(event);
    }

    public void broadcastStats(Map<String, Integer> stats) {
        broadcast(Collections.singletonMap("stats", stats));
    }

    private void broadcast(Object obj) {
        try {
            String msg = objectMapper.writeValueAsString(obj);
            System.out.println("Broadcasting: " + msg);

            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(msg));
                    }
                }
            }
        } catch (Exception e) {
            // Log error
        }
    }
}
