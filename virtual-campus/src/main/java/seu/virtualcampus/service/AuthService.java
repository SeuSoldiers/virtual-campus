package seu.virtualcampus.service;


import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.mapper.UserMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务类。
 * <p>
 * 提供用户登录、登出、注册及通过Token获取用户信息等相关业务逻辑。
 * 注意：当前实现为内存级Token存储，仅适用于演示目的，不适用于生产环境。
 */
@Service
public class AuthService {
    private final UserMapper userMapper;
    // simple in-memory token store (for demo). Key -> token, Value -> username
    private final Map<String, Integer> tokenStore = new ConcurrentHashMap<>();

    /**
     * AuthService的构造函数。
     *
     * @param userMapper 用户Mapper，用于用户数据的数据库操作。
     */
    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录。
     * <p>
     * 验证用户名和密码，成功则生成唯一Token并存储，返回该Token。
     * 注意：密码校验为明文，仅为演示。
     *
     * @param username 用户名（必须为数字字符串）。
     * @param password 密码。
     * @return 登录成功返回认证Token，否则返回null。
     */
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

    /**
     * 根据Token获取用户信息。
     *
     * @param token 认证Token。
     * @return Token有效返回对应的用户对象，否则返回null。
     */
    public User getUserByToken(String token) {
        Integer username = tokenStore.get(token);
        if (username == null) return null;
        return userMapper.findByUsername(username);
    }

    /**
     * 用户登出。
     * <p>
     * 从Token存储中移除指定Token，使其失效。
     *
     * @param token 要使其失效的认证Token。
     */
    public void logout(String token) {
        tokenStore.remove(token);
    }

    /**
     * 注册新用户。
     *
     * @param username 用户名（必须为数字字符串）。
     * @param password 密码。
     * @param role     用户角色。
     * @return 注册成功返回true，用户名已存在或格式不正确返回false。
     */
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