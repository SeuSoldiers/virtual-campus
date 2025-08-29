## 对外接口
### 1. 查询图书（支持多条件搜索）

**POST** `/api/library/books/query`
*   **输入：**

```json
{
  "isbn": "978711554...", 
  "title": "Java",        
  "author": "Bruce",      
  "category": "计算机"    
  //所有字段均可选
}
```
*   **输出（成功）：**
```json
{
  "code": 0,
  "message": "查询成功",
  "data": {
    "books": [
      {
        "bookId": "B001",
        "title": "Java核心技术 卷I",
        "author": "Cay S. Horstmann",
        "isbn": "978711554...",
        "category": "计算机",
        "totalCount": 5,
        "availableCount": 3,//可借数量
        "location": "3楼A区12排"
      }
    ]
  }
}
```
*   **输出（失败）：**
```json
{
  "code": 1001,
  "message": "未查询到符合条件的图书",
  "data": {}
}
```
### 2. 借阅图书

**POST** `/api/library/borrow`
*   **输入：**

```json
{
  "userId":"stu124",
  "bookId": "B001"
}
```
*   **输出（成功）：**
```json
{
  "code": 0,
  "message": "借阅成功",
  "data": {
    "recordId": "BR202508250001",
    "dueDate": "2025-09-25"
  }
}
```
*   **输出（失败）：**
```json
{
  "code": 1002,
  "message": "当前图书无可借阅库存",
  "data": {}
}
```
```json
{
  "code": 1003,
  "message": "已达学生最大借阅上限",
  "data": {}
}
```
### 3. 归还图书

**POST** `/api/library/return`

*   **输入：**

```json
{
  "recordId": "BR202508250001"
}
```
*   **输出：**
```json
{
  "code": 0,
  "message": "归还成功"
}
```
### 4. 续借图书
**POST** `/api/library/renew`
*   **输入：**
```json
{
  "recordId": "BR202508250001"
}
```
*   **输出（成功）：**
```json
{
  "code": 0,
  "message": "续借成功",
  "data": {
    "newDueDate": "2025-10-25"
  }
}
```
*   **输出（失败）：**
```json
{
  "code": 1005,
  "message": "该图书已达最大续借次数",
  "data": {}
}
```
### 5. 获取借阅记录

**POST** `/api/library/records/query`
*   **输入：**
```json
{
  "queryType": "BY_USER",  // 查询类型（必选：BY_USER-按用户查，BY_BOOK-按图书查，ALL-全量查）
  "userId": "stu123",      // 当queryType=BY_USER时必选（用户ID）
  "bookId": "B001",        // 当queryType=BY_BOOK时必选（图书ID）
  "status": "BORROWED",    // 记录状态（可选：BORROWED-已借出，RETURNED-已归还，OVERDUE-已逾期）
}
```
*   **输出（成功）：**
```json
{
  "code": 0,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "recordId": "BR202508250001",
        "userId": "stu123",
        "userName": "张三",
        "bookId": "B001",
        "bookTitle": "Java核心技术 卷I",
        "borrowDate": "2025-08-25",
        "dueDate": "2025-09-25",
        "returnDate": null,
        "status": "BORROWED"
      }
    ],
    "total": 1
  }
}
```
*   **输出（失败）：**
```json
{
  "code": 1006,
  "message": "您无权限查询全量借阅记录，仅图书管理员可操作",
  "data": {}
}
```
### 6.管理图书（图书管理员）
**POST** `/api/library/books/manage`
*   **输入：**
```json
{
  "action": "CREATE",  // 操作类型（必选：CREATE-新增，UPDATE-更新，DELETE-删除，QUERY-查询）
  "bookData": {        // 当action=CREATE/UPDATE时必选（图书信息）
    "bookId": "B001",  // UPDATE时必选（系统生成），CREATE时可选（系统自动生成）
    "title": "Java核心技术 卷I", 
    "author": "Cay S. Horstmann",
    "isbn": "978711554...",
    "category": "计算机", 
    "publishDate": "2018-06-01", 
    "publisher": "人民邮电出版社",
    "totalCount": 5, 
    "availableCount": 5,
    "location": "3楼A区12排"
  },
  "queryCondition": {  // 当action=QUERY/DELETE时必选（查询/删除条件）
    "isbn": "978711554...", 
    "title": "Java", 
    "author": "Bruce",  
    "category": "计算机" //可选
  }
}
```
*   **输出(Action: QUERY)：**
```json
{
  "code": 0,
  "message": "查询成功",
  "data": {
    "books": [
      {
        "bookId": "B001",
        "title": "Java核心技术 卷I",
        "author": "Cay S. Horstmann",
        "isbn": "978711554...",
        "category": "计算机",
        "totalCount": 5,
        "availableCount": 3,
        "location": "3楼A区12排",
        "publishDate": "2018-06-01",
        "publisher": "人民邮电出版社"
      }
    ],
    "total": 1 // 符合条件的图书总数
  }
}
```
*   **输出(Action: CREATE)：**
```json
{
  "code": 0,
  "message": "图书新增成功",
  "data": {
    "bookId": "B002" // 系统生成的图书唯一标识
  }
}
```
*   **输出(Action: UPDATE/DELETE)：**
```json
{
  "code": 0,
  "message": "操作成功"
}
```
### 7. 预约图书
**POST** `/api/library/reserve`
*   **输入：**
```json
{
  "userId": "stu124",
  "bookId": "B001"
}
```
*   **输出（成功）：**
```json
{
  "code": 0,
  "message": "预约成功",
  "data": {
    "reservationId": "RSV202508250001",
    "expectedAvailableDate": "2025-09-10",
    "queuePosition": 3
  }
}
```
*   **输出（失败）：**
```json
{
  "code": 1007,
  "message": "该图书当前有可借库存，无需预约",
  "data": {}
}
```
```json
{
  "code": 1008,
  "message": "您已预约过该图书，请勿重复预约",
  "data": {}
}
```
```json
{
  "code": 1009,
  "message": "已达最大预约数量限制",
  "data": {}
}
```
### 8. 取消预约
**POST** `/api/library/reserve/cancel`
*   **输入：**
```json
{
  "reservationId": "RSV202508250001"
}
```
*   **输出：**
```json
{
  "code": 0,
  "message": "取消预约成功"
}
```


