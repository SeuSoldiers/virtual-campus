# 银行系统组件设计

## 对外接口 `/api/bank/`

### 用户功能接口

> 以下接口通常需要 `Authorization: Bearer <user_token>` 请求头，且操作的账户需属于当前登录用户。

#### 1. 账户开户 [`openAccount`]

**POST** `/account/open`

*   **说明**: 为用户创建一个新的银行账户。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountType": "SAVINGS", 
      "initialDeposit": 1000.00,
      "password": "my_encrypted_password" 
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "accountNumber": "20250701120001",
        "accountType": "SAVINGS",
        "balance": 1000.00
      },
      "message": "Account opened successfully."
    }
    ```
*   **响应失败 (HTTP 400)**:
    ```json
    {
      "success": false,
      "message": "User already has an account of this type."
    }
    ```

#### 2. 账户存款 [`deposit`]

**POST** `/transaction/deposit`

*   **说明**: 向指定账户存入金额。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "amount": 500.00
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "transactionId": "T20250701123456",
        "newBalance": 1500.00
      },
      "message": "Deposit successful."
    }
    ```
*   **响应失败 (HTTP 404)**:
    ```json
    {
      "success": false,
      "message": "Account not found."
    }
    ```

#### 3. 账户取款 [`withdraw`]

**POST** `/transaction/withdraw`

*   **说明**: 从指定账户取出金额。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "amount": 200.00,
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "transactionId": "T20250701123457",
        "newBalance": 1300.00
      },
      "message": "Withdrawal successful."
    }
    ```
*   **响应失败 (HTTP 400)**:
    ```json
    {
      "success": false,
      "message": "Insufficient balance."
    }
    ```
*   **响应失败 (HTTP 401)**:
    ```json
    {
      "success": false,
      "message": "Invalid transaction password."
    }
    ```

#### 4. 执行转账 [`transfer`]

**POST** `/transaction/transfer`

*   **说明**: 在账户间进行转账。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "fromAccountNumber": "20250701120001",
      "toAccountNumber": "20250701120002",
      "amount": 100.00,
      "password": "my_encrypted_password",
      "remark": "Lunch money"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "transactionId": "T20250701123458"
      },
      "message": "Transfer successful."
    }
    ```
*   **响应失败 (HTTP 404)**:
    ```json
    {
      "success": false,
      "message": "Target account not found."
    }
    ```

#### 5. 查询账户信息 [`getAccountInfo`]

**GET** `/account/info/{accountNumber}`

*   **说明**: 查询指定账户的详细信息。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    ```
*   **路径参数**:
    - `accountNumber`: 要查询的账户号码
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "accountNumber": "20250701120001",
        "userId": "stu01",
        "accountType": "SAVINGS",
        "balance": 1200.00,
        "status": "NORMAL",
        "createdDate": "2024-07-01T10:30:00Z"
      }
    }
    ```
*   **响应失败 (HTTP 403)**:
    ```json
    {
      "success": false,
      "message": "Access denied. This account does not belong to you."
    }
    ```

#### 6. 查询交易记录 [`getTransactions`]

**GET** `/account/transactions`

*   **说明**: 查询指定时间范围内的交易流水。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    ```
*   **查询参数 (Query String)**:
    `?accountNumber=20250701120001&startDate=2024-07-01&endDate=2024-07-31`
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": [
        {
          "transactionId": "T20250701123456",
          "type": "DEPOSIT",
          "amount": 500.00,
          "fromAccountNumber": null,
          "toAccountNumber": "20250701120001",
          "time": "2024-07-01T10:30:00Z",
          "remark": null
        },
        {
          "transactionId": "T20250701123458",
          "type": "TRANSFER",
          "amount": -100.00,
          "fromAccountNumber": "20250701120001",
          "toAccountNumber": "20250701120002",
          "time": "2024-07-01T14:22:15Z",
          "remark": "Lunch money"
        }
      ]
    }
    ```

#### 7. 活期转定期 [`demandToFixed`]

**POST** `/transaction/convert-to-fixed`

*   **说明**: 将活期存款转为定期存款。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "amount": 1000.00,
      "term": 12, // 定期月数，如 3, 12, 36
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "transactionId": "T20250701123459",
        "fixedDepositAccountNumber": "FD20250701120001" // 新生成的定期账户号
      },
      "message": "Successfully converted to fixed deposit."
    }
    ```
*   **响应失败 (HTTP 400)**:
    ```json
    {
      "success": false,
      "message": "Amount exceeds available balance."
    }
    ```

#### 8. 定期转活期 [`fixedToDemand`]

**POST** `/transaction/convert-to-demand`

*   **说明**: 将定期存款提前转为活期存款（可能有利息损失）。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "FD20250701120001", // 定期账户号
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "transactionId": "T20250701123460",
        "amountReturned": 1005.00, // 返回的金额（本金+可能的部分利息）
        "destinationAccountNumber": "20250701120001" // 资金转入的活期账户
      },
      "message": "Fixed deposit terminated successfully."
    }
    ```

