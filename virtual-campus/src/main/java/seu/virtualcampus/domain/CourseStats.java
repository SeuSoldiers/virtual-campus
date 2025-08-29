package seu.virtualcampus.domain;

public class CourseStats {
    private String courseId;
    private String courseName;
    private Integer currentEnrollment;
    private Integer capacity;
    private Integer availableSpots;
    private Double enrollmentRate;

    // 构造函数
    public CourseStats() {}

    public CourseStats(String courseId, String courseName, Integer currentEnrollment,
                       Integer capacity, Integer availableSpots, Double enrollmentRate) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.currentEnrollment = currentEnrollment;
        this.capacity = capacity;
        this.availableSpots = availableSpots;
        this.enrollmentRate = enrollmentRate;
    }

    // Getter和Setter方法
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCurrentEnrollment() {
        return currentEnrollment;
    }

    public void setCurrentEnrollment(Integer currentEnrollment) {
        this.currentEnrollment = currentEnrollment;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(Integer availableSpots) {
        this.availableSpots = availableSpots;
    }

    public Double getEnrollmentRate() {
        return enrollmentRate;
    }

    public void setEnrollmentRate(Double enrollmentRate) {
        this.enrollmentRate = enrollmentRate;
    }

    @Override
    public String toString() {
        return "CourseStats{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", currentEnrollment=" + currentEnrollment +
                ", capacity=" + capacity +
                ", availableSpots=" + availableSpots +
                ", enrollmentRate=" + enrollmentRate +
                '}';
    }
}