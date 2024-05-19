
import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationThread extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private SocketChannel socketChannel;

    public AuthenticationThread(Socket userSocket, SocketChannel socketChannel, Thread QueueHandler){
        this.socket = userSocket;
        this.socketChannel = socketChannel;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        long timeout = 7000; // Timeout duration in milliseconds

        while(true) {
            try {
                // Check if the timeout has been exceeded
                if (System.currentTimeMillis() - startTime > timeout) {
                    System.out.println("Timeout exceeded. Exiting Authentication.");
                    writer.println("Timeout exceeded. Exit Authentication");

                    break;
                }

                if (reader.ready()) {
                    String message = reader.readLine();

                    startTime = System.currentTimeMillis();

                    String[] response = message.split(" ");
                    if (response[0].equals("login")) {

                        String username = response[1];
                        String password = response[2];
                        String t = null;

                        if (DatabaseManager.authenticate(username, password)) {
                            if(response.length >=4){
                                t = response[3]; // index out of bounds
                            }

                            Player player = DatabaseManager.getPlayer(username, password);

                            Token token = DatabaseManager.generateToken(1000);
                            player.setToken(token);

                            player.setSocket(socket);
                            player.setChannel(socketChannel);
                            player.setLoggedIn(true);

                            Server.lockPlayersQueue.lock();
                            Server.playersQueue.add(player);
                            Server.lockPlayersQueue.unlock();

                            writer.println("login successfully " + token.get_identifier());

                            break;
                        } else {
                            writer.println("login failed");
                            continue;
                        }

                    } else if (Objects.equals(message.split(" ")[0], "register")) {

                        String username = message.split(" ")[1];
                        String password = message.split(" ")[2];

                        Player player = DatabaseManager.register(username, password);
                        if (player != null) {
                            Token token = DatabaseManager.generateToken(1000);

                            player.setToken(token);

                            player.setLoggedIn(true);
                            player.setSocket(socket);
                            player.setChannel(socketChannel);

                            Server.lockPlayersQueue.lock();
                            Server.playersQueue.add(player);
                            Server.lockPlayersQueue.unlock();

                            writer.println("registration successfully " + token.get_identifier());

                            break;

                        } else {
                            writer.println("registration failed");
                            continue;
                        }
                    }
                    else {
                        System.out.println("Invalid option");
                        continue;
                    }
                }
                else {
                    Thread.sleep(100);
                }


            } catch (IOException | InterruptedException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

}