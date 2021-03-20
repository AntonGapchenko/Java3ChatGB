import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService = new DbAuthService();

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        this.clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Server is listening on 8189");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }
        String out = sb.toString();
        broadcastMessage(out);
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

    public void privateMsg(ClientHandler sender, String receiverNick, String msg) {
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMessage("Note for myself: " + msg);
            return;
        }
        for (ClientHandler receiver : clients) {
            if (receiver.getNickname().equals(receiverNick)) {
                receiver.sendMessage("from " + sender.getNickname() + ": " + msg);
                sender.sendMessage("for " + receiverNick + ": " + msg);
                return;
            }
        }
        sender.sendMessage("Client " + receiverNick + " is not found");
    }
}
