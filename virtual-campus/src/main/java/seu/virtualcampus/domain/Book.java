package seu.virtualcampus.domain;

import java.time.LocalDate;

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

    // 默认构造方法
    public Book() {
    }

    // 带参数的构造方法
    public Book(String bookId, String title, String author, String isbn, String category,
                LocalDate publishDate, String publisher, Integer totalCount,
                Integer availableCount, String location, Integer reservationCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publishDate = publishDate;
        this.publisher = publisher;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
        this.location = location;
        this.reservationCount = reservationCount;
    }

    // Getter 和 Setter 方法
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getReservationCount() {
        return reservationCount;
    }

    public void setReservationCount(Integer reservationCount) {
        this.reservationCount = reservationCount;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", category='" + category + '\'' +
                ", publishDate=" + publishDate +
                ", publisher='" + publisher + '\'' +
                ", totalCount=" + totalCount +
                ", availableCount=" + availableCount +
                ", location='" + location + '\'' +
                ", reservationCount=" + reservationCount +
                '}';
    }
}