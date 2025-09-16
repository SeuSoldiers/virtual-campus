package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {
    private String bookId;   // 每本书的唯一ID（条码）
    private String isbn;     // 所属书目的ISBN
    private String location; // 馆藏位置
    private String status;   // IN_LIBRARY, BORROWED, RESERVED 等
}
