package com.project.server;

import com.project.protocol.request.RequestHandler;
import com.project.protocol.response.Response;

import java.io.*;
import java.net.Socket;

/**
 * This class is responsible for handling client connections to the server.
 * It extends the Thread class and processes incoming requests from the client,
 * sends the response back to the client, and manages the client's connection lifecycle.
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private final ExpressionServer expressionServer;
    private final RequestHandler requestHandler;

    /**
     * Constructs a new ClientHandler instance with the given Socket and ExpressionServer.
     * Initializes a new RequestHandler with the provided ExpressionServer.
     *
     * @param socket           The Socket through which the client is connected.
     * @param expressionServer The ExpressionServer instance associated with this client handler.
     */
    public ClientHandler(Socket socket, ExpressionServer expressionServer) {
        this.socket = socket;
        this.expressionServer = expressionServer;
        this.requestHandler = new RequestHandler(expressionServer);
    }

    /**
     * The main method for handling client connections and processing requests.
     * Continuously reads requests from the client, processes them, and sends the responses back to the client.
     * If the client sends a quit command or closes the connection abruptly, the connection is terminated.
     */
    public void run() {
        try (socket) {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    System.err.println("Client abruptly closed connection");
                    break;
                }
                // Quit Request
                if (line.equals(expressionServer.getQuitCommand())) {
                    break;
                }
                Response response = requestHandler.handleRequest(line);
                bw.write(response.toString() + System.lineSeparator());
                bw.flush();
            }
        } catch (IOException e) {
            System.err.printf("IO error: %s%n", e);
        } finally {
            System.out.printf("Client %s disconnected from server%n", this.socket.getInetAddress().toString());
        }
    }
}
