// TransactionMapper.java
package seu.virtualcampus.mapper;

import seu.virtualcampus.domain.Transaction;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TransactionMapper {
    @Insert("INSERT INTO banktransaction(transactionId, fromAccountNumber, toAccountNumber, amount, " +
            "transactionType, transactionTime, remark, status) " +
            "VALUES(#{transactionId}, #{fromAccountNumber}, #{toAccountNumber}, #{amount}, " +
            "#{transactionType}, #{transactionTime}, #{remark}, #{status})")
    int insertTransaction(Transaction transaction);

    @Select("SELECT * FROM banktransaction WHERE fromAccountNumber = #{accountNumber} OR toAccountNumber = #{accountNumber} " +
            "ORDER BY transactionTime DESC")
    List<Transaction> selectByAccountNumber(String accountNumber);

    @Select("SELECT * FROM banktransaction WHERE (fromAccountNumber = #{accountNumber} OR toAccountNumber = #{accountNumber}) " +
            "AND transactionTime BETWEEN #{start} AND #{end} ORDER BY transactionTime DESC")
    List<Transaction> selectByAccountNumberAndTimeRange(@Param("accountNumber") String accountNumber,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);
}