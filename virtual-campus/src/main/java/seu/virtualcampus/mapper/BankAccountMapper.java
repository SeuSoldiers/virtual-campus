// BankAccountMapper.java
package seu.virtualcampus.mapper;

import seu.virtualcampus.domain.BankAccount;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BankAccountMapper {
    @Insert("INSERT INTO bank_account(accountNumber, userId, accountType, balance, status, createdDate) " +
            "VALUES(#{accountNumber}, #{userId}, #{accountType}, #{balance}, #{status}, #{createdDate})")
    int insertAccount(BankAccount account);

    @Select("SELECT * FROM bank_account WHERE accountNumber = #{accountNumber}")
    BankAccount selectByAccountNumber(String accountNumber);

    @Select("SELECT * FROM bank_account WHERE userId = #{userId}")
    List<BankAccount> selectByUserId(String userId);

    @Update("UPDATE bank_account SET balance = #{balance} WHERE accountNumber = #{accountNumber}")
    int updateBalance(@Param("accountNumber") String accountNumber, @Param("balance") BigDecimal balance);

    @Update("UPDATE bank_account SET status = #{status} WHERE accountNumber = #{accountNumber}")
    int updateStatus(@Param("accountNumber") String accountNumber, @Param("status") String status);
}