package sample.server.inter;

public interface AuthService {
    String getNick(String login, String password);
    void stop();
}
