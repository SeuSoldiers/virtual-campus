# 示例接口设计

## 对外接口

输出格式统一为：

```json
{
  "code": "error_code",
  "message": "error_message",
  "data": {
    "...": "..."
  }
}
```

### 例子1

**POST** `/example/echo`

* **输入：**

```json
{
  "message": "Hello, World!"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "Echo successful",
  "data": {
    "echoed_message": "Hello, World!"
  }
}
```

* **说明：**
    * `code` 为 0 表示成功，非 0 表示失败。
    * `echoed_message` 返回输入的消息内容。

### 例子2

...

## 数据库接口

### 例子1：查询某账户余额

**GET** `/account/balance`

* **输入：**

```json
{
  "user_id": "213231111"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "查询成功",
  "data": {
    "user_id": "213231111",
    "balance": 1000.50
  }
}
```

# 示例类分析

## 实体类

所有数据都存储在数据库中，所以实体类的作用是作为缓存，避免频繁访问数据库。

### 例子1：用户类

| 序号 | 名称       | 类型     | 约束   | 说明                               |
|----|----------|--------|------|----------------------------------|
| 1  | user_id  | String | 主键   | 用户唯一标识                           |
| 2  | name     | String | 非空   | 用户姓名                             |
| 3  | password | String | 哈希   | 用户密码                             |
| 4  | role     | Enum   | 非空   | 用户角色（STUDENT, TEACHER, ADMIN...） |
| 5  | status   | Enum   | 默认启用 | 用户状态（ACTIVE, DISABLED, LOCKED）   |

### 例子2：会话类

| 序号 | 名称      | 类型     | 约束   | 说明                             |
|----|---------|--------|------|--------------------------------|
| 1  | token   | String | 主键   | 会话唯一标识                         |
| 2  | user_id | String | 外键   | 关联用户ID                         |
| 3  | issued  | Date   | 非空   | 令牌签发时间                         |
| 4  | expires | Date   | 非空   | 令牌过期时间                         |
| 5  | status  | Enum   | 默认启用 | 令牌状态（ACTIVE, REVOKED, EXPIRED） |

## 服务类/接口类

服务类负责处理业务逻辑，接口类负责对外提供API。

### 例子1：用户服务类

| 序号 | 名称   | 方法                                                                    | 说明                                 |
|----|------|-----------------------------------------------------------------------|------------------------------------|
| 1  | 登录   | Session login(String userId, String password)                         | 用户登录，验证账号密码，返回会话信息                 |
| 2  | 登出   | Boolean logout(String tokenId)                                        | 用户登出，销毁指定 token                    |
| 3  | 验证   | Boolean validate(String userId, String tokenId, String action)        | 校验某个用户的 token 是否有效，且是否有权限执行 action |
| 4  | 注册   | User register(String userId, String name, String password, Role role) | 用户注册                               |
| 5  | 刷新令牌 | Session refreshToken(String tokenId)                                  | 刷新 token，延长过期时间                    |

# 示例类调用关系（供理解，具体写设计时可以忽略）

```text
             ┌───────────┐
             │   MainUI  │
             └─────▲─────┘
                   │
             ┌─────┴────────┐
             │ UserInfoUI   │
             └─────▲────────┘
                   │
             ┌─────┴────────┐
             │ UIController │
             └─────▲────────┘
                   │ 调用
             ┌─────┴─────────┐
             │FrontendService│
             └─────▲─────────┘
                   │ 网络请求
       ┌───────────┴─────────────┐
       │                         │
 ┌─────────────┐            ┌─────────────┐
 │   AuthAPI   │            │   UserAPI   │   ← 对外 REST 接口
 └─────▲───────┘            └─────▲───────┘
       │                          │
 ┌─────┴───────┐            ┌─────┴─────────┐
 │ AuthService │            │  UserService  │  ← 后端业务逻辑
 └─────▲───────┘            └─────▲─────────┘
       │                          │
       │                          │
 ┌─────┴────────┐           ┌─────┴──────────┐
 │  Session     │           │  UserRepository│ ← 数据访问
 └─────▲────────┘           └─────▲──────────┘
       └──────────────────────────│
                           ┌──────┴──────────┐
                           │ DatabaseWrapper │
                           └─────────────────┘

```

这张图中，MainUI是所有UI的入口，UserInfoUI、UIController是各个组件自己的UI逻辑，FrontendService是统一的前端服务类，负责发起网络请求。
AuthAPI、AuthService、Session是认证相关的类，UserAPI、UserService、UserRepository是用户信息相关的类，DatabaseWrapper是数据库访问类。

举例，对于银行开发者，最基本的需要编写BankUI、BankController、BankAPI、BankService、BankRepository这几个类。然后需要声明自己的数据库表结构，以便于DatabaseWrapper的开发者进行对应的适配。

# 示例数据表设计

DatabaseWrapper的开发者需要操作一个大的SQLite数据库，所有数据都存储在这个数据库中。
并给不同的业务模块分配不同的表空间，提供对应的增删改查接口。

## 用户表 (users)

| 序号 | 字段名      | 类型           | 约束               | 说明      |
|----|----------|--------------|------------------|---------|
| 1  | user_id  | VARCHAR(50)  | PRIMARY KEY      | 用户唯一 标识 |
| 2  | name     | VARCHAR(100) | NOT NULL         | 用户姓名    |
| 3  | password | VARCHAR(255) | NOT NULL         | 用户密码    |
| 4  | role     | VARCHAR(20)  | NOT NULL         | 用户角色    |
| 5  | status   | VARCHAR(20)  | DEFAULT 'ACTIVE' | 用户状态    |

## 会话表 (sessions)

| 序号 | 字段名     | 类型          | 约束               | 说明       |
|----|---------|-------------|------------------|----------|
| 1  | token   | VARCHAR(36) | PRIMARY KEY      | 会话唯一 标识  |
| 2  | user_id | VARCHAR(50) | FOREIGN KEY      | 关联用户ID   |
| 3  | issued  | DATETIME    |                  | NOT NULL | 令牌签发时间     |
| 4  | expires | DATETIME    | NOT NULL         | 令牌过期时间   |
| 5  | status  | VARCHAR(20) | DEFAULT 'ACTIVE' | 令牌状态     |

## 银行账户表 (bank_accounts)

| 序号 | 字段名     | 类型            | 约束          | 说明 |
|----|---------|---------------|-------------|----|
| 1  | user_id | VARCHAR(50)   | FOREIGN KEY |    | 用户ID       |
| 2  | balance | DECIMAL(15,2) | NOT NULL    |    | 账户余额        |

