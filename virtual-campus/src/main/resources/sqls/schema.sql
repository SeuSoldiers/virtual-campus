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