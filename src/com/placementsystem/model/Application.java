package src.com.placementsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Application implements Serializable {
    private String applicationId;
    private String driveId;
    private String studentId;
    private String status;
    private LocalDateTime appliedAt;

    public Application(String applicationId, String driveId, String studentId) {
        this.applicationId = applicationId;
        this.driveId = driveId;
        this.studentId = studentId;
        this.status = "APPLIED";
        this.appliedAt = LocalDateTime.now();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getDriveId() {
        return driveId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Required by Member 2 / Member 3 integration contract
    public void updateStatus(String status) {
        this.status = status;
    }
}