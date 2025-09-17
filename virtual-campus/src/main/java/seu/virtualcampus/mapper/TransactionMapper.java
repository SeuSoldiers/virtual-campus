// TransactionMapper.java
package seu.virtualcampus.mapper;

import seu.virtualcampus.domain.Transaction;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 银行交易Mapper接口。
 * <p>
 * 定义了与数据库中banktransaction表相关的操作。
 */
@Mapper
public interface TransactionMapper {
    /**
     * 插入一条新的银行交易记录。
     *
     * @param transaction 要插入的交易记录对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO banktransaction(transactionId, fromAccountNumber, toAccountNumber, amount, " +
            "transactionType, transactionTime, remark, status) " +
            "VALUES(#{transactionId}, #{fromAccountNumber}, #{toAccountNumber}, #{amount}, " +
            "#{transactionType}, #{transactionTime}, #{remark}, #{status})")
    int insertTransaction(Transaction transaction);

    /**
     * 根据账户号码查询所有相关的交易记录（作为转出方或转入方）。
     *
     * @param accountNumber 账户号码。
     * @return 相关交易记录的列表，按交易时间降序排列。
     */
    @Select("SELECT * FROM banktransaction WHERE fromAccountNumber = #{accountNumber} OR toAccountNumber = #{accountNumber} " +
            "ORDER BY transactionTime DESC")
    List<Transaction> selectByAccountNumber(String accountNumber);

    /**
     * 根据账户号码和时间范围查询交易记录。
     *
     * @param accountNumber 账户号码。
     * @param start         查询的开始时间。
     * @param end           查询的结束时间。
     * @return 符合条件的交易记录列表，按交易时间降序排列。
     */
    @Select("SELECT * FROM banktransaction WHERE (fromAccountNumber = #{accountNumber} OR toAccountNumber = #{accountNumber}) " +
            "AND transactionTime BETWEEN #{start} AND #{end} ORDER BY transactionTime DESC")
    List<Transaction> selectByAccountNumberAndTimeRange(@Param("accountNumber") String accountNumber,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    /**
     * 根据交易ID查询交易记录。
     *
     * @param transactionId 交易ID。
     * @return 对应的交易记录对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM banktransaction WHERE transactionId = #{transactionId}")
    Transaction selectByTransactionId(String transactionId);

    /**
     * 更新交易的类型和备注。
     *
     * @param transaction 包含更新信息的交易对象。
     * @return 受影响的行数。
     */
    @Update("UPDATE banktransaction SET transactionType = #{transactionType}, remark = #{remark} WHERE transactionId = #{transactionId}")
    int updateTransactionTypeAndRemark_fc(Transaction transaction);

    /**
     * 更新交易的状态和备注。
     *
     * @param transactionId 交易ID。
     * @param status        新的状态。
     * @param remark        新的备注。
     * @return 受影响的行数。
     */
    @Update("UPDATE banktransaction SET status = #{status}, remark = #{remark} WHERE transactionId = #{transactionId}")
    int updateTransactionStatusAndRemark(@Param("transactionId") String transactionId,
                                         @Param("status") String status,
                                         @Param("remark") String remark);

    /**
     * 查询所有的银行交易记录。
     *
     * @return 所有交易记录的列表，按交易时间降序排列。
     */
    @Select("SELECT * FROM banktransaction ORDER BY transactionTime DESC")
    List<Transaction> selectAllTransactions();

    /**
     * 根据时间范围查询所有的银行交易记录。
     *
     * @param start 查询的开始时间。
     * @param end   查询的结束时间。
     * @return 符合条件的所有交易记录列表，按交易时间降序排列。
     */
    @Select("SELECT * FROM banktransaction WHERE transactionTime BETWEEN #{start} AND #{end} ORDER BY transactionTime DESC")
    List<Transaction> selectAllTransactionsByTimeRange(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);


}