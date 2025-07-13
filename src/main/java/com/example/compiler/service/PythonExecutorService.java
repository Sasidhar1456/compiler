package com.example.compiler.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PythonExecutorService {

    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final Map<String, BufferedWriter> processInputs = new ConcurrentHashMap<>();

    public void executeCodeAsync(WebSocketSession session, String code) {
        try {
            // Inject Python input wrapper to detect input() calls
            String wrappedCode = """
                import builtins
                real_input = builtins.input
                builtins.input = lambda prompt='': print("", end='', flush=True) or real_input(prompt)
                """ + "\n" + code;

            File scriptFile = File.createTempFile("script", ".py");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
                writer.write(wrappedCode);
            }

            ProcessBuilder pb = new ProcessBuilder("python", "-u", scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            runningProcesses.put(session.getId(), process);
            processInputs.put(session.getId(), new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

            // Stream output from Python process to frontend
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    char[] buffer = new char[1024];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        String output = new String(buffer, 0, len);
                        session.sendMessage(new TextMessage(output));

                        // Signal to frontend to request user input
                        if (output.contains("")) {
                            session.sendMessage(new TextMessage(" "));
                        }
                    }
                    session.sendMessage(new TextMessage("\nProcess Exited with Code 0"));
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cleanup(session.getId());
                }
            }).start();

        } catch (IOException e) {
            try {
                session.sendMessage(new TextMessage("Error: " + e.getMessage()));
            } catch (IOException ignored) {}
        }
    }

    public void sendInput(String sessionId, String input) {
        BufferedWriter writer = processInputs.get(sessionId);
        Process process = runningProcesses.get(sessionId);

        if (writer != null && process != null && process.isAlive()) {
            try {
                writer.write(input);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot send input: process is not alive.");
        }
    }

    public void stopExecution(String sessionId) {
        Process process = runningProcesses.get(sessionId);
        if (process != null) {
            process.destroy();
        }
        cleanup(sessionId);
    }

    public void cleanup(String sessionId) {
        Process process = runningProcesses.remove(sessionId);
        BufferedWriter writer = processInputs.remove(sessionId);

        if (process != null && process.isAlive()) {
            process.destroy();
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
