## 对外接口 `/api/auth/`

### 1. 登录（获取 token）

**POST** `login`

* **输入：**

```json
{
  "user_id": "stu01",
  "password": "123456"
}
```

* **输出（成功）：**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "role": "STUDENT",
  "username": "stu01"
}
```

* **输出（未登录/无效token）：**

```text
401 Unauthorized
```

```json
{
  "message": "invalid credentials"
}
```

## 实体类

### 用户类 `User`

| 序号 | 名称       | 类型     | 说明                               |
|----|----------|--------|----------------------------------|
| 1  | username | int    | 用户唯一标识                           |
| 2  | password | String | 用户密码                             |
| 3  | role     | String | 用户角色（STUDENT, TEACHER, ADMIN...） |

## 接口类

### 认证服务接口 `AuthService`

| 序号 | 方法名            | 输入参数               | 返回值   | 说明            |
|----|----------------|--------------------|-------|---------------|
| 1  | login          | username, password | token | 用户登录          |
| 2  | getUserByToken | token              | User  | 根据token获取用户信息 |

## 数据库设计

### 用户表（users）：

```sql
CREATE TABLE users
(
    username INTEGER PRIMARY KEY,
    password TEXT NOT NULL,
    role     TEXT NOT NULL
);
```

| 序号 | 字段名      | 类型      | 约束          | 说明   |
|----|----------|---------|-------------|------|
| 1  | username | INTEGER | PRIMARY KEY | 用户唯一 |
| 2  | password | TEXT    | NOT NULL    | 用户密码 |
| 3  | role     | TEXT    | NOT NULL    | 用户角色 | 