#### 9. 账户挂失 [`reportLoss`]

**POST** `/account/report-loss`

*   **说明**: 挂失账户，将其状态设置为 FROZEN。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account reported lost and frozen successfully."
    }
    ```

#### 10. 解除挂失 [`reportUnloss`]

**POST** `/account/report-unloss`

*   **说明**: 解除账户挂失，将其状态恢复为 NORMAL。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account loss report removed successfully."
    }
    ```

#### 11. 账户销户 [`closeAccount`]

**POST** `/account/close`

*   **说明**: 关闭指定账户（余额须为零）。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "password": "my_encrypted_password"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account closed successfully."
    }
    ```
*   **响应失败 (HTTP 400)**:
    ```json
    {
      "success": false,
      "message": "Cannot close account with non-zero balance."
    }
    ```

#### 12. 申请分期 [`applyForStaging`]

**POST** `/staging/apply`

*   **说明**: 为商店消费申请分期付款。
*   **请求头**:
    ```http
    Authorization: Bearer <user_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "amount": 1200.00, // 分期总金额
      "stages": 12, // 分期期数
      "password": "my_encrypted_password",
      "storeOrderId": "ORDER_123456" // 关联的商店订单ID
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "stagingPlanId": "SP20250701120001",
        "monthlyPayment": 100.00
      },
      "message": "Staging plan application submitted successfully."
    }
    ```
*   **响应失败 (HTTP 400)**:
    ```json
    {
      "success": false,
      "message": "Credit assessment failed. Application denied."
    }
    ```

---

### 管理员功能接口

> 以下接口需要 `Authorization: Bearer <admin_token>` 请求头，且token需具有管理员权限。

#### 13. (管理员) 查询任意账户 [`queryAccount`]

**GET** `/admin/account`

*   **说明**: 管理员查询任意账户的详细信息。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    ```
*   **查询参数 (Query String)**:
    `?accountNumber=20250701120001` 或 `?userId=stu01`
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "accountNumber": "20250701120001",
        "userId": "stu01",
        "accountType": "SAVINGS",
        "balance": 1200.00,
        "status": "NORMAL",
        "createdDate": "2024-07-01T10:30:00Z"
      }
    }
    ```
*   **响应失败 (HTTP 404)**:
    ```json
    {
      "success": false,
      "message": "Account not found."
    }
    ```

#### 14. (管理员) 查询全系统交易 [`queryAllTransactions`]

**GET** `/admin/transactions`

*   **说明**: 管理员根据条件查询全系统的交易记录。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    ```
*   **查询参数 (Query String)**:
    `?accountNumber=20250701120001&type=TRANSFER&startDate=2024-07-01&endDate=2024-07-31`
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": [
        {
          "transactionId": "T20250701123458",
          "type": "TRANSFER",
          "amount": 100.00,
          "fromAccountNumber": "20250701120001",
          "toAccountNumber": "20250701120002",
          "time": "2024-07-01T14:22:15Z",
          "remark": "Lunch money"
        }
        // ... 其他交易记录
      ]
    }
    ```

#### 15. (管理员) 强制冻结账户 [`forceFreezeAccount`]

**POST** `/admin/account/freeze`

*   **说明**: 管理员强制冻结账户。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "reason": "Suspicious activity detected"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account frozen successfully."
    }
    ```

#### 16. (管理员) 强制解冻账户 [`forceUnfreezeAccount`]

**POST** `/admin/account/unfreeze`

*   **说明**: 管理员强制解冻账户。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001"
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account unfrozen successfully."
    }
    ```

#### 17. (管理员) 强制销户 [`forceCloseAccount`]

**POST** `/admin/account/close`

*   **说明**: 管理员强制关闭账户（可能涉及余额处理）。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "accountNumber": "20250701120001",
      "refundAccountNumber": "20250701120002" // 可选，余额退至此账户
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "message": "Account force-closed successfully."
    }
    ```

#### 18. (管理员) 为用户创建账户 [`createAccountForUser`]

**POST** `/admin/account/create`

*   **说明**: 管理员直接为用户创建一个账户。
*   **请求头**:
    ```http
    Authorization: Bearer <admin_token>
    Content-Type: application/json
    ```
