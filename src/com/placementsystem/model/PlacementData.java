package com.placementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container object for all application entities.
 * Serializing all lists together in a single stream ensures Java Serialization
 * preserves object graph references and reference equality across restarts.
 */
public class PlacementData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Student> students;
    private List<Company> companies;
    private List<PlacementDrive> drives;
    private List<Application> applications;
    private List<PlacementOfficer> officers;
    private List<Interview> interviews;

    public PlacementData() {
        this.students = new ArrayList<>();
        this.companies = new ArrayList<>();
        this.drives = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.officers = new ArrayList<>();
        this.interviews = new ArrayList<>();
    }

    public PlacementData(List<Student> students,
                         List<Company> companies,
                         List<PlacementDrive> drives,
                         List<Application> applications,
                         List<PlacementOfficer> officers,
                         List<Interview> interviews) {
        this.students = students != null ? students : new ArrayList<>();
        this.companies = companies != null ? companies : new ArrayList<>();
        this.drives = drives != null ? drives : new ArrayList<>();
        this.applications = applications != null ? applications : new ArrayList<>();
        this.officers = officers != null ? officers : new ArrayList<>();
        this.interviews = interviews != null ? interviews : new ArrayList<>();
    }

    public List<Student> getStudents() {
        if (students == null) students = new ArrayList<>();
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Company> getCompanies() {
        if (companies == null) companies = new ArrayList<>();
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    public List<PlacementDrive> getDrives() {
        if (drives == null) drives = new ArrayList<>();
        return drives;
    }

    public void setDrives(List<PlacementDrive> drives) {
        this.drives = drives;
    }

    public List<Application> getApplications() {
        if (applications == null) applications = new ArrayList<>();
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<PlacementOfficer> getOfficers() {
        if (officers == null) officers = new ArrayList<>();
        return officers;
    }

    public void setOfficers(List<PlacementOfficer> officers) {
        this.officers = officers;
    }

    public List<Interview> getInterviews() {
        if (interviews == null) interviews = new ArrayList<>();
        return interviews;
    }

    public void setInterviews(List<Interview> interviews) {
        this.interviews = interviews;
    }
}
