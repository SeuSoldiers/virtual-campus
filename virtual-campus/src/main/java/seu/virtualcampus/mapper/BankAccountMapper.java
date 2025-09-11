// BankAccountMapper.java
package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BankAccount;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BankAccountMapper {

    // 根据账户号查询账户
    @Select("SELECT * FROM bank_account WHERE accountNumber = #{accountNumber}")
    BankAccount selectByAccountNumber(String accountNumber);

    // 根据用户ID查询账户列表
    @Select("SELECT * FROM bank_account WHERE userId = #{userId}")
    List<BankAccount> selectByUserId(String userId);

    // 插入新账户
    @Insert("INSERT INTO bank_account(accountNumber, userId, password, accountType, balance, status, createdDate) " +
            "VALUES(#{accountNumber}, #{userId}, #{password}, #{accountType}, #{balance}, #{status}, #{createdDate})")
    int insertAccount(BankAccount account);

    // 更新账户信息
    @Update("UPDATE bank_account SET password=#{password}, accountType=#{accountType}, " +
            "balance=#{balance}, status=#{status} WHERE accountNumber=#{accountNumber}")
    int updateAccount(BankAccount account);

    // 更新账户余额
    @Update("UPDATE bank_account SET balance = #{balance} WHERE accountNumber = #{accountNumber}")
    int updateBalance(@Param("accountNumber") String accountNumber, @Param("balance") BigDecimal balance);

    // 更新账户状态
    @Update("UPDATE bank_account SET status = #{status} WHERE accountNumber = #{accountNumber}")
    int updateStatus(@Param("accountNumber") String accountNumber, @Param("status") String status);

    // 删除账户
    @Delete("DELETE FROM bank_account WHERE accountNumber = #{accountNumber}")
    int deleteAccount(String accountNumber);

    // 存款操作
    @Update("UPDATE bank_account SET balance = balance + #{amount} WHERE accountNumber = #{accountNumber}")
    int deposit(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);

    // 取款操作
    @Update("UPDATE bank_account SET balance = balance - #{amount} WHERE accountNumber = #{accountNumber}")
    int withdraw(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);

    // 返回所有账户列表
    @Select("SELECT * FROM bank_account")
    List<BankAccount> selectAllAccounts();


}