*   **请求体 (JSON)**:
    ```json
    {
      "userId": "stu02",
      "accountInfo": {
        "accountType": "SAVINGS",
        "initialDeposit": 0.00,
        "password": "default_encrypted_password" // 通常由系统生成初始密码
      }
    }
    ```
*   **响应成功 (HTTP 200)**:
    ```json
    {
      "success": true,
      "data": {
        "accountNumber": "20250701120003"
      },
      "message": "Account created for user successfully."
    }
    ```
## 实体类

### 银行账户类 `BankAccount`
<!--
    public class BankAccount {
    private String accountNumber;      // 账户唯一号码
    private String userId;             // 账户所属用户的唯一ID
    private String password;           // 【重要】经过加密哈希处理的支付密码
    private String accountType;        // 账户类型（SAVINGS, CHECKING）
    private BigDecimal balance;        // 账户余额
    private String status;             // 账户状态（NORMAL, FROZEN, CLOSED）
    private LocalDateTime createdDate; // 开户日期
    
    // ... 构造函数 ...
    
    // ... Getter 和 Setter 方法 ...
}
-->

| 序号 | 名称               | 类型      | 说明                                     |
|----|------------------|---------|----------------------------------------|
| 1  | accountNumber   | String  | 账户唯一号码                                 |
| 2  | userId          | String  | 账户所属用户的唯一ID                             |
| 3  | password   | String         | 账户密码       |
| 4  | accountType     | String  | 账户类型（USER/ADMINISTRATOR）       |
| 5  | balance          | BigDecimal | 账户余额                                   |
| 6  | status           | String  | 账户状态（NORMAL, FROZEN, CLOSED）           |
| 7  | createdDate           | Date  | 开户日期           |


### 交易记录类 `Transaction`

| 序号 | 名称                 | 类型         | 说明                         |
|----|--------------------|------------|----------------------------|
| 1  | transactionId     | String     | 交易唯一流水号                   |
| 2  | fromAccountNumber | String     | 转出账户号码                    |
| 3  | toAccountNumber  | String     | 转入账户号码                    |
| 4  | amount             | BigDecimal | 交易金额                      |
| 5  | transactionType   | String     | 交易类型（TRANSFER, DEPOSIT, WITHDRAW,FIXED_TIME_ DEPOSIT（1、3、5）） |
| 6  | transactionTime   | Date       | 交易时间                      |
| 7  | remark             | String     | 备注                         |
| 8  | status             | String     |  交易状态（PENDING/SUCCESS/FAILED）                       |
## 接口类

### Client端：BankClientController(银行客户端控制器)

| 序号 | 方法名             | 输入参数                               | 返回值              | 说明               |
|------|--------------------|----------------------------------------|---------------------|--------------------|
| 1    | openAccount        | userId, accountType, initialDeposit, password | BankAccount         | 用户开户           |
| 2    | deposit            | accountNumber, amount                  | Transaction         | 存款业务           |
| 3    | withdraw           | accountNumber, amount, password        | Transaction         | 取款业务           |
| 4    | transfer           | fromAccountNumber, toAccountNumber, amount, password | Transaction | 转账汇款业务       |
| 5    | getAccountInfo     | accountNumber                          | BankAccount         | 个人信息查询       |
| 6    | getTransactions    | accountNumber, startDate, endDate      | List&lt;Transaction&gt;   | 交易记录查询       |
| 7    | demandToFixed      | accountNumber, amount, term, password  | Transaction         | 活期转定期业务     |
| 8    | fixedToDemand      | accountNumber, password                | Transaction         | 定期转活期业务     |
| 9    | applyForStaging    | accountNumber, amount, stages, password | Boolean            | 申请商店分期功能   |
| 10   | reportLoss         | accountNumber, password                | Boolean             | 账户挂失功能       |
| 11   | reportUnloss       | accountNumber, password                | Boolean             | 账户解除挂失功能   |
| 12   | closeAccount       | accountNumber, password                | Boolean             | 账户销户功能       |

### Server端：BankService(银行服务接口)

