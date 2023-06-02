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
    private static final String QUIT_COMMAND = "BYE";


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
        this.threadPool = Executors.newFixedThreadPool(10000);
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
     * @return The QUIT_COMMAND String.
     */
    public String getQuitCommand() {
        return QUIT_COMMAND;
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
            System.out.printf("[%1$tY-%1$tm-%1$td %1$tT] ExpressionServer started on port %2$d%n", System.currentTimeMillis(), port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.printf("[%1$tY-%1$tm-%1$td %1$tT] New connection from client %2$s", System.currentTimeMillis(), socket.getRemoteSocketAddress());
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    System.err.printf("Cannot accept connection due to %s\n", e);
                }
            }
        } catch (IOException e) {
            System.err.printf("Error starting the server on port %1$s due to %2$s", port, e.getMessage());
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
