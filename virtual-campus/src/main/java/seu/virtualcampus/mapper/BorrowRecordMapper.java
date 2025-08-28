package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.BorrowRecord;
import java.util.List;

@Mapper
public interface BorrowRecordMapper {
    @Insert("INSERT INTO borrow_records(recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status) " +
            "VALUES(#{recordId}, #{userId}, #{bookId}, #{borrowDate}, #{dueDate}, #{returnDate}, #{renewCount}, #{status})")
    void insert(BorrowRecord record);

    @Update("UPDATE borrow_records SET userId=#{userId}, bookId=#{bookId}, borrowDate=#{borrowDate}, dueDate=#{dueDate}, " +
            "returnDate=#{returnDate}, renewCount=#{renewCount}, status=#{status} WHERE recordId=#{recordId}")
    void update(BorrowRecord record);

    @Select("SELECT * FROM borrow_records WHERE recordId=#{recordId}")
    BorrowRecord findById(String recordId);

    @Select("SELECT * FROM borrow_records WHERE userId=#{userId}")
    List<BorrowRecord> findByUserId(String userId);

    @Select("SELECT * FROM borrow_records WHERE bookId=#{bookId}")
    List<BorrowRecord> findByBookId(String bookId);

    @Select("SELECT * FROM borrow_records WHERE userId=#{userId} AND status='BORROWED'")
    List<BorrowRecord> findActiveByUserId(String userId);

    @Select("SELECT * FROM borrow_records WHERE bookId=#{bookId} AND status='BORROWED'")
    List<BorrowRecord> findActiveByBookId(String bookId);

    @Update("UPDATE borrow_records SET returnDate=#{returnDate}, status='RETURNED' WHERE recordId=#{recordId}")
    void returnBook(@Param("recordId") String recordId, @Param("returnDate") String returnDate);

    @Update("UPDATE borrow_records SET dueDate=#{dueDate}, renewCount=renewCount+1 WHERE recordId=#{recordId}")
    void renewBook(@Param("recordId") String recordId, @Param("dueDate") String dueDate);

    @Select("SELECT * FROM borrow_records WHERE userId = #{userId} AND bookId = #{bookId}")
    List<BorrowRecord> findByUserAndBook(@Param("userId") String userId, @Param("bookId") String bookId);

    @Select("SELECT * FROM borrow_records")
    List<BorrowRecord> findAll();

}