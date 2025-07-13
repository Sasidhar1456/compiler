package com.example.compiler.handler;

import com.example.compiler.service.PythonExecutorService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;

@Component
public class MyWebSocketHandler implements WebSocketHandler {

    private final PythonExecutorService pythonService;

    public MyWebSocketHandler(PythonExecutorService pythonService) {
        this.pythonService = pythonService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ Connection established with session ID: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String payload = message.getPayload().toString();

        if (payload.startsWith("runCode:")) {
            String code = payload.substring("runCode:".length());
            pythonService.executeCodeAsync(session, code);
        } else if (payload.startsWith("stdin:")) {
            String input = payload.substring("stdin:".length());
            pythonService.sendInput(session.getId(), input);
        } else if (payload.equals("stopExecution")) {
            pythonService.stopExecution(session.getId());
            session.sendMessage(new TextMessage("[Execution Stopped by User]\nprocessEnd"));
        } else {
            session.sendMessage(new TextMessage("❌ Unknown command"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("❗ Transport error in session " + session.getId() + ": " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("🔌 Connection closed for session ID: " + session.getId() + " with status: " + status);
        pythonService.cleanup(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
