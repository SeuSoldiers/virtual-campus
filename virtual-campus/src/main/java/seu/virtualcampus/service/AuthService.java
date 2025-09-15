package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.mapper.UserMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class AuthService {
    private final UserMapper userMapper;
    // simple in-memory token store (for demo). Key -> token, Value -> username
    private final Map<String, Integer> tokenStore = new ConcurrentHashMap<>();


    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public String login(String username, String password) {
        User u = null;
        try {
            u = userMapper.findByUsername(Integer.parseInt(username));
        } catch (NumberFormatException e) {
            return null; // username must be an integer
        }
        if (u == null) return null;
// NOTE: in production use hashing & salted password
        if (!u.getPassword().equals(password)) return null;
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, Integer.parseInt(username));
        return token;
    }


    public User getUserByToken(String token) {
        Integer username = tokenStore.get(token);
        if (username == null) return null;
        return userMapper.findByUsername(username);
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public boolean register(String username, String password, String role) {
        User exist;
        try {
            exist = userMapper.findByUsername(Integer.parseInt(username));
        } catch (NumberFormatException e) {
            return false; // 用户名必须为数字
        }
        if (exist != null) return false;
        User user = new User();
        user.setUsername(Integer.parseInt(username));
        user.setPassword(password);
        user.setRole(role);
        int res = userMapper.insert(user);
        return res > 0;
    }
}
