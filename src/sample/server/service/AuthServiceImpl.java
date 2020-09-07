package sample.server.service;

import sample.server.dao.DBConn;
import sample.server.inter.AuthService;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {

    private List<User> usersList;

    public AuthServiceImpl() {
        try {
            System.out.println("Сервис аутентификации запущен.");
            Statement statement = DBConn
                    .getInstance()
                    .getConn()
                    .createStatement();
            String query =
                    "SELECT nickname, login, password " +
                    "FROM users";
            ResultSet rs = statement.executeQuery(query);
            this.usersList = new LinkedList<>();
            while (rs.next()){
                String nickname = rs.getString("nickname");
                String login = rs.getString("login");
                String password = rs.getString("password");
                User user = new User(login, password, nickname);
                this.usersList.add(user);
            }
            for (User user : this.usersList) {
                System.out.println("Найден и добавлен пользователь:\n" + user.toString());
            }
            System.out.println("Данные пользователей получены из базы данных.");

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public String getNick(String login, String password) {
        for (User user : usersList) {

            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nick;
            }

        }
        return null;
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }

    private class User {
        private String login;
        private String password;
        private String nick;

        public User(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
        @Override
        public String toString(){
            return "Логин: " + this.login + "\nНик: " + this.nick + "\nПароль: " + this.password;
        };
    }
}
