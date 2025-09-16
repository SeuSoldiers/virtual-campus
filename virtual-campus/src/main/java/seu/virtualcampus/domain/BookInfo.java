package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInfo {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private String publishDate;

    private Integer totalCount;
    private Integer availableCount;
    private Integer reservationCount;
}