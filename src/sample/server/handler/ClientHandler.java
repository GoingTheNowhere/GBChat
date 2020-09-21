package sample.server.handler;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sample.server.inter.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    public static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    private String nick;

    public String getNick() {
        return this.nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";

            new Thread(() -> {
                try {
                    boolean authenticationSuccessful = false;
                    int timeout = 120000;
                    long connectionTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - connectionTime <= timeout) {
                        authenticationSuccessful = authentication();
                        if(authenticationSuccessful){break;}
                    }
                    if(authenticationSuccessful) {
                        readMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            LOGGER.warn("Не удалось создать обработчик клиента.");
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private boolean authentication() throws IOException {
        while (true) {
            String str = dis.readUTF();
            if (str.startsWith("/auth")) {
                String[] dataArray = str.split("\\s");
                String nick = server.getNick(dataArray[1], dataArray[2]);
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {
                        sendMsg("/authOk " + nick);
                        this.nick = nick;
                        server.broadcastMsg(this.nick + " join the chat");
                        server.subscribe(this);
                        LOGGER.info("Пользователь " + this.nick + "подключился к чату.");
                    } else {

                        sendMsg("You are already logged in");
                    }
                    return true;
                } else {
                    sendMsg("Incorrect password or login");
                    LOGGER.info("Неудачная попытка авторизации под ником " + this.nick + " с использованием логина: "
                            + dataArray[1] + " и пароля " + dataArray[2]);
                    return false;
                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
            LOGGER.info(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String clientStr = dis.readUTF();
            if (clientStr.startsWith("/")) {
                if (clientStr.equals("/exit")) {
                    LOGGER.info("Пользователь " + this.nick + " вышел из чата.");
                    return;
                }
                if (clientStr.startsWith("/w")) {
                    String[] strArray = clientStr.split("\\s");
                    String nickName = strArray[1];
                    String msg = clientStr.substring(4 + nickName.length());
                    server.sendMsgToClient(this, nickName, msg);
                    LOGGER.info("Персональное сообщение от " + this.nick + " пользователю "
                            + nickName + ": " + msg);
                }
                continue;
            }
            server.broadcastMsg(this.nick + ": " + clientStr);
            LOGGER.info("Cообщение от " + this.nick + ": "
                    + clientStr);
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(this.nick + ": out from chat");
        LOGGER.info(this.nick + " отключился от чата.");

        try {
            dis.close();
            LOGGER.info("Поток ввода закрыт.");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Не удалось закрыть поток ввода либо поток ввода уже закрыт.");
        }

        try {
            dos.close();
            LOGGER.info("Поток вывода закрыт.");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Не удалось закрыть поток вывода либо поток вывода уже закрыт.");
        }

        try {
            socket.close();
            LOGGER.info("Подключение закрыто.");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Не удалось закрыть подключение либо подключение уже закрыто.");
        }
    }
}
