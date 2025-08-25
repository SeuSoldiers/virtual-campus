## 对外接口

### 1. 登录（获取 token）

**POST** `/auth/login`

* **输入：**

```json
{
  "user_id": "stu01",
  "password": "123456"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "user_id": "stu01",
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "role": "STUDENT",
    "expires_in": 3600
  }
}
```

* **说明：**

    * `token` 是临时会话ID，有效期 3600 秒（可续期）。
    * `role` 用于区分权限。
    * `code` 为 0 表示成功，非 0 表示失败。

---

### 2. 登出（销毁 token）

**POST** `/auth/logout`

* **输入：**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "登出成功"
}
```

* **说明：**

    * `token` 为需要销毁的会话ID。

---

### 3. 校验凭证（鉴权接口）

**POST** `/auth/validate`

* **输入：**

```json
{
  "userId": "stu01",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "action": "borrowBook"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "验证成功",
  "data": {
    "uuidValid": true,
    "allowed": true
  }
}
```

* **说明：**

    * `uuidValid` 表示 uuid 是否有效。
    * `allowed` 表示是否允许该角色执行此操作（例如学生不能 `addBook`）。

---

### 4. 刷新会话（续期）

**POST** `/auth/refresh`

* **输入：**

```json
{
  "userId": "stu01",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

* **输出：**

```json
{
  "code": 0,
  "message": "续期成功",
  "data": {
    "newExpiresIn": 3600
  }
}
```

### 🔗 其他模块调用流程

例如：

* 学生调用 **图书馆借书 API** → 图书馆模块收到请求时，先把 `userId + uuid + action` 发到 `/auth/validate`。
* 验证成功 → 图书馆模块执行逻辑（借书）。
* 验证失败 → 学生重新登录。