# 类
## 实体类
### 1.Book图书类

| 序号 | 名称       | 类型     | 约束   | 说明                               |
|----|----------|--------|------|----------------------------------|
| 1  | bookId  | String | 主键   | 图书唯一标识                           |
| 2  | title    | String | 非空   | 图书标题                             |
| 3  | author | String | 非空 | 作者                             |
| 4  | isbn | String | 唯一  | ISBN号 |
| 5  | category	|String	|	|图书分类
6	|publishDate	|Date	||	出版日期
7	|publisher	|String||		出版社
8	|totalCount	|int	|非空，≥0|	总数量
9	|availableCount	|int	|非空，≥0，≤totalCount|	可借数量
10	|location|	String	||	馆藏位置
11 |reservationCount|INT|非空，≥0|预约人数
### 2.BorrowRecord借阅记录类

| 序号 | 名称       | 类型     | 约束   | 说明                               |
|----|----------|--------|------|----------------------------------|
| 1  | recordId  | String | 主键   | 记录唯一标识                           |
| 2  |userId	|String	|外键	|关联用户ID
3	|bookId	|String|	外键|	关联图书ID
4	|borrowDate|	Date	|非空	|借出日期
5	|dueDate	|Date	|非空	|应还日期
6	|returnDate|	Date	||	实际归还日期
7	|renewCount	|int	|默认0	|续借次数
8	|status	|Enum	|非空	|状态(BORROWED/RETURNED/OVERDUE)
### 3.ReservationRecord 预约记录类
|序号|	名称|	类型|	约束|	说明|
|----|----------|--------|------|----------------------------------|
1	|reservationId	|String|	主键	|预约记录唯一标识
2	|userId	|String|	外键	|关联用户ID
3	|bookId|	String|	外键	|关联图书ID
4	|reserveDate|	Date	|非空	|预约日期
5	|status	|Enum	|非空|	状态(ACTIVE-有效, CANCELLED-已取消, FULFILLED-已履约)
6	|expectedAvailableDate	|Date	|	|预计可借日期
7	|queuePosition	|int	|非空	|排队位置
8	|notifyStatus	|Enum		||通知状态(NONE-未通知, NOTIFIED-已通知)
## 接口类
### 1. LibraryService (图书馆服务接口)
|序号	|名称	|方法	|说明|
|----|----------|--------|------|
1	|查询图书	|List<Book> searchBooks(BookQuery query)	|根据条件搜索图书
2	|借阅图书	|BorrowResult borrowBook(String userId, String bookId)	|执行借书操作
3	|归还图书	|ReturnResult returnBook(String recordId)	|执行还书操作
4	|续借图书	|RenewResult renewBook(String recordId)	|执行续借操作
5	|获取借阅记录	|List<BorrowRecord> getBorrowRecords(BorrowQuery query)	|查询借阅记录
6	|管理图书	|ManageResult manageBook(BookAction action, Book book)	|管理图书(增删改查)
7|预约图书|ReservationResult reserveBook(String userId, String bookId)|执行预约图书操作
8|取消预约|CancelReservationResult cancelReservation(String reservationId)|取消预约
9|获取预约记录|List<ReservationRecord> getUserReservations(String userId)|查询预约记录
10|处理预约通知(当图书归还时调用)|void processReservationOnReturn(String bookId)|处理预约通知，当图书归还时调用

### 2. LibraryAPIController (图书馆API控制器)
|序号|	名称	|端点	|说明|
|----|----------|--------|------|
1	|查询图书	|POST| /api/library/books/query	|对外提供的查询图书接口
2	|借阅图书	|POST /api/library/borrow	|对外提供的借书接口
3	|归还图书	|POST /api/library/return	|对外提供的还书接口
4	|续借图书	|POST /api/library/renew	|对外提供的续借接口
5	|获取借阅记录	|POST /api/library/records/query	|对外提供的查询借阅记录接口
6	|管理图书	|POST /api/library/books/manage	|图书管理接口(需管理员权限)
7|预约图书|POST /api/library/reserve|对外提供的预约接口
8|取消预约|POST /api/library/reserve/cancel|对外提供的取消预约接口
9|查询用户预约记录|POST /api/library/reservations/query|对外提供的查询预约记录接口


