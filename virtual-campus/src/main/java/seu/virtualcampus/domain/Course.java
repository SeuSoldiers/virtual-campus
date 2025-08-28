package seu.virtualcampus.domain;

public class Course {
    private String courseId;
    private String courseName;
    private String courseTeacher;
    private Integer courseCredit;
    private Integer courseCapacity;
    private Integer coursePeopleNumber;
    private String courseTime;
    private String courseLocation;

    // 构造函数
    public Course() {}

    public Course(String courseId, String courseName, String courseTeacher,
                  Integer courseCredit, Integer courseCapacity, Integer coursePeopleNumber,
                  String courseTime, String courseLocation) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseTeacher = courseTeacher;
        this.courseCredit = courseCredit;
        this.courseCapacity = courseCapacity;
        this.coursePeopleNumber = coursePeopleNumber;
        this.courseTime = courseTime;
        this.courseLocation = courseLocation;
    }

    // Getter和Setter方法
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseTeacher() { return courseTeacher; }
    public void setCourseTeacher(String courseTeacher) { this.courseTeacher = courseTeacher; }

    public Integer getCourseCredit() { return courseCredit; }
    public void setCourseCredit(Integer courseCredit) { this.courseCredit = courseCredit; }

    public Integer getCourseCapacity() { return courseCapacity; }
    public void setCourseCapacity(Integer courseCapacity) { this.courseCapacity = courseCapacity; }

    public Integer getCoursePeopleNumber() { return coursePeopleNumber; }
    public void setCoursePeopleNumber(Integer coursePeopleNumber) { this.coursePeopleNumber = coursePeopleNumber; }

    public String getCourseTime() { return courseTime; }
    public void setCourseTime(String courseTime) { this.courseTime = courseTime; }

    public String getCourseLocation() { return courseLocation; }
    public void setCourseLocation(String courseLocation) { this.courseLocation = courseLocation; }
}