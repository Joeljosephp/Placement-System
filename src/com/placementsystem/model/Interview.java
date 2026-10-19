package com.placementsystem.model;

import java.io.Serializable;

public class Interview implements Serializable {
    private static final long serialVersionUID = 1L;

    private String interviewId;
    private Application application;
    private String roundName;
    private String result;
    private String interviewDate;

    public Interview() {
        this.result = "Scheduled";
    }

    public Interview(String interviewId, Application application, String roundName, String result, String interviewDate) {
        this.interviewId = interviewId;
        this.application = application;
        this.roundName = roundName;
        this.result = result;
        this.interviewDate = interviewDate;
    }

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    @Override
    public String toString() {
        return roundName + " [" + result + "] on " + interviewDate;
    }
}
