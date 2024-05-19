import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;



public class QueueHandler implements Runnable {

	@Override
	public void run(){
	int t = 0;
	
	while (true){
		try {
        Thread.sleep(300); // Wait for some time before checking the queue again 5 min

        Server.lockPlayersQueue.lock();
        for (Player player : Server.playersQueue) {

            System.out.println("queue | name: " + player.getUsername() + " | token: " + player.getToken() + " | logged in: "  + player.getLoggedIn() + "|");

            //renovar token dos players logados na queue
            if(player.isSocketChannelOpen()) {
                if (player.getToken() != null && player.getToken().isExpired() && player.getLoggedIn()) {

                    Token newToken = DatabaseManager.generateToken(1000);
                    player.setToken(newToken);
                    DatabaseManager.tokenFile(player.getUsername(), newToken.get_identifier());
                }
            }
            //dar logout dos jogadores desconectados e retirar da queue os jogadores nao logados e com token expirado
            else {
                player.setLoggedIn(false);
                if (player.getToken() != null && player.getToken().isExpired()) {
                    Server.playersQueue.remove(player); 

                }
            }
        }

        System.out.println("Queue size: " + Server.playersQueue.size());

        // Filter the list to get only logged-in players1
        List<Player> loggedInPlayers = Server.playersQueue.stream().filter(Player::getLoggedIn).collect(Collectors.toList());
        
        
        if (loggedInPlayers.size() >= 3){
          if(Server.mode == 1){
            List<Player> gamePlayers = new ArrayList<>();
            for (int i = 0; i < 3; i++){
                    gamePlayers.add(loggedInPlayers.get(i));

                    //Server.lockPlayersQueue.lock();
                    Server.playersQueue.remove(loggedInPlayers.get(i));
                    //Server.lockPlayersQueue.unlock();
                }

                String gameId = UUID.randomUUID().toString();
                Server.executor.submit(new GameThread(gamePlayers, gameId)); 
          }
        }
        } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        Server.lockPlayersQueue.unlock();
    }
	
	
	
	
	}}}