| 序号 | 方法名                 | 输入参数                               | 返回值              | 说明                 |
|------|------------------------|----------------------------------------|---------------------|----------------------|
| 1    | createAccount          | userId, accountType, initialDeposit, password | BankAccount         | 创建新账户           |
| 2    | processDeposit         | accountNumber, amount                  | Transaction         | 处理存款逻辑         |
| 3    | processWithdrawal      | accountNumber, amount, password        | Transaction         | 处理取款逻辑         |
| 4    | processTransfer        | fromAccountNumber, toAccountNumber, amount, password | Transaction | 处理转账逻辑         |
| 5    | processGetAccountInfo  | accountNumber                          | BankAccount         | 获取账户余额         |
| 6    | getTransactionHistory  | accountNumber, timeRange               | List&lt;Transaction&gt;   | 获取交易历史记录     |
| 7    | convertToFixedDeposit  | accountNumber, amount, term, password  | Transaction         | 执行活期转定期操作   |
| 8    | convertToDemandDeposit | accountNumber, password                | Transaction         | 执行定期转活期操作   |
| 9    | handleStagingPlan      | accountNumber, amount, stages, password | Boolean            | 处理分期付款申请     |
| 10   | freezeAccount          | accountNumber, password                | Boolean             | （管理员）冻结账户   |
| 11   | unfreezeAccount        | accountNumber                          | Boolean             | （管理员）解冻账户   |
| 12   | closeAccount           | accountNumber, password                | Boolean             | 处理账户销户         |
| 13   | updateAccountStatus    | accountNumber, newStatus               | boolean             | 更新账户状态         |

### Server端：BankAdminService(银行管理服务接口)

| 序号 | 方法名               | 输入参数                 | 返回值            | 说明                 |
|------|----------------------|--------------------------|-------------------|----------------------|
| 1    | queryAccount         | accountNumber/userId     | BankAccount       | 查询任意账户信息     |
| 2    | queryAllTransactions | filters (account, date, type) | List&lt;Transaction&gt; | 全系统交易记录查询   |
| 3    | forceFreezeAccount   | accountNumber, reason    | Boolean           | 强制冻结账户         |
| 4    | forceUnfreezeAccount | accountNumber            | Boolean           | 强制解冻账户         |
| 5    | forceCloseAccount    | accountNumber            | Boolean           | 强制销户             |
| 6    | createAccountForUser | userId, accountInfo      | BankAccount       | 为用户创建账户       |
## 数据库设计

### 银行账户表（bank_accounts）：

```sql
CREATE TABLE bank_accounts
(
    accountNumber VARCHAR(20) PRIMARY KEY,
    userId        TEXT      NOT NULL,
    password      VARCHAR(100) NOT NULL,
     accountType VARCHAR(20) NOT NULL DEFAULT 'USER',
    balance        DECIMAL(15, 2) DEFAULT 0.00,
    status         TEXT      NOT NULL DEFAULT 'NORMAL',
    createdDate     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

| 序号 | 字段名           | 类型           | 约束                  | 说明         |
|----|----------------|--------------|---------------------|------------|
| 1  | accountNumber | VARCHAR(20)  | PRIMARY KEY         | 账户唯一号码     |
| 2  | userId        | TEXT         | NOT NULL            | 用户ID       |
| 3 | password   | VARCHAR(100)         | NOT NULL            | 账户密码       |
| 4  | accountType   | VARCHAR(20)         | NOT NULL DEFAULT 'USER'            | 账户类型       |
| 5  | balance        | DECIMAL(15,2)| NOT NULL DEFAULT 0.00 | 账户余额       |
| 6  | status         | TEXT         | NOT NULL DEFAULT 'NORMAL' | 账户状态       |
| 7  | createdDate     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 账户创建时间     |

### 交易记录表（bank_transactions）：

```sql
CREATE TABLE bank_transactions
(
    transactionId     VARCHAR(30) PRIMARY KEY,
    fromAccountNumber TEXT NOT NULL,
    toAccountNumber   TEXT NOT NULL,
    amount            DECIMAL(15, 2) NOT NULL,
    transactionType  VARCHAR(20) NOT NULL,
    transactionTime  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark            TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);
```

| 序号 | 字段名               | 类型           | 约束                  | 说明         |
|----|--------------------|--------------|---------------------|------------|
| 1  | transactionId     | VARCHAR(30)  | PRIMARY KEY         | 交易唯一流水号   |
| 2  | fromAccountNumber | TEXT         | NOT NULL            | 转出账户号码     |
| 3  | toAccountNumber  | TEXT         | NOT NULL            | 转入账户号码     |
| 4  | amount             | DECIMAL(15,2)| NOT NULL            | 交易金额       |
| 5  | transactionType   | VARCHAR(20)         | NOT NULL            | 交易类型（TRANSFER/DEPOSIT/WITHDRAW/FIXED_TIME_ DEPOSIT（1、3、5））       |
| 6  | transactionTime   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 交易时间       |
| 7  | remark             | TEXT         |                     | 备注         |
| 8  | status             | VARCHAR(20)        |     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SUCCESS','FAILED'))                | 交易状态（PENDING/SUCCESS/FAILED）         |