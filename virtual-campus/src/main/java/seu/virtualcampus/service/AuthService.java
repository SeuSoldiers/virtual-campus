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
    // simple in-memory token store (for demo). Key -> token, Value -> userId
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();


    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public String login(String username, String password) {
        User u = userMapper.findByUsername(username);
        if (u == null) return null;
        // NOTE: in production use hashing & salted password
        if (!u.getPassword().equals(password)) return null;
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, u.getId());
        return token;
    }


    public User getUserByToken(String token) {
        Long id = tokenStore.get(token);
        if (id == null) return null;
        return userMapper.findById(id);
    }
}