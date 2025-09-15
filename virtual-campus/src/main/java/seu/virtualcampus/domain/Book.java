package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private LocalDate publishDate;
    private String publisher;
    private Integer totalCount;
    private Integer availableCount;
    private String location;
    private Integer reservationCount;
}