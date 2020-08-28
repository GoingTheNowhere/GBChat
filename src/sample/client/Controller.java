package sample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Controller {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String myNick;

    @FXML
    TextArea mainTextArea;

    @FXML
    TextField textField;

    public Controller() {
    }

    public void start() {
        myNick = "";

        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            Thread t1 = new Thread(() -> {
                try {
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.startsWith("/authOk")) {
                            myNick = strMsg.split("\\s")[1];
                            mainTextArea.appendText(strMsg + "\n");
                            break;
                        }
                    }
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.equals("/exit")) {
                            break;
                        }
                        mainTextArea.appendText(strMsg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mainTextArea.appendText("Вы вышли из чата.");
                        socket.close();
                        myNick = "";
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            start();
        }
        try {
            if (textField.getText().trim().isEmpty() || textField.getText().trim().equals("")) {
                textField.clear();
                return;
            }
            dos.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            System.out.println("По техническим причинам сообщение не было отправлено");
        }
    }
}
