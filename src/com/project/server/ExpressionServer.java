package com.project.server;

import com.project.service.Computer;
import com.project.service.StatsCollector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ExpressionServer {

    private final int port;
    private final ExecutorService threadPool;
    private final ExecutorService computationThreadPool;
    private final StatsCollector statsCollector;
    private final Computer computer;


    /**
     * Constructor for the ExpressionServer. It initializes the server port,
     * creates the thread pools for handling connections and computations,
     * and instantiates the StatsCollector for collecting statistics.
     *
     * @param port The port number on which the server will listen for connections.
     */
    public ExpressionServer(int port) {
        this.port = port;
        this.statsCollector = new StatsCollector();
        // Create a thread pool with a fixed number of threads for handling client connections.
        this.threadPool = Executors.newFixedThreadPool(1000);
        // Create a separate thread pool for computations, with as many threads as available processors.
        this.computationThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.computer = new Computer();
    }

    /**
     * Getter for the computationThreadPool.
     *
     * @return An ExecutorService representing the thread pool for computations.
     */
    public ExecutorService getComputationThreadPool() {
        return computationThreadPool;
    }
    /**
     * Getter for the computer.
     *
     * @return The Computer used for computations.
     */
    public Computer getComputer() {
        return computer;
    }

    /**
     * Returns the command string that signifies a client wishes to disconnect.
     *
     * @return The string "BYE".
     */
    public String getQuitCommand() {
        return "BYE";
    }

    /**
     * Getter for the statsCollector.
     *
     * @return A StatsCollector instance that collects statistics on the server's operations.
     */
    public StatsCollector getStatsCollector() {
        return statsCollector;
    }

    /**
     * Starts the server, allowing it to accept client connections. Each connection is handled in a separate thread.
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("ExpressionServer started on port %s%n", port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New connection from client " + socket.getRemoteSocketAddress());
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    System.err.printf("Cannot accept connection due to %s\n", e);
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting the server on port " + port + " : " + e.getMessage());
        }
    }

    /**
     * The main method for the ExpressionServer class. It parses the command line arguments for the server port number,
     * instantiates an ExpressionServer, and runs it.
     *
     * @param args Command line arguments, expecting the server port number as the first argument.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ExpressionServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ExpressionServer server = new ExpressionServer(port);
        server.run();
    }

}