# 数据表
### 1. 图书表

| 序号 | 字段名          | 类型         | 约束                          | 说明               |
|------|-----------------|--------------|-------------------------------|--------------------|
| 1    | bookId         | VARCHAR(50)  | PRIMARY KEY                   | 图书唯一标识       |
| 2    | title           | VARCHAR(200) | NOT NULL                      | 图书标题           |
| 3    | author          | VARCHAR(100) | NOT NULL                      | 作者               |
| 4    | isbn            | VARCHAR(20)  | UNIQUE                        | ISBN号             |
| 5    | category        | VARCHAR(50)  | -                             | 图书分类           |
| 6    | publishDate    | DATE         | -                             | 出版日期           |
| 7    | publisher       | VARCHAR(100) | -                             | 出版社             |
| 8    | totalCount     | INT          | NOT NULL, DEFAULT 0           | 总数量             |
| 9    | availableCount | INT          | NOT NULL, DEFAULT 0           | 可借数量           |
| 10   | location        | VARCHAR(100) | -                             | 馆藏位置           |
11|reservationCount|INT|DEFAULT 0|预约人数

```sql
CREATE TABLE library_books (
    book_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    publish_date DATE,
    publisher VARCHAR(100),
    total_count INTEGER NOT NULL DEFAULT 0 CHECK(total_count >= 0),
    available_count INTEGER NOT NULL DEFAULT 0 CHECK(available_count >= 0 AND available_count <= total_count),
    location VARCHAR(100),
    reservation_count INTEGER NOT NULL DEFAULT 0 CHECK(reservation_count >= 0),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```
### 2. 借阅记录表

| 序号 | 字段名       | 类型         | 约束                                      | 说明               |
|------|--------------|--------------|-------------------------------------------|--------------------|
| 1    | recordId    | VARCHAR(50)  | PRIMARY KEY                               | 记录唯一标识       |
| 2    | userId      | VARCHAR(50)  | FOREIGN KEY REFERENCES users(user_id)     | 关联用户ID（关联`users`表的`user_id`字段） |
| 3    | bookId      | VARCHAR(50)  | FOREIGN KEY REFERENCES library_books(bookId) | 关联图书ID（关联`library_books`表的`book_id`字段） |
| 4    | borrowDate  | DATETIME     | NOT NULL                                  | 借出日期           |
| 5    | dueDate     | DATETIME     | NOT NULL                                  | 应还日期           |
| 6    | returnDate  | DATETIME     | -                                         | 实际归还日期（可为空） |
| 7    | renewCount  | INT          | DEFAULT 0                                 | 续借次数           |
| 8    | status       | VARCHAR(20)  | NOT NULL                                  | 状态（如：借出、已归还、逾期等） |
```sql
CREATE TABLE borrow_records (
    record_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    borrow_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    renew_count INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES library_books(book_id)
);
```
### 3. 预约数据表
序号	|字段名|	类型	|约束|	说明
|------|--------------|--------------|-------------------------------------------|--------------------|
1	|reservation_id|	VARCHAR(50)	|PRIMARY KEY|	预约记录唯一标识
2	|user_id|	VARCHAR(50)	|FOREIGN KEY	|关联用户ID
3	|book_id|	VARCHAR(50)	|FOREIGN KEY|	关联图书ID
4	|reserve_date|	DATETIME|	NOT NULL|	预约日期
5	|status|	VARCHAR(20)|	NOT NULL	|状态(ACTIVE/CANCELLED/FULFILLED)
6	|expected_available_date|	DATETIME	||	预计可借日期
7	|queue_position	|INT	|NOT NULL|	排队位置
8	|notify_status|	VARCHAR(20)	|DEFAULT 'NONE'	|通知状态
```sql
CREATE TABLE reservation_records (
    reservation_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    reserve_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expected_available_date DATETIME,
    queue_position INTEGER NOT NULL,
    notify_status VARCHAR(20) DEFAULT 'NONE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES library_books(book_id)
);
```
### 4. 需补充的学生信息
| 序号 | 字段名       | 类型         | 约束                                      | 说明               |
|------|--------------|--------------|-------------------------------------------|--------------------|
| 1    |  currentBorrowedCount   |  INT |  NOT NULL, DEFAULT 0  | 当前已借阅图书数       |
2 |currentReservationCount|INT| NOT NULL, DEFAULT 0  | 当前已预约图书数    
```sql
ALTER TABLE users ADD COLUMN current_borrowed_count INTEGER NOT NULL DEFAULT 0 CHECK(current_borrowed_count >= 0);
ALTER TABLE users ADD COLUMN current_reservation_count INTEGER NOT NULL DEFAULT 0 CHECK(current_reservation_count >= 0);
```