## 对外接口 `/api/student/`

### 1. 获取本人信息

**GET** `me`

* **请求头：**

```
Authorization: <token>
```

* **输出（成功）：**

`StudentInfo` 对象

* **输出（未登录/无效token）：**

```text
401 Unauthorized
```

* **输出（非学生身份）：**

```text
403 Forbidden
```

---

### 2. 提交本人信息修改

**POST** `submit`

* **请求头：**

```
Authorization: <token>
```

* **输入：**

`StudentInfo` 对象

* **输出（成功）：**

```json
{
  "msg": "submitted"
}
```

* **输出（未登录/无效token）：**

```text
401 Unauthorized
```

* **输出（非学生身份）：**

```text
403 Forbidden
```

## 实体类

### 学生信息类 `StudentInfo`

| 序号 | 名称        | 类型     | 说明     |
|----|-----------|--------|--------|
| 1  | studentId | Long   | 学生唯一标识 | 
| 2  | name      | String | 学生姓名   |
| 3  | major     | String | 专业     |
| 4  | address   | String | 地址     |
| 5  | phone     | String | 电话号码   |

## 接口类

### 学生信息服务接口 `StudentInfoService`

| 序号 | 方法名            | 输入参数        | 返回值         | 说明     |
|----|----------------|-------------|-------------|--------|
| 1  | getStudentInfo | studentId   | StudentInfo | 获取学生信息 |
| 2  | submitChanges  | StudentInfo | void        | 提交学生信息 |

## 数据库设计

### 学生信息表（student_info）：

```sql
CREATE TABLE IF NOT EXISTS student_info
(

    student_id INTEGER PRIMARY KEY,
    name       TEXT,
    major      TEXT,
    address    TEXT,
    phone      TEXT
);
```

| 序号 | 字段名        | 类型      | 约束          | 说明     |
|----|------------|---------|-------------|--------|
| 1  | student_id | INTEGER | PRIMARY KEY | 学生唯一标识 |
| 2  | name       | TEXT    |             | 学生姓名   |
| 3  | major      | TEXT    |             | 专业     |
| 4  | address    | TEXT    |             | 地址     |
| 5  | phone      | TEXT    |             | 电话号码   |
