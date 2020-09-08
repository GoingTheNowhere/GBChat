package sample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Controller {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String myNick;
    private File localMessageHistory = new File("history.txt");

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
            loadHistory();
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
                        writeMessageToHistory(strMsg);
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
    void loadHistory() throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader
                (new InputStreamReader
                        (new FileInputStream
                                (localMessageHistory)
                        )
                );

        List<String> messageHistory = new ArrayList<>();
        String message;
        try {
            while ((message = bufferedReader.readLine()) != null) {
                messageHistory.add(message);
            }
        } catch (Exception e){

        }
        int messagesForShow = 100;
        for (int i = messageHistory.size() > messagesForShow ?
                     messageHistory.size() - messagesForShow :
                     0;
             i < messageHistory.size();
             i++) {
            mainTextArea.appendText(messageHistory.get(i) + "\n");
        }
    }

    void writeMessageToHistory(String message){
        try (BufferedWriter bf = new BufferedWriter
                (new PrintWriter
                        (new FileWriter
                                (localMessageHistory, true)));
                )
        {
            bf.write(message + "\n");
        } catch (IOException e) {

        }
    }

}
