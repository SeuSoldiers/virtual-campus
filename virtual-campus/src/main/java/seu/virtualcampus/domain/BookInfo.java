package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 图书信息实体类。
 * <p>
 * 代表一种图书的通用信息，如书名、作者、ISBN等。
 * 它也包含了该种图书在图书馆中的馆藏统计信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInfo {
    /**
     * 图书的国际标准书号 (ISBN)，是图书的唯一标识。
     */
    private String isbn;
    /**
     * 图书的标题。
     */
    private String title;
    /**
     * 图书的作者。
     */
    private String author;
    /**
     * 出版社。
     */
    private String publisher;
    /**
     * 图书的分类。
     */
    private String category;
    /**
     * 出版日期。
     */
    private String publishDate;

    /**
     * 该图书的总馆藏数量。
     */
    private Integer totalCount;
    /**
     * 当前可借阅的数量。
     */
    private Integer availableCount;
    /**
     * 当前的预约数量。
     */
    private Integer reservationCount;
}