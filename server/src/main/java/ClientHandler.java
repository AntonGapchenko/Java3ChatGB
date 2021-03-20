import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String nickname;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                boolean continueChat = true;
                String msg = "";
                boolean isAuthorized = false;
                try {
                    long start = System.currentTimeMillis();
                    while (!isAuthorized && continueChat) {
                        if ((System.currentTimeMillis() - start) > 120000) {
                            continueChat = false;
                            sendMessage("/error timeout");
                        } else if (in.available() > 0) {
                            msg = in.readUTF();
                            if (msg.startsWith("/auth")) {
                                String[] tokens = msg.split("\\s");
                                nickname = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                                if (nickname != null) {
                                    isAuthorized = true;
                                    sendMessage("/authok");
                                    server.subscribe(this);
                                } else {
                                    sendMessage("/error");
                                }
                            }
                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;
                            }
                        }
                    }

                    while (continueChat) {
                        msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;
                            } else if (msg.startsWith("/w")) {
                                String[] tokens = msg.split("\\s", 3);
                                System.out.println(tokens);
                                if (tokens.length == 3) {
                                    System.out.println(tokens);
                                    server.privateMsg(this, tokens[1], tokens[2]);
                                }
                            }
                        } else {
                            server.broadcastMessage(nickname + " : " + msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }

            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public void disconnect() {
        sendMessage("/end");
        System.out.println("disconnected " + nickname);
        server.unsubscribe(this);
        try {
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
