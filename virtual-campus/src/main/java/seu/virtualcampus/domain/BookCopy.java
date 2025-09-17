package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 图书副本实体类。
 * <p>
 * 代表图书馆中每一本具体的、可借阅的实体书。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {
    /**
     * 图书副本的唯一ID，通常对应物理条码。
     */
    private String bookId;   // 每本书的唯一ID（条码）
    /**
     * 副本所属图书信息的ISBN号。
     */
    private String isbn;     // 所属书目的ISBN
    /**
     * 副本在图书馆中的馆藏位置。
     */
    private String location; // 馆藏位置
    /**
     * 副本的当前状态。
     * <p>
     * 例如: "IN_LIBRARY" (在馆), "BORROWED" (已借出), "RESERVED" (已预约)。
     */
    private String status;   // IN_LIBRARY, BORROWED, RESERVED 等
}