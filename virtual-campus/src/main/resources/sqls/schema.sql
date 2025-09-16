PRAGMA foreign_keys = ON;


CREATE TABLE IF NOT EXISTS users
(
    username INTEGER PRIMARY KEY,
    password TEXT NOT NULL,
    role     TEXT NOT NULL
);


CREATE TABLE IF NOT EXISTS student_info
(

    student_id INTEGER PRIMARY KEY,
    name       TEXT,
    major      TEXT,
    address    TEXT,
    phone      TEXT
);


CREATE TABLE IF NOT EXISTS audit_record
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id  INTEGER NOT NULL,
    field       TEXT    NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    status      TEXT    NOT NULL,
    reviewer_id INTEGER,
    FOREIGN KEY (student_id) REFERENCES student_info (student_id)
);

-- 银行账户表
CREATE TABLE IF NOT EXISTS bank_account (
    accountNumber TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    password TEXT NOT NULL,
    accountType TEXT NOT NULL,
    Balance REAL CHECK(Balance >= 0) DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    createdDate TEXT NOT NULL
);

-- 交易记录表
CREATE TABLE IF NOT EXISTS banktransaction (
   transactionId TEXT PRIMARY KEY,
   fromAccountNumber TEXT,
   toAccountNumber TEXT,
   amount REAL CHECK(amount >= 0) NOT NULL,
   transactionType TEXT NOT NULL,
   transactionTime TEXT NOT NULL,
   remark TEXT,
   status TEXT NOT NULL,
   FOREIGN KEY (fromAccountNumber) REFERENCES bank_account(accountNumber),
   FOREIGN KEY (toAccountNumber) REFERENCES bank_account(accountNumber)
);

-- 图书表
CREATE TABLE IF NOT EXISTS book_info (
                                         isbn TEXT PRIMARY KEY,
                                         title TEXT,
                                         author TEXT,
                                         publisher TEXT,
                                         category TEXT,
                                         publishDate TEXT
);

CREATE TABLE IF NOT EXISTS book_copy (
                                         bookId TEXT PRIMARY KEY,
                                         isbn TEXT,
                                         location TEXT,
                                         status TEXT, -- IN_LIBRARY, BORROWED, RESERVED
                                         FOREIGN KEY (isbn) REFERENCES book_info(isbn)
    );

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
      recordId TEXT PRIMARY KEY,
      userId TEXT NOT NULL,
      bookId TEXT NOT NULL,
      borrowDate TEXT NOT NULL,
      dueDate TEXT NOT NULL,
      returnDate TEXT,
      renewCount INTEGER CHECK(renewCount >= 0) DEFAULT 0,
      status TEXT NOT NULL
);

-- 预约记录表
CREATE TABLE IF NOT EXISTS reservation_records (
       reservationId TEXT PRIMARY KEY,
       userId TEXT NOT NULL,
       isbn TEXT NOT NULL,
       reserveDate TEXT NOT NULL,
       status TEXT NOT NULL,
       queuePosition INTEGER CHECK(queuePosition >= 1)
);

CREATE TABLE IF NOT EXISTS course (
        courseId TEXT PRIMARY KEY,
        courseName TEXT NOT NULL,
        courseTeacher TEXT NOT NULL,
        courseCredit INTEGER CHECK(courseCredit >= 0),
        courseCapacity INTEGER CHECK(courseCapacity >= 0),
        coursePeopleNumber INTEGER CHECK(coursePeopleNumber >= 0) DEFAULT 0,
        courseTime TEXT,
        courseLocation TEXT
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
       productId TEXT PRIMARY KEY,
       productName TEXT NOT NULL,
       productPrice REAL CHECK(productPrice >= 0),
       availableCount INTEGER CHECK(availableCount >= 0),
       productType TEXT
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
      orderId TEXT PRIMARY KEY,
      userId TEXT NOT NULL,
      totalAmount REAL CHECK(totalAmount >= 0),
      status TEXT,
      orderDate TEXT,
      paymentMethod TEXT,
      paymentStatus TEXT,
      createdAt TEXT,
      updatedAt TEXT
);

-- 创建订单商品表
CREATE TABLE IF NOT EXISTS order_item (
      itemId TEXT PRIMARY KEY,
      orderId TEXT NOT NULL,
      quantity INTEGER CHECK(quantity > 0),
      productId TEXT NOT NULL,
      unitPrice REAL CHECK(unitPrice >= 0),
      subtotal REAL CHECK(subtotal >= 0),
      FOREIGN KEY (orderId) REFERENCES orders(orderId),
      FOREIGN KEY (productId) REFERENCES product(productId)
);

-- 创建购物车表
CREATE TABLE IF NOT EXISTS cart (
    cartItemId TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    productId TEXT NOT NULL,
    quantity INTEGER CHECK(quantity > 0),
    isActive INTEGER DEFAULT 1,
    FOREIGN KEY (productId) REFERENCES product(productId)
);
-- 为book_info表添加索引
CREATE INDEX IF NOT EXISTS idx_bookinfo_title ON book_info(title);
CREATE INDEX IF NOT EXISTS idx_bookinfo_author ON book_info(author);
CREATE INDEX IF NOT EXISTS idx_bookinfo_category ON book_info(category);

-- 为book_copy表添加索引
CREATE INDEX IF NOT EXISTS idx_bookcopy_isbn ON book_copy(isbn);
CREATE INDEX IF NOT EXISTS idx_bookcopy_status ON book_copy(status);
CREATE INDEX IF NOT EXISTS idx_bookcopy_location ON book_copy(location);

-- 为borrow_records表添加索引
CREATE INDEX IF NOT EXISTS idx_borrow_records_userId ON borrow_records(userId);
CREATE INDEX IF NOT EXISTS idx_borrow_records_bookId ON borrow_records(bookId);
CREATE INDEX IF NOT EXISTS idx_borrow_records_status ON borrow_records(status);

-- 为reservation_records表添加索引
CREATE INDEX IF NOT EXISTS idx_reservation_records_userId ON reservation_records(userId);
CREATE INDEX IF NOT EXISTS idx_reservation_records_isbn ON reservation_records(isbn);
CREATE INDEX IF NOT EXISTS idx_reservation_records_status ON reservation_records(status);
